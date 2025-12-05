package io.github.hyscript7.ascendancy.features.magic.listeners;

import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellUtility;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class SpellIncantationListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatted(AsyncChatEvent event) {
        String message = ((TextComponent) event.message()).content();
        @Nullable Spell spell = SpellUtility.getSpellFromString(SpellUtility.cleanString(message));
        if (spell == null) return;
        SpellContext context = SpellContext.builder().caster(event.getPlayer()).rawIncantation(message).location(event.getPlayer().getLocation()).build();
        SpellUtility.runSpell(context, spell);
    }

}
