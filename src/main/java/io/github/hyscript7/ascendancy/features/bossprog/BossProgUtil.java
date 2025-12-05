package io.github.hyscript7.ascendancy.features.bossprog;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BossProgUtil {
    public static boolean hasKillOfFamily(Player player, BossFamily family) {
        return hasKillOfFamily(
                PlayerDataManager.getInstance().getPlayerData(player),
                family
        );
    }

    public static boolean hasKillOfFamily(PlayerData data, BossFamily family) {
        return data.getKilledBosses().stream().anyMatch(bossType -> bossType.getFamily().equals(family));
    }

    public static boolean hasKillFromSource(Player player, BossSource source) {
        return hasKillFromSource(
                PlayerDataManager.getInstance().getPlayerData(player),
                source
        );
    }

    public static boolean hasKillFromSource(PlayerData data, BossSource source) {
        return data.getKilledBosses().stream().anyMatch(bossType -> bossType.getSource().equals(source));
    }

    private static final Predicate<PlayerData> allowByDefault = (unused) -> true;
    private static Map<ItemTier, Predicate<PlayerData>> bossKillRequirements = null;

    public static Map<ItemTier, Predicate<PlayerData>> getBossKillRequirements() {
        int bossKillsForDiamond = AscendancyConfig.getInstance().getBossProgression().diamondKills();
        int bossKillsForNetherite = AscendancyConfig.getInstance().getBossProgression().netheriteKills();
        Map<ItemTier, Predicate<PlayerData>> bossKillRequirements = new HashMap<>();
        bossKillRequirements.put(ItemTier.WOODEN, allowByDefault);
        bossKillRequirements.put(ItemTier.STONE, allowByDefault);
        bossKillRequirements.put(ItemTier.IRON, allowByDefault);
        bossKillRequirements.put(ItemTier.GOLDEN, allowByDefault);
        bossKillRequirements.put(ItemTier.DIAMOND, (playerData) -> playerData.getKilledBossesCount() >= bossKillsForDiamond);
        bossKillRequirements.put(ItemTier.NETHERITE, (playerData) -> playerData.getKilledBossesCount() >= bossKillsForNetherite);
        return bossKillRequirements;
    }

    public static boolean isLocked(PlayerData playerData, ItemStack something) {
        @Nullable ItemTier itemTier = ItemTier.fromMaterial(something.getType());
        if (itemTier == null) return false;
        if (bossKillRequirements == null) bossKillRequirements = getBossKillRequirements();
        return !bossKillRequirements.get(itemTier).test(playerData);
    }
}
