package rpg.npc;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicesManager;
import rpg.api.GuiApi;
import rpg.api.ItemApi;
import rpg.gui.framework.GuiManager;
import rpg.npc.command.NpcSpawnCommand;
import rpg.npc.listener.NpcInteractListener;
import rpg.npc.repository.NpcRepository;
import rpg.npc.service.NpcKeys;
import rpg.npc.service.NpcSpawnService;
import rpg.npc.service.NpcSpawnSyncService;
import rpg.quest.QuestModule;
import rpg.world.core.OreliaWorldPlugin;
import rpg.world.core.module.WorldModule;

/**
 * NPC module: config-driven NPC placement (npc.yml) and dispatching interactions to
 * orelia-core's shop/job-change/warehouse/enhancement screens (via {@link GuiApi}/
 * {@link ItemApi}) or orelia-world's own quest screen.
 */
public final class NpcModule implements WorldModule {

    private final NpcRepository repository = new NpcRepository();
    private NpcSpawnService spawnService;
    private OreliaWorldPlugin plugin;

    @Override
    public String getName() {
        return "npc";
    }

    @Override
    public void onEnable(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
        ServicesManager services = plugin.getServer().getServicesManager();

        GuiApi guiApi = services.load(GuiApi.class);
        ItemApi itemApi = services.load(ItemApi.class);
        if (guiApi == null || itemApi == null) {
            throw new IllegalStateException("npc module requires OreliaCore's GuiApi and ItemApi");
        }
        Economy economy = services.load(Economy.class);

        QuestModule questModule = plugin.getModuleManager().get(QuestModule.class)
                .orElseThrow(() -> new IllegalStateException("npc module requires quest module"));

        reloadNpcs();

        NpcKeys keys = new NpcKeys(plugin);
        this.spawnService = new NpcSpawnService(keys, repository);
        NpcSpawnSyncService syncService = new NpcSpawnSyncService(keys, repository, spawnService);

        plugin.getServer().getPluginManager().registerEvents(new NpcInteractListener(
                spawnService, guiApi, new GuiManager(), questModule.getQuestGuiScreen(), questModule.getProgressService(),
                itemApi, economy), plugin);

        plugin.getAdminCommandRegistry().register("spawnnpc", new NpcSpawnCommand(repository, spawnService));

        // Delay one tick so every world referenced by npc.yml has finished loading.
        plugin.getSchedulerService().runLater(syncService::syncAll, 1L);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadNpcs();
    }

    private void reloadNpcs() {
        plugin.getConfigManager().register("npc.yml");
        YamlConfiguration config = plugin.getConfigManager().get("npc.yml").get();
        repository.load(config);
    }

    public NpcRepository getRepository() {
        return repository;
    }

    public NpcSpawnService getSpawnService() {
        return spawnService;
    }
}
