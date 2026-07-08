package rpg.quest.model;

import rpg.core.player.PlayerDataComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player quest log: active quest progress, ever-completed quest ids (used for both
 * "one-time only" enforcement and prerequisite checks), and earned titles.
 */
public final class PlayerQuestComponent implements PlayerDataComponent {

    private final UUID owner;
    private final Map<String, PlayerQuestProgress> activeQuests = new ConcurrentHashMap<>();
    private final Set<String> completedQuestIds;
    private final Set<String> titles;

    public PlayerQuestComponent(UUID owner, Set<String> completedQuestIds, Set<String> titles) {
        this.owner = owner;
        this.completedQuestIds = new HashSet<>(completedQuestIds);
        this.titles = new HashSet<>(titles);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public Map<String, PlayerQuestProgress> getActiveQuests() {
        return activeQuests;
    }

    public void startQuest(String questId) {
        activeQuests.put(questId, new PlayerQuestProgress(QuestState.IN_PROGRESS));
    }

    public void completeQuest(String questId) {
        activeQuests.remove(questId);
        completedQuestIds.add(questId);
    }

    public boolean hasCompleted(String questId) {
        return completedQuestIds.contains(questId);
    }

    public Set<String> getCompletedQuestIds() {
        return Set.copyOf(completedQuestIds);
    }

    public void addTitle(String title) {
        if (title != null && !title.isBlank()) {
            titles.add(title);
        }
    }

    public Set<String> getTitles() {
        return Set.copyOf(titles);
    }
}
