package ua.klesaak.vaultchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ua.klesaak.vaultchat.manager.VaultChatManager;

public class ReloadCommand implements CommandExecutor {
    private final VaultChatManager manager;

    public ReloadCommand(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getCommand("chat-reload").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(VaultChatManager.RELOAD_PERMISSION)) {
            return true;
        }
        this.manager.reload();
        sender.sendMessage("§aУспешно перезагружено.");
        return true;
    }
}