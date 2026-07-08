package rpg.world.core.module;

import rpg.world.core.OreliaWorldPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Registration-order lifecycle registry for {@link WorldModule}s, mirroring orelia-core's
 * {@code ModuleManager}.
 */
public final class WorldModuleManager {

    private final OreliaWorldPlugin plugin;
    private final List<WorldModule> registrationOrder = new ArrayList<>();
    private final Map<Class<? extends WorldModule>, WorldModule> byType = new LinkedHashMap<>();

    public WorldModuleManager(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(WorldModule module) {
        registrationOrder.add(module);
        byType.put(module.getClass(), module);
    }

    public void enableAll() {
        for (WorldModule module : registrationOrder) {
            try {
                module.onEnable(plugin);
                plugin.getLogger().info("Module enabled: " + module.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to enable module: " + module.getName(), e);
            }
        }
    }

    public void disableAll() {
        List<WorldModule> reversed = new ArrayList<>(registrationOrder);
        Collections.reverse(reversed);
        for (WorldModule module : reversed) {
            try {
                module.onDisable();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to disable module: " + module.getName(), e);
            }
        }
    }

    public void reloadAll() {
        for (WorldModule module : registrationOrder) {
            try {
                module.onReload();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reload module: " + module.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends WorldModule> Optional<T> get(Class<T> type) {
        return Optional.ofNullable((T) byType.get(type));
    }
}
