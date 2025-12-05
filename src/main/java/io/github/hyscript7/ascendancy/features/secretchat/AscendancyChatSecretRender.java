package io.github.hyscript7.ascendancy.features.secretchat;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellUtility;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AscendancyChatSecretRender implements ChatRenderer {

    private static final double INNATE_NAME_REVEAL_DISTANCE = 48.0;
    private static final TextColor OWN_NAME_COLOR = TextColor.color(0xB9926A);
    private static final TextColor OTHER_NAME_COLOR = TextColor.color(0x6933b9);
    private static final TextColor SPELL_INDICATOR_COLOR = TextColor.color(0x6933b9);

    private final String honeypotName;
    private final boolean honeypotEnabled;

    public AscendancyChatSecretRender() {
        AscendancyConfig config = AscendancyConfig.getInstance();
        this.honeypotName = config.getInnateNames().honeypot().name();
        this.honeypotEnabled = config.getInnateNames().honeypot().enabled();
    }

    @Override
    public @NotNull Component render(
            @NotNull Player source,
            @NotNull Component sourceDisplayName,
            @NotNull Component message,
            @NotNull Audience viewer
    ) {
        if (!(viewer instanceof Player player)) {
            return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
        }

        String content = extractTextContent(message);

        // Check for honeypot violation
        if (containsHoneypot(content)) {
            return renderHoneypotViolation(source, sourceDisplayName, viewer);
        }

        // Process innate names
        message = processInnateNames(message, content, source, player);

        // Add spell indicator if applicable
        message = addSpellIndicator(message, content);

        return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
    }

    private String extractTextContent(Component message) {
        return message instanceof TextComponent tc ? tc.content() : "";
    }

    private boolean containsHoneypot(String content) {
        return honeypotEnabled && content.toLowerCase().contains(honeypotName.toLowerCase());
    }

    private Component renderHoneypotViolation(Player source, Component sourceDisplayName, Audience viewer) {
        Component foolMessage = Component.text(
                "I am a fool who thought Script doesn't know how obfuscated text works!"
        );
        return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, foolMessage, viewer);
    }

    private Component processInnateNames(Component message, String content, Player source, Player viewer) {
        String innateName = InnateUtils.findFirstValidInnateName(content, 0);
        if (innateName == null) {
            return message;
        }

        UUID ownerId = TrueNameManager.getInstance().findTrueNameOwner(innateName);
        OfflinePlayer owner = ownerId != null ? Bukkit.getOfflinePlayer(ownerId) : null;

        if (owner == null) {
            return message;
        }

        PlayerData viewerData = PlayerDataManager.getInstance().getPlayerData(viewer);

        // Handle name learning
        if (isWithinRevealDistance(source, viewer) && !viewerData.knowsTrueName(innateName)) {
            learnTrueName(viewer, owner, viewerData, innateName);
        }

        // Render name based on viewer's knowledge
        return viewerData.knowsTrueName(innateName)
                ? renderKnownName(message, innateName, viewerData, viewer)
                : renderUnknownName(message, innateName);
    }

    private boolean isWithinRevealDistance(Player source, Player viewer) {
        return source.getLocation().distance(viewer.getLocation()) < INNATE_NAME_REVEAL_DISTANCE;
    }

    private void learnTrueName(Player viewer, OfflinePlayer owner, PlayerData viewerData, String name) {
        viewerData.learnName(name);
        AscendancyMessagingAPI.getInstance().send(
                viewer,
                AscendancyMessagingAPI.MessageType.INFO,
                "You have learned " + owner.getName() + "'s true name: " + name
        );
    }

    private Component renderKnownName(Component message, String name, PlayerData viewerData, Player viewer) {
        boolean isOwnName = viewerData.getTrueName().equalsIgnoreCase(name);
        Style style = isOwnName
                ? Style.style(OWN_NAME_COLOR, TextDecoration.UNDERLINED)
                : Style.style(OTHER_NAME_COLOR);

        if (isOwnName) {
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }

        return message.replaceText(builder ->
                builder.matchLiteral(name)
                        .replacement(Component.text(name).style(style))
        );
    }

    private Component renderUnknownName(Component message, String name) {
        return message.replaceText(builder ->
                builder.matchLiteral(name)
                        .replacement(obfuscateName(name))
        );
    }

    private Component obfuscateName(String originalName) {
        String display = honeypotEnabled ? honeypotName : "*".repeat(originalName.length());
        Component obfuscated = Component.text(display)
                .style(Style.style(TextDecoration.OBFUSCATED));

        Component hoverText = Component.text(
                "You have not heard this innate name yet.\n" +
                        "Try listening a little closer to see if you can hear someone whisper it."
        );

        return obfuscated.hoverEvent(HoverEvent.showText(hoverText));
    }

    private Component addSpellIndicator(Component message, String content) {
        Spell spell = SpellUtility.getSpellFromString(content);
        if (spell == null) {
            return message;
        }

        Component indicator = Component.text(" ")
                .append(Component.text("(i)")
                        .style(Style.style(SPELL_INDICATOR_COLOR))
                ).hoverEvent(HoverEvent.showText(
                        Component.text("Spell: ").style(Style.style(SPELL_INDICATOR_COLOR))
                                .append(Component.text(spell.getDisplayName()).style(Style.style(TextColor.color(0x6933b9))))
                                .append(Component.text("\nID: " + spell.getId()).style(Style.style(NamedTextColor.GRAY)))
                ));

        return message.append(indicator);
    }
}