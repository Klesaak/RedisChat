package ua.klesaak.vaultchat;

import lombok.Getter;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.LoadBefore;
import org.bukkit.plugin.java.annotation.dependency.LoadBeforePlugins;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import ua.klesaak.vaultchat.manager.VaultChatManager;

@Plugin(name = "ChatManager", version = "0.1")
@Author("Klesaak")
@Dependency("Vault")
@SoftDependency("PlaceholderAPI")
@LoadBeforePlugins({
        @LoadBefore("Vault"),
        @LoadBefore("PlaceholderAPI")
})
@Commands({
        @Command(name = "chat-reload"),
        @Command(name = "pm", aliases = {"privatemessage", "message", "m", "msg", "mes"}),
        @Command(name = "reply", aliases = {"r", "otvet", "rm"}),
        @Command(name = "chat"), // вкл/выкл чат
})
@Description("Async and packet simple score board plugin.")
@Permissions({
        @Permission(name = "chatmanager.reload", defaultValue = PermissionDefault.OP, desc = "Доступ к команде перезагрузки."),
        @Permission(name = "chatmanager.colorchat", defaultValue = PermissionDefault.OP, desc = "Возможность писать цветные сообщения."),
        @Permission(name = "chatmanager.admin-chat", defaultValue = PermissionDefault.OP, desc = "Доступ к админ-чату."),
        @Permission(name = "chatmanager.donate-chat", defaultValue = PermissionDefault.OP, desc = "Доступ к донат-чату.")
})
public final class VaultChatPlugin extends JavaPlugin {
   @Getter private static VaultChatManager vaultChatManager;

    @Override
    public void onEnable() {
        vaultChatManager = new VaultChatManager(this);
    }

    @Override
    public void onDisable() {
        vaultChatManager.getJedisPubSub().unsubscribe();
        vaultChatManager.getRedisPool().close();
    }
}
