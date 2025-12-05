package io.github.hyscript7.ascendancy.features.threefold;

import io.github.hyscript7.ascendancy.registries.RegistryManager;
import lombok.Getter;

@Getter
public abstract class AbstractThreefoldIncantation implements ThreefoldIncantation {
    private final String id;
    private final String displayName;
    private final String[] incantations;
    private final String audienceId;
    private ThreefoldAudience audience;

    protected AbstractThreefoldIncantation(String id, String displayName, String[] incantations, String audienceId) {
        this.id = id;
        this.displayName = displayName;
        this.incantations = incantations;
        this.audienceId = audienceId;
    }

    /**
     * Checks for incantation matches using a sliding window approach over the full length of the passed message history.
     */
    @Override
    public boolean matches(String[] incantations) {
        int requiredMatches = this.incantations.length;
        int audienceMatches = getAudience().getIncantation().length;
        int totalLength = requiredMatches + audienceMatches;
        // Window is too short to match... since the window size SHOULD BE decided dynamically from the longest
        // size requirement, if this is true, someone fucked up somewhere.
        if (incantations.length < totalLength) return false;
        int matches = 0;
        // Since we do not know what sort of data got passed incantations (as the context can have varying length)
        // We have to check from 0, since we do not know where the audience's incantation ends.
        // The incantations array should already be ordered.
        for (String incantation : incantations) {
            // This shit will absolutely not work if the message history wasn't unwrapped properly.
            // But there is nothing we can do about that, since we cannot guarantee how this is being called.
            // One option would be to make this private, but that takes away from some of the flexibility this approach
            // otherwise allows for.
            if (!this.incantations[matches].equalsIgnoreCase(incantation)) {
                matches = 0;
                continue;
            }
            matches++;
            if (matches == requiredMatches) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ThreefoldAudience getAudience() {
        if (audience == null) {
            audience = RegistryManager.getInstance().getThreefoldAudienceRegistry().get(audienceId).orElse(null);
        }
        return audience;
    }

}
