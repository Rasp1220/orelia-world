package rpg.world.core.module;

import rpg.world.core.OreliaWorldPlugin;

/**
 * Lifecycle contract for orelia-world's own top-level modules (Quest, NPC, Dungeon,
 * Dialogue, Story, CutScene, Event). Mirrors {@code rpg.core.module.RpgModule}
 * from orelia-core - kept as a small separate interface here (rather than reused from the
 * core dependency) because it is parameterized on this plugin's own main class, not
 * orelia-core's.
 */
public interface WorldModule {

    String getName();

    void onEnable(OreliaWorldPlugin plugin);

    void onDisable();

    default void onReload() {
    }
}
