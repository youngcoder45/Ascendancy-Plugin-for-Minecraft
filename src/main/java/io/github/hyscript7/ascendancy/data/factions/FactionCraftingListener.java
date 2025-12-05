package io.github.hyscript7.ascendancy.data.factions;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class FactionCraftingListener implements Listener {

    @EventHandler
    public void onIllegalCrafting(PrepareItemCraftEvent event) {
        Inventory inventory = event.getInventory();

        for (ItemStack stack : inventory.getContents()) {
            if (stack != null) {
                PersistentDataContainerView container = stack.getPersistentDataContainer();
                boolean faction = container.has(new NamespacedKey(AscendancyPlugin.getInstance(), "faction"), PersistentDataType.STRING);
                boolean action = container.has(new NamespacedKey(AscendancyPlugin.getInstance(), "action"), PersistentDataType.STRING);
                if (faction || action) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }
}
