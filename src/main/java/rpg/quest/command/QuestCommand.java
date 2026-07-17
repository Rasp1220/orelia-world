package rpg.quest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
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
    private final MessageManager messages;

    public QuestCommand(PlayerDataManager playerDataManager, MessageManager messages) {
        this.playerDataManager = playerDataManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }
        PlayerQuestComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerQuestComponent.class))
                .orElse(null);
        if (component == null) {
            messages.send(sender, "quest.data-not-loaded");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("abandon")) {
            String questId = args[1];
            if (component.getActiveQuests().remove(questId) != null) {
                messages.send(sender, "quest.abandoned", "quest", questId);
            } else {
                messages.send(sender, "quest.not-active");
            }
            return true;
        }

        if (component.getActiveQuests().isEmpty()) {
            messages.send(sender, "quest.no-active");
            return true;
        }
        messages.send(sender, "quest.active-header");
        for (var entry : component.getActiveQuests().entrySet()) {
            QuestState state = entry.getValue().getState();
            messages.sendRaw(sender, "quest.active-entry", "quest", entry.getKey(), "state", state);
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
