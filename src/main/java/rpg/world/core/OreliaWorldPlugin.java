package rpg.world.core;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import rpg.core.config.ConfigManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.dungeon.DungeonModule;
import rpg.npc.NpcModule;
import rpg.quest.QuestModule;
import rpg.world.core.command.WorldAdminCommand;
import rpg.world.core.module.WorldModuleManager;
import rpg.world.cutscene.CutSceneModule;
import rpg.world.dialogue.DialogueModule;
import rpg.world.event.EventModule;
import rpg.world.region.RegionModule;
import rpg.world.story.StoryModule;

/**
 * Plugin entry point for the orelia-world repo/jar: the content layer (quest/NPC/dialogue/
 * story/dungeon/region/cutscene/event). Requires OreliaCore to already be enabled - every
 * gameplay-domain lookup goes through {@code rpg.api} (published by orelia-core via Bukkit's
 * ServicesManager), never through orelia-core's internal module classes.
 *
 * <p>{@link ConfigManager}, {@link SchedulerService} and {@link PlayerDataManager} are
 * reused as-is from orelia-core: they are generic per-plugin/per-player infrastructure,
 * not gameplay logic, so orelia-world doesn't need its own copy of that plumbing -
 * {@code PlayerDataManager} specifically is looked up from OreliaCore's ServicesManager
 * registration since it is process-wide shared state (the online-player cache), not
 * something each plugin can own independently.
 */
public final class OreliaWorldPlugin extends JavaPlugin {

    private static OreliaWorldPlugin instance;

    private ConfigManager configManager;
    private SchedulerService schedulerService;
    private PlayerDataManager playerDataManager;
    private WorldModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        RegisteredServiceProvider<PlayerDataManager> registration =
                getServer().getServicesManager().getRegistration(PlayerDataManager.class);
        if (registration == null) {
            getLogger().severe("OreliaCore's PlayerDataManager service was not found. "
                    + "Is OreliaCore installed and enabled before OreliaWorld?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.playerDataManager = registration.getProvider();

        this.configManager = new ConfigManager(this);
        this.configManager.register("config.yml");

        this.schedulerService = new SchedulerService(this);
        this.moduleManager = new WorldModuleManager(this);

        getCommand("rpgworldadmin").setExecutor(new WorldAdminCommand(this));

        // Registration order doubles as dependency order, exactly like orelia-core.
        moduleManager.register(new RegionModule());
        moduleManager.register(new DialogueModule());
        moduleManager.register(new StoryModule());
        moduleManager.register(new EventModule());
        moduleManager.register(new CutSceneModule());
        moduleManager.register(new DungeonModule());
        moduleManager.register(new QuestModule());
        moduleManager.register(new NpcModule());

        moduleManager.enableAll();
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        instance = null;
    }

    public void reload() {
        configManager.reloadAll();
        moduleManager.reloadAll();
    }

    public static OreliaWorldPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public WorldModuleManager getModuleManager() {
        return moduleManager;
    }
}
