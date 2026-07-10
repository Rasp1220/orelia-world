package rpg.npc.repository;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import rpg.api.ShopEntry;
import rpg.npc.model.NpcData;
import rpg.npc.model.NpcEquipmentItem;
import rpg.npc.model.NpcType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of every {@link NpcData}, rebuilt from {@code npc.yml}.
 */
public final class NpcRepository {

    private static final Map<String, EquipmentSlot> EQUIPMENT_SLOTS_BY_KEY = Map.of(
            "head", EquipmentSlot.HEAD,
            "chest", EquipmentSlot.CHEST,
            "legs", EquipmentSlot.LEGS,
            "feet", EquipmentSlot.FEET,
            "hand", EquipmentSlot.HAND);

    private Map<String, NpcData> npcs = new LinkedHashMap<>();

    public void load(YamlConfiguration config) {
        Map<String, NpcData> loaded = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("npcs");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection npcSection = section.getConfigurationSection(id);
                if (npcSection == null) {
                    continue;
                }
                loaded.put(id, parse(id, npcSection));
            }
        }
        this.npcs = loaded;
    }

    private NpcData parse(String id, ConfigurationSection section) {
        List<ShopEntry> stock = new ArrayList<>();
        ConfigurationSection stockSection = section.getConfigurationSection("shop-stock");
        if (stockSection != null) {
            for (String stockId : stockSection.getKeys(false)) {
                ConfigurationSection entrySection = stockSection.getConfigurationSection(stockId);
                if (entrySection == null) {
                    continue;
                }
                stock.add(new ShopEntry(
                        entrySection.getString("kind", "WEAPON"),
                        entrySection.getString("id", stockId),
                        entrySection.getDouble("price", 0)));
            }
        }

        return new NpcData(
                id,
                section.getString("name", id),
                NpcType.valueOf(section.getString("type", "GUILD_RECEPTIONIST").trim().toUpperCase()),
                EntityType.valueOf(section.getString("entity-type", "VILLAGER").trim().toUpperCase()),
                section.getString("world", "world"),
                section.getDouble("x", 0),
                section.getDouble("y", 64),
                section.getDouble("z", 0),
                (float) section.getDouble("yaw", 0),
                section.getStringList("dialogue"),
                section.getString("conditional-item-id"),
                section.getStringList("conditional-dialogue"),
                stock,
                section.getStringList("quest-ids"),
                section.getDouble("enhancement-cost-base", 100),
                section.getDouble("enhancement-cost-per-level", 50),
                section.getString("profession"),
                parseEquipment(section.getConfigurationSection("equipment")));
    }

    private Map<EquipmentSlot, NpcEquipmentItem> parseEquipment(ConfigurationSection equipmentSection) {
        Map<EquipmentSlot, NpcEquipmentItem> equipment = new EnumMap<>(EquipmentSlot.class);
        if (equipmentSection == null) {
            return equipment;
        }
        for (Map.Entry<String, EquipmentSlot> entry : EQUIPMENT_SLOTS_BY_KEY.entrySet()) {
            ConfigurationSection slotSection = equipmentSection.getConfigurationSection(entry.getKey());
            if (slotSection == null) {
                continue;
            }
            try {
                Material material = Material.valueOf(slotSection.getString("material", "").trim().toUpperCase());
                equipment.put(entry.getValue(), new NpcEquipmentItem(material, slotSection.getInt("custom-model-data", 0)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return equipment;
    }

    public Optional<NpcData> findById(String id) {
        return Optional.ofNullable(npcs.get(id));
    }

    public Map<String, NpcData> getAll() {
        return Map.copyOf(npcs);
    }
}
