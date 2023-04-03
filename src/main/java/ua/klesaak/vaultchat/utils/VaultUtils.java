package ua.klesaak.vaultchat.utils;

import lombok.experimental.UtilityClass;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

@UtilityClass
public class VaultUtils {
    private final Permission PERMS   = getProvider(Permission.class);
    private final Economy ECO        = getProvider(Economy.class);
    private final Chat CHAT          = getProvider(Chat.class);

    public String getGroup(Player p) {
        if (PERMS == null || p == null) return "";
        return PERMS.getPrimaryGroup(p);
    }

    public String getPrefix(Player p) {
        if (CHAT == null || p == null) return "";
        return Utils.color(CHAT.getPlayerPrefix(p));
    }

    public String getGroupPrefix(Player p) {
        if (CHAT == null || p == null) return "";
        return Utils.color(CHAT.getGroupPrefix(p.getWorld(), CHAT.getPrimaryGroup(p)));
    }

    public String getGroupSuffix(Player p) {
        if (CHAT == null || p == null) return "";
        return Utils.color(CHAT.getGroupSuffix(p.getWorld(), CHAT.getPrimaryGroup(p)));
    }

    public String getSuffix(Player p) {
        if (CHAT == null || p == null) return "";
        return Utils.color(CHAT.getPlayerSuffix(p));
    }

    @SuppressWarnings("deprecation")
    public void addMoney(Player p, double count) {
        if (ECO == null || p == null) return;
        ECO.depositPlayer(p.getName(), count);
    }

    @SuppressWarnings("deprecation")
    public void addMoney(String playerName, double count) {
        if (ECO == null || playerName == null) return;
        ECO.depositPlayer(playerName, count);
    }

    @SuppressWarnings("deprecation")
    public void takeMoney(Player p, double count) {
        if (ECO == null || p == null) return;
        if (getMoney(p) >= count) {
            ECO.withdrawPlayer(p.getName(), count).transactionSuccess();
        }
    }

    @SuppressWarnings("deprecation")
    public int getMoney(Player p) {
        if (ECO == null || p == null) return 0;
        return (int) ECO.getBalance(p.getName());
    }

    @SuppressWarnings("deprecation")
    public int getMoney(String playerName) {
        if (ECO == null || playerName == null) return 0;
        return (int) ECO.getBalance(playerName);
    }

    public boolean hasMoney(String playerName, double moneyCount) {
        return getMoney(playerName) >= moneyCount;
    }

    public void setMoney(String playerName, double moneyCount) {
        double playerBalance = getMoney(playerName);

        if (moneyCount > playerBalance) {
            giveMoney(playerName, moneyCount - playerBalance);
        }

        if (moneyCount < playerBalance) {
            takeMoney(playerName, playerBalance - moneyCount);
        }
    }

    @SuppressWarnings("deprecation")
    public void giveMoney(String playerName, double moneyToGive) {
        if (ECO == null) return;
        ECO.depositPlayer(playerName, moneyToGive);
    }

    @SuppressWarnings("deprecation")
    public void takeMoney(String playerName, double moneyToTake) {
        if (ECO == null) return;
        ECO.withdrawPlayer(playerName, moneyToTake);
    }

    private <P> P getProvider(Class<P> clazz) {
        RegisteredServiceProvider<P> provider = Bukkit.getServer().getServicesManager().getRegistration(clazz);
        return (provider != null) ? provider.getProvider() : null;
    }
}
