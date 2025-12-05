package io.github.hyscript7.ascendancy.features.threefold.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.threefold.*;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ThreefoldListener implements Listener {
    // Honestly, at this point, just let it manage its own player data.
    private final Map<UUID, ThreefoldContext> contexts;
    private final int defaultWindowSize;

    public ThreefoldListener() {
        int audienceMaxLength = RegistryManager.getInstance().getThreefoldAudienceRegistry().getAll().stream().map(ThreefoldAudience::getIncantation).map(Array::getLength).max(Comparator.comparingInt(Integer::intValue)).orElse(0);
        int incantationMaxLength = RegistryManager.getInstance().getThreefoldIncantationRegistry().getAll().stream().map(ThreefoldIncantation::getIncantations).map(Array::getLength).max(Comparator.comparingInt(Integer::intValue)).orElse(0);
        this.defaultWindowSize = audienceMaxLength + incantationMaxLength;
        this.contexts = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        contexts.remove(event.getPlayer().getUniqueId());
    }

    private ThreefoldContext newContext(Player player) {
        return new ThreefoldContext(player, new ThreefoldHistory(defaultWindowSize));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncChatEvent event) {
        ThreefoldContext context = contexts.computeIfAbsent(event.getPlayer().getUniqueId(), unused -> newContext(event.getPlayer()));
        context.history().update(((TextComponent) event.message()).content());
        ThreefoldAudience audience = ThreefoldUtils.matchAudience(context);
        if (audience != null) {
            ThreefoldIncantation incantation = ThreefoldUtils.matchIncantation(context, audience);
            if (incantation == null) {
                return;
            }
            if (incantation.canExecute(context)) {
                Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> {
                    incantation.execute(context);
                });
                AscendancyMessagingAPI.getInstance().send(event.getPlayer(), AscendancyMessagingAPI.MessageType.SUCCESS, "The existence heard your prayers and granted your wishes.");
            } else {
                AscendancyMessagingAPI.getInstance().send(event.getPlayer(), AscendancyMessagingAPI.MessageType.ERROR, "You chant the honorific name and incantation, but there is no response.");
            }
            context.history().clear();
        }
    }
}
