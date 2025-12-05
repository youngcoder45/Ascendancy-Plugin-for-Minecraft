package io.github.hyscript7.ascendancy.registries;

public interface Identifiable {
    /**
     * Get the unique identifier for this object.
     * @return The unique identifier
     */
    String getId();

    /**
     * Get the pretty identifier / display name for this object.
     * @return The display name
     */
    String getDisplayName();
}
