package ua.klesaak.vaultchat.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.List;

public class BungeePlayerList implements PluginMessageListener {
    private final VaultChatManager manager;
    private String server;
    private List<String> playerList;
    private BukkitTask updateTask;

    public BungeePlayerList(VaultChatManager manager) {
        this.manager = manager;
        manager.getPlugin().getServer().getMessenger().registerOutgoingPluginChannel(manager.getPlugin(), "BungeeCord");
        manager.getPlugin().getServer().getMessenger().registerIncomingPluginChannel(manager.getPlugin(), "BungeeCord", this);
        this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getPlugin(), this::updatePlayerList, 5L, 5L);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("PlayerList")) {
            this.server = in.readUTF();
            this.playerList = Arrays.asList(in.readUTF().split(", "));
        }
    }

    public void stopTask() {
        this.updateTask.cancel();
        this.updateTask = null;
    }

    @SneakyThrows
    private void updatePlayerList() {
        val b = new ByteArrayOutputStream();
        val out = new DataOutputStream(b);
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");
        this.sendPluginMessage(b.toByteArray());
    }

    @SneakyThrows
    public List<String> getPlayerList() {
        return playerList;
    }

    private void sendPluginMessage(Player player, byte[] bytes) {
        Bukkit.getScheduler().runTask(this.manager.getPlugin(), () -> player.sendPluginMessage(this.manager.getPlugin(), "BungeeCord", bytes));
    }

    private void sendPluginMessage(byte[] bytes) {
        Bukkit.getOnlinePlayers().parallelStream().findAny().ifPresent(player -> sendPluginMessage(player, bytes));
    }
}
