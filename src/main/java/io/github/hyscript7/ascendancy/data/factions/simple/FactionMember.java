package io.github.hyscript7.ascendancy.data.factions.simple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class FactionMember {
    UUID uuid;
    EFactionPermission permission;

    @Override
    public String toString() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
}
