package io.github.hyscript7.ascendancy.builtins.innate.commands.self;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class GetOwnName extends AbstractInnateCommand {

    public GetOwnName() {
        super("get_own_name", "Discover Own Name", false, false, true);
    }

    @Override
    public boolean execute(InnateContext context) {
        String trueName = TrueNameManager.getInstance().getTrueName(context.target().getUniqueId());
        if (trueName == null) {
            return false;
        }
        AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.INFO, "Your true name is " + trueName);
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(InnateUtils.removeNonAlpha(message)).equalsIgnoreCase("I wish to know my name") || message.equalsIgnoreCase("I desire my name") || message.equalsIgnoreCase("I recall my name");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[0];
    }

    @Override
    public String getUsage() {
        return "I [wish to know | desire | recall] my name";
    }
}
