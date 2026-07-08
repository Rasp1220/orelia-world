package rpg.quest.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.player.PlayerDataManager;
import rpg.quest.model.PlayerQuestComponent;
import rpg.quest.model.QuestState;

/**
 * {@code /rpgquest list} - shows the sender's active quests and their state.
 * {@code /rpgquest abandon <id>} - drops an in-progress quest without rewards.
 */
public final class QuestCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public QuestCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        PlayerQuestComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerQuestComponent.class))
                .orElse(null);
        if (component == null) {
            sender.sendMessage(ChatColor.RED + "Quest data is not loaded yet.");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("abandon")) {
            String questId = args[1];
            if (component.getActiveQuests().remove(questId) != null) {
                sender.sendMessage(ChatColor.YELLOW + "Abandoned quest: " + questId);
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have that quest active.");
            }
            return true;
        }

        if (component.getActiveQuests().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "You have no active quests.");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Active quests:");
        for (var entry : component.getActiveQuests().entrySet()) {
            QuestState state = entry.getValue().getState();
            sender.sendMessage(ChatColor.GRAY + "- " + entry.getKey() + " " + ChatColor.WHITE + "[" + state + "]");
        }
        return true;
    }
}
