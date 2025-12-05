package io.github.hyscript7.ascendancy.features.threefold;

import io.github.hyscript7.ascendancy.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

public class ThreefoldUtils {
    public static @Nullable ThreefoldAudience matchAudience(ThreefoldContext context) {
        return RegistryManager.getInstance().getThreefoldAudienceRegistry().getAll().stream()
                .filter(threefoldAudience -> threefoldAudience.matches(context))
                .findFirst().orElse(null);
    }

    public static @Nullable ThreefoldIncantation matchIncantation(ThreefoldContext context, ThreefoldAudience audience) {
        return RegistryManager.getInstance().getThreefoldIncantationRegistry().getAll().stream()
                .filter(threefoldIncantation -> threefoldIncantation.getAudience().equals(audience))
                .filter(threefoldIncantation -> threefoldIncantation.matches(context))
                .findFirst().orElse(null);
    }

    public static String normalizeContent(String content) {
        return content.replaceAll("[^a-zA-Z0-9_ ]", "");
    }
}
