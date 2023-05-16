package ua.klesaak.vaultchat.commands.privatemessage;

import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.klesaak.vaultchat.utils.AbstractBukkitCommand;
import ua.klesaak.vaultchat.manager.VaultChatManager;
import ua.klesaak.vaultchat.utils.Utils;

public class ReplyCommand extends AbstractBukkitCommand {
    private final VaultChatManager manager;

    public ReplyCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("reply").setExecutor(this);
    }

    @Override
    public void onReceiveCommand(CommandSender sender, Command command, String[] args) {
        Player player = this.cmdVerifyPlayer(sender);
        if (args.length < 1 || args[0].trim().isEmpty()) {
            player.sendMessage(this.manager.getConfigFile().getReplyMessageUsage(command.getLabel()));
            return;
        }
        val companion = this.manager.getCachedMessageSender(player.getUniqueId().toString());
        if (companion == null) {
            player.sendMessage(this.manager.getConfigFile().getEmptyCompanions());
            return;
        }
        this.manager.sendPrivateMessage(player, companion, Utils.getFinalArg(args, 0));
    }
}