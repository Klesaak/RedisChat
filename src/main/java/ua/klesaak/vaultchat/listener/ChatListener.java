package ua.klesaak.vaultchat.listener;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ua.klesaak.vaultchat.configurations.ConfigFile;
import ua.klesaak.vaultchat.manager.ChatType;
import ua.klesaak.vaultchat.manager.VaultChatManager;
import ua.klesaak.vaultchat.utils.Utils;
import ua.klesaak.vaultchat.utils.VaultUtils;

import java.util.Collection;
import java.util.HashSet;

public class ChatListener implements Listener {
    private static final char GLOBAL_CHAR = '!';
    private static final char DONATE_CHAR = '$';
    private static final char ADMIN_CHAR  = '%';
    private static final String PLAYER_PLACEHOLDER = "%1$s";
    private static final String MESSAGE_PLACEHOLDER = "%2$s";
    private final VaultChatManager manager;

    public ChatListener(VaultChatManager manager) {
        this.manager = manager;
        this.manager.getPlugin().getServer().getPluginManager().registerEvents(this, this.manager.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public final void onChat(AsyncPlayerChatEvent event) { //todo Отмечание игроков через @Klesaak в чате
        ChatType chatType = null;
        val player = event.getPlayer();
        String message = Utils.color(event.getMessage());
        val configFile = this.manager.getConfigFile();
        String format = null;
        if (message.charAt(0) == DONATE_CHAR && !player.hasPermission(VaultChatManager.DONATE_CHAT_PERMISSION)) {
            message = message.substring(1);
        }
        if (message.charAt(0) == ADMIN_CHAR && !player.hasPermission(VaultChatManager.ADMIN_CHAT_PERMISSION)) {
            message = message.substring(1);
        }
        switch (message.charAt(0)) {
            case GLOBAL_CHAR: {
                chatType = ChatType.GLOBAL;
                format = configFile.getGlobalChatFormat();
                message = message.substring(1);
                break;
            }
            case DONATE_CHAR: {
                chatType = ChatType.DONATE;
                format = configFile.getDonateChatFormat();
                message = message.substring(1);
                event.getRecipients().clear();
                event.getRecipients().addAll(this.getDonateRecipients());
                break;
            }
            case ADMIN_CHAR: {
                chatType = ChatType.ADMIN;
                format = configFile.getAdminChatFormat();
                message = message.substring(1);
                event.getRecipients().clear();
                event.getRecipients().addAll(this.getAdminRecipients());
                break;
            }
            default: {
                chatType = ChatType.LOCAL;
                format = configFile.getLocalChatFormat();
                event.getRecipients().clear();
                event.getRecipients().add(player);
                event.getRecipients().addAll(this.getLocalRecipients(player));
                break;
            }
        }
        if (!player.hasPermission(VaultChatManager.COLOR_CHAT_PERMISSION)) {
            message = ChatColor.stripColor(message.trim());
        }
        if (message.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(message);
        format = configFile.replaceAll(ConfigFile.PREFIX_PLACEHOLDER_PATTERN, format, () -> VaultUtils.getPrefix(player));
        format = configFile.replaceAll(ConfigFile.SUFFIX_PLACEHOLDER_PATTERN, format, () -> VaultUtils.getSuffix(player));
        format = configFile.replaceAll(ConfigFile.NAME_PLACEHOLDER_PATTERN, format,  () -> PLAYER_PLACEHOLDER);
        format = configFile.replaceAll(ConfigFile.MESSAGE_PLACEHOLDER_PATTERN, format, () -> MESSAGE_PLACEHOLDER);
        event.setFormat(format);
    }

    private Collection<Player> getLocalRecipients(Player player) {
        val players = new HashSet<Player>(Bukkit.getMaxPlayers());
        int range = this.manager.getConfigFile().getLocalChatRange();
        for (Entity ent : player.getNearbyEntities(range, range, range)) {
            if (ent.getType() != EntityType.PLAYER) continue;
            players.add((Player)ent);
        }
        return players;
    }

    private Collection<Player> getAdminRecipients() {
        val players = new HashSet<Player>(Bukkit.getMaxPlayers());
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (!pl.hasPermission(VaultChatManager.ADMIN_CHAT_PERMISSION)) continue;
            players.add(pl);
        }
        return players;
    }

    private Collection<Player> getDonateRecipients() {
        val players = new HashSet<Player>(Bukkit.getMaxPlayers());
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (!pl.hasPermission(VaultChatManager.DONATE_CHAT_PERMISSION)) continue;
            players.add(pl);
        }
        return players;
    }
}
