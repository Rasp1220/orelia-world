package rpg.world.api;

import java.util.Set;
import java.util.UUID;

/**
 * Cross-plugin surface over the quest module, published by orelia-world the same way
 * orelia-core publishes {@code rpg.api} - so orelia-extra (Achievement, ...) can react to
 * quest completion without depending on {@code rpg.quest} internals.
 */
public interface QuestApi {

    /** Quest ids currently in progress (accepted but not yet turned in) for this player. */
    Set<String> getActiveQuestIds(UUID playerId);

    /** Whether the player has ever completed the given quest id. */
    boolean hasCompletedQuest(UUID playerId, String questId);
}
