package io.github.hyscript7.ascendancy.features.innate.names;

import lombok.Builder;
import org.bukkit.entity.Player;

@Builder
public record InnateContext(Player invoker, boolean invokerHasImmunityBypass, Player target, boolean targetHasImmunity,
                            String originalMessage, String[] args) {
}
