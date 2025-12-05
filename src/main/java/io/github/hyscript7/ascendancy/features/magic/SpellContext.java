package io.github.hyscript7.ascendancy.features.magic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Builder
@Getter
public class SpellContext {
    private final Player caster;
    private final Location location;
    private final String rawIncantation;
}
