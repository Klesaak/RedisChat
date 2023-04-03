package ua.klesaak.vaultchat.configurations;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ua.klesaak.vaultchat.utils.Utils;
import ua.klesaak.vaultchat.utils.VaultUtils;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ConfigFile extends PluginConfig {
    public static final String PLAYER_NAME_PLACEHOLDER = "(playerName)";
    public static final String MESSAGE_PLACEHOLDER = "(message)";
    public static final String PREFIX_PLACEHOLDER = "(prefix)";
    public static final String SUFFIX_PLACEHOLDER = "(suffix)";
    public static final String SENDER_PLACEHOLDER = "(sender)";
    public static final String RECEIVER_PLACEHOLDER = "(receiver)";
    public static final String LABEL_PLACEHOLDER = "(label)";

    public static final Pattern NAME_PLACEHOLDER_PATTERN = Pattern.compile(PLAYER_NAME_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern PREFIX_PLACEHOLDER_PATTERN = Pattern.compile(PREFIX_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern SUFFIX_PLACEHOLDER_PATTERN = Pattern.compile(SUFFIX_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern MESSAGE_PLACEHOLDER_PATTERN = Pattern.compile(MESSAGE_PLACEHOLDER, Pattern.LITERAL);

    public static final Pattern SENDER_PLACEHOLDER_PATTERN = Pattern.compile(SENDER_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern RECEIVER_PLACEHOLDER_PATTERN = Pattern.compile(RECEIVER_PLACEHOLDER, Pattern.LITERAL);
    public static final Pattern LABEL_PLACEHOLDER_PATTERN = Pattern.compile(LABEL_PLACEHOLDER, Pattern.LITERAL);

    private final int localChatRange, replyLifetimeInSeconds;
    private final String localChatFormat, globalChatFormat, donateChatFormat, adminChatFormat, privateMessageFormat;
    private final String privateMessageUsage, privateMessageSelf, replyUsage, emptyCompanions, playerNotFound;

    public ConfigFile(JavaPlugin plugin) {
        super(plugin, "config.yml");
        this.localChatRange = this.getInt("localChatRange");
        this.replyLifetimeInSeconds = this.getInt("privateChatSettings.replyLifetime");
        this.localChatFormat = Utils.color(this.getString("chatFormat.local"));
        this.globalChatFormat = Utils.color(this.getString("chatFormat.global"));
        this.donateChatFormat = Utils.color(this.getString("chatFormat.donate"));
        this.adminChatFormat = Utils.color(this.getString("chatFormat.admin"));
        this.privateMessageFormat = Utils.color(this.getString("privateChatSettings.messageFormat"));
        this.privateMessageUsage = Utils.color(this.getString("messages.privateMessageUsage"));
        this.privateMessageSelf = Utils.color(this.getString("messages.privateMessageSelf"));
        this.replyUsage = Utils.color(this.getString("messages.replyUsage"));
        this.emptyCompanions = Utils.color(this.getString("messages.emptyCompanions"));
        this.playerNotFound = Utils.color(this.getString("messages.playerNotFound"));
    }

    public String getPrivateMessageUsage(String label) {
        return this.replaceAll(ConfigFile.LABEL_PLACEHOLDER_PATTERN, this.privateMessageUsage, () -> label);
    }

    public String getReplyMessageUsage(String label) {
        return this.replaceAll(ConfigFile.LABEL_PLACEHOLDER_PATTERN, this.replyUsage, () -> label);
    }

    public ConfigurationSection getRedisSection() {
        return this.getConfigurationSection("redis");
    }

    /**
     * Equivalent to {@link String#replace(CharSequence, CharSequence)}, but uses a
     * {@link Supplier} for the replacement.
     *
     * @param pattern     the pattern for the replacement target
     * @param input       the input string
     * @param replacement the replacement
     * @return the input string with the replacements applied
     */
    public String replaceAll(Pattern pattern, String input, Supplier<String> replacement) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) return matcher.replaceAll(Matcher.quoteReplacement(replacement.get()));
        return input;

    }
}
