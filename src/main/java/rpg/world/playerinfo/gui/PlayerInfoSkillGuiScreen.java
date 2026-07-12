package rpg.world.playerinfo.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.api.SkillApi;
import rpg.api.SkillSummary;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.util.ItemBuilder;

import java.util.List;

/**
 * Dedicated "スキル" sub-screen of the player-info nether-star menu. Opened from
 * {@link PlayerInfoGuiScreen}, which supplies the back button placed in this screen's
 * bottom-right slot.
 */
public final class PlayerInfoSkillGuiScreen {

    private static final int[] SKILL_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final int SIZE = 36;
    private static final int BACK_SLOT = SIZE - 1;

    private final SkillApi skillApi;

    public PlayerInfoSkillGuiScreen(SkillApi skillApi) {
        this.skillApi = skillApi;
    }

    public Gui build(Player player, GuiButton backButton) {
        Gui gui = new Gui("&8プレイヤー情報 - スキル", SIZE);
        gui.set(BACK_SLOT, backButton);

        List<SkillSummary> learned = skillApi.getLearnedSkills(player.getUniqueId());
        if (learned.isEmpty()) {
            gui.set(SKILL_SLOTS[0], GuiButton.display(new ItemBuilder(Material.PAPER)
                    .name("&7習得済みスキルはありません")
                    .build()));
            return gui;
        }

        int slotIndex = 0;
        for (SkillSummary skill : learned) {
            if (slotIndex >= SKILL_SLOTS.length) {
                break;
            }
            gui.set(SKILL_SLOTS[slotIndex++], GuiButton.display(new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&e" + skill.name())
                    .lore("&7Lv. " + skill.level() + " / " + skill.maxLevel())
                    .build()));
        }
        return gui;
    }
}
