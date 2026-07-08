package rpg.world.region;

import org.bukkit.configuration.file.YamlConfiguration;
import rpg.world.core.OreliaWorldPlugin;
import rpg.world.core.module.WorldModule;
import rpg.world.region.listener.RegionMoveListener;
import rpg.world.region.manager.RegionTracker;
import rpg.world.region.repository.RegionRepository;
import rpg.world.region.service.RegionService;

/**
 * Region module: config-driven named areas (regions.yml) - town/field/dungeon-entrance/
 * warp - with enter/exit messages and warp teleports.
 */
public final class RegionModule implements WorldModule {

    private final RegionRepository repository = new RegionRepository();
    private final RegionTracker tracker = new RegionTracker();
    private RegionService regionService;
    private OreliaWorldPlugin plugin;

    @Override
    public String getName() {
        return "region";
    }

    @Override
    public void onEnable(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
        reloadRegions();
        this.regionService = new RegionService(repository);
        plugin.getServer().getPluginManager().registerEvents(new RegionMoveListener(regionService, repository, tracker), plugin);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadRegions();
    }

    private void reloadRegions() {
        plugin.getConfigManager().register("regions.yml");
        YamlConfiguration config = plugin.getConfigManager().get("regions.yml").get();
        repository.load(config);
    }

    public RegionService getRegionService() {
        return regionService;
    }

    public RegionRepository getRepository() {
        return repository;
    }
}
