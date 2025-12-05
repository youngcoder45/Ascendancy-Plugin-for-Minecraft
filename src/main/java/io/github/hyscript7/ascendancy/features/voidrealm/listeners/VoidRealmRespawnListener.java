package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class VoidRealmRespawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (PlayerDataManager.getInstance().getPlayerData(event.getPlayer()).getLives() > 0) {
            return;
        }
        Location location = event.getRespawnLocation();
        location.setWorld(VoidRealmLayer.ABYSS.getWorld());
        event.setRespawnLocation(location);
    }

}
