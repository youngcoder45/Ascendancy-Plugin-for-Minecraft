package io.github.hyscript7.ascendancy.features.bossprog.listeners;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.bossprog.BossProgUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class EquipmentRestrictionEnforcer implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        checkForIllegalEquipsLater(event.getPlayer(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        checkForIllegalEquipsLater(event.getPlayer(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        checkForIllegalEquipsLater(player, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDispenserEquipArmor(BlockDispenseArmorEvent event) {
        if (!(event.getTargetEntity() instanceof Player player)) return;

        checkForIllegalEquipsLater(player, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDispenserEquipItem(BlockDispenseLootEvent event) {
        if (event.getPlayer() == null) return;

        checkForIllegalEquipsLater(event.getPlayer(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        checkForIllegalEquipsLater(player, 1);
    }

    private static final Set<Integer> hotbarSlots = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
    private static final Set<Integer> inventorySlots = Set.of(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);
    private static final int offhandSlot = -106;
    private static final Set<Integer> armorSlots = Set.of(100, 101, 102, 103);
    private static final Set<Integer> allSlots = getAllSlots();

    private static Set<Integer> getAllSlots() {
        Set<Integer> set = new HashSet<>();
        set.addAll(hotbarSlots);
        set.addAll(inventorySlots);
        set.add(offhandSlot);
        set.addAll(armorSlots);
        return set;
    }

    private void checkForIllegalEquipsLater(Player player, long delay) {
        Bukkit.getScheduler().runTaskLater(AscendancyPlugin.getInstance(), () -> {
            checkForIllegalEquips(player);
        }, delay);
    }

    private void checkForIllegalEquips(Player player) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        PlayerInventory inventory = player.getInventory();

        // is holding locked item?
        int mainHandSlot = inventory.getHeldItemSlot();
        @Nullable ItemStack mainHandItem = inventory.getItem(mainHandSlot);
        if (mainHandItem != null && BossProgUtil.isLocked(playerData, mainHandItem)) {
            inventory.setItem(mainHandSlot, null);
            moveOrDropItem(player, mainHandItem);
        }
        // is off-handing locked item?
        ItemStack offHandItem = inventory.getItemInOffHand();
        if (BossProgUtil.isLocked(playerData, offHandItem)) {
            inventory.setItem(offhandSlot, null);
            moveOrDropItem(player, mainHandItem);
        }
        // Check armor
        ItemStack[] armorContents = inventory.getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorItem = armorContents[i];
            if (armorItem == null) continue;
            if (BossProgUtil.isLocked(playerData, armorItem)) {
                armorContents[i] = null;
                moveOrDropItem(player, armorItem);
            }
        }
        inventory.setArmorContents(armorContents);
    }

    /**
     * Finds the first free slot and moves the item there.
     * This excludes all slots which a banned item cannot be in, i.e. armor, hand and offhand
     *
     * @param player The player
     * @param item   The item to move
     */
    private void moveOrDropItem(Player player, ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) return;

        PlayerInventory inv = player.getInventory();

        Set<Integer> disallowedSlots = new HashSet<>(armorSlots);
        disallowedSlots.add(offhandSlot);
        disallowedSlots.add(inv.getHeldItemSlot());
        for (int slot : inventorySlots) {
            ItemStack itemAtSlot = inv.getItem(slot);
            if (itemAtSlot != null && itemAtSlot.getType() != Material.AIR) {
                disallowedSlots.add(slot);
            }
        }
        for (int slot : hotbarSlots) {
            ItemStack itemAtSlot = inv.getItem(slot);
            if (itemAtSlot != null && itemAtSlot.getType() != Material.AIR) {
                disallowedSlots.add(slot);
            }
        }

        Set<Integer> scanSlots = new HashSet<>(allSlots);
        scanSlots.removeAll(disallowedSlots);

        // Find free slot
        for (int slot : scanSlots) {
            inv.setItem(slot, item);
            AscendancyPlugin.getInstance().getLogger().info("Player " + player.getName() + " has tried to hold an item which they haven't unlocked. It has been moved.");
            return;
        }
        // No free slots, drop that bitch
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        AscendancyPlugin.getInstance().getLogger().info("Player " + player.getName() + " has tried to hold an item which they haven't unlocked. It has been dropped at " + player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ());
    }
}
