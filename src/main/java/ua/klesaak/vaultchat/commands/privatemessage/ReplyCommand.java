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

public class ReplyCommand implements CommandExecutor, TabCompleter {
    private final VaultChatManager manager;

    public ReplyCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("reply").setExecutor(this);
        this.manager.getPlugin().getCommand("reply").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 1 || args[1].trim().isEmpty()) {
            sender.sendMessage(this.manager.getConfigFile().getReplyMessageUsage(label));
            return true;
        }
        val companion = this.manager.getCachedMessageSender(((Player)sender).getUniqueId().toString());
        if (companion == null) {
            sender.sendMessage(this.manager.getConfigFile().getEmptyCompanions());
            return true;
        }
        this.manager.sendPrivateMessage(sender, companion, Utils.getFinalArg(args, 1));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            String receiver = args[0].toLowerCase();
            String companion = manager.getCachedMessageSender(((Player)sender).getUniqueId().toString());
            if (companion == null) return Collections.emptyList();
            return Utils.copyPartialMatches(receiver, Collections.singletonList(companion), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}