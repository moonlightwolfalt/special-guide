package io.mopesbox.Milestones;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier16.DinoMonster;
import io.mopesbox.Animals.Tier16.GiantScorpion;
import io.mopesbox.Animals.Tier16.IceMonster;
import io.mopesbox.Animals.Tier16.LandMonster;
import io.mopesbox.Animals.Tier16.SeaMonster;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;

/**
 * Which animals have milestones, at what XP, and what each one gives.
 *
 * Black Dragon and King Dragon: three milestones (12.5m, 17.5m, 25m XP). Each one powers up
 * the dragon's abilities (the Black Dragon's fire volley; the King Dragon's fire stream and
 * tail slap) and opens the next ring of the skill tree.
 *
 * The 5m tier (Dino Monster, Land Monster, Sea Monster, Giant Scorpion, Ice Monster): one
 * "Titan" milestone at 7.25m XP.
 *
 * The badge images are made by tools/milestones/make-milestone-images.js into web/img/milestones/.
 */
public final class Milestones {
    public static final long[] DRAGON_XP = { 12_500_000L, 17_500_000L, 25_000_000L };
    public static final long[] TITAN_XP = { 7_250_000L };

    private Milestones() {}

    /** Short id of the animal's milestone track, or null when it has none. */
    public static String key(Animal a) {
        if (a instanceof BlackDragon) return "blackdragon";
        if (a instanceof KingDragon) return "kingdragon";
        if (a instanceof DinoMonster) return "dinomonster";
        if (a instanceof LandMonster) return "landmonster";
        if (a instanceof SeaMonster) return "seamonster";
        if (a instanceof GiantScorpion) return "giantscorpion";
        if (a instanceof IceMonster) return "icemonster";
        return null;
    }

    public static boolean isDragon(String key) {
        return "blackdragon".equals(key) || "kingdragon".equals(key);
    }

    /** Which skill tree the animal uses, or null when it has none. */
    public static String track(String key) {
        if (key == null) return null;
        return isDragon(key) ? SkillTree.DRAGON : SkillTree.TITAN;
    }

    public static long[] thresholds(String key) {
        if (key == null) return new long[0];
        return isDragon(key) ? DRAGON_XP : TITAN_XP;
    }

    public static String animalName(String key) {
        switch (key) {
            case "blackdragon": return "Black Dragon";
            case "kingdragon": return "King Dragon";
            case "dinomonster": return "Dino Monster";
            case "landmonster": return "Land Monster";
            case "seamonster": return "Sea Monster";
            case "giantscorpion": return "Giant Scorpion";
            case "icemonster": return "Ice Monster";
            default: return key;
        }
    }

    public static String title(String key, int level) {
        String[] roman = { "", "I", "II", "III" };
        switch (key) {
            case "blackdragon":
                return new String[] { "", "Kindled Volley", "Scorching Volley", "Cataclysm Volley" }[level];
            case "kingdragon":
                return "Flame Sovereign " + roman[level];
            default:
                return "Titan Milestone";
        }
    }

    /** What reaching the milestone gives, one line each. */
    public static List<String> lines(String key, int level) {
        List<String> l = new ArrayList<>();
        if ("blackdragon".equals(key)) {
            if (level <= 2) l.add("Ability slot " + level + " of 2: choose Wing Slash, Wingspan or Shoot Meteor");
            if (level == 1) l.add("Fire volley: +1 fireball");
            if (level == 2) l.add("Fire volley recharges 20% faster");
            if (level == 3) {
                l.add("Fire volley costs half the lava");
                l.add("Bigger fireballs that fly 20% further");
            }
        } else if ("kingdragon".equals(key)) {
            l.add("Defensive: " + io.mopesbox.Animals.Tier17.DragonAbilities.PASSIVE_NAMES[level - 1] + " ("
                    + io.mopesbox.Animals.Tier17.DragonAbilities.PASSIVE_TEXT[level - 1] + ")");
            l.add("Offensive: " + io.mopesbox.Animals.Tier17.DragonAbilities.NAMES[level + 2] + " (key "
                    + io.mopesbox.Animals.Tier17.DragonAbilities.KEYS[level + 2] + ")");
            if (level == 1) {
                l.add("Fire stream lasts 30% longer");
                l.add("Tail slap recharges in 11s (was 15s)");
            }
            if (level == 2) {
                l.add("Fire stream recharges in 5s (was 7s)");
                l.add("Tail slap hits 25% harder");
            }
            if (level == 3) {
                l.add("Fire stream costs half the lava, with bigger fireballs");
                l.add("Tail slap is 30% bigger and recharges in 8s");
            }
        } else {
            l.add("Ability recharges 25% faster");
            l.add("+50 max health, +10 bite damage");
        }
        if (isDragon(key))
            l.add("Skill tree " + SkillTree.RING_NAMES[level - 1] + " opens: pick 1 of " + SkillTree.RING_SIZES[level - 1] + " (press T)");
        else
            l.add("Pick 1 of " + SkillTree.TITAN_SIZE + " Titan skills (press T)");
        return l;
    }

    public static String image(String key, int level) {
        return "img/milestones/" + key + "_" + level + ".png";
    }
}
