package rpg.world.region.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import rpg.util.ColorUtil;
import rpg.world.region.manager.RegionTracker;
import rpg.world.region.model.RegionData;
import rpg.world.region.repository.RegionRepository;
import rpg.world.region.service.RegionService;

import java.util.Objects;

/**
 * Detects region enter/exit on block-boundary movement and sends the configured messages
 * (SOW RegionModule). Triggers the warp teleport for {@code WARP}-type regions.
 */
public final class RegionMoveListener implements Listener {

    private final RegionService regionService;
    private final RegionRepository repository;
    private final RegionTracker tracker;

    public RegionMoveListener(RegionService regionService, RegionRepository repository, RegionTracker tracker) {
        this.regionService = regionService;
        this.repository = repository;
        this.tracker = tracker;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        RegionData newRegion = regionService.findRegionAt(event.getTo()).orElse(null);
        String previousId = tracker.getCurrentRegion(player.getUniqueId());
        String newId = newRegion != null ? newRegion.getId() : null;
        if (Objects.equals(previousId, newId)) {
            return;
        }

        if (previousId != null) {
            repository.findById(previousId).ifPresent(previousRegion -> {
                if (previousRegion.getExitMessage() != null && !previousRegion.getExitMessage().isBlank()) {
                    player.sendMessage(ColorUtil.colorize(previousRegion.getExitMessage()));
                }
            });
        }

        tracker.setCurrentRegion(player.getUniqueId(), newId);

        if (newRegion != null) {
            if (newRegion.getEnterMessage() != null && !newRegion.getEnterMessage().isBlank()) {
                player.sendMessage(ColorUtil.colorize(newRegion.getEnterMessage()));
            }
            regionService.warp(player, newRegion);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tracker.clear(event.getPlayer().getUniqueId());
    }
}
