package ua.klesaak.vaultchat.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ua.klesaak.vaultchat.VaultChatPlugin;
import ua.klesaak.vaultchat.commands.ReloadCommand;
import ua.klesaak.vaultchat.commands.privatemessage.PrivateMessageCommand;
import ua.klesaak.vaultchat.commands.privatemessage.ReplyCommand;
import ua.klesaak.vaultchat.configurations.ConfigFile;
import ua.klesaak.vaultchat.listener.ChatListener;
import ua.klesaak.vaultchat.redis.RedisConfig;
import ua.klesaak.vaultchat.redis.RedisMessenger;

import java.util.concurrent.CompletableFuture;

@Getter @Setter
public class VaultChatManager {
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().disableHtmlEscaping().setLenient().create();
    public static final String RELOAD_PERMISSION        = "chatmanager.reload";
    public static final String COLOR_CHAT_PERMISSION    = "chatmanager.colorchat";
    public static final String ADMIN_CHAT_PERMISSION    = "chatmanager.admin-chat";
    public static final String DONATE_CHAT_PERMISSION   = "chatmanager.donate-chat";
    public static final int SERVER_PORT = Bukkit.getServer().getPort();
    private final VaultChatPlugin plugin;
    private ConfigFile configFile;
    private RedisConfig redisConfig;
    private JedisPool jedisPool;
    private RedisMessenger redisMessenger;
    private BungeePlayerList bungeePlayerList;

    public VaultChatManager(VaultChatPlugin plugin) {
        this.plugin = plugin;
        new ChatListener(this);
        this.configFile = new ConfigFile(this.plugin);
        this.redisConfig = new RedisConfig(this.configFile.getRedisSection());
        this.bungeePlayerList = new BungeePlayerList(this);
        new ReloadCommand(this);
        new PrivateMessageCommand(this);
        new ReplyCommand(this);
        this.jedisPool = this.redisConfig.newJedisPool();
        this.redisMessenger = new RedisMessenger(this);
    }

    public void sendPrivateMessage(Player messageSender, String receiver, String message) {
        if (messageSender.getName().equalsIgnoreCase(receiver)) {
            messageSender.sendMessage(this.configFile.getPrivateMessageSelf());
            return;
        }

        Player playerReceiver = Bukkit.getPlayerExact(receiver);
        String format = this.configFile.getPrivateMessageFormat();
        format = this.configFile.replaceAll(ConfigFile.SENDER_PLACEHOLDER_PATTERN, format, messageSender::getName);
        format = this.configFile.replaceAll(ConfigFile.RECEIVER_PLACEHOLDER_PATTERN, format, () -> receiver);
        format = this.configFile.replaceAll(ConfigFile.MESSAGE_PLACEHOLDER_PATTERN, format,  () -> message);
        if (playerReceiver == null && !this.bungeePlayerList.getPlayerList().contains(receiver)) {
            messageSender.sendMessage(this.configFile.getPlayerNotFound());
            return;
        }
        if (playerReceiver != null) {
            playerReceiver.sendMessage(format);
            messageSender.sendMessage(format);
            this.cachePlayer(playerReceiver.getUniqueId().toString(), messageSender.getName());
            return;
        }
        val messageData = new MessageData(ChatType.PRIVATE, VaultChatManager.SERVER_PORT, messageSender.getName(), receiver, message);
        this.redisMessenger.sendOutgoingMessage(this.redisConfig.getServerChanel(), messageData);
        messageSender.sendMessage(format);
    }

    public void cachePlayer(String receiverUUID, String sender) {
        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                jedis.select(this.redisConfig.getDatabase());
                jedis.set(receiverUUID, sender);
                jedis.expire(receiverUUID, this.configFile.getReplyLifetimeInSeconds());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String getCachedMessageSender(String receiverUUID) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.select(this.redisConfig.getDatabase());
            val cachedSender = jedis.get(receiverUUID);
            if (cachedSender != null) return cachedSender;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String reload() {
        this.configFile = new ConfigFile(this.plugin);
        this.redisConfig = new RedisConfig(this.configFile.getRedisSection());
        this.jedisPool.destroy();
        this.redisMessenger.close();
        this.jedisPool = this.redisConfig.newJedisPool();
        this.redisMessenger = new RedisMessenger(this);
        return "§aУспешно перезагружено.";
    }
}
