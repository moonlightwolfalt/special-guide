package io.mopesbox.Milestones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import io.mopesbox.Milestones.Skill.Kind;

/**
 * The dragon skill tree: three circular rings.
 *
 *   Ring I   (outer):  45 skills, 23 ability reworks + 22 new abilities. Opens at milestone I.
 *   Ring II  (middle): 25 skills, 13 reworks + 12 new abilities. Opens at milestone II.
 *   Ring III (inner):  10 skills, 5 reworks + 5 new abilities. Opens at milestone III.
 *
 * The player picks one skill from each ring, so the choice narrows from 45 to 10.
 * Black Dragon and King Dragon share the tree; reworks change whichever fire ability the dragon has.
 */
public final class SkillTree {
    public static final int[] RING_SIZES = { 45, 25, 10 };
    public static final String[] RING_NAMES = { "Ring I: Ember", "Ring II: Inferno", "Ring III: Cataclysm" };

    private static final List<Skill> ALL = new ArrayList<>();
    private static String json;

    private static Skill rework(int ring, String name) {
        Skill s = new Skill(ALL.size(), ring, Kind.REWORK, name);
        ALL.add(s);
        return s;
    }

    private static Skill ability(int ring, String name) {
        Skill s = new Skill(ALL.size(), ring, Kind.NEW, name);
        ALL.add(s);
        return s;
    }

    static {
        // ---------------- Ring I: 23 reworks ----------------
        rework(1, "Kindling Volley").volley(1);
        rework(1, "Wide Breath").spread(0.4);
        rework(1, "Narrow Breath").spread(-0.4).size(4);
        rework(1, "Long Flame").travel(0.25);
        rework(1, "Heavy Embers").size(6).travel(-0.1);
        rework(1, "Lingering Burn").burn(2);
        rework(1, "Quick Breath").recharge(-0.12);
        rework(1, "Lava Thrift").lava(-0.25);
        rework(1, "Hot Blood").hp(40);
        rework(1, "Sharp Fangs").bite(8);
        rework(1, "Ember Heart").regen(2);
        rework(1, "Magma Gland").lavaRegen(1);
        rework(1, "Scatter Shot").volley(2).spread(0.6).size(-4);
        rework(1, "Focused Fire").spread(-0.6).burn(1);
        rework(1, "Far Flame").travel(0.4).recharge(0.1);
        rework(1, "Thick Scales").hp(70).recharge(0.05);
        rework(1, "Searing Bite").bite(5).burn(1);
        rework(1, "Fuel Line").lava(-0.15).recharge(-0.06);
        rework(1, "Twin Burn").volley(1).lava(0.15);
        rework(1, "Blaze Core").size(3).burn(1);
        rework(1, "Swift Embers").travel(0.15).recharge(-0.06);
        rework(1, "Iron Hide").hp(55);
        rework(1, "Warm Blood").regen(1).lavaRegen(1);

        // ---------------- Ring I: 22 new abilities ----------------
        ability(1, "Flame Nova").nova(8).cd(16);
        ability(1, "Ember Ring").nova(6).cd(11);
        ability(1, "Tail Embers").rear(3).cd(9);
        ability(1, "Rear Guard").rear(5).cd(14);
        ability(1, "Cauterize").heal(15).cd(25);
        ability(1, "Second Wind").heal(25).cd(40);
        ability(1, "Magma Sip").lavaFill(30).cd(20);
        ability(1, "Lava Well").lavaFill(50).cd(35);
        ability(1, "Scale Guard").shield(1).cd(25);
        ability(1, "Molten Shell").shield(2).cd(45);
        ability(1, "Wing Dash").dash(1).cd(10);
        ability(1, "Long Glide").dash(2).cd(18);
        ability(1, "Scorched Earth").field(6).cd(20);
        ability(1, "Fire Circle").field(10).cd(30);
        ability(1, "Wing Gust").gust(400).cd(15);
        ability(1, "Roaring Gust").gust(550).cd(24);
        ability(1, "Spark Nova").nova(4).cd(7);
        ability(1, "Burning Retreat").dash(1).rear(3).cd(16);
        ability(1, "Ember Mend").heal(10).lavaFill(20).cd(22);
        ability(1, "Ash Cloak").shield(1).field(6).cd(30);
        ability(1, "Blast Off").gust(400).dash(1).cd(20);
        ability(1, "Fire Bloom").nova(6).field(4).cd(24);

        // ---------------- Ring II: 13 reworks ----------------
        rework(2, "Triple Ember").volley(2);
        rework(2, "Firestorm Arc").volley(2).spread(0.8);
        rework(2, "Lance of Flame").spread(-0.7).size(8).travel(0.3);
        rework(2, "Inferno Burn").burn(4);
        rework(2, "Rapid Breath").recharge(-0.25);
        rework(2, "Lava Reservoir").lava(-0.45);
        rework(2, "Dragon Heart").hp(120);
        rework(2, "Rending Fangs").bite(18);
        rework(2, "Phoenix Blood").regen(5);
        rework(2, "Magma Core").lavaRegen(3);
        rework(2, "Comet Embers").size(10).travel(0.35).recharge(0.1);
        rework(2, "Blazing Tempo").recharge(-0.15).lava(-0.2);
        rework(2, "Scaled Fortress").hp(90).regen(3);

        // ---------------- Ring II: 12 new abilities ----------------
        ability(2, "Supernova").nova(14).cd(18);
        ability(2, "Tail Barrage").rear(8).cd(14);
        ability(2, "Rebirth Flame").heal(40).cd(45);
        ability(2, "Lava Spring").lavaFill(80).cd(35);
        ability(2, "Obsidian Shell").shield(3).cd(45);
        ability(2, "Sky Rush").dash(3).cd(14);
        ability(2, "Inferno Field").field(14).cd(28);
        ability(2, "Tempest Wings").gust(700).cd(20);
        ability(2, "Meteor Dive").dash(2).nova(8).cd(22);
        ability(2, "Burning Sanctuary").heal(20).field(10).cd(35);
        ability(2, "Molten Aegis").shield(2).lavaFill(40).cd(40);
        ability(2, "Thunder Wings").gust(600).rear(5).cd(22);

        // ---------------- Ring III: 5 reworks ----------------
        rework(3, "Dragonfire Volley").volley(4).spread(1.0);
        rework(3, "Sunlance").spread(-0.8).size(14).travel(0.5).burn(3);
        rework(3, "Endless Breath").recharge(-0.4).lava(-0.6);
        rework(3, "Ancient Heart").hp(250).regen(6);
        rework(3, "Tyrant Fangs").bite(35).burn(3);

        // ---------------- Ring III: 5 new abilities ----------------
        ability(3, "Cataclysm").nova(24).field(16).cd(30);
        ability(3, "Undying Flame").heal(60).shield(2).cd(60);
        ability(3, "Skyfall").dash(3).gust(800).nova(10).cd(26);
        ability(3, "Volcano Heart").lavaFill(100).field(18).cd(40);
        ability(3, "Dragon's Wrath").rear(10).nova(16).cd(24);

        for (int r = 1; r <= 3; r++) {
            int n = 0;
            for (Skill s : ALL) if (s.ring == r) n++;
            if (n != RING_SIZES[r - 1])
                throw new IllegalStateException("Skill tree ring " + r + " has " + n + " skills, expected " + RING_SIZES[r - 1]);
        }
    }

    private SkillTree() {}

    public static List<Skill> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static Skill byId(int id) {
        return id >= 0 && id < ALL.size() ? ALL.get(id) : null;
    }

    /** The whole tree as JSON, built once. */
    public static synchronized String toJson() {
        if (json == null) {
            Map<String, Object> m = new LinkedHashMap<>();
            List<Object> skills = new ArrayList<>();
            for (Skill s : ALL) skills.add(s.toJson());
            List<Object> rings = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("ring", i + 1);
                r.put("name", RING_NAMES[i]);
                r.put("size", RING_SIZES[i]);
                r.put("xp", Milestones.DRAGON_XP[i]);
                rings.add(r);
            }
            m.put("rings", rings);
            m.put("skills", skills);
            json = new Gson().toJson(m);
        }
        return json;
    }
}
