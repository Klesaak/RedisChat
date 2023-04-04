package ua.klesaak.vaultchat.commands.privatemessage;

import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.klesaak.vaultchat.manager.VaultChatManager;
import ua.klesaak.vaultchat.utils.Utils;

public class ReplyCommand implements CommandExecutor {
    private final VaultChatManager manager;

    public ReplyCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("reply").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 1 || args[0].trim().isEmpty()) {
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
}