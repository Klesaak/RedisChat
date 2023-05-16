package ua.klesaak.vaultchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ua.klesaak.vaultchat.manager.VaultChatManager;
import ua.klesaak.vaultchat.utils.AbstractBukkitCommand;

public class ReloadCommand extends AbstractBukkitCommand {
    private final VaultChatManager manager;

    public ReloadCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("chat-reload").setExecutor(this);
    }

    @Override
    public void onReceiveCommand(CommandSender sender, Command command, String[] args) {
        this.cmdVerifyPermission(sender, VaultChatManager.RELOAD_PERMISSION);
        sender.sendMessage(this.manager.reload());
    }
}