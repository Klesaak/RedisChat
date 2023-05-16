package ua.klesaak.vaultchat.redis;

import lombok.val;
import org.bukkit.Bukkit;
import redis.clients.jedis.*;
import ua.klesaak.vaultchat.configurations.ConfigFile;
import ua.klesaak.vaultchat.manager.MessageData;
import ua.klesaak.vaultchat.manager.VaultChatManager;

import java.util.concurrent.CompletableFuture;

public class RedisMessenger {

    private /* final */ JedisPool jedisPool;
    private /* final */ Subscription sub;
    private boolean closing = false;
    private VaultChatManager manager;

    public RedisMessenger(VaultChatManager manager) {
        this.manager = manager;
    }

    public void init(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.sub = new Subscription();
        CompletableFuture.runAsync(this.sub);
    }

    public void sendOutgoingMessage(String channel, MessageData messageData) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.publish(channel, VaultChatManager.GSON.toJson(messageData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void close() {
        this.closing = true;
        this.sub.unsubscribe();
        this.jedisPool.destroy();
    }

    private class Subscription extends JedisPubSub implements Runnable {

        @Override
        public void run() {
            boolean first = true;
            while (!RedisMessenger.this.closing && !Thread.interrupted() && !RedisMessenger.this.jedisPool.isClosed()) {
                try (Jedis jedis = RedisMessenger.this.jedisPool.getResource()) {
                    if (first) {
                        first = false;
                    } else {
                        RedisMessenger.this.manager.getPlugin().getLogger().info("Redis pub-sub connection re-established");
                    }

                    jedis.subscribe(this, RedisMessenger.this.manager.getRedisConfig().getServerChanel()); // blocking call
                } catch (Exception e) {
                    if (RedisMessenger.this.closing) {
                        return;
                    }

                    RedisMessenger.this.manager.getPlugin().getLogger().info("Redis pub-sub connection dropped, trying to re-open the connection " + e);
                    try {
                        unsubscribe();
                    } catch (Exception ignored) {

                    }

                    // Sleep for 5 seconds to prevent massive spam in console
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        @Override
        public void onMessage(String channel, String msg) {
            val messageData = VaultChatManager.GSON.fromJson(msg, MessageData.class);
            if (messageData.getServerPort() == VaultChatManager.SERVER_PORT) return;
            switch (messageData.getChatType()) {
                case PRIVATE: {
                    val receiverBukkitPlayer = Bukkit.getPlayerExact(messageData.getReceiver());
                    if (receiverBukkitPlayer != null) {
                        String format = RedisMessenger.this.manager.getConfigFile().getPrivateMessageFormat();
                        format = RedisMessenger.this.manager.getConfigFile().replaceAll(ConfigFile.SENDER_PLACEHOLDER_PATTERN, format, messageData::getSender);
                        format = RedisMessenger.this.manager.getConfigFile().replaceAll(ConfigFile.RECEIVER_PLACEHOLDER_PATTERN, format, messageData::getReceiver);
                        format = RedisMessenger.this.manager.getConfigFile().replaceAll(ConfigFile.MESSAGE_PLACEHOLDER_PATTERN, format, messageData::getMessage);
                        RedisMessenger.this.manager.cachePlayer(receiverBukkitPlayer.getUniqueId().toString(), messageData.getSender());
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
    }

}