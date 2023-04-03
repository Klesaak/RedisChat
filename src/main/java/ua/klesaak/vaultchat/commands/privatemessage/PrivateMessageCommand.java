package ua.klesaak.vaultchat.commands.privatemessage;

import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.klesaak.vaultchat.manager.VaultChatManager;
import ua.klesaak.vaultchat.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivateMessageCommand implements CommandExecutor, TabCompleter {
    private final VaultChatManager manager;

    public PrivateMessageCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("pm").setExecutor(this);
        this.manager.getPlugin().getCommand("pm").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 2 || args[0].trim().length() < 2 || args[1].trim().isEmpty()) {
            sender.sendMessage(this.manager.getConfigFile().getPrivateMessageUsage(label));
            return true;
        }
        this.manager.sendPrivateMessage(sender, args[0].trim(), Utils.getFinalArg(args, 1));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            String receiver = args[0].toLowerCase();
            val playerList = manager.getBungeePlayerList().getPlayerList();
            if (playerList == null) return Collections.emptyList();
            return Utils.copyPartialMatches(receiver, manager.getBungeePlayerList().getPlayerList(), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
