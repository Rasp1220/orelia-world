package rpg.quest.service;

import org.bukkit.entity.Player;
import rpg.api.StatusApi;
import rpg.core.player.PlayerDataManager;
import rpg.quest.model.PlayerQuestComponent;
import rpg.quest.model.QuestData;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Evaluates the "クエストフラグ" rules from SOW section 11: prerequisite quests, one-time
 * vs repeatable, required level, and time-of-day availability.
 */
public final class QuestEligibilityService {

    public enum Ineligibility {
        ALREADY_ACTIVE, ALREADY_COMPLETED, LEVEL_TOO_LOW, PREREQUISITE_MISSING, NOT_AVAILABLE_NOW
    }

    private final PlayerDataManager playerDataManager;
    private final StatusApi statusApi;

    public QuestEligibilityService(PlayerDataManager playerDataManager, StatusApi statusApi) {
        this.playerDataManager = playerDataManager;
        this.statusApi = statusApi;
    }

    public Optional<Ineligibility> checkEligibility(Player player, QuestData quest) {
        PlayerQuestComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerQuestComponent.class))
                .orElse(null);
        if (component == null) {
            return Optional.of(Ineligibility.LEVEL_TOO_LOW);
        }
        if (component.getActiveQuests().containsKey(quest.getId())) {
            return Optional.of(Ineligibility.ALREADY_ACTIVE);
        }
        if (component.hasCompleted(quest.getId()) && !quest.isRepeatable()) {
            return Optional.of(Ineligibility.ALREADY_COMPLETED);
        }
        int level = statusApi.getLevel(player.getUniqueId()).orElse(1);
        if (level < quest.getRequiredLevel()) {
            return Optional.of(Ineligibility.LEVEL_TOO_LOW);
        }
        for (String prerequisite : quest.getPrerequisiteQuestIds()) {
            if (!component.hasCompleted(prerequisite)) {
                return Optional.of(Ineligibility.PREREQUISITE_MISSING);
            }
        }
        if (!quest.isAvailableAtHour(LocalDateTime.now().getHour())) {
            return Optional.of(Ineligibility.NOT_AVAILABLE_NOW);
        }
        return Optional.empty();
    }
}
