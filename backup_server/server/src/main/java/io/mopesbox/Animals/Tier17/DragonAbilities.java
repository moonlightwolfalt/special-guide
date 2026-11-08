package io.mopesbox.Animals.Tier17;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.KingDragonFx;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

/**
 * The dragons' milestone abilities.
 *
 * Active, cast with a key:
 *   0 Wing Slash (Z), 1 Wingspan (X), 2 Shoot Meteor (C)
 *   3 Dragon Dive (V), 4 Firestorm (B), 5 Volcanic Eruption (N): King Dragon offensive milestones I, II, III
 * Passive, automatic (King Dragon defensive milestones, see tick()):
 *   I Inferno Ring (a burst of fire around the dragon every 5 s), II Dragonscale (25% less damage),
 *   III Phoenix Heart (below 30% health: heal 40% and 2 s immunity, every 45 s)
 *
 * King Dragon: Z, X and C from the start, then one offensive and one defensive per milestone.
 * Black Dragon: at most 2. It gets one slot at milestone I (12.5m XP) and another at milestone II
 * (17.5m), and the player chooses which ability goes in each (MilestoneState keeps the choice for the life).
 *
 * The client casts with message 120, action 4 (and chooses with action 5). The effects are
 * KingDragonFx objects, drawn from the frames made by tools/kingdragon_fx/make_fx_frames.py.
 */
public class DragonAbilities {
    public static final int COUNT = 6;
    public static final String[] NAMES = { "Wing Slash", "Wingspan", "Shoot Meteor", "Dragon Dive", "Firestorm", "Volcanic Eruption" };
    public static final String[] KEYS = { "Z", "X", "C", "V", "B", "N" };
    public static final String[] ICONS = { "wingslash", "wingspan", "meteor", "dive", "firestorm", "eruption" };
    public static final int[] COOLDOWN = { 6, 14, 20, 8, 12, 25 }; // seconds
    /** King Dragon milestone that unlocks each ability (0 = from the start). */
    public static final int[] UNLOCK = { 0, 0, 0, 1, 2, 3 };
    public static final int BLACK_DRAGON_LIMIT = 2;
    private static final int METEOR_LAVA = 20;          // % of lava a meteor costs
    private static final int ERUPTION_LAVA = 30;

    // defensive passives, by King Dragon milestone I, II, III
    public static final String[] PASSIVE_NAMES = { "Inferno Ring", "Dragonscale", "Phoenix Heart" };
    public static final String[] PASSIVE_ICONS = { "infernoring", "dragonscale", "phoenix" };
    public static final String[] PASSIVE_TEXT = {
            "Every 5s a ring of fire bursts around you: 30 damage, burn and push",
            "You take 25% less damage",
            "Below 30% health: heal 40% and 2s immunity (every 45s)" };
    public static final int INFERNO_EVERY_MS = 5000;
    public static final int PHOENIX_EVERY_MS = 45000;
    public static final double DRAGONSCALE = 0.75;

    private final Animal dragon;
    private final long[] ready = new long[COUNT];
    private long infernoNext = 0;  // time of the next Inferno Ring
    private long phoenixReady = 0; // time Phoenix Heart can save the dragon again
    private int diveTicks = 0;     // ticks left of a Dragon Dive charge
    private double diveX, diveY;

    public DragonAbilities(Animal dragon) {
        this.dragon = dragon;
    }

    private GameClient client() {
        return dragon.getClient();
    }

    private boolean isKing() {
        return dragon instanceof KingDragon;
    }

    /** The dragon's milestone level (0 to 3). */
    private int level() {
        return client().milestones.level();
    }

    /** Whether this dragon may use ability i (King Dragon: by milestone; Black Dragon: if chosen). */
    public boolean has(int i) {
        if (i < 0 || i >= COUNT) return false;
        if (isKing()) return level() >= UNLOCK[i];
        return i < 3 && client().milestones.blackDragonHas(i);
    }

    /** Whether the King Dragon has defensive passive p (0 Inferno Ring, 1 Dragonscale, 2 Phoenix Heart). */
    public boolean hasPassive(int p) {
        return isKing() && level() >= p + 1;
    }

    /** Milliseconds until passive p fires again (Inferno Ring) or can save the dragon again (Phoenix Heart). */
    public long passiveLeft(int p) {
        long now = System.currentTimeMillis();
        if (p == 0) return Math.max(0, infernoNext - now);
        if (p == 2) return Math.max(0, phoenixReady - now);
        return 0;
    }

    /** Damage the dragon takes is multiplied by this (Dragonscale). */
    public double damageTakenMult() {
        return hasPassive(1) ? DRAGONSCALE : 1;
    }

    private boolean canAct() {
        Animal d = dragon;
        return !(d.isRammed || d.inArena || d.flag_flying || d.isInHole() || d.isGhost || d.health <= 0);
    }

    /** Every server tick (from the dragon's update): the passives and the Dragon Dive charge. */
    public void tick() {
        Animal d = dragon;
        if (diveTicks > 0) {
            diveTicks--;
            d.setVelocityX(diveX * 28);
            d.setVelocityY(diveY * 28);
        }
        if (!isKing() || d.getClient() == null) return;
        long now = System.currentTimeMillis();
        if (hasPassive(0)) {
            if (infernoNext == 0) infernoNext = now + INFERNO_EVERY_MS;
            if (now >= infernoNext) {
                infernoNext = now + INFERNO_EVERY_MS;
                if (canAct()) infernoRing();
            }
        }
        if (hasPassive(2) && now >= phoenixReady && d.health > 0 && d.health < d.maxHealth * 0.3 && !d.inArena) {
            phoenixReady = now + PHOENIX_EVERY_MS;
            d.health = Math.min(d.maxHealth, d.health + d.maxHealth * 0.4);
            d.setImmunity(2);
            client().send(Networking.personalGameEvent(255, "Phoenix Heart saved you!"));
            addFx(d.getX(), d.getY(), (int) (d.getRadius() * 2.2), KingDragonFx.PHOENIX, 0, 900);
        }
    }

    /** Milliseconds until ability i can be used again. */
    public long left(int i) {
        return Math.max(0, ready[i] - System.currentTimeMillis());
    }

    /** Uses ability i. Returns whether it happened. */
    public boolean cast(int i) {
        if (!has(i)) return false;
        Animal d = dragon;
        if (!canAct() || d.isStunned()) return false;
        long now = System.currentTimeMillis();
        if (now < ready[i]) return false;
        if (i == 2 && d.getWater() < METEOR_LAVA) {
            client().send(Networking.personalGameEvent(255, "Not enough lava for a meteor!"));
            return false;
        }
        if (i == 5 && d.getWater() < ERUPTION_LAVA) {
            client().send(Networking.personalGameEvent(255, "Not enough lava for an eruption!"));
            return false;
        }
        switch (i) {
            case 0: wingSlash(); break;
            case 1: wingspan(); break;
            case 2: shootMeteor(); break;
            case 3: dragonDive(); break;
            case 4: firestorm(); break;
            default: volcanicEruption(); break;
        }
        ready[i] = client().instantRecharge ? now : now + d.milestoneCooldown(COOLDOWN[i]) * 1000L;
        return true;
    }

    /** Animals the abilities can hit: not this dragon, nor anything flying, hiding or protected. */
    private List<Animal> targets() {
        Room room = client().getRoom();
        List<GameClient> cs = new ArrayList<>(room.clients);
        cs.addAll(room.aiClients);
        List<Animal> out = new ArrayList<>();
        for (GameClient c : cs) {
            Animal o = c == null ? null : c.getPlayer();
            if (o == null || o == dragon || o.inArena || o.isInvincible() || o.flag_flying || o.isInHole()
                    || o.isDiveActive() || o.isGhost || o.health <= 0)
                continue;
            out.add(o);
        }
        return out;
    }

    private static void push(Animal o, double fromX, double fromY, double force) {
        double th = Math.atan2(o.getY() - fromY, o.getX() - fromX);
        o.setVelocityX(Math.cos(th) * force);
        o.setVelocityY(Math.sin(th) * force);
    }

    private void addFx(double x, double y, int radius, int kind, double angle, int lifeMs) {
        Room room = client().getRoom();
        room.addObj(new KingDragonFx(room.getID(), x, y, radius, kind, angle, lifeMs, room));
    }

    /** A claw of fire sweeping across the front: hits everything in a 140-degree cone. */
    private void wingSlash() {
        Animal d = dragon;
        double reach = d.getRadius() * 2.4;
        for (Animal o : targets()) {
            double dist = Utils.distance(d.getX(), o.getX(), d.getY(), o.getY());
            if (dist > reach + o.getRadius()) continue;
            double ang = Math.toDegrees(Math.atan2(o.getY() - d.getY(), o.getX() - d.getX()));
            double diff = ((ang - d.getAngle()) % 360 + 540) % 360 - 180;
            if (Math.abs(diff) > 70) continue;
            o.hurt(55, 0, d);
            o.setFire(3, d, 2);
            push(o, d.getX(), d.getY(), 18);
        }
        addFx(d.getX(), d.getY(), (int) reach, KingDragonFx.WINGSLASH, d.getAngle(), 400);
    }

    /** Both wings snap open: pushes every animal around away, hurts and stuns it. */
    private void wingspan() {
        Animal d = dragon;
        double reach = d.getRadius() * 3.2;
        for (Animal o : targets()) {
            double dist = Utils.distance(d.getX(), o.getX(), d.getY(), o.getY());
            if (dist > reach + o.getRadius() || dist < 1) continue;
            o.hurt(35, 0, d);
            o.setStun(1);
            push(o, d.getX(), d.getY(), 18 + 22 * (1 - Math.min(1, dist / reach)));
        }
        addFx(d.getX(), d.getY(), (int) reach, KingDragonFx.WINGSPAN, d.getAngle(), 600);
    }

    /** Launches a meteor that lands about 650 pixels ahead (meteorLanded does the damage). */
    private void shootMeteor() {
        Animal d = dragon;
        d.takeWater(METEOR_LAVA);
        double a = d.getAngle();
        double x = d.getX() + Math.cos(Math.toRadians(a)) * d.getRadius();
        double y = d.getY() + Math.sin(Math.toRadians(a)) * d.getRadius();
        Room room = client().getRoom();
        KingDragonFx m = new KingDragonFx(room.getID(), x, y, 120, KingDragonFx.METEOR, a, 0, room)
                .flyAsMeteor(this, 24, 26);
        m.setSpawnID(d.getId());
        room.addObj(m);
    }

    /** The meteor hit the ground: blast damage that drops off with distance, and burning. */
    public void meteorLanded(double x, double y) {
        Animal d = dragon;
        double blast = 230;
        for (Animal o : targets()) {
            double dist = Utils.distance(x, o.getX(), y, o.getY());
            if (dist > blast + o.getRadius()) continue;
            double f = dist < blast / 2 ? 1 : 1 - (dist - blast / 2) / (blast / 2 + o.getRadius()) * 0.6;
            o.hurt(140 * f, 0, d);
            o.setFire(5, d, 3);
            push(o, x, y, 26 * f);
        }
        addFx(x, y, (int) blast, KingDragonFx.IMPACT, 0, 700);
    }

    // ---------------------------------------------------------------- offensive milestones

    /** Charges about 380 px forward; everything in the path is burned, hurt and knocked aside. */
    private void dragonDive() {
        Animal d = dragon;
        double a = Math.toRadians(d.getAngle());
        double dx = Math.cos(a), dy = Math.sin(a), len = 380;
        double sx = d.getX(), sy = d.getY();
        for (Animal o : targets()) {
            double px = o.getX() - sx, py = o.getY() - sy;
            double along = px * dx + py * dy;
            if (along < -o.getRadius() || along > len + o.getRadius()) continue;
            double side = px * dy - py * dx; // which side of the path
            if (Math.abs(side) > d.getRadius() + o.getRadius()) continue;
            o.hurt(60, 0, d);
            o.setFire(3, d, 2);
            double s = side >= 0 ? 1 : -1;
            o.setVelocityX(dy * 24 * s);
            o.setVelocityY(-dx * 24 * s);
        }
        diveX = dx;
        diveY = dy;
        diveTicks = 8;
        addFx(sx + dx * len / 2, sy + dy * len / 2, (int) (len / 2 + d.getRadius()), KingDragonFx.DIVE, d.getAngle(), 450);
    }

    /** A wide 60-degree burst of 12 fireballs (with the dragon's fireball bonuses from the skill tree). */
    private void firestorm() {
        Animal d = dragon;
        io.mopesbox.Milestones.MilestoneState ms = client().milestones;
        ms.fireFan(d, io.mopesbox.Milestones.MilestoneState.fireSpecies(d), 12, 30, d.getAngle());
    }

    /** The ground bursts around the dragon: 90 damage, a long burn and a ring of fires. */
    private void volcanicEruption() {
        Animal d = dragon;
        d.takeWater(ERUPTION_LAVA);
        double reach = d.getRadius() * 3.4;
        for (Animal o : targets()) {
            double dist = Utils.distance(d.getX(), o.getX(), d.getY(), o.getY());
            if (dist > reach + o.getRadius() || dist < 1) continue;
            o.hurt(90, 0, d);
            o.setFire(6, d, 3);
            push(o, d.getX(), d.getY(), 28);
        }
        Room room = client().getRoom();
        int species = io.mopesbox.Milestones.MilestoneState.fireSpecies(d);
        for (int k = 0; k < 10; k++) {
            double ang = Math.toRadians(k * 36);
            io.mopesbox.Objects.Fire.Fire f = new io.mopesbox.Objects.Fire.Fire(room.getID(),
                    d.getX() + Math.cos(ang) * reach * 0.7, d.getY() + Math.sin(ang) * reach * 0.7, 40, species, room,
                    client(), 5, false);
            f.setStyle(io.mopesbox.Objects.Fire.FireStyles.of(d.getInfo(), species));
            room.addObj(f);
        }
        addFx(d.getX(), d.getY(), (int) reach, KingDragonFx.ERUPTION, 0, 900);
    }

    // ---------------------------------------------------------------- defensive milestones

    /** Inferno Ring: a burst of fire around the dragon (every 5 s, from tick()). */
    private void infernoRing() {
        Animal d = dragon;
        double reach = d.getRadius() * 2.6;
        for (Animal o : targets()) {
            double dist = Utils.distance(d.getX(), o.getX(), d.getY(), o.getY());
            if (dist > reach + o.getRadius() || dist < 1) continue;
            o.hurt(30, 0, d);
            o.setFire(3, d, 2);
            push(o, d.getX(), d.getY(), 16);
        }
        addFx(d.getX(), d.getY(), (int) reach, KingDragonFx.INFERNO, 0, 700);
    }
}
