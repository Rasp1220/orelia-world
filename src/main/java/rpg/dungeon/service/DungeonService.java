package rpg.dungeon.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import rpg.api.StatusApi;
import rpg.dungeon.manager.DungeonInstanceManager;
import rpg.dungeon.model.DungeonData;
import rpg.dungeon.model.DungeonInstance;
import rpg.dungeon.model.DungeonInstanceStatus;
import rpg.dungeon.repository.DungeonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Starts/completes dungeon runs: party-size validation against {@link DungeonData}, entry
 * teleport, and reward distribution on completion. Rewards go through orelia-core's
 * {@link StatusApi} (EXP) and Vault's {@link Economy} (money) - orelia-world never touches
 * orelia-core's internal status/economy classes directly.
 */
public final class DungeonService {

    public enum StartFailure {
        UNKNOWN_DUNGEON, PARTY_TOO_SMALL, PARTY_TOO_LARGE, WORLD_NOT_FOUND, ALREADY_IN_DUNGEON
    }

    private final DungeonRepository repository;
    private final DungeonInstanceManager instanceManager;
    private final StatusApi statusApi;
    private final Economy economy;

    public DungeonService(DungeonRepository repository, DungeonInstanceManager instanceManager,
                           StatusApi statusApi, Economy economy) {
        this.repository = repository;
        this.instanceManager = instanceManager;
        this.statusApi = statusApi;
        this.economy = economy;
    }

    public Optional<StartFailure> start(String dungeonId, List<Player> party) {
        DungeonData data = repository.findById(dungeonId).orElse(null);
        if (data == null) {
            return Optional.of(StartFailure.UNKNOWN_DUNGEON);
        }
        if (party.size() < data.getMinPartySize()) {
            return Optional.of(StartFailure.PARTY_TOO_SMALL);
        }
        if (party.size() > data.getMaxPartySize()) {
            return Optional.of(StartFailure.PARTY_TOO_LARGE);
        }
        for (Player player : party) {
            if (instanceManager.getByPlayer(player.getUniqueId()).isPresent()) {
                return Optional.of(StartFailure.ALREADY_IN_DUNGEON);
            }
        }
        var world = Bukkit.getWorld(data.getWorld());
        if (world == null) {
            return Optional.of(StartFailure.WORLD_NOT_FOUND);
        }

        DungeonInstance instance = new DungeonInstance(data);
        Location entry = new Location(world, data.getX(), data.getY(), data.getZ());
        for (Player player : party) {
            instance.addMember(player.getUniqueId(), player.getLocation());
            player.teleport(entry);
        }
        instanceManager.register(instance);
        return Optional.empty();
    }

    /** Ends the run the given player is in (if any), rewarding the whole party on success. */
    public void finish(UUID playerId, boolean success) {
        DungeonInstance instance = instanceManager.getByPlayer(playerId).orElse(null);
        if (instance == null) {
            return;
        }
        instance.setStatus(success ? DungeonInstanceStatus.COMPLETED : DungeonInstanceStatus.FAILED);

        for (var entry : instance.getMembers().entrySet()) {
            UUID memberId = entry.getKey();
            Location returnLocation = entry.getValue();
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.teleport(returnLocation);
                if (success) {
                    statusApi.addExperience(memberId, instance.getData().getRewardExp());
                    if (economy != null) {
                        economy.depositPlayer(member, instance.getData().getRewardMoney());
                    }
                }
            }
        }
        instanceManager.remove(instance.getId());
    }
}
