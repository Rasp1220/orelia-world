package rpg.world.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import rpg.npc.NpcModule;
import rpg.npc.model.NpcData;
import rpg.world.core.OreliaWorldPlugin;

/**
 * {@code /rpgworldadmin reload} - re-reads every orelia-world config file and asks each
 * module to rebuild its in-memory state.
 * {@code /rpgworldadmin spawnnpc <id>} - spawns the given npc.yml entry at the sender's
 * current location, for quickly placing/testing NPCs without editing world data.
 * {@code /rpgworldadmin spawnnpc list} - lists every configured NPC id.
 */
public final class WorldAdminCommand implements CommandExecutor {

    private final OreliaWorldPlugin plugin;

    public WorldAdminCommand(OreliaWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("spawnnpc")) {
            handleSpawnNpc(sender, args);
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgworldadmin <reload|spawnnpc>");
            return true;
        }
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Orelia World configuration reloaded.");
        return true;
    }

    private void handleSpawnNpc(CommandSender sender, String[] args) {
        NpcModule npcModule = plugin.getModuleManager().get(NpcModule.class).orElse(null);
        if (npcModule == null) {
            sender.sendMessage(ChatColor.RED + "NPC module is not available.");
            return;
        }

        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(ChatColor.GREEN + "Configured NPCs:");
            for (NpcData data : npcModule.getRepository().getAll().values()) {
                sender.sendMessage(ChatColor.GRAY + "- " + data.getId() + " " + ChatColor.WHITE + data.getName());
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgworldadmin spawnnpc <id>");
            }
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return;
        }

        String npcId = args[1];
        Entity entity = npcModule.getSpawnService().spawn(npcId, player.getLocation()).orElse(null);
        if (entity == null) {
            sender.sendMessage(ChatColor.RED + "Unknown NPC id: " + npcId);
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Spawned " + npcId + " at your location.");
    }
}
