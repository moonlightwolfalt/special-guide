package io.mopesbox.Milestones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import com.google.gson.Gson;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.DragonAbilities;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Fire.Fire;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Objects.Fire.FireStyles;
import io.mopesbox.Server.MsgReader;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.PacketException;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

/**
 * One player's milestones and skill-tree picks for the current life.
 *
 * Milestones count from the most XP the player has had this life, so losing XP to bites
 * doesn't take them away. Everything resets when the player is below the 5m tier again
 * (after dying, the new animal starts low).
 *
 * Messages (type 120, MessageType.SKILLTREE):
 *   server to client: uint8 sub, string json. sub 1 = tree, 2 = state, 3 = milestone reached.
 *   client to server: uint8 action. 1 = send tree and state, 2 = pick (uint16 skill id), 3 = cast Q.
 */
public class MilestoneState {
    private final GameClient client;
    private final Gson gson = new Gson();

    private long peakXP = 0;
    private final Set<String> announced = new HashSet<>();
    // picks per tree: the dragon tree has 3 rings, the titan tree 1
    private final int[] dragonPicks = { -1, -1, -1 };
    private final int[] titanPicks = { -1 };
    // the Black Dragon's chosen milestone abilities (Wing Slash, Wingspan, Shoot Meteor), at most 2
    private final boolean[] blackDragonAbilities = new boolean[3];
    private final Map<Integer, Long> readyAt = new HashMap<>();

    private String lastKey = null;
    private int lastLevel = -1;

    // body stats: the animal they were added to, and its own values before the bonus
    private Animal appliedTo = null;
    private double baseMaxHealth, baseBite;
    private boolean bodyDirty = true;

    private final Timer second = new Timer(1000);

    public MilestoneState(GameClient client) {
        this.client = client;
    }

    // ------------------------------------------------------------------ per tick

    public void update() {
        Animal p = client.getPlayer();
        if (p == null || p.getInfo() == null) return;
        if (p.getInfo().getTier() < 16) {
            if (peakXP != 0 || lastKey != null) reset();
            return;
        }
        peakXP = Math.max(peakXP, client.getXPLong());

        String key = Milestones.key(p);
        if (key == null) return;
        int level = level(key);
        if (!key.equals(lastKey) || level != lastLevel) {
            for (int i = 1; i <= level; i++)
                if (announced.add(key + ":" + i)) announce(key, i);
            lastKey = key;
            lastLevel = level;
            bodyDirty = true;
            sendState();
        }

        if (p != appliedTo || bodyDirty) applyBody(p);

        second.update(Constants.TICKS_PER_SECOND);
        if (second.isDone()) {
            second.reset();
            int regen = (int) sum(s -> s.regen);
            if (regen > 0 && p.health > 0 && p.health < p.maxHealth)
                p.health = Math.min(p.maxHealth, p.health + regen);
            int lavaRegen = (int) sum(s -> s.lavaRegen);
            if (lavaRegen > 0) p.addWater(lavaRegen);
        }
    }

    private void reset() {
        peakXP = 0;
        announced.clear();
        dragonPicks[0] = dragonPicks[1] = dragonPicks[2] = -1;
        titanPicks[0] = -1;
        java.util.Arrays.fill(blackDragonAbilities, false);
        readyAt.clear();
        lastKey = null;
        lastLevel = -1;
        appliedTo = null;
        bodyDirty = true;
        sendState(); // tells the client to hide the milestone HUD
    }

    private int level(String key) {
        int n = 0;
        for (long t : Milestones.thresholds(key)) if (peakXP >= t) n++;
        return n;
    }

    private String curKey() {
        Animal p = client.getPlayer();
        return p == null ? null : Milestones.key(p);
    }

    private int curLevel() {
        String k = curKey();
        return k == null ? 0 : level(k);
    }

    private boolean curIsDragon() {
        return Milestones.isDragon(curKey());
    }

    private String curTrack() {
        return Milestones.track(curKey());
    }

    /** The current animal's tree picks (empty when it has no tree). */
    private int[] picks() {
        String t = curTrack();
        if (SkillTree.DRAGON.equals(t)) return dragonPicks;
        if (SkillTree.TITAN.equals(t)) return titanPicks;
        return new int[0];
    }

    // ------------------------------------------------------------------ stats

    /** Total of one stat over the rework skills picked in the current animal's tree. */
    private double sum(ToDoubleFunction<Skill> f) {
        double t = 0;
        for (int id : picks()) {
            Skill s = SkillTree.byId(id);
            if (s != null && s.kind == Skill.Kind.REWORK) t += f.applyAsDouble(s);
        }
        return t;
    }

    /** Extra fireballs per shot. */
    public int volley() {
        int v = (int) sum(s -> s.volley);
        if ("blackdragon".equals(curKey()) && curLevel() >= 1) v += 1;
        return Math.max(0, Math.min(8, v));
    }

    public double spreadMult() {
        return Math.max(0.2, 1 + sum(s -> s.spread));
    }

    public int sizeBonus() {
        return (int) sum(s -> s.size) + (curIsDragon() && curLevel() >= 3 ? 6 : 0);
    }

    public double travelMult() {
        double m = 1 + sum(s -> s.travel);
        if ("blackdragon".equals(curKey()) && curLevel() >= 3) m += 0.2;
        return Math.max(0.5, m);
    }

    public int burnBonus() {
        return (int) sum(s -> s.burn);
    }

    public double rechargeMult() {
        double m = 1 + sum(s -> s.recharge);
        String k = curKey();
        int lvl = curLevel();
        if ("blackdragon".equals(k) && lvl >= 2) m *= 0.8;
        if ("kingdragon".equals(k) && lvl >= 2) m *= 5.0 / 7.0;
        if (k != null && !Milestones.isDragon(k) && lvl >= 1) m *= 0.75;
        return Math.max(0.35, m);
    }

    public double lavaMult() {
        double m = 1 + sum(s -> s.lava);
        if (curIsDragon() && curLevel() >= 3) m *= 0.5;
        return Math.max(0.2, m);
    }

    /** How many bursts a King Dragon's fire stream lasts (20 normally). */
    public int streamLength() {
        return 20 + ("kingdragon".equals(curKey()) && curLevel() >= 1 ? 6 : 0);
    }

    public double tailCooldownMult() {
        if (!"kingdragon".equals(curKey())) return 1;
        int lvl = curLevel();
        return lvl >= 3 ? 8.0 / 15.0 : lvl >= 1 ? 11.0 / 15.0 : 1;
    }

    public double tailDamageMult() {
        return "kingdragon".equals(curKey()) && curLevel() >= 2 ? 1.25 : 1;
    }

    public double tailSizeMult() {
        return "kingdragon".equals(curKey()) && curLevel() >= 3 ? 1.3 : 1;
    }

    private int hpBonus() {
        String k = curKey();
        return (int) sum(s -> s.hp) + (k != null && !Milestones.isDragon(k) && curLevel() >= 1 ? 50 : 0);
    }

    private int biteBonus() {
        String k = curKey();
        return (int) sum(s -> s.bite) + (k != null && !Milestones.isDragon(k) && curLevel() >= 1 ? 10 : 0);
    }

    // ------------------------------------------------------------------ dragon milestone abilities

    /** The current animal's milestone level (0 when it has no milestones). */
    public int level() {
        return curLevel();
    }

    public boolean blackDragonHas(int i) {
        return i >= 0 && i < 3 && blackDragonAbilities[i];
    }

    private int blackDragonChosen() {
        int n = 0;
        for (boolean b : blackDragonAbilities) if (b) n++;
        return n;
    }

    /** Black Dragon ability slots open now: one per milestone, at most 2. */
    private int blackDragonSlots() {
        return "blackdragon".equals(curKey()) ? Math.min(DragonAbilities.BLACK_DRAGON_LIMIT, curLevel()) : 0;
    }

    private DragonAbilities dragonAbilities() {
        Animal p = client.getPlayer();
        if (p instanceof KingDragon) return ((KingDragon) p).abilities;
        if (p instanceof BlackDragon) return ((BlackDragon) p).abilities;
        return null;
    }

    private void chooseBlackDragonAbility(int i) {
        if (i < 0 || i > 2 || blackDragonAbilities[i]) return;
        if (blackDragonChosen() >= blackDragonSlots()) {
            client.send(Networking.popup(blackDragonSlots() >= DragonAbilities.BLACK_DRAGON_LIMIT
                    ? "A Black Dragon can have only " + DragonAbilities.BLACK_DRAGON_LIMIT + " milestone abilities."
                    : "Reach the next milestone to open another ability slot.", "error", 4));
            return;
        }
        blackDragonAbilities[i] = true;
        client.send(Networking.popup("Learned " + DragonAbilities.NAMES[i] + " (" + DragonAbilities.KEYS[i] + ")!", "success", 4));
        sendState();
    }

    /** An ability recharge in whole seconds, after milestones and picks. */
    public int cooldown(int secs) {
        return Math.max(1, (int) Math.round(secs * rechargeMult()));
    }

    private void applyBody(Animal p) {
        if (p != appliedTo) {
            appliedTo = p;
            baseMaxHealth = p.maxHealth;
            baseBite = p.biteDamage;
        }
        double newMax = baseMaxHealth + hpBonus();
        double delta = newMax - p.maxHealth;
        p.maxHealth = newMax;
        if (delta > 0) p.health += delta;
        if (p.health > p.maxHealth) p.health = p.maxHealth;
        p.biteDamage = baseBite + biteBonus();
        bodyDirty = false;
    }

    // ------------------------------------------------------------------ fire helpers

    /** The fireball species a dragon shoots (the King Dragon's are its species + 5). */
    public static int fireSpecies(Animal a) {
        return a instanceof KingDragon ? a.getInfo().getAnimalSpecies() + 5 : a.getInfo().getAnimalSpecies();
    }

    /** A fireball with this player's size, range and burn bonuses, added to the room. */
    public FireBall fireBall(Animal owner, double x, double y, int species, double angle) {
        Room room = client.getRoom();
        FireBall fb = new FireBall(room.getID(), x, y, Math.max(16, 30 + sizeBonus()), species, angle, 25,
                owner.getId(), room, client, 5);
        fb.setTravel(18 * travelMult());
        fb.setBurnSeconds(6 + burnBonus());
        room.addObj(fb);
        return fb;
    }

    /** count fireballs fanned evenly across baseAngle +- halfSpread degrees, from the dragon's edge. */
    public void fireFan(Animal owner, int species, int count, double halfSpread, double baseAngle) {
        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0 : -halfSpread + 2 * halfSpread * i / (count - 1);
            double pa = baseAngle + t * 1.6 + Utils.randomDouble(-2, 2);
            double x = Math.cos(Math.toRadians(pa)) * owner.getRadius();
            double y = Math.sin(Math.toRadians(pa)) * owner.getRadius();
            fireBall(owner, owner.getX() + x, owner.getY() + y, species, baseAngle + t);
        }
    }

    // ------------------------------------------------------------------ Q abilities

    public void cast() {
        Animal p = client.getPlayer();
        if (p == null || curTrack() == null || p.inArena || p.health <= 0) return;
        long now = System.currentTimeMillis();
        boolean any = false;
        for (int id : picks()) {
            Skill s = SkillTree.byId(id);
            if (s == null || s.kind != Skill.Kind.NEW) continue;
            Long r = readyAt.get(s.id);
            if (r != null && now < r) continue;
            perform(p, s);
            readyAt.put(s.id, client.instantRecharge ? now : now + s.cooldown * 1000L);
            any = true;
        }
        if (any) sendState();
    }

    private void perform(Animal p, Skill s) {
        Room room = client.getRoom();
        int species = fireSpecies(p);
        double a = p.getAngle();

        if (s.nova > 0) {
            for (int i = 0; i < s.nova; i++) {
                double ang = a + 360.0 * i / s.nova;
                double x = Math.cos(Math.toRadians(ang)) * p.getRadius();
                double y = Math.sin(Math.toRadians(ang)) * p.getRadius();
                fireBall(p, p.getX() + x, p.getY() + y, species, ang);
            }
        }
        if (s.rear > 0) fireFan(p, species, s.rear, Math.min(45, 10 + 3 * s.rear), a + 180);
        if (s.field > 0) {
            double dist = p.getRadius() * 1.8;
            for (int i = 0; i < s.field; i++) {
                double ang = 360.0 * i / s.field;
                Fire f = new Fire(room.getID(), p.getX() + Math.cos(Math.toRadians(ang)) * dist,
                        p.getY() + Math.sin(Math.toRadians(ang)) * dist, 34, species, room, client, 5, false);
                f.setStyle(FireStyles.of(p.getInfo(), species));
                f.setBurnSeconds(6 + burnBonus());
                room.addObj(f);
            }
        }
        if (s.gust > 0) {
            List<GameClient> others = new ArrayList<>(room.clients);
            others.addAll(room.aiClients);
            for (GameClient c : others) {
                Animal o = c == null ? null : c.getPlayer();
                if (o == null || o == p || o.inArena || o.flag_flying || o.isInvincible()) continue;
                double d = Utils.distance(p.getX(), o.getX(), p.getY(), o.getY());
                if (d < 1 || d > s.gust) continue;
                double th = Math.atan2(o.getY() - p.getY(), o.getX() - p.getX());
                double force = 12 + 30 * (1 - d / s.gust);
                o.setVelocityX(Math.cos(th) * force);
                o.setVelocityY(Math.sin(th) * force);
                o.setStun(1);
            }
        }
        if (s.dash > 0) {
            double sp = 20 + 12 * s.dash;
            p.setVelocityX(Math.cos(Math.toRadians(a)) * sp);
            p.setVelocityY(Math.sin(Math.toRadians(a)) * sp);
        }
        if (s.shield > 0) p.setImmunity(s.shield);
        if (s.heal > 0) p.health = Math.min(p.maxHealth, p.health + p.maxHealth * s.heal / 100.0);
        if (s.lavaFill > 0) p.addWater(p.maxwater * s.lavaFill / 100.0);
    }

    // ------------------------------------------------------------------ picking

    private void pick(int id) {
        Skill s = SkillTree.byId(id);
        if (s == null || !s.track.equals(curTrack())) return;
        int ring = s.ring;
        if (curLevel() < ring) {
            long xp = Milestones.thresholds(curKey())[ring - 1];
            client.send(Networking.popup("Reach " + format(xp) + " XP to pick this skill.", "error", 4));
            return;
        }
        int[] picks = picks();
        if (picks[ring - 1] != -1) return;
        picks[ring - 1] = id;
        bodyDirty = true;
        client.send(Networking.popup("Learned " + s.name + "!", "success", 4));
        sendState();
    }

    private static String format(long xp) {
        double m = xp / 1_000_000.0;
        return (m == Math.floor(m) ? String.valueOf((long) m) : String.valueOf(m)) + "m";
    }

    // ------------------------------------------------------------------ network

    public void handle(MsgReader reader) throws PacketException {
        int action = reader.readUInt8();
        switch (action) {
            case 1:
                if (curTrack() != null) send(1, SkillTree.toJson(curTrack()));
                sendState();
                break;
            case 2:
                pick(reader.readUInt16());
                break;
            case 3:
                cast();
                break;
            case 4: { // dragons: Wing Slash (0), Wingspan (1), Shoot Meteor (2)
                int i = reader.readUInt8();
                DragonAbilities da = dragonAbilities();
                if (da != null && da.cast(i)) sendState();
                break;
            }
            case 5: { // Black Dragon: choose a milestone ability for an open slot
                int i = reader.readUInt8();
                if (client.getPlayer() instanceof BlackDragon) chooseBlackDragonAbility(i);
                break;
            }
            default:
                break;
        }
    }

    private void send(int sub, String json) {
        try {
            if (client.socket == null) return; // bots
            MsgWriter w = new MsgWriter();
            w.writeType(MessageType.SKILLTREE);
            w.writeUInt8(sub);
            w.writeString(json);
            client.send(w);
        } catch (Exception e) {
            // the socket closed; nothing to tell
        }
    }

    public void sendState() {
        Map<String, Object> m = new LinkedHashMap<>();
        String key = curKey();
        m.put("animal", key == null ? "" : key);
        m.put("name", key == null ? "" : Milestones.animalName(key));
        int lvl = key == null ? 0 : level(key);
        long[] th = Milestones.thresholds(key);
        m.put("level", lvl);
        m.put("maxLevel", th.length);
        m.put("thresholds", th);
        m.put("next", lvl < th.length ? th[lvl] : 0);
        String track = Milestones.track(key);
        int[] picks = picks();
        m.put("tree", track != null);
        m.put("track", track == null ? "" : track);
        m.put("picks", picks.clone());
        int points = 0;
        if (track != null)
            for (int r = 0; r < SkillTree.ringsOpen(track, lvl); r++) if (picks[r] == -1) points++;
        m.put("points", points);
        List<Object> q = new ArrayList<>();
        if (track != null) {
            long now = System.currentTimeMillis();
            for (int id : picks) {
                Skill s = SkillTree.byId(id);
                if (s == null || s.kind != Skill.Kind.NEW) continue;
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("id", s.id);
                e.put("name", s.name);
                e.put("icon", s.icon());
                e.put("cd", s.cooldown);
                Long r = readyAt.get(s.id);
                e.put("left", r == null ? 0 : Math.max(0, r - now));
                q.add(e);
            }
        }
        m.put("q", q);
        // dragon milestone abilities: the usable ones, and (Black Dragon) the ones it can still choose
        List<Object> da = new ArrayList<>();
        List<Object> daChoose = new ArrayList<>();
        DragonAbilities abil = dragonAbilities();
        if (abil != null) {
            boolean canChoose = client.getPlayer() instanceof BlackDragon && blackDragonChosen() < blackDragonSlots();
            for (int i = 0; i < DragonAbilities.COUNT; i++) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("i", i);
                e.put("name", DragonAbilities.NAMES[i]);
                e.put("key", DragonAbilities.KEYS[i]);
                e.put("icon", DragonAbilities.ICONS[i]);
                e.put("offense", DragonAbilities.UNLOCK[i] > 0);
                if (abil.has(i)) {
                    e.put("left", abil.left(i));
                    da.add(e);
                } else if (canChoose && i < 3) {
                    daChoose.add(e);
                }
            }
        }
        m.put("da", da);
        m.put("daChoose", daChoose);
        // King Dragon defensive passives: all three, with whether each is unlocked yet
        List<Object> passives = new ArrayList<>();
        if (abil != null && client.getPlayer() instanceof KingDragon) {
            for (int p = 0; p < 3; p++) {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("name", DragonAbilities.PASSIVE_NAMES[p]);
                e.put("icon", DragonAbilities.PASSIVE_ICONS[p]);
                e.put("text", DragonAbilities.PASSIVE_TEXT[p]);
                e.put("on", abil.hasPassive(p));
                e.put("level", p + 1);
                e.put("left", abil.passiveLeft(p));
                e.put("every", p == 0 ? DragonAbilities.INFERNO_EVERY_MS : p == 2 ? DragonAbilities.PHOENIX_EVERY_MS : 0);
                passives.add(e);
            }
        }
        m.put("passives", passives);
        if (client.getPlayer() instanceof BlackDragon) {
            m.put("daUsed", blackDragonChosen());
            m.put("daMax", DragonAbilities.BLACK_DRAGON_LIMIT);
        }
        List<Object> badges = new ArrayList<>();
        for (int i = 1; i <= lvl; i++) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("level", i);
            b.put("title", Milestones.title(key, i));
            b.put("image", Milestones.image(key, i));
            badges.add(b);
        }
        m.put("milestones", badges);
        send(2, gson.toJson(m));
    }

    private void announce(String key, int level) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("animal", key);
        m.put("name", Milestones.animalName(key));
        m.put("level", level);
        m.put("title", Milestones.title(key, level));
        m.put("lines", Milestones.lines(key, level));
        m.put("image", Milestones.image(key, level));
        m.put("xp", Milestones.thresholds(key)[level - 1]);
        send(3, gson.toJson(m));
    }
}
