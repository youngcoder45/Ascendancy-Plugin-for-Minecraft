package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Surrender extends AbstractInnateCommand {

    // We use a set with tasks instead of a map, because I am too tired to implement another cooldown manager.
    // TODO: Replace with a generic player join/leave aware cooldown manager
    private final Set<UUID> cooldowns = new HashSet<>();

    public Surrender() {
        super("surrender", "Surrender", true, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        cooldowns.add(context.invoker().getUniqueId());
        final UUID invokerUuid = context.invoker().getUniqueId();
        Bukkit.getScheduler().runTaskLater(AscendancyPlugin.getInstance(), () -> {
            cooldowns.remove(invokerUuid);
        }, 2 * 60 * 20); // 2 minute CD
        // Inflict weakness 2 for 30 seconds
        context.target().addPotionEffect(PotionEffectType.WEAKNESS.createEffect(20 * 30, 1));
        return true;
    }

    @Override
    public boolean canExecute(InnateContext context) {
        return !cooldowns.contains(context.target().getUniqueId());
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).toLowerCase().matches(".+ (surrender|give\\sup|weaken)");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[]{};
    }

    @Override
    public String getUsage() {
        return "<name> (surrender|give up|weaken)";
    }
}
