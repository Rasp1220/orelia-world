package rpg.world.api;

import org.bukkit.plugin.ServicePriority;
import rpg.quest.QuestModule;
import rpg.world.core.OreliaWorldPlugin;
import rpg.world.core.module.WorldModule;

/**
 * Publishes orelia-world's own cross-plugin API ({@link QuestApi}) to Bukkit's
 * {@code ServicesManager}, mirroring orelia-core's {@code ApiModule}. Registered last so
 * every module it wraps is already enabled.
 */
public final class WorldApiModule implements WorldModule {

    @Override
    public String getName() {
        return "world-api";
    }

    @Override
    public void onEnable(OreliaWorldPlugin plugin) {
        QuestModule questModule = plugin.getModuleManager().get(QuestModule.class)
                .orElseThrow(() -> new IllegalStateException("world-api module requires quest module"));

        plugin.getServer().getServicesManager().register(
                QuestApi.class, new QuestApiImpl(plugin.getPlayerDataManager()), plugin, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
    }
}
