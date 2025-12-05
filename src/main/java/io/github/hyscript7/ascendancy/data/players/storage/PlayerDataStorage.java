package io.github.hyscript7.ascendancy.data.players.storage;

import io.github.hyscript7.ascendancy.data.players.PlayerData;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PlayerDataStorage {
    PlayerData load(UUID uuid) throws IOException;

    void save(PlayerData data) throws IOException;

    boolean exists(UUID uuid);

    void delete(UUID uuid) throws IOException;

    List<PlayerData> loadAll() throws IOException;
}
