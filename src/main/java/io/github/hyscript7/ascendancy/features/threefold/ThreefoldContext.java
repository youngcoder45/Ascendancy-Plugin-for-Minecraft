package io.github.hyscript7.ascendancy.features.threefold;

import org.bukkit.entity.Player;

// Unlike other contexts like Spell or Ritual, this one is re-usable.
public record ThreefoldContext(Player player, ThreefoldHistory history) {
}
