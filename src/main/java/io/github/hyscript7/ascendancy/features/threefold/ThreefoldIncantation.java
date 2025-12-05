package io.github.hyscript7.ascendancy.features.threefold;

import io.github.hyscript7.ascendancy.registries.Identifiable;

public interface ThreefoldIncantation extends Identifiable {
    /**
     * Executes the incantation.
     * @param context The incantation context
     */
    void execute(ThreefoldContext context);

    /**
     * Called before execute is called. Can be used to make additional checks.
     * @param context The incantation context
     * @return true if the incantation can execute, otherwise false
     */
    boolean canExecute(ThreefoldContext context);

    /**
     * Checks if the context is referring to this incantation.
     * @param context The incantation context
     * @return true if it is, otherwise false
     */
    default boolean matches(ThreefoldContext context) {
        return matches(context.history().get());
    }

    /**
     * Checks if the provided message history is referring to this incantation.
     * @param incantations The message history array to check against
     * @return true if it is, otherwise false
     */
    boolean matches(String[] incantations);

    /**
     * Returns the sentences used in this incantation.
     * <p>
     *     <b>This should not include the audience's incantations.</b>
     * They are handled separately.</p>
     * @return An array with the incantations.
     */
    String[] getIncantations();

    /**
     * The audience this incantation points to.
     * @return The audience
     */
    ThreefoldAudience getAudience();
}
