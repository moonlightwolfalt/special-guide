package io.mopesbox.Animals.Info;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Rares {

    public static Rare KingDragon = new Rare(Animals.KingDragon, 0.002, 0, 500, 250);
    public static Rare WhiteRhino = new Rare(Animals.Rhino, 0.02, 1, 150, 50);
    public static Rare GoldenEagle = new Rare(Animals.Eagle, 0.04, 1, 200, 60);
    public static Rare Shahbaz = new Rare(Animals.Eagle, 0.002, 2, 250, 70);
    public static Rare GoldenShahbaz = new Rare(Animals.Eagle, 0.003, 3, 300, 80); // потом поменять эфф
    public static Rare PakiVulture = new Rare(Animals.Vulture, 0.001, 1, 200, 60);
    public static Rare PakiToucan = new Rare(Animals.Toucan, 0.002, 5, 300, 100);
    public static Rare LavaToucan = new Rare(Animals.Toucan, 0.11, 4, 100, 50);
    public static Rare IdkToucan = new Rare(Animals.Toucan, 0.36, 3, 50, 50);
    public static Rare IdkToucan2 = new Rare(Animals.Toucan, 0.36, 2, 50, 50);
    public static Rare IdkToucan3 = new Rare(Animals.Toucan, 0.36, 1, 50, 50);
    public static Rare BlackRhino = new Rare(Animals.Rhino, 0.02, 2, 250, 80);
    public static Rare WhiteDove = new Rare(Animals.Pigeon, 0.1, 1, 10, 10);
    public static Rare PinkyPig = new Rare(Animals.Pig, 0.196, 1, 15, 10);
    public static Rare StinkyPig = new Rare(Animals.Pig, 0.004, 2, 30, 15);
    public static Rare Doe = new Rare(Animals.Deer, 0.2, 1, 5, 5);
    public static Rare MarshDeer = new Rare(Animals.Deer, 0.1, 2, 5, 5);
    public static Rare ArcticDeer = new Rare(Animals.Reindeer, 0.1, 1, 5, 5);
    public static Rare BlackCat = new Rare(Animals.Cheetah, 0.0001, 4, 20, 20);
    public static Rare cat1 = new Rare(Animals.Cheetah, 0.2, 1, 50, 50);
    public static Rare cat2 = new Rare(Animals.Cheetah, 0.2, 2, 50, 50);
    public static Rare cat3 = new Rare(Animals.Cheetah, 0.05, 3, 50, 50);
    // public static Rare FireAnt = new Rare(Animals.BulletAnt, 0.02, 1, 100, 70);
    // public static Rare EaglePufferFish = new Rare(Animals.PufferFish, 0.02, 1, 50, 50);
    public static Rare DemonPufferFish = new Rare(Animals.PufferFish, 0.005, 2, 100, 70);
    public static Rare Jackass = new Rare(Animals.Donkey, 0.01, 1, 50, 50);
    public static Rare BlueMacaw = new Rare(Animals.Macaw, 0.1, 1, 50, 50);
    public static Rare PakiMacaw = new Rare(Animals.Macaw, 0.001, 2, 300, 100);
    public static Rare Lion1 = new Rare(Animals.Lion, 0.01, 1, 25, 25);
    public static Rare Lion2 = new Rare(Animals.Lion, 0.01, 2, 25, 25);
    public static Rare Lion3 = new Rare(Animals.Lion, 0.01, 3, 25, 25);
    public static Rare Lion4 = new Rare(Animals.Lion, 0.01, 4, 25, 25);
    public static Rare Lion5 = new Rare(Animals.Lion, 0.01, 5, 25, 25);
    public static Rare WhiteTiger = new Rare(Animals.Tiger, 0.001, 1, 25, 25); // TIGER ABILITY WHEN - NEVER
    public static Rare AquaYeti = new Rare(Animals.Yeti, 0.009, 3, 50, 90);

    // public static Rare

    // very dangerous shenanigans
    public static List<AnimalInfo> byTier(int tier, boolean alls) {
        try {
            List<AnimalInfo> rares = new ArrayList<>();
            List<Integer> types = new ArrayList<>();
            Field[] fields = Rares.class.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    Class<?> type = f.getType();
                    Rare rev = (Rare) f.get(type);
                    boolean rand = (Math.random() <= (rev.chance)) ? true : false;
                    if (rev.animal.getTier() == tier && ((rand && !types.contains(rev.animal.getAnimalType())) || alls)) {
                        AnimalInfo dbl = new AnimalInfo(rev.animal);
                        types.add(rev.animal.getAnimalType());
                        dbl.setSkin(rev.skin);
                        rares.add(dbl);
                    }
                }
            }
            return rares;
        } catch (IllegalAccessException e) {
            return new ArrayList<>();
        }
    }
}
