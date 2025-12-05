package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class VoidRealmEffects implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFallDamageInTheVoidRealm(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            if (VoidRealmLayer.fromWorld(event.getEntity().getLocation().getWorld()) != null) {
                event.setCancelled(true);
            }
        }
    }

}
