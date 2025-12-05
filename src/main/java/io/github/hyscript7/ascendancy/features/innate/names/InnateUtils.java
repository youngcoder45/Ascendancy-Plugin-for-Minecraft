package io.github.hyscript7.ascendancy.features.innate.names;

import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InnateUtils {
    /**
     * Unifies commonly interchanged keywords.
     * <p>
     * E.g. want -> wish
     * @param message The message to unify
     * @return The unified message
     */
    public static String unifyInnateCommand(String message) {
        return message.replace("want", "wish");
    }

    /**
     * Removes all non-alphanumeric characters from a string. (Spaces are kept)
     * @param message The message to clean
     * @return The cleaned message
     */
    public static String removeNonAlpha(String message) {
        return message.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    /**
     *
     */
    public static String removeNonAlpha(String message, boolean allowSeparators) {
        if (!allowSeparators) return removeNonAlpha(message);
        return message.replaceAll("[^a-zA-Z0-9_ ]", "");
    }

    /**
     * Parses an entire message and returns the first found innate name.
     * @param message The message
     * @param beginIndex The character index from which to start checking
     * @return The <b>first</b> valid name, otherwise null if no name found
     */
    public static @Nullable String findFirstValidInnateName(String message, int beginIndex) {
        String[] words = InnateUtils.removeNonAlpha(message)
                .substring(beginIndex)
                .split("\\s+");

        if (words.length == 0) return null;

        final int MAX_LEN = 5;

        // Sliding-window over the words
        for (int start = 0; start < words.length; start++) {
            int maxNameLength = Math.min(MAX_LEN, words.length - start);

            for (int nameLength = 1; nameLength <= maxNameLength; nameLength++) {
                StringBuilder trueNameBuilder = new StringBuilder();

                for (int i = 0; i < nameLength; i++) {
                    if (i > 0) trueNameBuilder.append(" ");
                    trueNameBuilder.append(words[start + i]);
                }

                String trueName = trueNameBuilder.toString();

                UUID targetUuid = TrueNameManager.getInstance().findTrueNameOwner(trueName);
                if (targetUuid != null) return trueName;
            }
        }

        return null;
    }

    /**
     * Attempts to parse an online player's innate name from the start of the provided message.
     * @param message The message to check
     * @return The name if found, otherwise null
     */
    public static String parseInnateNameAtSentenceBeginning(String message) {
        String[] words = message.split("\\s+");
        if (words.length < 2) return null;

        for (int nameLength = 1; nameLength <= Math.min(5, words.length - 1); nameLength++) {
            String trueName = String.join(" ", java.util.Arrays.copyOfRange(words, 0, nameLength));

            UUID targetUuid = TrueNameManager.getInstance().findTrueNameOwner(trueName);
            if (targetUuid == null) continue;

            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null) continue;

            return trueName;
        }
        return null;
    }
}
