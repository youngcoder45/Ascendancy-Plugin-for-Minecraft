package io.github.hyscript7.ascendancy.data.factions;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.factions.simple.Faction;
import io.github.hyscript7.ascendancy.data.factions.simple.FullFactionMember;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collection;

public class FactionCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        // Check if the sender is a Player
        if (!(source.getSender() instanceof Player)) {
            return;
        }
        Player player = (Player) source.getSender();

        FactionManager fm = FactionManager.getInstance();
        if (args.length == 0) {
            FullFactionMember member = fm.getFactionMember(player.getUniqueId());
            if (member == null) {
                player.sendMessage("Not a faction member");
            } else {
                player.sendMessage(member.getFaction()+": "+member.getPermission());
            }
            return;
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "info" -> {
                    player.sendMessage("Factions:");
                    for (Faction f : fm.getFactions()) {
                        player.sendMessage(f.toString());
                    }
                }
                case "item" -> {
                    player.sendMessage("Item pdc:");
                    player.getActiveItem().getPersistentDataContainer().getKeys().forEach(key -> {
                        player.sendMessage(key.namespace() + ": " + key.getKey());
                    });
                }
            }
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "create" -> {
                    fm.create(args[1], player);
                }
                case "delete" -> {
                    fm.delete(args[1], player);
                }
                case "join" -> {
                    fm.join(args[1], player);
                }
            }
        }

        if (args.length == 3) {
            if (args[0].equals("item")) {
                fm.giveItem(args[1], player, args[2]);
            }
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length < 2) {
            return Arrays.stream(new String[]{"create", "delete", "join", "info", "item"}).toList();
        }

        return Arrays.stream(new String[]{}).toList();
    }
}