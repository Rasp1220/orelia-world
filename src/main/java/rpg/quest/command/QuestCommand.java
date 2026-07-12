package rpg.quest.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import rpg.core.player.PlayerDataManager;
import rpg.quest.model.PlayerQuestComponent;
import rpg.quest.model.QuestState;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /ol quest list} - shows the sender's active quests and their state.
 * {@code /ol quest abandon <id>} - drops an in-progress quest without rewards.
 */
public final class QuestCommand implements CommandExecutor, TabCompleter {

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return matching(List.of("list", "abandon"), args.length == 0 ? "" : args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("abandon") && sender instanceof Player player) {
            PlayerQuestComponent component = playerDataManager.get(player.getUniqueId())
                    .flatMap(d -> d.component(PlayerQuestComponent.class))
                    .orElse(null);
            if (component != null) {
                return matching(component.getActiveQuests().keySet(), args[1]);
            }
        }
        return List.of();
    }

    private List<String> matching(Iterable<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
