package io.github.hyscript7.ascendancy.data.factions;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class FactionItemInteractionListener implements Listener {
    // This method handles the PlayerInteractEvent
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        String faction = item.getPersistentDataContainer().get(new NamespacedKey(AscendancyPlugin.getInstance(), "faction"), PersistentDataType.STRING);
        String action = item.getPersistentDataContainer().get(new NamespacedKey(AscendancyPlugin.getInstance(), "action"), PersistentDataType.STRING);

        if (faction == null || action == null) {
            return;
        }

        FactionManager fm = FactionManager.getInstance();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);

            switch (action) {
                case "create" -> {
                    fm.create(faction, player);
                }
                case "delete" -> {
                    fm.delete(faction, player);
                }
                case "join" -> {
                    fm.join(faction, player);
                }
                default -> {
                    return;
                }
            }
            player.getInventory().setItemInMainHand(null);

        }
    }
}
