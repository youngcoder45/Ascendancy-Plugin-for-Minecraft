package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.Optional;

public class Die extends AbstractInnateCommand {
    private static final double MAX_DISTANCE = 32.0;

    public Die() {
        // Fun means it shouldn't work in combat
        super("die", "Die", true, true, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        double distance = context.invoker().getLocation().distance(context.target().getLocation());
        if (distance > MAX_DISTANCE) {
            if (distance > 10 * MAX_DISTANCE && AscendancyPlugin.getInstance().getRandom().nextBoolean()) {
                // Random chance to troll cowards who try to troll from too far away.
                context.invoker().getLocation().getWorld().createExplosion(context.invoker().getLocation(), 2.0f, false, false);
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You are too far from the target! Also <red>Uno</red> reverse lmao.");
            } else {
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You are too far from the target!");
            }
            AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.WARNING, "Someone just tried to kill you but was too far away!");
            return false;
        }
        double targetMaxHealth = Optional.ofNullable(context.target().getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getBaseValue).orElse(20.0);
        if (context.target().getHealth() > targetMaxHealth * 0.2) { // Only executes below 20% health
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "Target has too much HP left! Try damaging them some more.");
            AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.WARNING, "Someone just tried to kill you but your vitality saved you!");
            return false;
        }
        context.target().setKiller(context.invoker());
        context.target().setHealth(0);
        context.target().getLocation().getWorld().playSound(context.target().getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).toLowerCase().matches(".+ (die|kill\\syour\\sself|death\\sbe\\supon\\s(yee?|you|thy|thee))");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[]{};
    }

    @Override
    public String getUsage() {
        return "<name> (die|kill your self|death be upon you)";
    }
}
