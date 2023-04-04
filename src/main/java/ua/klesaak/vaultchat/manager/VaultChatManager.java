package ua.klesaak.vaultchat.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import ua.klesaak.vaultchat.VaultChatPlugin;
import ua.klesaak.vaultchat.commands.ReloadCommand;
import ua.klesaak.vaultchat.commands.privatemessage.PrivateMessageCommand;
import ua.klesaak.vaultchat.commands.privatemessage.ReplyCommand;
import ua.klesaak.vaultchat.configurations.ConfigFile;
import ua.klesaak.vaultchat.listener.ChatListener;
import ua.klesaak.vaultchat.redis.RedisConfig;

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
    private RedisConfig.RedisPool redisPool;
    private RedisConfig redisConfig;
    private JedisPubSub jedisPubSub;
    private BungeePlayerList bungeePlayerList;

    public VaultChatManager(VaultChatPlugin plugin) {
        this.plugin = plugin;
        new ChatListener(this);
        this.configFile = new ConfigFile(this.plugin);
        this.redisConfig = new RedisConfig(this.configFile.getRedisSection());
        this.redisPool = this.redisConfig.newRedisPool();
        CompletableFuture.runAsync(this::subscribe); //запускаем слушатель канала в асинхроне, потому что он займёт текущий поток!
        this.bungeePlayerList = new BungeePlayerList(this);
        new ReloadCommand(this);
        new PrivateMessageCommand(this);
        new ReplyCommand(this);
    }

    public void sendPrivateMessage(CommandSender sender, String receiver, String message) {
        Player messageSender = (Player)sender;
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
            this.cachePlayer(playerReceiver.getUniqueId().toString(), playerReceiver.getName());
            return;
        }
        val messageData = new MessageData(ChatType.PRIVATE, VaultChatManager.SERVER_PORT, messageSender.getName(), receiver, message);
        this.redisPool.getRedis().publish(this.redisConfig.getServerChanel(), VaultChatManager.GSON.toJson(messageData));
        messageSender.sendMessage(format);
    }

    private void cachePlayer(String receiverUUID, String sender) {
        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = this.redisPool.getRedis()) {
                jedis.select(this.redisConfig.getDatabase());
                jedis.set(receiverUUID, sender);
                jedis.expire(receiverUUID, this.configFile.getReplyLifetimeInSeconds());
            }
        });
    }

    public String getCachedMessageSender(String receiverUUID) {
        try (Jedis jedis = this.redisPool.getRedis()) {
            jedis.select(this.redisConfig.getDatabase());
            if (jedis.exists(receiverUUID)) return jedis.get(receiverUUID);
        }
        return null;
    }

    private void subscribe() {
        this.jedisPubSub = new JedisPubSub() {

            @Override
            public void onMessage(String channel, String message) {
                val messageData = GSON.fromJson(message, MessageData.class);
                if (messageData.getServerPort() == SERVER_PORT) return;
                switch (messageData.getChatType()) {
                    case PRIVATE: {
                        val receiverBukkitPlayer = Bukkit.getPlayerExact(messageData.getReceiver());
                        if (receiverBukkitPlayer != null) {
                            String format = VaultChatManager.this.configFile.getPrivateMessageFormat();
                            format = VaultChatManager.this.configFile.replaceAll(ConfigFile.SENDER_PLACEHOLDER_PATTERN, format, messageData::getSender);
                            format = VaultChatManager.this.configFile.replaceAll(ConfigFile.RECEIVER_PLACEHOLDER_PATTERN, format, messageData::getReceiver);
                            format = VaultChatManager.this.configFile.replaceAll(ConfigFile.MESSAGE_PLACEHOLDER_PATTERN, format, messageData::getMessage);
                            VaultChatManager.this.cachePlayer(receiverBukkitPlayer.getUniqueId().toString(), messageData.getSender());
                            receiverBukkitPlayer.sendMessage(format);
                        }
                        break;
                    }
                    case LOCAL: {
                        System.out.println("todo1");
                        break;
                    }
                    case GLOBAL: {
                        System.out.println("todo2");
                        break;
                    }
                    case DONATE: {
                        System.out.println("todo3");
                        break;
                    }
                    case ADMIN: {
                        System.out.println("todo4");
                        break;
                    }
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
            }
        };
        this.redisPool.getRedis().subscribe(this.jedisPubSub, this.redisConfig.getServerChanel());
    }

    public void reload() {
        this.configFile = new ConfigFile(this.plugin);
        this.redisConfig = new RedisConfig(this.configFile.getRedisSection());
        this.redisPool = this.redisConfig.newRedisPool();
        CompletableFuture.runAsync(this::subscribe); //запускаем слушатель канала в асинхроне, потому что он займёт текущий поток!
    }
}
