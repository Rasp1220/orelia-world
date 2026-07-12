package rpg.world.playerinfo;

import org.bukkit.plugin.ServicesManager;
import rpg.api.JobApi;
import rpg.api.SkillApi;
import rpg.gui.framework.GuiManager;
import rpg.quest.QuestModule;
import rpg.world.core.OreliaWorldPlugin;
import rpg.world.core.module.WorldModule;
import rpg.world.playerinfo.gui.PlayerInfoGuiScreen;
import rpg.world.playerinfo.listener.PlayerInfoItemListener;
import rpg.world.playerinfo.service.PlayerInfoItemKeys;
import rpg.world.playerinfo.service.PlayerInfoItemService;

/**
 * The nether-star "プレイヤー情報" menu: quests come from orelia-world's own quest module,
 * job/skill come from orelia-core through {@link JobApi}/{@link SkillApi}.
 */
public final class PlayerInfoModule implements WorldModule {

    @Override
    public String getName() {
        return "playerinfo";
    }

    @Override
    public void onEnable(OreliaWorldPlugin plugin) {
        ServicesManager services = plugin.getServer().getServicesManager();
        JobApi jobApi = services.load(JobApi.class);
        SkillApi skillApi = services.load(SkillApi.class);
        if (jobApi == null || skillApi == null) {
            throw new IllegalStateException("playerinfo module requires OreliaCore's JobApi and SkillApi");
        }

        QuestModule questModule = plugin.getModuleManager().get(QuestModule.class)
                .orElseThrow(() -> new IllegalStateException("playerinfo module requires quest module"));

        PlayerInfoItemService itemService = new PlayerInfoItemService(new PlayerInfoItemKeys(plugin));
        PlayerInfoGuiScreen guiScreen = new PlayerInfoGuiScreen(
                questModule.getQuestRepository(), plugin.getPlayerDataManager(), jobApi, skillApi);

        plugin.getServer().getPluginManager().registerEvents(
                new PlayerInfoItemListener(itemService, guiScreen, new GuiManager()), plugin);
    }

    @Override
    public void onDisable() {
    }
}
