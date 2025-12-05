package io.github.hyscript7.ascendancy.data.players.storage;

import com.google.gson.*;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.features.bossprog.BossType;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class JsonPlayerDataStorage implements PlayerDataStorage {
    private final File dataFolder;
    private final Gson gson;

    public JsonPlayerDataStorage(AscendancyPlugin plugin) {
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public PlayerData load(UUID uuid) throws IOException {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            return deserialize(json);
        }
    }

    @Override
    public void save(PlayerData data) throws IOException {
        File file = getPlayerFile(data.getUuid());

        JsonObject json = serialize(data);

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        }
    }

    @Override
    public boolean exists(UUID uuid) {
        return getPlayerFile(uuid).exists();
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }

    @Override
    public List<PlayerData> loadAll() {
        var files = dataFolder.listFiles();
        if (files == null) {
            AscendancyPlugin.getInstance().getLogger().log(Level.WARNING, "List files on player data returned null, is the player data path correctly pointing to a directory?");
            return Collections.emptyList();
        }
        return Arrays.stream(files).map(
                file -> {
                    try (Reader reader = new FileReader(file)) {
                        JsonObject json = gson.fromJson(reader, JsonObject.class);
                        return deserialize(json);
                    } catch (IOException e) {
                        return null;
                    }
                }
        ).filter(Objects::nonNull).toList();
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".json");
    }

    private JsonObject serialize(PlayerData data) {
        JsonObject json = new JsonObject();

        json.addProperty("uuid", data.getUuid().toString());
        json.addProperty("maxLives", data.getMaxLives());
        json.addProperty("lives", data.getLives());
        json.addProperty("dead", data.isDead());
        json.addProperty("pvpDeaths", data.getPvpDeaths());
        json.addProperty("pveDeaths", data.getPveDeaths());
        json.addProperty("pvpKills", data.getPvpKills());
        json.addProperty("trueName", data.getTrueName());

        JsonArray knownSpells = new JsonArray();
        for (String spellId : data.getKnownSpells()) {
            knownSpells.add(spellId);
        }
        json.add("knownSpells", knownSpells);

        JsonArray knownTrueNames = new JsonArray();
        for (String name : data.getKnownTrueNames()) {
            knownTrueNames.add(name);
        }
        json.add("knownTrueNames", knownTrueNames);

        JsonArray killedBosses = new JsonArray();
        for (BossType bossType : data.getKilledBosses()) {
            killedBosses.add(bossType.name());
        }
        json.add("killedBosses", killedBosses);

        json.addProperty("firstSeenTimestamp", data.getFirstSeenTimestamp());
        json.addProperty("lastSeenTimestamp", data.getLastSeenTimestamp());

        return json;
    }

    private PlayerData deserialize(JsonObject json) {
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        PlayerData data = new PlayerData(uuid);

        if (json.has("maxLives")) {
            data.setMaxLives(json.get("maxLives").getAsInt());
        }

        if (json.has("lives")) {
            data.setLives(json.get("lives").getAsInt());
        }

        if (json.has("dead")) {
            data.setDead(json.get("dead").getAsBoolean());
        }

        if (json.has("pvpDeaths")) {
            data.setPvpDeaths(json.get("pvpDeaths").getAsInt());
        }

        if (json.has("pveDeaths")) {
            data.setPveDeaths(json.get("pveDeaths").getAsInt());
        }

        if (json.has("pvpKills")) {
            data.setPvpKills(json.get("pvpKills").getAsInt());
        }

        if (json.has("trueName")) {
            data.setTrueName(json.get("trueName").getAsString());
        }

        if (json.has("knownSpells")) {
            Set<String> knownSpellIds = new HashSet<>();
            JsonArray knownSpells = json.getAsJsonArray("knownSpells");
            for (JsonElement spellId : knownSpells) {
                knownSpellIds.add(spellId.getAsString());
            }
            data.setKnownSpells(knownSpellIds);
        }

        if  (json.has("knownTrueNames")) {
            Set<String> knownTrueNames = new HashSet<>();
            JsonArray knownTrueNamesArray = json.getAsJsonArray("knownTrueNames");
            for (JsonElement name : knownTrueNamesArray) {
                knownTrueNames.add(name.getAsString());
            }
            data.setKnownTrueNames(knownTrueNames);
        }

        if (json.has("killedBosses")) {
            Set<BossType> killedBosses = new HashSet<>();
            JsonArray killedBossesArray = json.getAsJsonArray("killedBosses");
            for (JsonElement bossId : killedBossesArray) {
                try {
                    BossType bossType = BossType.valueOf(bossId.getAsString());
                    killedBosses.add(bossType);
                } catch (IllegalArgumentException e) {
                    AscendancyPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid boss id " + bossId.getAsString() + " in player data of " + data.getUuid() + ". Skipping!");
                }
            }
            data.setKilledBosses(killedBosses);
        }

        if (json.has("firstSeenTimestamp")) {
            data.setFirstSeenTimestamp(json.get("firstSeenTimestamp").getAsLong());
        }

        if (json.has("lastSeenTimestamp")) {
            data.setLastSeenTimestamp(json.get("lastSeenTimestamp").getAsLong());
        }

        data.markClean(); // Clean, since we just loaded it
        return data;
    }
}
