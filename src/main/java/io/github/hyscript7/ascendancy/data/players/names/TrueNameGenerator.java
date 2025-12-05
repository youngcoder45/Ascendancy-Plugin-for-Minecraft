package io.github.hyscript7.ascendancy.data.players.names;

import java.util.*;
import java.util.function.Supplier;

/**
 * Generates unique, mystical-sounding true names for players
 */
public class TrueNameGenerator {
    // Since we need the names to be believable, we use finite, predefined prefixes, middles and suffixes.
    // This setup should be able to handle AT LEAST 100 players.
    // TODO: Load from config
    private static final String[] PREFIXES = {
            "Ael", "Zar", "Mor", "Kael", "Thal", "Dor", "Var", "Nyx",
            "Lyr", "Val", "Xan", "Eth", "Syl", "Vor", "Ash", "Mal",
            "Zen", "Kor", "Thol", "Myr", "Drak", "Zeph", "Nex", "Ryn",
            "Skal", "Vex", "Kyr", "Oth", "Zul", "Mor", "Fel", "Gar"
    };
    private static final String[] MIDDLES = {
            "an", "or", "eth", "al", "on", "ar", "en", "ir",
            "ul", "ak", "os", "ix", "yn", "um", "az", "el",
            "ith", "ok", "un", "av", "ep", "od", "yl", "ax"
    };
    private static final String[] SUFFIXES = {
            "ion", "oth", "iel", "ath", "us", "is", "or", "ax",
            "yn", "os", "ix", "um", "el", "ak", "ir", "on",
            "ius", "eth", "ara", "eon", "yx", "ul", "az", "en"
    };
    private static final String[] TITLES = {
            "the Forgotten", "the Eternal", "Shadowborn", "Voidtouched",
            "the Ancient", "Starbinder", "Soulkeeper", "the Cursed",
            "Dreamwalker", "the Whispered", "Truthseeker", "the Lost",
            "Darkened", "the Veiled", "Fatebound", "the Shattered"
    };

    private final Random random;

    public TrueNameGenerator() {
        this.random = new Random();
    }

    public TrueNameGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Keeps trying to generate a unique true name given a set of existing true names.
     *
     * @return A guaranteed to be free true name
     */
    public String generateUniqueName(Set<String> existingNames, Supplier<String> generator) {
        String name;
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            name = generator.get();
            attempts++;

            if (attempts >= maxAttempts) {
                // Fallback: add random suffix to guarantee uniqueness
                name = name + random.nextInt(1000);
                break;
            }
        } while (existingNames.contains(name));

        return name;
    }

    /**
     * Generates a random true name with a random structure.
     *
     * @return A possibly non-unique true name
     */
    public String generateName() {
        NameStructure structure = chooseStructure();
        return generateName(structure);
    }

    /**
     * Generates a random true name with the specified structure.
     *
     * @param structure The name structure to use
     * @return A possibly non-unique true name
     */
    public String generateName(NameStructure structure) {
        return switch (structure) {
            case SHORT -> generateShortName();
            case MEDIUM -> generateMediumName();
            case LONG -> generateLongName();
            case TITLED -> generateTitledName();
        };
    }

    /**
     * Generates a name in the format of Prefix + Suffix (e.g., "Aelion", "Zarnyx")
     */
    private String generateShortName() {
        String prefix = randomElement(PREFIXES);
        String suffix = randomElement(SUFFIXES);
        return prefix + suffix.toLowerCase();
    }

    /**
     * Generates a name in the format of Prefix + Middle + Suffix (e.g., "Kaelaroth", "Morethis")
     */
    private String generateMediumName() {
        String prefix = randomElement(PREFIXES);
        String middle = randomElement(MIDDLES);
        String suffix = randomElement(SUFFIXES);
        return prefix + middle + suffix.toLowerCase();
    }

    /**
     * Generates a name in the format of Prefix + Middle + Middle + Suffix (e.g., "Thalorakion")
     */
    private String generateLongName() {
        String prefix = randomElement(PREFIXES);
        String middle1 = randomElement(MIDDLES);
        String middle2 = randomElement(MIDDLES);
        String suffix = randomElement(SUFFIXES);
        return prefix + middle1 + middle2 + suffix.toLowerCase();
    }

    /**
     * Generates a name in the format of Name + Title (e.g., "Zarnyx the Forgotten")
     * Name can either be a Short or Medium (chosen randomly)
     */
    private String generateTitledName() {
        String name = random.nextBoolean() ? generateShortName() : generateMediumName();
        String title = randomElement(TITLES);
        return name + " " + title;
    }

    /**
     * Rolls a D100 and returns a name structure based on the result.
     *
     * @return The chosen name structure
     */
    private NameStructure chooseStructure() {
        int roll = random.nextInt(100);

        if (roll < 35) {
            return NameStructure.SHORT;      // about 35% chance
        } else if (roll < 75) {
            return NameStructure.MEDIUM;     // about 40% chance
        } else if (roll < 90) {
            return NameStructure.LONG;       // about 15% chance
        } else {
            return NameStructure.TITLED;     // about 10% chance
        }
    }

    /**
     * Utility function for getting a random element from an array.
     */
    private <T> T randomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * Performs a light check on whether something is likely to be a true name.
     * <p>
     * The concrete conditions are likely to catch things that aren't true names,
     * so be sure to perform a lookup using player data.
     *
     * @param name The string that we suspect to be a true name
     * @return True if it looks like a true name, otherwise false
     */
    public boolean isValidTrueName(String name) {
        // Cannot be empty
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Check length
        if (name.length() < 4 || name.length() > 50) {
            return false;
        }

        // Should start with capital letter
        if (!Character.isUpperCase(name.charAt(0))) {
            return false;
        }

        // Should contain only letters, spaces, and apostrophes
        return name.matches("[A-Za-z ']+");
    }

    /**
     * Represents various structures an innate name can have.
     */
    public enum NameStructure {
        SHORT, MEDIUM, LONG, TITLED
    }

    /**
     * Generates an n amount of random (possibly non-unique) true names with random structures.
     *
     * @return A list (not a set) of true names.
     */
    public List<String> generateSuggestions(int count) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            suggestions.add(generateName());
        }
        return suggestions;
    }
}
