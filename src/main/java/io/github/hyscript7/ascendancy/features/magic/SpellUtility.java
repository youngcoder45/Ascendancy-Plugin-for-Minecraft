package io.github.hyscript7.ascendancy.features.magic;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class SpellUtility {

    public static @Nullable Spell getSpellFromString(String s) {
        return RegistryManager.getInstance().getSpellRegistry().getAll().stream()
                .filter(spell -> spell.incantationMatches(s))
                .findFirst().orElse(null);
    }

    public static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    public static void runSpell(SpellContext context, Spell spell) {
        if (SpellCooldownManager.getInstance().isPlayerOnCooldown(context.getCaster(), spell)) {
            double cooldownSeconds = Math.round(SpellCooldownManager.getInstance().getRemainingCooldownMillis(context.getCaster(), spell) / 100d) / 10d;
            AscendancyMessagingAPI.getInstance().send(context.getCaster(), AscendancyMessagingAPI.MessageType.ERROR, spell.getDisplayName() + " is still on cooldown for " + cooldownSeconds + "s!");
            return;
        }
        if (spell.getTier().requiresLearning()) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.getCaster());
            if (!playerData.knowsSpell(spell.getId())) {
                AscendancyMessagingAPI.getInstance().send(context.getCaster(), AscendancyMessagingAPI.MessageType.ERROR, "You whisper the words, but nothing happens...\nThis " + (spell.getTier().equals(SpellTier.ARCANA) ? "arcana" : "spell") + " is beyond your current knowledge.");
                return;
            }
        }
        if (spell.canCast(context)) {
            // TODO: Check mana & send feedback if low
            Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> {
                if (spell.cast(context)) {
                    AscendancyMessagingAPI.getInstance().send(context.getCaster(), AscendancyMessagingAPI.MessageType.SUCCESS, "You cast " + spell.getDisplayName() + "!");
                    SpellCooldownManager.getInstance().setPlayerOnCooldown(context.getCaster(), spell);
                } else {
                    AscendancyMessagingAPI.getInstance().send(context.getCaster(), AscendancyMessagingAPI.MessageType.ERROR, "You whispered the words to cast " + spell.getDisplayName() + ", but the spell failed!");
                    // Don't CD if the spell didn't cast (exited with a false)
                }
            });
        }
    }

}
