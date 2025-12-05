package io.github.hyscript7.ascendancy.data.factions.simple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class FullFactionMember {
        UUID uuid;
        EFactionPermission permission;
        String faction;
}
