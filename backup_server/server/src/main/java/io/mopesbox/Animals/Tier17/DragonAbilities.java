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
 * The dragons' milestone abilities: Wing Slash (0, key Z), Wingspan (1, key X), Shoot Meteor (2, key C).
 *
 * King Dragon: all three, from the start.
 * Black Dragon: at most 2. It gets one slot at milestone I (12.5m XP) and another at milestone II
 * (17.5m), and the player chooses which ability goes in each (MilestoneState keeps the choice for the life).
 *
 * The client casts with message 120, action 4 (and chooses with action 5). The effects are
 * KingDragonFx objects, drawn from the frames made by tools/kingdragon_fx/make_fx_frames.py.
 */
public class DragonAbilities {
    public static final String[] NAMES = { "Wing Slash", "Wingspan", "Shoot Meteor" };
    public static final String[] KEYS = { "Z", "X", "C" };
    public static final String[] ICONS = { "wingslash", "wingspan", "meteor" };
    public static final int[] COOLDOWN = { 6, 14, 20 }; // seconds
    public static final int BLACK_DRAGON_LIMIT = 2;
    private static final int METEOR_LAVA = 20;          // % of lava a meteor costs

    private final Animal dragon;
    private final long[] ready = new long[3];

    public DragonAbilities(Animal dragon) {
        this.dragon = dragon;
    }

    private GameClient client() {
        return dragon.getClient();
    }

    /** Whether this dragon may use ability i right now (King Dragon: always; Black Dragon: if chosen). */
    public boolean has(int i) {
        if (i < 0 || i > 2) return false;
        if (dragon instanceof KingDragon) return true;
        return client().milestones.blackDragonHas(i);
    }

    /** Milliseconds until ability i can be used again. */
    public long left(int i) {
        return Math.max(0, ready[i] - System.currentTimeMillis());
    }

    /** Uses ability i. Returns whether it happened. */
    public boolean cast(int i) {
        if (!has(i)) return false;
        Animal d = dragon;
        if (d.isRammed || d.inArena || d.flag_flying || d.isInHole() || d.isGhost || d.isStunned() || d.health <= 0)
            return false;
        long now = System.currentTimeMillis();
        if (now < ready[i]) return false;
        if (i == 2 && d.getWater() < METEOR_LAVA) {
            client().send(Networking.personalGameEvent(255, "Not enough lava for a meteor!"));
            return false;
        }
        switch (i) {
            case 0: wingSlash(); break;
            case 1: wingspan(); break;
            default: shootMeteor(); break;
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
}
