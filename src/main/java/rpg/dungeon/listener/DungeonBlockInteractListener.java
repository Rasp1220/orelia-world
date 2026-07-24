package rpg.dungeon.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import rpg.core.message.MessageManager;
import rpg.core.player.PlayerDataManager;
import rpg.dungeon.model.PlayerDungeonComponent;
import rpg.dungeon.repository.DungeonBlockRepository;
import rpg.dungeon.service.DungeonEncounterService;

/**
 * Right-clicking a registered trigger block (see {@code /oladmin dungeonblock set}) unlocks
 * the associated dungeon on first click - a pure "discovery" that does not also start a run -
 * and attempts to start a run on every click after that.
 */
public final class DungeonBlockInteractListener implements Listener {

    private final DungeonBlockRepository blockRepository;
    private final DungeonEncounterService encounterService;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messages;

    public DungeonBlockInteractListener(DungeonBlockRepository blockRepository, DungeonEncounterService encounterService,
                                         PlayerDataManager playerDataManager, MessageManager messages) {
        this.blockRepository = blockRepository;
        this.encounterService = encounterService;
        this.playerDataManager = playerDataManager;
        this.messages = messages;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        String dungeonId = blockRepository.findDungeonId(clicked.getLocation()).orElse(null);
        if (dungeonId == null) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerDungeonComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerDungeonComponent.class)).orElse(null);
        if (component == null) {
            messages.send(player, "dungeon.data-not-loaded");
            return;
        }

        if (!component.isUnlocked(dungeonId)) {
            component.unlock(dungeonId);
            messages.send(player, "dungeon.unlocked", "dungeon", dungeonId);
            return;
        }
        encounterService.challenge(player, dungeonId).ifPresentOrElse(
                failure -> messages.send(player, "dungeon.challenge-failed." + failure.name().toLowerCase()),
                () -> messages.send(player, "dungeon.challenge-started", "dungeon", dungeonId));
    }
}
