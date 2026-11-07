package io.mopesbox.Objects.ETC;

// Pumpkin kinds, listed from most common to rarest. The id is sent to the client,
// which draws web/img/pumpkin/<id>/<frame>.png, so ids must not change.
//
// Rarity follows the usual loot colours: green is uncommon, blue rare,
// purple epic, orange legendary, red mythic, then diamond, with the black
// hole rarest of all. Ids are only the order kinds were added in.
public enum PumpkinType {
    //          id  name          max  spawnMs  carryXpDiv  carryCoins  goalCoins  goalExp
    NORMAL     (0, "Pumpkin",      2,   30000,  500,         5,         150,        50),
    GOLDEN     (1, "Golden",       1,  100000,   50,         5,         150,        50),
    EMERALD    (6, "Emerald",      1,  180000,   48,         6,         200,        65),
    SAPPHIRE   (2, "Sapphire",     1,  300000,   45,         8,         250,        80),
    AMETHYST   (3, "Amethyst",     1,  600000,   40,        12,         400,       120),
    AMBER      (4, "Amber",        1, 1200000,   35,        18,         600,       180),
    RUBY       (7, "Ruby",         1, 1800000,   32,        22,         750,       220),
    DIAMOND    (8, "Diamond",      1, 2400000,   28,        26,         875,       260),
    BLACK_HOLE (5, "Black Hole",   1, 3600000,   25,        30,        1000,       300);

    public final int id;
    public final String displayName;
    // Most of this kind alive in the land biome at once.
    public final int maxAlive;
    // How often a new one may spawn while fewer than maxAlive are out.
    public final int spawnIntervalMs;
    // Carrying gives the carrier's XP divided by this every 5 seconds.
    public final int carryXpDivisor;
    public final int carryCoins;
    public final int goalCoins;
    public final int goalExp;

    PumpkinType(int id, String displayName, int maxAlive, int spawnIntervalMs,
            int carryXpDivisor, int carryCoins, int goalCoins, int goalExp) {
        this.id = id;
        this.displayName = displayName;
        this.maxAlive = maxAlive;
        this.spawnIntervalMs = spawnIntervalMs;
        this.carryXpDivisor = carryXpDivisor;
        this.carryCoins = carryCoins;
        this.goalCoins = goalCoins;
        this.goalExp = goalExp;
    }

    private static final PumpkinType[] BY_ID = new PumpkinType[values().length];
    static {
        for (PumpkinType t : values()) BY_ID[t.id] = t;
    }

    public static PumpkinType fromId(int id) {
        return isValidId(id) ? BY_ID[id] : NORMAL;
    }

    public static boolean isValidId(int id) {
        return id >= 0 && id < BY_ID.length && BY_ID[id] != null;
    }
}
