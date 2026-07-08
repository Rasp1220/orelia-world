package rpg.dungeon.model;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One live attempt at a dungeon: the party inside it and where each member should be
 * returned to when the run ends.
 */
public final class DungeonInstance {

    private final UUID id = UUID.randomUUID();
    private final DungeonData data;
    private final Map<UUID, Location> membersAndReturnLocations = new ConcurrentHashMap<>();
    private final long startedAtMillis = System.currentTimeMillis();
    private volatile DungeonInstanceStatus status = DungeonInstanceStatus.ACTIVE;

    public DungeonInstance(DungeonData data) {
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public DungeonData getData() {
        return data;
    }

    public void addMember(UUID playerId, Location returnLocation) {
        membersAndReturnLocations.put(playerId, returnLocation);
    }

    public Location getReturnLocation(UUID playerId) {
        return membersAndReturnLocations.get(playerId);
    }

    public Map<UUID, Location> getMembers() {
        return Map.copyOf(membersAndReturnLocations);
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public DungeonInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(DungeonInstanceStatus status) {
        this.status = status;
    }
}
