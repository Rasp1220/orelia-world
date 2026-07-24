package rpg.dungeon.model;

import java.util.Map;

/**
 * Static dungeon definition loaded from {@code dungeons.yml}. The entry point is a plain
 * world/coordinate rather than a generated instance - each dungeon is a physical area
 * players are teleported into and back out of, not a per-party cloned world.
 */
public final class DungeonData {

    private final String id;
    private final String name;
    private final DungeonType type;
    private final int minPartySize;
    private final int maxPartySize;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final long rewardExp;
    private final double rewardMoney;
    private final Map<String, Integer> enemies;
    private final String bossId;
    private final int timeLimitSeconds;

    public DungeonData(String id, String name, DungeonType type, int minPartySize, int maxPartySize,
                        String world, double x, double y, double z, long rewardExp, double rewardMoney,
                        Map<String, Integer> enemies, String bossId, int timeLimitSeconds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.minPartySize = minPartySize;
        this.maxPartySize = maxPartySize;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rewardExp = rewardExp;
        this.rewardMoney = rewardMoney;
        this.enemies = Map.copyOf(enemies);
        this.bossId = bossId;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DungeonType getType() {
        return type;
    }

    public int getMinPartySize() {
        return minPartySize;
    }

    public int getMaxPartySize() {
        return maxPartySize;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getRewardExp() {
        return rewardExp;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    /** monsters.yml id -> count required to clear. Empty if this dungeon has no regular enemies (boss-only). */
    public Map<String, Integer> getEnemies() {
        return enemies;
    }

    /** bosses.yml id, or {@code null} if this dungeon has no boss. */
    public String getBossId() {
        return bossId;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }
}
