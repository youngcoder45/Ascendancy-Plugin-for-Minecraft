package io.github.hyscript7.ascendancy.features.secretchat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Applies custom chat renderer to hide true names and annotate spells.
 */
public class SecretChatListener implements Listener {
    AscendancyChatSecretRender render = new AscendancyChatSecretRender();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.renderer(render);
    }
}
