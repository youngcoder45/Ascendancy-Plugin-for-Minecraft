package io.github.hyscript7.ascendancy.features.magic.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellUtility;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BookCastListener implements Listener {

    private static final Set<Action> castActions = Set.of(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR);

    @EventHandler
    public void onRightClickWithSignedBook(PlayerInteractEvent event) {
        if (!castActions.contains(event.getAction())) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!item.getType().equals(Material.WRITTEN_BOOK)) return;
        BookMeta meta = (BookMeta) item.getItemMeta();
        SpellAndIncantation spell = getSpellFromItemMeta(meta);
        if (spell == null) {
            spell = getSpellFromBook(meta);
        }
        if (spell == null) return;

        Player player = event.getPlayer();

        // Handle tattered books
        if (meta.getGeneration() != null && meta.getGeneration().equals(BookMeta.Generation.TATTERED)) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            // If you ever think this code base isn't retarded enough, just look at how the spell is accessed
            playerData.learnSpells(spell.spell().getId());

            AscendancyMessagingAPI.getInstance().sendBoxed(player, AscendancyMessagingAPI.MessageType.INFO, "Spell Incantation Learned", null, spell.spell.getIncantation());
            playLearnSpellEffect(player);

            player.getInventory().remove(item);
            event.setCancelled(true);
            return;
        }

        SpellUtility.runSpell(SpellContext.builder().caster(player).location(player.getLocation()).rawIncantation(spell.incantation()).build(), spell.spell());
        event.setCancelled(true);
    }

    private @Nullable SpellAndIncantation getSpellFromItemMeta(ItemMeta itemMeta) {
        String itemName = ((TextComponent) itemMeta.itemName()).content();
        Spell spell = SpellUtility.getSpellFromString(itemName);
        if (spell == null) return null;
        return new SpellAndIncantation(spell, itemName);
    }

    private @Nullable SpellAndIncantation getSpellFromBook(Book book) {
        SpellAndIncantation spellAndIncantation = null;
        for (Component pageContent : book.pages()) {
            TextComponent component = (TextComponent) pageContent;
            Spell spell = SpellUtility.getSpellFromString(component.content());
            if (spell != null) {
                spellAndIncantation = new SpellAndIncantation(spell, component.content());
                break;
            }
        }
        return spellAndIncantation;
    }

    private void playLearnSpellEffect(Player player) {
        // If I had a $ for every magic number in this function, I could legit go buy a cheeze burger from Mc Donald's
        // TODO
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5
        );
    }

    private record SpellAndIncantation(Spell spell, String incantation) {
    }
}
