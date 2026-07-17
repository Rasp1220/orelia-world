package rpg.world.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Cross-plugin surface for debug/testplay tooling (orelia-debug) over orelia-world: config
 * inspection/editing (same pattern as orelia-core's {@code rpg.api.DebugApi}) plus a couple
 * of world-specific shortcuts (force-completing quest objectives, listing NPC ids) that don't
 * fit a generic config API.
 */
public interface WorldDebugApi {

    Set<String> listConfigFiles();

    Optional<String> getConfigValue(String fileName, String path);

    boolean setConfigValue(String fileName, String path, String rawValue);

    void saveConfig(String fileName);

    List<String> describeConfigKeys(String fileName);

    /** Forces every objective of {@code questId} to completion for {@code playerId}, if in progress. */
    boolean forceCompleteQuestObjectives(UUID playerId, String questId);

    /** Every configured NPC id, sorted - for a "debug npc show" style listing. */
    List<String> listNpcIds();
}
