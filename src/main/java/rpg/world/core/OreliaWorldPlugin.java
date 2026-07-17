package rpg.world.core;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import rpg.core.command.AdminCommandRegistry;
import rpg.core.command.PlayerCommandRegistry;
import rpg.core.config.ConfigManager;
import rpg.core.message.MessageManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.dungeon.DungeonModule;
import rpg.npc.NpcModule;
import rpg.quest.QuestModule;
import rpg.world.api.WorldApiModule;
import rpg.world.core.command.WorldAdminCommand;
import rpg.world.core.module.WorldModuleManager;
import rpg.world.cutscene.CutSceneModule;
import rpg.world.dialogue.DialogueModule;
import rpg.world.event.EventModule;
import rpg.world.playerinfo.PlayerInfoModule;
import rpg.world.story.StoryModule;

/**
 * Plugin entry point for the orelia-world repo/jar: the content layer (quest/NPC/dialogue/
 * story/dungeon/cutscene/event). Requires OreliaCore to already be enabled - every
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
    private MessageManager messageManager;
    private SchedulerService schedulerService;
    private PlayerDataManager playerDataManager;
    private PlayerCommandRegistry playerCommandRegistry;
    private AdminCommandRegistry adminCommandRegistry;
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

        RegisteredServiceProvider<PlayerCommandRegistry> playerCommandRegistration =
                getServer().getServicesManager().getRegistration(PlayerCommandRegistry.class);
        RegisteredServiceProvider<AdminCommandRegistry> adminCommandRegistration =
                getServer().getServicesManager().getRegistration(AdminCommandRegistry.class);
        if (playerCommandRegistration == null || adminCommandRegistration == null) {
            getLogger().severe("OreliaCore's command registries were not found. "
                    + "Is OreliaCore installed and enabled before OreliaWorld?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.playerCommandRegistry = playerCommandRegistration.getProvider();
        this.adminCommandRegistry = adminCommandRegistration.getProvider();

        this.configManager = new ConfigManager(this);
        this.configManager.register("config.yml");
        this.messageManager = new MessageManager(configManager.register("messages.yml"));

        this.schedulerService = new SchedulerService(this);
        this.moduleManager = new WorldModuleManager(this);

        adminCommandRegistration.getProvider().register("worldreload", new WorldAdminCommand(this),
                "orelia-world の設定を再読み込みします。", "worldreload");

        // Registration order doubles as dependency order, exactly like orelia-core.
        moduleManager.register(new DialogueModule());
        moduleManager.register(new StoryModule());
        moduleManager.register(new EventModule());
        moduleManager.register(new CutSceneModule());
        moduleManager.register(new DungeonModule());
        moduleManager.register(new QuestModule());
        moduleManager.register(new NpcModule());
        moduleManager.register(new PlayerInfoModule());
        moduleManager.register(new WorldApiModule());

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

    public MessageManager getMessageManager() {
        return messageManager;
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

    public PlayerCommandRegistry getPlayerCommandRegistry() {
        return playerCommandRegistry;
    }

    public AdminCommandRegistry getAdminCommandRegistry() {
        return adminCommandRegistry;
    }
}
