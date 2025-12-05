package io.github.hyscript7.ascendancy.features.rituals;

public interface RitualStage {
    /**
     * Determine whether this stage's conditions are met.
     * @param context The ritual context
     * @return True if the stage is complete, false otherwise.
     */
    boolean isComplete(RitualContext context);

    /**
     * Returns the minimessage formatted hint on how to complete this ritual.
     * @param context The ritual context
     * @return A minimessage formatted String
     */
    String getHint(RitualContext context);

    /**
     * Called when the ritual is completed.
     * @param context The ritual context
     */
    default void onComplete(RitualContext context) {}
}
