package io.github.hyscript7.ascendancy.data.factions;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.NotInitializedException;
import io.github.hyscript7.ascendancy.data.factions.simple.EFactionPermission;
import io.github.hyscript7.ascendancy.data.factions.simple.Faction;
import io.github.hyscript7.ascendancy.data.factions.simple.FactionMember;
import io.github.hyscript7.ascendancy.data.factions.simple.FullFactionMember;
import io.github.hyscript7.ascendancy.data.players.storage.PlayerDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class FactionManager {
    /**
     * Manages factions
     */

    // TODO autosave
// === singleton logic ===
    private static FactionManager instance;

    public static void initialize() {
        if (instance != null) {
            throw new AlreadyInitializedException("PlayerDataManager has already been initialized!");
        }
        instance = new FactionManager();
        //instance.startAutoSave();
    }

    public static FactionManager getInstance() {
        if (instance == null) {
            throw new NotInitializedException("PlayerDataManager hasn't been initialized yet!");
        }
        return instance;
    }

// === faction operations ===

    private ArrayList<Faction> factions = new ArrayList();

    public void createFaction(String factionName, UUID owner) {
        Faction f = new Faction(factionName);
        FactionMember m = new FactionMember(owner, EFactionPermission.OWNER);
        f.getMembers().add(m);
        factions.add(f);
    }

    public void addMember(String factionName, UUID member) {
        Faction f = factions.stream().filter(faction -> faction.getName().equals(factionName)).findFirst().orElse(null);
        if (f != null) {
            f.getMembers().add(new FactionMember(member, EFactionPermission.MEMBER));
        } else {
            // what happens when you
        }
    }

    public void deleteFaction(String factionName) {
        factions.removeIf(faction -> faction.getName().equals(factionName));
    }

    public FullFactionMember getFactionMember(UUID member) {
        for (Faction f : factions) {
            Optional<FactionMember> fm = f.getMembers().stream().filter(a -> a.getUuid().equals(member)).findFirst();
            if (fm.isPresent()) {
                FactionMember fmm = fm.get();
                return new FullFactionMember(fmm.getUuid(), fmm.getPermission(), f.getName());
            }
        }
        return null;
    }

    public UUID getFactionOwner(String factionName) {
        Faction faction = factions.stream().filter(a -> a.getName().equals(factionName)).findFirst().orElse(null);
        if (faction != null) {
            Optional<FactionMember> owner = faction.getMembers().stream().filter(a -> a.getPermission().equals(EFactionPermission.OWNER)).findFirst();
            if (owner.isPresent()) {
                return owner.get().getUuid();
            } else {
                // maybe delete the faction if it somehow doesn't have an owner
            }
        }
        return null;
    }

    public ArrayList<Faction> getFactions() {
        return factions;
    }


// === Higher abstraction works with player ===
    public void create(String faction, Player player) {
        if (getFactionMember(player.getUniqueId()) != null) {
            player.sendMessage("Player "+player.getName()+" already joined a faction");
            return;
        }
        if (getFactions().stream().anyMatch(a -> a.getName().equals(faction))) {
            player.sendMessage("Faction already exists");
            return;
        }
        createFaction(faction, player.getUniqueId());
        player.sendMessage("Faction "+faction+" created");
    }

    public void delete(String faction, Player player) {
        deleteFaction(faction);
        player.sendMessage("Faction "+faction+" deleted");
    }

    public void join(String faction, Player player) {
        if (getFactionMember(player.getUniqueId()) != null) {
            player.sendMessage("Player "+player.getName()+" already joined a faction");
            return;
        }
        addMember(faction, player.getUniqueId());
        player.sendMessage("Added "+player.getName()+" to "+faction+" created");
    }

    public void giveItem(String faction, Player player, String action) {
        String[] actions = new String[]{"create", "delete", "join"};
        if (!Arrays.asList(actions).contains(action)) {
            return;
        }
        if (action.equals("delete") || action.equals("join")) {
            if (factions.stream().noneMatch(o -> o.getName().equals(faction))) {
                return;
            }
        }

        ItemStack item = new ItemStack(Material.LIGHT_BLUE_DYE);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(AscendancyPlugin.getInstance(), "faction"), PersistentDataType.STRING, faction);
        pdc.set(new NamespacedKey(AscendancyPlugin.getInstance(), "action"), PersistentDataType.STRING, action);
        meta.customName(Component.text(faction + " " + action, NamedTextColor.AQUA));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }


}
