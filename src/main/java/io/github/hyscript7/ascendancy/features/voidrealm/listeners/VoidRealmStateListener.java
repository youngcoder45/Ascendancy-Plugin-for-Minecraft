package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

public class VoidRealmStateListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveUpdateVoidRealmFields(PlayerMoveEvent event) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        @Nullable VoidRealmLayer layer = VoidRealmLayer.fromWorld(event.getPlayer().getWorld());
        data.setInVoidRealm(layer != null);
        data.setCurrentVoidRealmLayer(layer);
    }

}
