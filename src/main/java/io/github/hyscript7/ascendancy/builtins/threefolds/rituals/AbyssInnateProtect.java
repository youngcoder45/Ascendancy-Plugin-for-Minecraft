package io.github.hyscript7.ascendancy.builtins.threefolds.rituals;

import io.github.hyscript7.ascendancy.builtins.threefolds.existences.ThePrimordialAbyss;
import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtection;
import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtectionManager;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AbyssInnateProtect extends AbstractThreefoldIncantation {
    public AbyssInnateProtect() {
        super("abyss innate name protect", "Abysmal Ascent", new String[]{
                "I beg for your attention",
                "I beg of you to hide my true name within the void"
        }, ThePrimordialAbyss.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        Player player = context.player();
        InnateProtectionManager.getInstance().addProtection(new AbyssBoundProtection(player));
        // Give darkness for visual effect
        player.addPotionEffect(PotionEffectType.DARKNESS.createEffect(5 * 20, 1));
        player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, 0.5f, 0.2f);
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        return getPlayersLayer(context.player()) != null;
    }

    private @Nullable VoidRealmLayer getPlayersLayer(Player player) {
        return VoidRealmLayer.fromWorld(player.getLocation().getWorld());
    }

    private record AbyssBoundProtection(Player player) implements InnateProtection {
        @Override
        public UUID getPlayerUuid() {
            return player.getUniqueId();
        }

        @Override
        public boolean isActive() {
            return VoidRealmLayer.fromWorld(player.getWorld()) != null;
        }
    }
}
