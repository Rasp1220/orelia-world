package rpg.world.api;

import rpg.core.player.PlayerDataManager;
import rpg.quest.model.PlayerQuestComponent;

import java.util.Set;
import java.util.UUID;

final class QuestApiImpl implements QuestApi {

    private final PlayerDataManager playerDataManager;

    QuestApiImpl(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public Set<String> getActiveQuestIds(UUID playerId) {
        return playerDataManager.get(playerId)
                .flatMap(data -> data.component(PlayerQuestComponent.class))
                .map(component -> Set.copyOf(component.getActiveQuests().keySet()))
                .orElse(Set.of());
    }

    @Override
    public boolean hasCompletedQuest(UUID playerId, String questId) {
        return playerDataManager.get(playerId)
                .flatMap(data -> data.component(PlayerQuestComponent.class))
                .map(component -> component.hasCompleted(questId))
                .orElse(false);
    }
}
