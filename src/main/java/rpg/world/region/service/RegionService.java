package rpg.world.region.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import rpg.world.region.model.RegionData;
import rpg.world.region.model.RegionType;
import rpg.world.region.repository.RegionRepository;

import java.util.Optional;

/**
 * Resolves which region a location falls in and performs the {@link RegionType#WARP}
 * teleport. Regions are not spatially indexed - a linear scan is fine at the scale of a
 * hand-authored regions.yml.
 */
public final class RegionService {

    private final RegionRepository repository;

    public RegionService(RegionRepository repository) {
        this.repository = repository;
    }

    public Optional<RegionData> findRegionAt(Location location) {
        if (location.getWorld() == null) {
            return Optional.empty();
        }
        String worldName = location.getWorld().getName();
        return repository.getAll().values().stream()
                .filter(region -> region.contains(worldName, location.getX(), location.getY(), location.getZ()))
                .findFirst();
    }

    public boolean warp(Player player, RegionData region) {
        if (region.getType() != RegionType.WARP || region.getWarpWorld() == null) {
            return false;
        }
        World world = Bukkit.getWorld(region.getWarpWorld());
        if (world == null) {
            return false;
        }
        player.teleport(new Location(world, region.getWarpX(), region.getWarpY(), region.getWarpZ(), region.getWarpYaw(), 0));
        return true;
    }
}
