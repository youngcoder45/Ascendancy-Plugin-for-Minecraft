package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DeadPlayerRestrictionListener implements Listener {

    private final boolean preventPlacing;
    private final boolean preventBreaking;
    private final boolean preventInteracting;

    public DeadPlayerRestrictionListener() {
        AscendancyConfig.VoidRealm.DeadPlayerRestrictions restrictionConfig = AscendancyConfig.getInstance().getVoidRealm().deadPlayerRestrictions();
        preventPlacing = restrictionConfig.noBuild();
        preventBreaking = restrictionConfig.noBreak();
        preventInteracting = restrictionConfig.noInteract();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlacingBlocksWhileDead(BlockPlaceEvent event) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data, preventPlacing)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakingBlocksWhileDead(BlockBreakEvent event) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data, preventBreaking)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractingBlocksWhileDead(PlayerInteractEvent event) {
        if (shouldRestrict(event.getPlayer(), PlayerDataManager.getInstance().getPlayerData(event.getPlayer()), preventInteracting)) {
            if (event.getClickedBlock() != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks whether the player should have various void ban imposed restrictions, such as mining or placing
     * blocks enforced.
     * @param player The player to test
     * @return true if the player has bypass, otherwise false.
     */
    private boolean shouldRestrict(Player player, PlayerData playerData, boolean featureFlag) {
        if (!featureFlag) return false;
        if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) return false;
        return playerData.isDead() || isInReflectionOfSelf(player);
    }

    /**
     * Checks whether the player is in the Reflection of Self layer of the void realm.
     * @param player The player to test
     * @return true if the player is in the third layer, otherwise false
     */
    private boolean isInReflectionOfSelf(Player player) {
        return VoidRealmLayer.fromWorld(player.getWorld()) == VoidRealmLayer.REFLECTION;
    }

}
