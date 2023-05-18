package ua.klesaak.vaultchat.redis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

@Getter @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisConfig {
    String address, password, serverChanel;
    int port, database;

    public RedisConfig(ConfigurationSection configurationSection) {
        this.address = configurationSection.getString("host");
        this.port = configurationSection.getInt("port");
        this.database = configurationSection.getInt("database");
        this.password = configurationSection.getString("password");
        this.serverChanel = configurationSection.getString("serverChannel");
    }

    public JedisPool newJedisPool() throws JedisException {
        val jpc = new JedisPoolConfig();
        //jpc.setLifo(false);
        jpc.setTestOnBorrow(true);
        jpc.setMinIdle(3);
        jpc.setMaxTotal(500);
        return new JedisPool(jpc, this.address, this.port, 30000, this.password == null || this.password.isEmpty() ? null : this.password);
    }
}
