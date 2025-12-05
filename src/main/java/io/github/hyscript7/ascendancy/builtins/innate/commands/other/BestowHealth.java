package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;

public class BestowHealth extends AbstractInnateCommand {
    public BestowHealth() {
        super("bestow health", "Health Bestowal", false, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        // Always bestows one half of current HP
        double toBestow = context.invoker().getHealth() / 2.0d;
        context.invoker().damage(toBestow);
        context.target().heal(toBestow);
        AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.INFO, "You have been bestowed health by " + context.invoker().getName() + "!");
        AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.SUCCESS, "You have bestowed " + context.target().getName() + " health!");
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).matches(".+ I bestow you ([Hh]ealth|[Hh][Pp]|[Hh]eal)");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[]{};
    }

    @Override
    public String getUsage() {
        return "<name> I bestow you (health|hp|heal)";
    }
}
