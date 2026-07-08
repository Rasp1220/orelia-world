package rpg.npc.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import rpg.npc.model.NpcData;
import rpg.npc.repository.NpcRepository;

/**
 * Ensures every configured NPC has exactly one entity alive in the world: on startup,
 * checks whether an entity tagged with that NPC's id already exists near its configured
 * location (it would, after a normal server restart, since Bukkit persists entities in
 * chunk data) and only spawns a fresh one if it is missing.
 */
public final class NpcSpawnSyncService {

    private static final double MATCH_RADIUS = 3.0;

    private final NpcKeys keys;
    private final NpcRepository repository;
    private final NpcSpawnService spawnService;

    public NpcSpawnSyncService(NpcKeys keys, NpcRepository repository, NpcSpawnService spawnService) {
        this.keys = keys;
        this.repository = repository;
        this.spawnService = spawnService;
    }

    public void syncAll() {
        for (NpcData data : repository.getAll().values()) {
            World world = Bukkit.getWorld(data.getWorld());
            if (world == null) {
                continue;
            }
            Location location = new Location(world, data.getX(), data.getY(), data.getZ(), data.getYaw(), 0);
            boolean alreadyPresent = world.getNearbyEntities(location, MATCH_RADIUS, MATCH_RADIUS, MATCH_RADIUS).stream()
                    .anyMatch(this::isTaggedAsAnyNpc);
            if (!alreadyPresent) {
                spawnService.spawn(data.getId(), location);
            }
        }
    }

    private boolean isTaggedAsAnyNpc(Entity entity) {
        return entity.getPersistentDataContainer().has(keys.npcId());
    }
}
