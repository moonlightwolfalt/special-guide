package io.mopesbox.Milestones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One node of the dragon skill tree.
 *
 * A "rework" changes how the dragon's fire ability (and body) works and is always on.
 * A "new" skill is an extra ability cast with Q, with its own cooldown.
 */
public class Skill {
    public enum Kind { REWORK, NEW }

    public final int id;
    public final String track; // "dragon" (Black/King Dragon) or "titan" (the 5m tier)
    public final int ring; // 1 = outer (45 skills), 2 = middle (25), 3 = inner (10)
    public final Kind kind;
    public final String name;

    // --- rework stats (added together over every pick) ---
    int volley;          // extra fireballs per shot
    double spread;       // fireball fan width, +0.4 = 40% wider
    int size;            // fireball radius, in pixels
    double travel;       // how far fireballs fly, +0.25 = 25% further
    int burn;            // seconds a hit animal burns
    double recharge;     // ability recharge, -0.12 = 12% faster
    double lava;         // lava each shot costs, -0.25 = 25% cheaper
    int hp;              // max health
    int bite;            // bite damage
    int regen;           // health healed each second
    int lavaRegen;       // lava refilled each second

    // --- new ability effects (all happen when Q is pressed) ---
    int cooldown;        // seconds
    int nova;            // fireballs in a full circle
    int rear;            // fireballs fired backwards
    int field;           // fires placed in a ring around the dragon
    int gust;            // range of a wing gust that pushes animals away
    int dash;            // strength of a forward dash (1-3)
    int shield;          // seconds of immunity
    int heal;            // % of max health healed
    int lavaFill;        // % of lava refilled

    Skill(int id, String track, int ring, Kind kind, String name) {
        this.id = id;
        this.track = track;
        this.ring = ring;
        this.kind = kind;
        this.name = name;
    }

    // fluent setters used by SkillTree
    Skill volley(int v) { volley = v; return this; }
    Skill spread(double v) { spread = v; return this; }
    Skill size(int v) { size = v; return this; }
    Skill travel(double v) { travel = v; return this; }
    Skill burn(int v) { burn = v; return this; }
    Skill recharge(double v) { recharge = v; return this; }
    Skill lava(double v) { lava = v; return this; }
    Skill hp(int v) { hp = v; return this; }
    Skill bite(int v) { bite = v; return this; }
    Skill regen(int v) { regen = v; return this; }
    Skill lavaRegen(int v) { lavaRegen = v; return this; }
    Skill cd(int v) { cooldown = v; return this; }
    Skill nova(int v) { nova = v; return this; }
    Skill rear(int v) { rear = v; return this; }
    Skill field(int v) { field = v; return this; }
    Skill gust(int v) { gust = v; return this; }
    Skill dash(int v) { dash = v; return this; }
    Skill shield(int v) { shield = v; return this; }
    Skill heal(int v) { heal = v; return this; }
    Skill lavaFill(int v) { lavaFill = v; return this; }

    private static String pct(double v) {
        return (v > 0 ? "+" : "") + Math.round(v * 100) + "%";
    }

    /** Plain-text lines describing what the skill does, for the tree's tooltip. */
    public List<String> describe() {
        List<String> l = new ArrayList<>();
        if (volley != 0) l.add((volley > 0 ? "+" : "") + volley + " fireball" + (Math.abs(volley) == 1 ? "" : "s") + " per shot");
        if (spread != 0) l.add(pct(spread) + " fireball spread");
        if (size != 0) l.add((size > 0 ? "+" : "") + size + " fireball size");
        if (travel != 0) l.add(pct(travel) + " fireball range");
        if (burn != 0) l.add("+" + burn + "s burn on hit");
        if (recharge != 0) l.add(pct(recharge) + " ability recharge time");
        if (lava != 0) l.add(pct(lava) + " lava cost");
        if (hp != 0) l.add("+" + hp + " max health");
        if (bite != 0) l.add("+" + bite + " bite damage");
        if (regen != 0) l.add("Heal " + regen + " health every second");
        if (lavaRegen != 0) l.add("Refill " + lavaRegen + " lava every second");
        if (nova != 0) l.add("Bursts " + nova + " fireballs in a full circle");
        if (rear != 0) l.add("Fires " + rear + " fireballs behind you");
        if (field != 0) l.add("Sets " + field + " fires in a ring around you");
        if (gust != 0) l.add("Shockwave pushes and stuns animals within " + gust);
        if (dash != 0) l.add("Dashes forward (" + new String[] { "", "short", "medium", "long" }[Math.min(3, dash)] + ")");
        if (shield != 0) l.add("Immune to damage for " + shield + "s");
        if (heal != 0) l.add("Heals " + heal + "% of max health");
        if (lavaFill != 0) l.add("Refills " + lavaFill + "% lava");
        if (kind == Kind.NEW) l.add("Cooldown: " + cooldown + "s (press Q)");
        return l;
    }

    /** What the client gets for drawing the tree. */
    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("ring", ring);
        m.put("kind", kind == Kind.REWORK ? "rework" : "new");
        m.put("name", name);
        m.put("lines", describe());
        m.put("icon", icon());
        if (kind == Kind.NEW) m.put("cd", cooldown);
        return m;
    }

    /** Which glyph the client draws in the node: the skill's strongest effect. */
    public String icon() {
        if (kind == Kind.NEW) {
            if (nova != 0) return "nova";
            if (rear != 0) return "rear";
            if (field != 0) return "field";
            if (gust != 0) return "gust";
            if (dash != 0) return "dash";
            if (shield != 0) return "shield";
            if (heal != 0) return "heal";
            return "lava";
        }
        if (volley != 0) return "volley";
        if (recharge < 0) return "recharge";
        if (size != 0 || travel != 0 || spread != 0) return "flame";
        if (burn != 0) return "burn";
        if (lava != 0 || lavaRegen != 0) return "lava";
        if (bite != 0) return "bite";
        return "heart";
    }
}
