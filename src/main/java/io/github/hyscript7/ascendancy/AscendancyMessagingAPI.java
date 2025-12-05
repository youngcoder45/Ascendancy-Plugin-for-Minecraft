package io.github.hyscript7.ascendancy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * A flexible messaging framework for PaperMC plugins.
 * Supports multiple message types, placeholders, and consistent formatting.
 */
public class AscendancyMessagingAPI {
    private static AscendancyMessagingAPI instance;

    public static AscendancyMessagingAPI getInstance() {
        if (instance == null) {
            instance = new AscendancyMessagingAPI("Ascendancy");
        }
        return instance;
    }

    private final MiniMessage miniMessage;
    private String prefix;
    private final Map<MessageType, MessageConfig> typeConfigs;

    public AscendancyMessagingAPI(String pluginName) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = "<gradient:#6933b9:#896dcf>[" + pluginName + "]</gradient> ";
        this.typeConfigs = new HashMap<>();

        // Initialize default message type configurations
        initializeDefaults();
    }

    private void initializeDefaults() {
        typeConfigs.put(MessageType.INFO, new MessageConfig("<gray>", null));
        typeConfigs.put(MessageType.SUCCESS, new MessageConfig("<green>", org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP));
        typeConfigs.put(MessageType.WARNING, new MessageConfig("<yellow>", org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS));
        typeConfigs.put(MessageType.ERROR, new MessageConfig("<red>", org.bukkit.Sound.ENTITY_VILLAGER_NO));
        typeConfigs.put(MessageType.HIGHLIGHT, new MessageConfig("<aqua>", null));
    }

    /**
     * Sets a custom prefix for all messages
     */
    public AscendancyMessagingAPI setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Configures a message type with color and optional sound
     */
    public AscendancyMessagingAPI configureType(MessageType type, String colorPrefix, org.bukkit.Sound sound) {
        typeConfigs.put(type, new MessageConfig(colorPrefix, sound));
        return this;
    }

    /**
     * Sends an info message to a recipient
     */
    public void info(CommandSender recipient, String message, Object... placeholders) {
        send(recipient, MessageType.INFO, message, placeholders);
    }

    /**
     * Sends a success message to a recipient
     */
    public void success(CommandSender recipient, String message, Object... placeholders) {
        send(recipient, MessageType.SUCCESS, message, placeholders);
    }

    /**
     * Sends a warning message to a recipient
     */
    public void warning(CommandSender recipient, String message, Object... placeholders) {
        send(recipient, MessageType.WARNING, message, placeholders);
    }

    /**
     * Sends an error message to a recipient
     */
    public void error(CommandSender recipient, String message, Object... placeholders) {
        send(recipient, MessageType.ERROR, message, placeholders);
    }

    /**
     * Sends a highlighted message to a recipient
     */
    public void highlight(CommandSender recipient, String message, Object... placeholders) {
        send(recipient, MessageType.HIGHLIGHT, message, placeholders);
    }

    /**
     * Sends a message without the prefix
     */
    public void raw(CommandSender recipient, String message, Object... placeholders) {
        String formatted = formatPlaceholders(message, placeholders);
        Component component = miniMessage.deserialize(formatted);
        recipient.sendMessage(component);
    }

    /**
     * Core send method that handles all message types
     */
    public void send(CommandSender recipient, MessageType type, String message, Object... placeholders) {
        MessageConfig config = typeConfigs.get(type);
        String formatted = formatPlaceholders(message, placeholders);
        String fullMessage = prefix + config.colorPrefix + formatted;

        Component component = miniMessage.deserialize(fullMessage);
        recipient.sendMessage(component);

        // Play sound if configured and recipient is a player
        if (config.sound != null && recipient instanceof Player player) {
            player.playSound(player.getLocation(), config.sound, 0.5f, 1.0f);
        }
    }

    /**
     * Sends a message with a header
     */
    public void sendWithHeader(CommandSender recipient, MessageType type, String header, String message, Object... placeholders) {
        sendBoxed(recipient, type, header, null, message, placeholders);
    }

    /**
     * Sends a message with a footer
     */
    public void sendWithFooter(CommandSender recipient, MessageType type, String footer, String message, Object... placeholders) {
        sendBoxed(recipient, type, null, footer, message, placeholders);
    }

    /**
     * Sends a message with both header and footer
     */
    public void sendBoxed(CommandSender recipient, MessageType type, String header, String footer, String message, Object... placeholders) {
        MessageConfig config = typeConfigs.get(type);
        String formatted = formatPlaceholders(message, placeholders);

        // Send header if present
        if (header != null && !header.isEmpty()) {
            String headerLine = createDivider(header);
            recipient.sendMessage(miniMessage.deserialize(headerLine));
        }

        // Send main message
        String fullMessage = prefix + config.colorPrefix + formatted;
        Component component = miniMessage.deserialize(fullMessage);
        recipient.sendMessage(component);

        // Send footer if present
        if (footer != null && !footer.isEmpty()) {
            String footerLine = createDivider(footer);
            recipient.sendMessage(miniMessage.deserialize(footerLine));
        }

        // Play sound if configured and recipient is a player
        if (config.sound != null && recipient instanceof Player player) {
            player.playSound(player.getLocation(), config.sound, 0.5f, 1.0f);
        }
    }

    /**
     * Creates a divider line with optional text in the center
     */
    private String createDivider(String text) {
        if (text == null || text.isEmpty()) {
            return "<gray>---------------------------------------------------";
        }

        // Calculate padding for centered text
        int totalLength = 51; // Total character width
        String plainText = " " + text + " ";
        int textLength = plainText.length();
        int dashesNeeded = totalLength - textLength;
        int leftDashes = dashesNeeded / 2;
        int rightDashes = dashesNeeded - leftDashes;

        StringBuilder divider = new StringBuilder("<gray>");
        divider.append("-".repeat(Math.max(0, leftDashes)));
        divider.append(plainText);
        divider.append("-".repeat(Math.max(0, rightDashes)));

        return divider.toString();
    }

    /**
     * Sends an action bar message to a player
     */
    public void actionBar(Player player, String message, Object... placeholders) {
        String formatted = formatPlaceholders(message, placeholders);
        Component component = miniMessage.deserialize(formatted);
        player.sendActionBar(component);
    }

    /**
     * Sends a title to a player
     */
    public void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = miniMessage.deserialize(title);
        Component subtitleComponent = subtitle != null ? miniMessage.deserialize(subtitle) : Component.empty();

        Title titleObj = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        );

        player.showTitle(titleObj);
    }

    /**
     * Broadcasts a message to all online players
     */
    public void broadcast(MessageType type, String message, Object... placeholders) {
        MessageConfig config = typeConfigs.get(type);
        String formatted = formatPlaceholders(message, placeholders);
        String fullMessage = prefix + config.colorPrefix + formatted;

        Component component = miniMessage.deserialize(fullMessage);

        org.bukkit.Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            player.sendMessage(component);
            if (config.sound != null) {
                player.playSound(player.getLocation(), config.sound, 0.5f, 1.0f);
            }
        });
    }

    /**
     * Broadcasts a boxed message to all online players
     */
    public void broadcastBoxed(MessageType type, String header, String footer, String message, Object... placeholders) {
        MessageConfig config = typeConfigs.get(type);
        String formatted = formatPlaceholders(message, placeholders);

        // Prepare components
        Component headerComponent = header != null ? miniMessage.deserialize(createDivider(header)) : null;
        Component messageComponent = miniMessage.deserialize(prefix + config.colorPrefix + formatted);
        Component footerComponent = footer != null ? miniMessage.deserialize(createDivider(footer)) : null;

        org.bukkit.Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            if (headerComponent != null) player.sendMessage(headerComponent);
            player.sendMessage(messageComponent);
            if (footerComponent != null) player.sendMessage(footerComponent);

            if (config.sound != null) {
                player.playSound(player.getLocation(), config.sound, 0.5f, 1.0f);
            }
        });
    }

    /**
     * Formats a message with placeholders
     * Supports {0}, {1}, {2}... style placeholders
     */
    private String formatPlaceholders(String message, Object... placeholders) {
        if (placeholders.length == 0) {
            return message;
        }

        String result = message;
        for (int i = 0; i < placeholders.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(placeholders[i]));
        }
        return result;
    }

    /**
     * Creates a Component from a MiniMessage string
     */
    public Component parse(String message) {
        return miniMessage.deserialize(message);
    }

    /**
     * Message type enumeration
     */
    public enum MessageType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        HIGHLIGHT
    }

    /**
         * Internal configuration for message types
         */
        private record MessageConfig(String colorPrefix, Sound sound) {
    }
}