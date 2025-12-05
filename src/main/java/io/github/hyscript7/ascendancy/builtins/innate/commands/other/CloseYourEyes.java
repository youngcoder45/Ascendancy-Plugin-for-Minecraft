package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import org.bukkit.potion.PotionEffectType;

public class CloseYourEyes extends AbstractInnateCommand {

    public CloseYourEyes() {
        // Fun command, but won't cause as much harm in combat, so we let it be "unfun"
        super("blind", "Close your eyes", true, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        // We try to blind the player as much as possible
        context.target().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20 * 5, 1));
        context.target().addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(20 * 5, 1));
        context.target().addPotionEffect(PotionEffectType.DARKNESS.createEffect(20 * 5, 1));
        // I would give them a carved pumpkin, but someone would find a dupe with it 100%
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).toLowerCase().matches(".+ (close\\syour\\seyes|blink)");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[]{};
    }

    @Override
    public String getUsage() {
        return "<name> (close your eyes | blink)";
    }
}
