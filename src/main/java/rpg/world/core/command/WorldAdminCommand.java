package rpg.world.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rpg.world.core.OreliaWorldPlugin;

/**
 * {@code /rpgworldadmin reload} - re-reads every orelia-world config file and asks each
 * module to rebuild its in-memory state.
 */
public final class WorldAdminCommand implements CommandExecutor {

    private final OreliaWorldPlugin plugin;

    public WorldAdminCommand(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgworldadmin reload");
            return true;
        }
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Orelia World configuration reloaded.");
        return true;
    }
}
