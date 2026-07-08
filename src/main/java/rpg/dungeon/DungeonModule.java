package rpg.dungeon;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.api.StatusApi;
import rpg.dungeon.listener.DungeonQuitListener;
import rpg.dungeon.manager.DungeonInstanceManager;
import rpg.dungeon.repository.DungeonRepository;
import rpg.dungeon.service.DungeonService;
import rpg.world.core.OreliaWorldPlugin;
import rpg.world.core.module.WorldModule;

/**
 * Dungeon module: config-driven dungeon definitions (dungeons.yml), party-size
 * validation, entry/exit teleport, and completion rewards.
 */
public final class DungeonModule implements WorldModule {

    private final DungeonRepository repository = new DungeonRepository();
    private final DungeonInstanceManager instanceManager = new DungeonInstanceManager();
    private DungeonService dungeonService;
    private OreliaWorldPlugin plugin;

    @Override
    public String getName() {
        return "dungeon";
    }

    @Override
    public void onEnable(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
        StatusApi statusApi = plugin.getServer().getServicesManager().load(StatusApi.class);
        if (statusApi == null) {
            throw new IllegalStateException("dungeon module requires OreliaCore's StatusApi");
        }
        Economy economy = plugin.getServer().getServicesManager().load(Economy.class);

        reloadDungeons();

        this.dungeonService = new DungeonService(repository, instanceManager, statusApi, economy);

        plugin.getServer().getPluginManager().registerEvents(new DungeonQuitListener(instanceManager), plugin);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadDungeons();
    }

    private void reloadDungeons() {
        plugin.getConfigManager().register("dungeons.yml");
        YamlConfiguration config = plugin.getConfigManager().get("dungeons.yml").get();
        repository.load(config);
    }

    public DungeonService getDungeonService() {
        return dungeonService;
    }

    public DungeonRepository getRepository() {
        return repository;
    }
}
