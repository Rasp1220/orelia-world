package rpg.world.playerinfo.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import rpg.gui.framework.GuiManager;
import rpg.world.playerinfo.gui.PlayerInfoGuiScreen;
import rpg.world.playerinfo.service.PlayerInfoItemService;

/**
 * Keeps the "プレイヤー情報" Nether Star in the player's rightmost hotbar slot, blocks it
 * from being thrown with Q, and opens {@link PlayerInfoGuiScreen} on right click.
 */
public final class PlayerInfoItemListener implements Listener {

    private final PlayerInfoItemService itemService;
    private final PlayerInfoGuiScreen guiScreen;
    private final GuiManager guiManager;

    public PlayerInfoItemListener(PlayerInfoItemService itemService, PlayerInfoGuiScreen guiScreen, GuiManager guiManager) {
        this.itemService = itemService;
        this.guiScreen = guiScreen;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        itemService.ensureInHotbar(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        itemService.ensureInHotbar(event.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (itemService.isPlayerInfoItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!itemService.isPlayerInfoItem(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        guiManager.open(player, guiScreen.build(player));
    }
}
