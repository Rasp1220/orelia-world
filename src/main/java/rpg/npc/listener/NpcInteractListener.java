package rpg.npc.listener;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import rpg.api.GuiApi;
import rpg.api.ItemApi;
import rpg.gui.framework.GuiManager;
import rpg.npc.model.NpcData;
import rpg.npc.service.NpcSpawnService;
import rpg.quest.gui.QuestGuiScreen;
import rpg.quest.service.QuestProgressService;
import rpg.util.ColorUtil;

import java.util.List;

/**
 * Dispatches an NPC click to the right screen/action for its {@link rpg.npc.model.NpcType}.
 * Shop/job-change/warehouse screens are core-owned and opened through {@link GuiApi}; the
 * quest screen is opened locally since Quest lives in orelia-world too.
 */
public final class NpcInteractListener implements Listener {

    private final NpcSpawnService spawnService;
    private final GuiApi guiApi;
    private final GuiManager guiManager;
    private final QuestGuiScreen questGuiScreen;
    private final QuestProgressService questProgressService;
    private final ItemApi itemApi;
    private final Economy economy;

    public NpcInteractListener(NpcSpawnService spawnService, GuiApi guiApi, GuiManager guiManager,
                                QuestGuiScreen questGuiScreen, QuestProgressService questProgressService,
                                ItemApi itemApi, Economy economy) {
        this.spawnService = spawnService;
        this.guiApi = guiApi;
        this.guiManager = guiManager;
        this.questGuiScreen = questGuiScreen;
        this.questProgressService = questProgressService;
        this.itemApi = itemApi;
        this.economy = economy;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        NpcData data = spawnService.dataOf(event.getRightClicked()).orElse(null);
        if (data == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        sendDialogue(player, data);
        questProgressService.onNpcTalked(player.getUniqueId(), data.getId());

        switch (data.getType()) {
            case WEAPON_SHOP, ARMOR_SHOP, ACCESSORY_SHOP -> guiApi.openShop(player, data.getShopStock());
            case QUEST_RECEPTIONIST -> guiManager.open(player, questGuiScreen.build(player, data.getQuestIds()));
            case JOB_CHANGE -> guiApi.openJobChange(player);
            case WAREHOUSE -> guiApi.openWarehouse(player);
            case ENHANCEMENT -> enhance(player, data);
            case GUILD_RECEPTIONIST -> {
                // Dialogue only for now - guild features are a hook for future modules.
            }
        }
    }

    private void enhance(Player player, NpcData data) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (itemApi.identifyWeapon(weapon).isEmpty()) {
            player.sendMessage(ChatColor.RED + "強化する武器を手に持ってください。");
            return;
        }
        int currentLevel = itemApi.getEnhancementLevel(weapon);
        double cost = data.getEnhancementCostBase() + data.getEnhancementCostPerLevel() * currentLevel;
        if (economy == null || !economy.has(player, cost) || !economy.withdrawPlayer(player, cost).transactionSuccess()) {
            player.sendMessage(ChatColor.RED + "強化費用が足りません（" + cost + "）。");
            return;
        }
        int newLevel = itemApi.enhanceWeapon(weapon);
        player.sendMessage(ChatColor.GREEN + "武器を強化しました！ (+" + newLevel + ")");
    }

    private void sendDialogue(Player player, NpcData data) {
        List<String> lines = data.getDialogueLines();
        if (data.getConditionalItemId() != null && !data.getConditionalDialogueLines().isEmpty()
                && hasItem(player, data.getConditionalItemId())) {
            lines = data.getConditionalDialogueLines();
        }
        for (String line : lines) {
            player.sendMessage(ColorUtil.colorize("&f[" + data.getName() + "] &7" + line));
        }
    }

    private boolean hasItem(Player player, String itemId) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) {
                continue;
            }
            if (itemApi.identifyWeapon(stack).map(itemId::equals).orElse(false)) {
                return true;
            }
            try {
                if (stack.getType() == Material.valueOf(itemId.trim().toUpperCase())) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }
}
