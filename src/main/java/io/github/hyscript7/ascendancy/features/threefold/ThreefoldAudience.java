package io.github.hyscript7.ascendancy.features.threefold;

import io.github.hyscript7.ascendancy.registries.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class ThreefoldAudience implements Identifiable {
    private final String id;
    private final String name;
    private final String[] incantation;
    private final ThreefoldAudiencePowerLevel powerLevel;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public boolean matches(ThreefoldContext context) {
        return matches(context.history().get());
    }

    public boolean matches(String[] incantations) {
        int matched = 0;
        for (String line : incantations) {
            if (!incantation[matched].equalsIgnoreCase(line)) {
                matched = 0;
                continue;
            }
            matched++;
            if (matched == 3) {
                return true;
            }
        }
        return false;
    }
}
