package rpg.npc.service;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import rpg.npc.model.NpcData;
import rpg.npc.model.NpcEquipmentItem;
import rpg.npc.repository.NpcRepository;

import java.util.Map;
import java.util.Optional;

/**
 * Spawns the vanilla entity backing an {@link NpcData} definition as a static, harmless
 * fixture: no AI pathing, invulnerable, silent - a "fake NPC" rather than a real mob.
 */
public final class NpcSpawnService {

    private final NpcKeys keys;
    private final NpcRepository repository;

    public NpcSpawnService(NpcKeys keys, NpcRepository repository) {
        this.keys = keys;
        this.repository = repository;
    }

    public Optional<Entity> spawn(String npcId, Location location) {
        NpcData data = repository.findById(npcId).orElse(null);
        if (data == null) {
            return Optional.empty();
        }

        Entity entity = location.getWorld().spawnEntity(location, data.getEntityType());
        entity.getPersistentDataContainer().set(keys.npcId(), PersistentDataType.STRING, data.getId());
        entity.setCustomName(data.getName());
        entity.setCustomNameVisible(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setGravity(false);

        if (entity instanceof Mob mob) {
            mob.setAware(false);
            mob.setAI(false);
        }
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setCollidable(false);
            applyCosmetics(livingEntity, data);
        }

        return Optional.of(entity);
    }

    /** Reskins the spawned entity per npc.yml's optional {@code profession}/{@code equipment} (SOW section 12). */
    private void applyCosmetics(LivingEntity entity, NpcData data) {
        if (entity instanceof Villager villager && data.getVillagerProfession() != null) {
            try {
                villager.setProfession(Villager.Profession.valueOf(data.getVillagerProfession().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }
        for (Map.Entry<EquipmentSlot, NpcEquipmentItem> entry : data.getEquipment().entrySet()) {
            NpcEquipmentItem item = entry.getValue();
            ItemStack stack = new ItemStack(item.material());
            if (item.customModelData() > 0) {
                ItemMeta meta = stack.getItemMeta();
                meta.setCustomModelData(item.customModelData());
                stack.setItemMeta(meta);
            }
            equipment.setItem(entry.getKey(), stack);
            equipment.setDropChance(entry.getKey(), 0f);
        }
    }

    public Optional<String> idOf(Entity entity) {
        return Optional.ofNullable(entity.getPersistentDataContainer().get(keys.npcId(), PersistentDataType.STRING));
    }

    public Optional<NpcData> dataOf(Entity entity) {
        return idOf(entity).flatMap(repository::findById);
    }
}
