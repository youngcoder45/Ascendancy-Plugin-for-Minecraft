package io.github.hyscript7.ascendancy.builtins.threefolds.rituals;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.builtins.threefolds.existences.ThePrimordialAbyss;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.LayerChanger;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AbyssLayerDescend extends AbstractThreefoldIncantation {
    private final LayerChanger layerChanger = new LayerChanger();

    public AbyssLayerDescend() {
        super("abyss descent", "Abysmal Descent", new String[]{
                "I pray for your attention",
                "I wish for you to pull me into your embrace"
        }, ThePrimordialAbyss.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        Player player = context.player();
        VoidRealmLayer oldLayer = getPlayersLayer(player);
        VoidRealmLayer newLayer = switch (oldLayer) {
            case ABYSS -> VoidRealmLayer.OBLIVION;
            case OBLIVION, REFLECTION -> VoidRealmLayer.REFLECTION;
            case null -> VoidRealmLayer.ABYSS;
        };
        if (oldLayer == null) {
            double maxHealth = Optional.ofNullable(player.getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getBaseValue).orElse(20.0);
            player.setHealth(Math.max(0, player.getHealth() - maxHealth / 2));
        }
        if (!player.isDead()) {
            // Teleports need to happen synchronously, this method is run asynchronously.
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    AscendancyPlugin.getInstance(),
                    () -> layerChanger.changeLayer(player, newLayer)
            );
        }
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        VoidRealmLayer layer = getPlayersLayer(context.player());
        // TODO: Allow tp from oblivion if player level is 50
        return layer != VoidRealmLayer.REFLECTION && layer != VoidRealmLayer.OBLIVION;
    }

    private @Nullable VoidRealmLayer getPlayersLayer(Player player) {
        return VoidRealmLayer.fromWorld(player.getLocation().getWorld());
    }
}
