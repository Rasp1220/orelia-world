package rpg.dungeon.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
import rpg.core.player.PlayerDataManager;
import rpg.dungeon.model.DungeonData;
import rpg.dungeon.model.PlayerDungeonComponent;
import rpg.dungeon.repository.DungeonRepository;
import rpg.dungeon.service.DungeonEncounterService;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.util.ItemBuilder;

import java.util.List;
import java.util.Set;

/**
 * Lists the viewing player's unlocked dungeons; clicking one attempts to start it, same as
 * right-clicking its trigger block a second time. Same shape as {@code QuestGuiScreen} - a
 * plain {@code build(Player) -> Gui} method reusing orelia-core's generic Gui framework.
 */
public final class DungeonGuiScreen {

    private final DungeonRepository repository;
    private final DungeonEncounterService encounterService;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messages;

    public DungeonGuiScreen(DungeonRepository repository, DungeonEncounterService encounterService,
                             PlayerDataManager playerDataManager, MessageManager messages) {
        this.repository = repository;
        this.encounterService = encounterService;
        this.playerDataManager = playerDataManager;
        this.messages = messages;
    }

    public Gui build(Player player) {
        Gui gui = new Gui("&%8ダンジョン", 27);
        PlayerDungeonComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerDungeonComponent.class))
                .orElse(null);
        Set<String> unlocked = component == null ? Set.of() : component.getUnlockedDungeonIds();

        int slot = 10;
        for (String dungeonId : unlocked) {
            DungeonData data = repository.findById(dungeonId).orElse(null);
            if (data == null) {
                continue;
            }
            gui.set(slot++, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                    .name("&%e" + data.getName())
                    .lore(List.of(
                            "&%7クリックして挑戦",
                            "&%7人数: " + data.getMinPartySize() + "〜" + data.getMaxPartySize(),
                            "&%7制限時間: " + data.getTimeLimitSeconds() + "秒"))
                    .build(), (clicker, clickType) -> handleClick(clicker, dungeonId)));
        }
        return gui;
    }

    private void handleClick(Player player, String dungeonId) {
        player.closeInventory();
        encounterService.challenge(player, dungeonId).ifPresentOrElse(
                failure -> messages.send(player, "dungeon.challenge-failed." + failure.name().toLowerCase()),
                () -> messages.send(player, "dungeon.challenge-started", "dungeon", dungeonId));
    }
}
