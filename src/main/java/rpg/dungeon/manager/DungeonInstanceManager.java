package rpg.dungeon.manager;

import rpg.dungeon.model.DungeonInstance;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks every active {@link DungeonInstance} and which instance (if any) each player is
 * currently inside.
 */
public final class DungeonInstanceManager {

    private final Map<UUID, DungeonInstance> instances = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToInstance = new ConcurrentHashMap<>();

    public void register(DungeonInstance instance) {
        instances.put(instance.getId(), instance);
        instance.getMembers().keySet().forEach(playerId -> playerToInstance.put(playerId, instance.getId()));
    }

    public Optional<DungeonInstance> getByPlayer(UUID playerId) {
        return Optional.ofNullable(playerToInstance.get(playerId)).map(instances::get);
    }

    public void remove(UUID instanceId) {
        DungeonInstance instance = instances.remove(instanceId);
        if (instance != null) {
            instance.getMembers().keySet().forEach(playerToInstance::remove);
        }
    }

    public void removePlayer(UUID playerId) {
        playerToInstance.remove(playerId);
    }
}
