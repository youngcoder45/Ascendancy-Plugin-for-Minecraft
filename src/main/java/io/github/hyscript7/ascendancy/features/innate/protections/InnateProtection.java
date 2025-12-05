package io.github.hyscript7.ascendancy.features.innate.protections;

import java.util.UUID;

public interface InnateProtection {
    /**
     * Returns the UUID of the player this protection targets
     * @return The player's uuid
     */
    UUID getPlayerUuid();

    /**
     * Performs updates and returns new state of the protection.
     * @return true if the protection still lasts, otherwise false.
     */
    boolean isActive();
}
