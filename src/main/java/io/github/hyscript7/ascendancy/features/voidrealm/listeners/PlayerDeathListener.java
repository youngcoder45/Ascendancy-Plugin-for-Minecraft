package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles loss of lives upon dying
 */
public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // "You won't use the shorthand" they said. Oh, yeah? What's this then?
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

        if (playerData == null) {
            // The fuck?
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE ||
                player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // But for real, how does this know to look for a player and not a mob?
        Player killer = player.getKiller();

        boolean isPvPDeath = killer != null;
        boolean isSuicide = killer != null && killer.equals(player);
        // Natural death causes, like fall damage, don't count as PvE.
        boolean isPvEDeath = killer == null && isDamagedByEntity(event);

        boolean isPvEEnabled = AscendancyPlugin.getInstance().getConfig().getBoolean("void_ban.pve_enabled", false);

        boolean shouldLoseLife = ((isPvEDeath && isPvEEnabled)|| (isPvPDeath && !isSuicide));

        if (!shouldLoseLife) {
            return;
        }

        if (isPvPDeath) {
            playerData.incrementPvpDeaths();
            PlayerDataManager.getInstance().getPlayerData(killer).incrementPvpKills();
        } else if (isPvEDeath) {
            playerData.incrementPveDeaths();
        }

        int previousLives = playerData.getLives();
        playerData.removeLives(1); // Potential for relics which take multiple lives?
        int currentLives = playerData.getLives();

        if (currentLives > 0) {
            // Still alive
            AscendancyMessagingAPI.getInstance().sendBoxed(player, AscendancyMessagingAPI.MessageType.HIGHLIGHT, "Life Lost", null, "You have died to " + killer.getName() + "!\nYou are now at " + playerData.getLives() + "/" + playerData.getMaxLives() + " lives!");
        } else {
            if (!playerData.isDead()) {
                AscendancyMessagingAPI.getInstance().broadcastBoxed(AscendancyMessagingAPI.MessageType.INFO, "Void Death", null, player.getName() + " has died to " + killer.getName() + " and will respawn in the Void Realm!");
                PlayerData killerData = PlayerDataManager.getInstance().getPlayerData(killer);
                // Fucking dead
                if (!playerData.knowsTrueName(playerData.getTrueName())) {
                    killerData.learnName(playerData.getTrueName());
                    AscendancyMessagingAPI.getInstance().sendBoxed(killer, AscendancyMessagingAPI.MessageType.HIGHLIGHT, "Final Kill", null, "You have void banned " + player.getName() + " and learned their true name: " + playerData.getTrueName() + "!");
                } else {
                    AscendancyMessagingAPI.getInstance().sendBoxed(killer, AscendancyMessagingAPI.MessageType.HIGHLIGHT, "Final Kill", null, "You have void banned " + player.getName() + "!");
                }
                playerData.setDead(true);

                AscendancyMessagingAPI.getInstance().sendBoxed(player, AscendancyMessagingAPI.MessageType.HIGHLIGHT, "Out of Lives", null, "You have run out of all your lives!\nYou will respawn in the Void Realm, good luck!\n\nYour true name has been disclosed to " + killer.getName());

                player.getLocation().getNearbyPlayers(64).forEach(nearby -> nearby.playSound(nearby.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f));
            }
        }
    }

    /**
     * Checks whether the cause of death is by a living non-player entity.
     * <p>
     * Will return false for natural deaths like fall damage.
     * @param event The player death event
     * @return True if the event was caused by a non-player mob, otherwise false.
     */
    private boolean isDamagedByEntity(PlayerDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (killer != null) return true;

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();

        return lastDamage instanceof EntityDamageByEntityEvent;
    }

    /**
     * Similar to isDamagedByEntity, but instead of a boolean, it returns either the attacker or null.
     * @param event The player death event
     * @return An entity if killed by a mob, otherwise null for natural and PvP causes
     */
    private Entity getKillerMob(PlayerDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (killer != null) return null;

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();

        if (lastDamage instanceof EntityDamageByEntityEvent damageEvent) {
            return damageEvent.getDamager();
        }
        return null;
    }
}
