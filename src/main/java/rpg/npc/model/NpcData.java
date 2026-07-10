package rpg.npc.model;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import rpg.api.ShopEntry;

import java.util.List;
import java.util.Map;

/**
 * Static NPC definition loaded from {@code npc.yml} (SOW section 12). Which fields are
 * meaningful depends on {@link #getType()}: shop NPCs use {@link #getShopStock()}, the
 * quest receptionist uses {@link #getQuestIds()}, the enhancement NPC uses the enhance-
 * cost fields, and every type can show {@link #getDialogueLines()} plus an optional
 * alternate line when the player holds {@link #getConditionalItemId()} ("アイテム所持で
 * 会話変化"). {@link #getVillagerProfession()} and {@link #getEquipment()} are purely
 * cosmetic (SOW section 12 "見た目カスタマイズ") and apply regardless of type.
 */
public final class NpcData {

    private final String id;
    private final String name;
    private final NpcType type;
    private final EntityType entityType;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final List<String> dialogueLines;
    private final String conditionalItemId;
    private final List<String> conditionalDialogueLines;
    private final List<ShopEntry> shopStock;
    private final List<String> questIds;
    private final double enhancementCostBase;
    private final double enhancementCostPerLevel;
    private final String villagerProfession;
    private final Map<EquipmentSlot, NpcEquipmentItem> equipment;

    public NpcData(String id, String name, NpcType type, EntityType entityType, String world, double x, double y, double z,
                   float yaw, List<String> dialogueLines, String conditionalItemId, List<String> conditionalDialogueLines,
                   List<ShopEntry> shopStock, List<String> questIds, double enhancementCostBase, double enhancementCostPerLevel,
                   String villagerProfession, Map<EquipmentSlot, NpcEquipmentItem> equipment) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.entityType = entityType;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.dialogueLines = dialogueLines;
        this.conditionalItemId = conditionalItemId;
        this.conditionalDialogueLines = conditionalDialogueLines;
        this.shopStock = shopStock;
        this.questIds = questIds;
        this.enhancementCostBase = enhancementCostBase;
        this.enhancementCostPerLevel = enhancementCostPerLevel;
        this.villagerProfession = villagerProfession;
        this.equipment = equipment;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NpcType getType() {
        return type;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public List<String> getDialogueLines() {
        return dialogueLines;
    }

    public String getConditionalItemId() {
        return conditionalItemId;
    }

    public List<String> getConditionalDialogueLines() {
        return conditionalDialogueLines;
    }

    public List<ShopEntry> getShopStock() {
        return shopStock;
    }

    public List<String> getQuestIds() {
        return questIds;
    }

    public double getEnhancementCostBase() {
        return enhancementCostBase;
    }

    public double getEnhancementCostPerLevel() {
        return enhancementCostPerLevel;
    }

    /** Villager.Profession name to reskin this NPC as, or null for the entity type's default look. */
    public String getVillagerProfession() {
        return villagerProfession;
    }

    /** Cosmetic gear to equip on spawn, keyed by slot. Never dropped, never a real item. */
    public Map<EquipmentSlot, NpcEquipmentItem> getEquipment() {
        return equipment;
    }
}
