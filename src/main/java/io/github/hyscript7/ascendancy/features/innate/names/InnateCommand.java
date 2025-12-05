package io.github.hyscript7.ascendancy.features.innate.names;

import io.github.hyscript7.ascendancy.registries.Identifiable;

public interface InnateCommand extends Identifiable {
    /**
     * Runs the command.
     * @param context The InnateCommand execution context
     * @return True if execution is successful, otherwise false.
     */
    boolean execute(InnateContext context);

    /**
     * Runs various checks to determine whether the command can be run.
     * @param context The InnateCommand execution context
     * @return True if execution is allowed, otherwise false.
     */
    boolean canExecute(InnateContext context);

    /**
     * Checks whether the specified message is referencing this command.
     * @param message The plaintext message
     * @return True if it is, otherwise false
     */
    boolean matchesMessage(String message);

    /**
     * Parses out arguments from a message.
     * Output will be passed to execute inside the context.
     * @param message The original plain text message.
     * @return An array of arguments (in order)
     */
    String[] resolveArguments(String message);

    /**
     * Get the usage example for this command.
     * @return A minimessage formatted String containing the example usage.
     */
    String getUsage();

    /**
     * Whether the command is harmful (harms the target in some way)
     * @return True if yes, otherwise false.
     */
    boolean isHarmful();

    /**
     * Whether the command is meant to be used on one self.
     * @return True if yes, otherwise false.
     */
    default boolean targetsSelf() {
        return false;
    }

    /**
     * Whether the command is "fun". Meaning if it should only be allowed out of combat.
     * @return True if yes, otherwise false.
     */
    default boolean isFun() {
        return false;
    }
}
