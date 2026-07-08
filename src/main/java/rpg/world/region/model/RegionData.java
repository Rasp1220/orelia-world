package rpg.world.region.model;

/**
 * A named, axis-aligned box area (SOW RegionModule). {@code WARP} regions additionally
 * teleport the player to {@code warp*} on entry.
 */
public final class RegionData {

    private final String id;
    private final String name;
    private final RegionType type;
    private final String world;
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final String enterMessage;
    private final String exitMessage;
    private final String warpWorld;
    private final double warpX;
    private final double warpY;
    private final double warpZ;
    private final float warpYaw;

    public RegionData(String id, String name, RegionType type, String world,
                       double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                       String enterMessage, String exitMessage,
                       String warpWorld, double warpX, double warpY, double warpZ, float warpYaw) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.world = world;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        this.enterMessage = enterMessage;
        this.exitMessage = exitMessage;
        this.warpWorld = warpWorld;
        this.warpX = warpX;
        this.warpY = warpY;
        this.warpZ = warpZ;
        this.warpYaw = warpYaw;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RegionType getType() {
        return type;
    }

    public String getWorld() {
        return world;
    }

    public boolean contains(String worldName, double x, double y, double z) {
        return world.equals(worldName) && x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public String getWarpWorld() {
        return warpWorld;
    }

    public double getWarpX() {
        return warpX;
    }

    public double getWarpY() {
        return warpY;
    }

    public double getWarpZ() {
        return warpZ;
    }

    public float getWarpYaw() {
        return warpYaw;
    }
}
