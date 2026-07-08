package rpg.world.region.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which region (by id) each online player is currently standing in, so enter/exit
 * transitions only fire once per boundary crossing instead of every move tick.
 */
public final class RegionTracker {

    private final Map<UUID, String> currentRegion = new ConcurrentHashMap<>();

    public String getCurrentRegion(UUID playerId) {
        return currentRegion.get(playerId);
    }

    public void setCurrentRegion(UUID playerId, String regionId) {
        if (regionId == null) {
            currentRegion.remove(playerId);
        } else {
            currentRegion.put(playerId, regionId);
        }
    }

    public void clear(UUID playerId) {
        currentRegion.remove(playerId);
    }
}
