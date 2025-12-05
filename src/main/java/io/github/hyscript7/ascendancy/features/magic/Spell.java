package io.github.hyscript7.ascendancy.features.magic;

import io.github.hyscript7.ascendancy.registries.Identifiable;

public interface Spell extends Identifiable {
    /**
     * Casts the spell using the provided context.
     * @param context The spell context
     * @return true if successful, otherwise false
     */
    boolean cast(SpellContext context);

    /**
     * Checks whether the player who invoked the spell can actually cast it.
     * @param context The spell context
     * @return true if they can, false otherwise
     */
    boolean canCast(SpellContext context);

    /**
     * Checks whether the provided spell context is trying to cast this spell.
     * @param context The spell context
     * @return true if it is, false otherwise
     */
    boolean incantationMatches(SpellContext context);

    /**
     * Checks whether the provided incantation is trying to cast this spell.
     * @param string The string potentially containing the incantation
     * @return true if it is, false otherwise
     */
    boolean incantationMatches(String string);

    /**
     * Should return a minimessage formatted incantation message.
     * <p>
     * This is what is shown to the player when they discover the spell from a book or boss kill.
     * @return A mini message formatted string
     */
    String getIncantation();

    /**
     * Returns the tier of the spell
     * @return The spell tier enum instance
     */
    SpellTier getTier();

    /**
     * Returns how much mana it costs to cast this spell
     * @return The mana cost
     */
    double getManaCost();

    /**
     * Returns how long the cooldown is for this spell
     * @return The cooldown in milliseconds
     */
    long getCooldownMillis();
}
