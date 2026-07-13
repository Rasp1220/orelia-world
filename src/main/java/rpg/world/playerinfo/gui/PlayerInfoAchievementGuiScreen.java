package rpg.world.playerinfo.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.util.ItemBuilder;

/**
 * Dedicated "実績" sub-screen of the player-info nether-star menu. No achievement-tracking
 * system exists yet in either plugin, so this is a placeholder entry point only. Opened from
 * {@link PlayerInfoGuiScreen}, which supplies the back button placed in this screen's
 * bottom-right slot.
 */
public final class PlayerInfoAchievementGuiScreen {

    private static final int SIZE = 36;
    private static final int PLACEHOLDER_SLOT = 13;
    private static final int BACK_SLOT = SIZE - 1;

    public Gui build(Player player, GuiButton backButton) {
        Gui gui = new Gui("&8プレイヤー情報 - 実績", SIZE);
        gui.set(BACK_SLOT, backButton);
        gui.set(PLACEHOLDER_SLOT, GuiButton.display(new ItemBuilder(Material.GRAY_DYE)
                .name("&7準備中")
                .lore("&8実績機能は今後実装予定です。")
                .build()));
        return gui;
    }
}
