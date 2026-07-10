package rpg.npc.model;

import org.bukkit.Material;

/**
 * One piece of cosmetic gear an NPC spawns wearing/holding (SOW section 12 "見た目カスタマイズ").
 * Purely visual - {@link rpg.npc.service.NpcSpawnService} sets it directly as equipment with
 * zero drop chance, it is never a real obtainable item.
 */
public record NpcEquipmentItem(Material material, int customModelData) {
}
