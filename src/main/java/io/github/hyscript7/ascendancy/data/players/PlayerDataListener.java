package io.github.hyscript7.ascendancy.data.players;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles loading and unloading of player data on join and leave.
 */
public class PlayerDataListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerDataListener() {
        this.dataManager = PlayerDataManager.getInstance();
    }

    /**
     * Caches player data BEFORE the player joins.
     * In a case where loading the player data fails, the player won't be allowed to join.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        dataManager.loadPlayerData(event.getUniqueId())
                .exceptionally(throwable -> {
                    event.disallow(
                            AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                            // TODO: Use message formatter
                            Component.text("Failed to load your player data. Please try again.").color(TextColor.color(0xFF5555))
                    );
                    return null;
                });
    }

    /**
     * Handles post-player join events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());

        if (data == null) {
            // We failed to load data in the prelogin phase but the player joined anyway.
            // (Perhaps the cache was flushed early?)
            // Either way, load it synchronously, because we're going to need it.
            data = dataManager.loadPlayerDataSync(player.getUniqueId());
        }

        // If the player is new, disclose their true name to them
        // firstSeenTimestamp is set by the data manager when it creates a new instance of Player Data.
        if (!player.hasPlayedBefore()) {
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.INFO, "Welcome to the ScriptSMP!\n\nYour true name is " + data.getTrueName() + ".\nGuard it carefully, those who speak your true name hold power over you...");
        }

        data.updateLastSeenTimestamp();
    }

    /**
     * Save and unload player data when they quit
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save and unload asynchronously
        dataManager.unloadPlayerData(player.getUniqueId())
                .exceptionally(throwable -> {
                    AscendancyPlugin.getInstance().getLogger().warning(
                            "Failed to save player data for " + player.getName()
                    );
                    return null;
                });
    }
}
