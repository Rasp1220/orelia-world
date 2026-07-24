package rpg.dungeon.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rpg.api.CombatApi;
import rpg.core.config.ConfigManager;
import rpg.core.message.MessageManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.dungeon.manager.DungeonInstanceManager;
import rpg.dungeon.model.DungeonData;
import rpg.dungeon.model.DungeonEndReason;
import rpg.dungeon.model.DungeonInstance;
import rpg.dungeon.model.PlayerDungeonComponent;
import rpg.quest.service.QuestProgressService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Drives a dungeon run's live encounter on top of {@link DungeonService}'s bare
 * teleport-in/teleport-out/reward shell: spawning the configured enemies/boss, tracking
 * which ones are still alive, the time-limit countdown, and manual retire. Composes
 * {@link DungeonService} rather than extending it - that class's own job (party-size
 * validation, teleport, reward) stays a clean, narrow unit.
 */
public final class DungeonEncounterService {

    public enum ChallengeFailure {
        NOT_UNLOCKED, UNKNOWN_DUNGEON, PARTY_TOO_SMALL, PARTY_TOO_LARGE, WORLD_NOT_FOUND, ALREADY_IN_DUNGEON
    }

    private static final double DEFAULT_PARTY_GATHER_RADIUS = 15.0;
    private static final double SPAWN_JITTER_RADIUS = 2.5;

    private final DungeonService dungeonService;
    private final DungeonInstanceManager instanceManager;
    private final CombatApi combatApi;
    private final SchedulerService schedulerService;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private final QuestProgressService questProgressService;
    private final MessageManager messages;
    private final Random random = new Random();

    public DungeonEncounterService(DungeonService dungeonService, DungeonInstanceManager instanceManager,
                                    CombatApi combatApi, SchedulerService schedulerService, ConfigManager configManager,
                                    PlayerDataManager playerDataManager, QuestProgressService questProgressService,
                                    MessageManager messages) {
        this.dungeonService = dungeonService;
        this.instanceManager = instanceManager;
        this.combatApi = combatApi;
        this.schedulerService = schedulerService;
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
        this.questProgressService = questProgressService;
        this.messages = messages;
    }

    /** Attempts to start a dungeon run for {@code initiator} (and any nearby players gathered as their party). */
    public Optional<ChallengeFailure> challenge(Player initiator, String dungeonId) {
        PlayerDungeonComponent component = playerDataManager.get(initiator.getUniqueId())
                .flatMap(d -> d.component(PlayerDungeonComponent.class)).orElse(null);
        if (component == null || !component.isUnlocked(dungeonId)) {
            return Optional.of(ChallengeFailure.NOT_UNLOCKED);
        }

        double radius = configManager.get("config.yml").get().getDouble("dungeon.party-gather-radius", DEFAULT_PARTY_GATHER_RADIUS);
        List<Player> party = gatherNearbyParty(initiator, radius);

        Optional<DungeonService.StartFailure> failure = dungeonService.start(dungeonId, party);
        if (failure.isPresent()) {
            return Optional.of(mapFailure(failure.get()));
        }

        DungeonInstance instance = instanceManager.getByPlayer(initiator.getUniqueId()).orElseThrow();
        spawnEncounter(instance);
        scheduleTimeout(instance);
        return Optional.empty();
    }

    /** Manually ends the run the given player is currently in. Returns false if they aren't in one. */
    public boolean retire(Player player) {
        return instanceManager.getByPlayer(player.getUniqueId())
                .map(instance -> {
                    forceEnd(instance, DungeonEndReason.RETIRED);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Debug helper: starts a solo run for {@code player} bypassing the unlock check (party-size
     * validation against {@code min-party-size} still applies via {@link DungeonService#start},
     * since a single player is the whole "party" here - a dungeon requiring more than one
     * member will still fail with {@link ChallengeFailure#PARTY_TOO_SMALL}).
     */
    public Optional<ChallengeFailure> forceStart(Player player, String dungeonId) {
        Optional<DungeonService.StartFailure> failure = dungeonService.start(dungeonId, List.of(player));
        if (failure.isPresent()) {
            return Optional.of(mapFailure(failure.get()));
        }
        DungeonInstance instance = instanceManager.getByPlayer(player.getUniqueId()).orElseThrow();
        spawnEncounter(instance);
        scheduleTimeout(instance);
        return Optional.empty();
    }

    /** The dungeon id of the instance the given player is currently in, if any. */
    public Optional<String> getActiveDungeonId(UUID playerId) {
        return instanceManager.getByPlayer(playerId).map(instance -> instance.getData().getId());
    }

    /** Called by {@code DungeonMobDeathListener} whenever any entity dies - a no-op if it wasn't a tracked dungeon mob. */
    public void onMobDeath(UUID entityId) {
        instanceManager.getByMonster(entityId).ifPresent(instance -> {
            if (instance.untrackMonster(entityId) && instance.isCleared()) {
                forceEnd(instance, DungeonEndReason.CLEARED);
            }
        });
    }

    private void spawnEncounter(DungeonInstance instance) {
        DungeonData data = instance.getData();
        var world = Bukkit.getWorld(data.getWorld());
        if (world == null) {
            return;
        }
        Location entry = new Location(world, data.getX(), data.getY(), data.getZ());
        for (Map.Entry<String, Integer> entry2 : data.getEnemies().entrySet()) {
            for (int i = 0; i < entry2.getValue(); i++) {
                combatApi.spawnMonster(entry2.getKey(), jitter(entry)).ifPresent(mob -> track(instance, mob));
            }
        }
        if (data.getBossId() != null) {
            combatApi.spawnBoss(data.getBossId(), jitter(entry)).ifPresent(boss -> track(instance, boss));
        }
    }

    private void track(DungeonInstance instance, LivingEntity entity) {
        instance.trackMonster(entity.getUniqueId());
        instanceManager.registerMonster(entity.getUniqueId(), instance.getId());
    }

    private Location jitter(Location center) {
        double dx = (random.nextDouble() * 2 - 1) * SPAWN_JITTER_RADIUS;
        double dz = (random.nextDouble() * 2 - 1) * SPAWN_JITTER_RADIUS;
        return center.clone().add(dx, 0, dz);
    }

    private void scheduleTimeout(DungeonInstance instance) {
        long delayTicks = instance.getData().getTimeLimitSeconds() * 20L;
        instance.setTimeoutTask(schedulerService.runLater(() -> forceEnd(instance, DungeonEndReason.TIMED_OUT), delayTicks));
    }

    private void forceEnd(DungeonInstance instance, DungeonEndReason reason) {
        instance.cancelTimeoutTask();
        despawnRemainingMobs(instance);
        Set<UUID> memberIds = instance.getMembers().keySet();
        String dungeonId = instance.getData().getId();
        UUID anyMember = memberIds.iterator().next();
        dungeonService.finish(anyMember, reason);
        if (reason == DungeonEndReason.CLEARED) {
            memberIds.forEach(id -> questProgressService.onDungeonCleared(id, dungeonId));
        }
        notifyOutcome(memberIds, reason);
    }

    private void despawnRemainingMobs(DungeonInstance instance) {
        for (UUID entityId : instance.getAliveMonsterIds()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity instanceof LivingEntity living && living.isValid()) {
                living.remove();
            }
        }
    }

    private void notifyOutcome(Set<UUID> memberIds, DungeonEndReason reason) {
        String key = switch (reason) {
            case CLEARED -> "dungeon.cleared";
            case TIMED_OUT -> "dungeon.timed-out";
            case RETIRED -> "dungeon.retired";
        };
        for (UUID memberId : memberIds) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                messages.send(member, key);
            }
        }
    }

    private List<Player> gatherNearbyParty(Player initiator, double radius) {
        double radiusSquared = radius * radius;
        return initiator.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(initiator.getLocation()) <= radiusSquared)
                .toList();
    }

    private ChallengeFailure mapFailure(DungeonService.StartFailure failure) {
        return switch (failure) {
            case UNKNOWN_DUNGEON -> ChallengeFailure.UNKNOWN_DUNGEON;
            case PARTY_TOO_SMALL -> ChallengeFailure.PARTY_TOO_SMALL;
            case PARTY_TOO_LARGE -> ChallengeFailure.PARTY_TOO_LARGE;
            case WORLD_NOT_FOUND -> ChallengeFailure.WORLD_NOT_FOUND;
            case ALREADY_IN_DUNGEON -> ChallengeFailure.ALREADY_IN_DUNGEON;
        };
    }
}
