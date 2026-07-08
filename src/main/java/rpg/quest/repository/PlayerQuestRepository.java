package rpg.quest.repository;

import rpg.database.manager.DatabaseManager;
import rpg.database.repository.SchemaOwner;
import rpg.quest.model.PlayerQuestComponent;
import rpg.quest.model.PlayerQuestProgress;
import rpg.quest.model.QuestState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Persists a player's quest log: completed quest ids, earned titles, and in-progress
 * quest state with per-objective counters (packed as {@code "idx:amount,idx:amount"}).
 */
public final class PlayerQuestRepository implements SchemaOwner {

    private final DatabaseManager databaseManager;

    public PlayerQuestRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void createSchemaIfNotExists() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quest_completed (
                        uuid VARCHAR(36) NOT NULL,
                        quest_id VARCHAR(64) NOT NULL,
                        completed_at BIGINT NOT NULL,
                        PRIMARY KEY (uuid, quest_id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quest_title (
                        uuid VARCHAR(36) NOT NULL,
                        title VARCHAR(64) NOT NULL,
                        PRIMARY KEY (uuid, title)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quest_progress (
                        uuid VARCHAR(36) NOT NULL,
                        quest_id VARCHAR(64) NOT NULL,
                        state VARCHAR(32) NOT NULL,
                        progress VARCHAR(512) NOT NULL DEFAULT '',
                        PRIMARY KEY (uuid, quest_id)
                    )
                    """);
        }
    }

    public PlayerQuestComponent loadOrCreate(UUID uuid) {
        Set<String> completed = new HashSet<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT quest_id FROM quest_completed WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    completed.add(resultSet.getString("quest_id"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load completed quests for " + uuid, e);
        }

        Set<String> titles = new HashSet<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT title FROM quest_title WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    titles.add(resultSet.getString("title"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load titles for " + uuid, e);
        }

        PlayerQuestComponent component = new PlayerQuestComponent(uuid, completed, titles);

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT quest_id, state, progress FROM quest_progress WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String questId = resultSet.getString("quest_id");
                    PlayerQuestProgress progress = new PlayerQuestProgress(QuestState.valueOf(resultSet.getString("state")));
                    String packed = resultSet.getString("progress");
                    if (packed != null && !packed.isBlank()) {
                        for (String entry : packed.split(",")) {
                            String[] parts = entry.split(":");
                            if (parts.length == 2) {
                                progress.setProgress(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                            }
                        }
                    }
                    component.getActiveQuests().put(questId, progress);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load quest progress for " + uuid, e);
        }

        return component;
    }

    public void save(PlayerQuestComponent component) {
        try (Connection connection = databaseManager.getConnection()) {
            for (String questId : component.getCompletedQuestIds()) {
                upsertCompleted(connection, component.getOwner(), questId);
            }
            for (String title : component.getTitles()) {
                upsertTitle(connection, component.getOwner(), title);
            }
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM quest_progress WHERE uuid = ?")) {
                delete.setString(1, component.getOwner().toString());
                delete.executeUpdate();
            }
            for (Map.Entry<String, PlayerQuestProgress> entry : component.getActiveQuests().entrySet()) {
                insertProgress(connection, component.getOwner(), entry.getKey(), entry.getValue());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save quest log for " + component.getOwner(), e);
        }
    }

    private void upsertCompleted(Connection connection, UUID uuid, String questId) throws SQLException {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> "INSERT INTO quest_completed (uuid, quest_id, completed_at) VALUES (?, ?, ?) ON CONFLICT(uuid, quest_id) DO NOTHING";
            case MYSQL -> "INSERT IGNORE INTO quest_completed (uuid, quest_id, completed_at) VALUES (?, ?, ?)";
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, questId);
            statement.setLong(3, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    private void upsertTitle(Connection connection, UUID uuid, String title) throws SQLException {
        String sql = switch (databaseManager.getType()) {
            case SQLITE -> "INSERT INTO quest_title (uuid, title) VALUES (?, ?) ON CONFLICT(uuid, title) DO NOTHING";
            case MYSQL -> "INSERT IGNORE INTO quest_title (uuid, title) VALUES (?, ?)";
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, title);
            statement.executeUpdate();
        }
    }

    private void insertProgress(Connection connection, UUID uuid, String questId, PlayerQuestProgress progress) throws SQLException {
        StringBuilder packed = new StringBuilder();
        progress.getAllProgress().forEach((index, amount) -> {
            if (packed.length() > 0) {
                packed.append(',');
            }
            packed.append(index).append(':').append(amount);
        });
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO quest_progress (uuid, quest_id, state, progress) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, questId);
            statement.setString(3, progress.getState().name());
            statement.setString(4, packed.toString());
            statement.executeUpdate();
        }
    }
}
