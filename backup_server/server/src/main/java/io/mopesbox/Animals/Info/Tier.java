package io.mopesbox.Animals.Info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Tier {
    ONE(1, new AnimalInfo[] { Animals.Mouse, Animals.Shrimp, Animals.Chipmunk, Animals.KangarooRat }, 12, 3.768, 0, 400,
            220, 20),
    TWO(2, new AnimalInfo[] { Animals.Rabbit, Animals.Pigeon, Animals.Trout, Animals.ArcticHare,
            Animals.DesertChipmunk }, 14, 3.5, 50, 580, 240, 22),
    THREE(3, new AnimalInfo[] { Animals.Mole, Animals.Chicken, Animals.Crab, Animals.Meerkat, Animals.Penguin }, 16,
            3.4, 150, 580, 240, 22),
    FOUR(4, new AnimalInfo[] { Animals.Pig, Animals.Woodpecker, Animals.Seal, Animals.SeaHorse, Animals.Armadillo }, 16,
            3.23, 500, 620, 280, 22),
    FIVE(5, new AnimalInfo[] { Animals.Deer, Animals.Squid, Animals.Flamingo, Animals.Reindeer, Animals.Gazelle }, 18,
            3.05, 1000, 580, 280, 23),
    SIX(6, new AnimalInfo[] { Animals.Fox, Animals.Hedgehog, Animals.Peacock, Animals.JellyFish,
            Animals.ArcticFox,
            Animals.FennecFox }, 18, 2.80, 2000, 665, 280, 24),
    SEVEN(7, new AnimalInfo[] { Animals.Zebra, Animals.Donkey, Animals.Macaw, Animals.Turtle, Animals.Muskox,
            Animals.Warthog }, 19, 2.70, 4000, 820, 300, 24),
    EIGHT(8, new AnimalInfo[] { Animals.Cobra, Animals.Giraffe, Animals.Stingray, Animals.Walrus, Animals.SnowyOwl,
            Animals.Camel }, 19, 2.60, 8000, 840, 320, 24),
    NINE(9, new AnimalInfo[] { Animals.Cheetah, Animals.Gorilla, Animals.Toucan, Animals.SnowLeopard,
            Animals.PufferFish, Animals.RattleSnake }, 19, 2.50, 16000, 890, 325, 26),
    TEN(10, new AnimalInfo[] { Animals.Bear, Animals.Tiger, Animals.SwordFish, Animals.Pelican, Animals.Wolf,
            Animals.GobiBear, Animals.Hyena }, 20, 2.35, 32000, 890, 325, 26),
    ELEVEN(11,
            new AnimalInfo[] { Animals.Crocodile, Animals.Lion, Animals.Falcon, Animals.Octopus, Animals.PolarBear,
                    Animals.Vulture },
            21, 2.25, 64000, 900, 330, 26),
    TWELVE(12,
            new AnimalInfo[] { Animals.Rhino, Animals.Eagle, Animals.Shark, Animals.Wolverine, Animals.Markhor,
                    Animals.Bison },
            22, 2.20, 120000, 1000, 350, 28),
    THIRTEEN(13,
            new AnimalInfo[] { Animals.Hippo, Animals.BoaConstrictor, Animals.Ostrich, Animals.KillerWhale,
                    Animals.SabertoothTiger, Animals.KomodoDragon },
            23, 2.15, 250000, 1150, 370, 29),
    FOURTEEN(14,
            new AnimalInfo[] { Animals.Elephant, Animals.GiantSpider, Animals.Cassowary, Animals.Mammoth,
                    Animals.BlueWhale, Animals.BlackWidow },
            38, 2.0, 500000, 1190, 420, 42),
    FIFTEEN(15,
            new AnimalInfo[] { Animals.Dragon, Animals.Trex, Animals.Kraken, Animals.KingCrab, Animals.Yeti,
                    Animals.Phoenix, Animals.Pterodactyl, Animals.BigFoot },
            new AnimalInfo[] { Animals.BigFoot, Animals.Snowman }, 43, 1.9, 1000000, 1330, 450, 45),
    SIXTEEN(16,
            new AnimalInfo[] { Animals.DinoMonster, Animals.LandMonster, Animals.SeaMonster, Animals.GiantScorpion,
                    Animals.IceMonster },
            44, 1.8, 5000000, 1400, 450, 52),
    SEVENTEEN(17, new AnimalInfo[] { Animals.BlackDragon, Animals.KingDragon },
            new AnimalInfo[] { Animals.KingDragon },
            100, 1.2, 10000000, 1580, 1200, 120);
    
            int value;
    AnimalInfo[] exclusiveAnimalInfos;
    AnimalInfo[] animalInfo;
    AnimalInfo[] aniInfo;

    int baseRadius;
    int maxRadius;
    double baseZoom;
    int startXP;
    int width = -1;
    int height = -1;

    Tier(int value, AnimalInfo[] animalInfos, int baseRadius, double baseZoom, int startXP, int maxRadius) {
        this.value = value;
        this.maxRadius = maxRadius;
        this.animalInfo = animalInfos;
        this.baseRadius = baseRadius;
        this.baseZoom = baseZoom;
        this.startXP = startXP;
    }

    Tier(int value, AnimalInfo[] animalInfos, AnimalInfo[] exclusiveAnimalInfos, int baseRadius, double baseZoom,
            int startXP, int width, int height, int maxRadius) {
        this.value = value;
        this.animalInfo = animalInfos;
        this.maxRadius = maxRadius;
        this.exclusiveAnimalInfos = exclusiveAnimalInfos;
        this.baseRadius = baseRadius;
        this.baseZoom = baseZoom;
        this.startXP = startXP;
        this.width = (int) Math.round(width * 1.3);
        this.height = (int) Math.round(height * 1.3);
    }

    Tier(int value, AnimalInfo[] animalInfos, AnimalInfo[] exclusiveAnimalInfos, AnimalInfo[] aniInfo, int baseRadius,
            double baseZoom, int startXP, int width, int height, int maxRadius) {
        this.value = value;
        this.animalInfo = animalInfos;
        this.maxRadius = maxRadius;
        this.exclusiveAnimalInfos = exclusiveAnimalInfos;
        this.baseRadius = baseRadius;
        this.baseZoom = baseZoom;
        this.startXP = startXP;
        this.aniInfo = aniInfo;
        this.width = (int) Math.round(width * 1.3);
        this.height = (int) Math.round(height * 1.3);
    }

    Tier(int value, AnimalInfo[] animalInfos, int baseRadius, double baseZoom, int startXP, int width, int height,
            int maxRadius) {
        this.value = value;
        this.animalInfo = animalInfos;
        this.baseRadius = baseRadius;
        this.maxRadius = maxRadius;
        this.baseZoom = baseZoom;
        this.startXP = startXP;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public int getHeight() {
        return height;
    }

    public int getValue() {
        return value;
    }

    public List<AnimalInfo> getAniSkin() {
        return Arrays.asList(aniInfo);
    }

    public List<AnimalInfo> getAnimalInfo() {
        List<AnimalInfo> infos = Arrays.asList(animalInfo);
        List<AnimalInfo> newInfos = new ArrayList<>();
        for (AnimalInfo a : infos) {
            AnimalInfo newa = Animals.byID(a.getAnimalType());
            newInfos.add(newa);
        }
        if (exclusiveAnimalInfos != null && exclusiveAnimalInfos.length > 0) {
            List<AnimalInfo> remInfos = new ArrayList<>();
            for (AnimalInfo a : newInfos) {
                for (AnimalInfo ao : exclusiveAnimalInfos) {
                    if (a.getAnimalType() == ao.getAnimalType())
                        remInfos.add(a);
                }
            }
            newInfos.removeAll(remInfos);
        }
        return newInfos;
    }

    public static Tier byOrdinal(int ord) {
        for (Tier m : Tier.values()) {
            if (m.value == ord) {
                return m;
            }
        }
        return null;
    }

    public int getStartXP() {
        return startXP;
    }

    public int getPreviousXP() {
        for (Tier m : Tier.values()) {
            if (m.value == value - 1) {
                return m.getStartXP();
            }
        }
        return 0;
    }

    public int getUpgradeXP() {
        for (Tier m : Tier.values()) {
            if (m.value == value + 1) {
                return m.getStartXP();
            }
        }
        return 40000000;
    }

    public int getUpgradeRadius() {
        for (Tier m : Tier.values()) {
            if (m.value == value + 1) {
                return m.getBaseRadius() == this.getBaseRadius() ? this.getBaseRadius() + 10 : m.getBaseRadius();
            }
        }
        return 120;
    }

    public int getBaseRadius() {
        return baseRadius;
    }

    public double getBaseZoom() {
        return baseZoom;
    }

    public int getNormal(int xp) {
        int a = 1;
        for (Tier m : Tier.values()) {
            if (m.startXP < xp) {
                a = m.value;
            }
        }
        return a;
    }

    public int getUpgraded(int xp) {
        int a = 1;
        for (Tier m : Tier.values()) {
            if (m.startXP <= xp) {
                a = m.value;
            }
        }
        return a;
    }

    public static Tier getTierByMass(double mass) {
        for (Tier tier : Tier.values()) {
            if (tier.value == 17 && mass >= tier.startXP) {
                return tier;
            }
            if (mass >= tier.startXP && mass < tier.getUpgradeXP()) {
                return tier;
            }
        }
        return null;
    }

}
