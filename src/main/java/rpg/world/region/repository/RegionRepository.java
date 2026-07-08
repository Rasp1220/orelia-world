package rpg.world.region.repository;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.world.region.model.RegionData;
import rpg.world.region.model.RegionType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of every {@link RegionData}, rebuilt from {@code regions.yml}.
 */
public final class RegionRepository {

    private Map<String, RegionData> regions = new LinkedHashMap<>();

    public void load(YamlConfiguration config) {
        Map<String, RegionData> loaded = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("regions");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection regionSection = section.getConfigurationSection(id);
                if (regionSection == null) {
                    continue;
                }
                loaded.put(id, parse(id, regionSection));
            }
        }
        this.regions = loaded;
    }

    private RegionData parse(String id, ConfigurationSection section) {
        ConfigurationSection bounds = section.getConfigurationSection("bounds");
        ConfigurationSection warp = section.getConfigurationSection("warp");
        return new RegionData(
                id,
                section.getString("name", id),
                RegionType.valueOf(section.getString("type", "AREA").trim().toUpperCase()),
                section.getString("world", "world"),
                bounds != null ? bounds.getDouble("min-x", 0) : 0,
                bounds != null ? bounds.getDouble("min-y", 0) : 0,
                bounds != null ? bounds.getDouble("min-z", 0) : 0,
                bounds != null ? bounds.getDouble("max-x", 0) : 0,
                bounds != null ? bounds.getDouble("max-y", 256) : 256,
                bounds != null ? bounds.getDouble("max-z", 0) : 0,
                section.getString("enter-message"),
                section.getString("exit-message"),
                warp != null ? warp.getString("world", section.getString("world", "world")) : null,
                warp != null ? warp.getDouble("x", 0) : 0,
                warp != null ? warp.getDouble("y", 64) : 0,
                warp != null ? warp.getDouble("z", 0) : 0,
                warp != null ? (float) warp.getDouble("yaw", 0) : 0);
    }

    public Optional<RegionData> findById(String id) {
        return Optional.ofNullable(regions.get(id));
    }

    public Map<String, RegionData> getAll() {
        return Map.copyOf(regions);
    }
}
