package io.mopesbox.Utils;
//here down put the path, just copy the pig one
import io.mopesbox.Animals.Tier4.SeaHorse;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier10.Tiger;
import io.mopesbox.Animals.Tier10.Bear;
import io.mopesbox.Animals.Tier10.GobiBear;
import io.mopesbox.Animals.Tier10.Hyena;
import io.mopesbox.Animals.Tier10.Pelican;
import io.mopesbox.Animals.Tier10.SwordFish;
import io.mopesbox.Animals.Tier10.Wolf;
import io.mopesbox.Animals.Tier11.Vulture;
import io.mopesbox.Animals.Tier11.Crocodile;
import io.mopesbox.Animals.Tier4.Seal;
import io.mopesbox.Animals.Tier11.Falcon;
import io.mopesbox.Animals.Tier11.Lion;
import io.mopesbox.Animals.Tier11.Octopus;
import io.mopesbox.Animals.Tier11.PolarBear;
import io.mopesbox.Animals.Tier2.Rabbit;
import io.mopesbox.Animals.Tier2.Hare;
import io.mopesbox.Animals.Tier2.Pigeon;
import io.mopesbox.Animals.Tier2.DesertChipmunk;
import io.mopesbox.Animals.Tier3.Chicken;
import io.mopesbox.Animals.Tier3.Meerkat;
import io.mopesbox.Animals.Tier3.Crab;
import io.mopesbox.Animals.Tier3.Penguin;
import io.mopesbox.Animals.Tier3.Duckling;
import io.mopesbox.Animals.Tier3.Mole;
import io.mopesbox.Animals.Tier4.Pig;
import io.mopesbox.Animals.Tier5.Squid;
import io.mopesbox.Animals.Tier4.Armadillo;
import io.mopesbox.Animals.Tier4.OstrishBaby;
import io.mopesbox.Animals.Tier4.Woodpecker;
import io.mopesbox.Animals.Tier2.Trout;
import io.mopesbox.Animals.Tier13.Hippo;
import io.mopesbox.Animals.Tier12.Bison;
import io.mopesbox.Animals.Tier12.Eagle;
import io.mopesbox.Animals.Tier12.Markhor;
import io.mopesbox.Animals.Tier12.Wolverine;
import io.mopesbox.Animals.Tier12.Rhino;
import io.mopesbox.Animals.Tier12.Shark;
import io.mopesbox.Animals.Tier13.KillerWhale;
import io.mopesbox.Animals.Tier13.KomodoDragon;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier13.BoaConstrictor;
import io.mopesbox.Animals.Tier13.SabertoothTiger;
import io.mopesbox.Animals.Tier14.BlackwidowSpider;
import io.mopesbox.Animals.Tier14.BlueWhale;
import io.mopesbox.Animals.Tier14.Elephant;
import io.mopesbox.Animals.Tier14.GiantSpider;
import io.mopesbox.Animals.Tier14.Cassowary;
import io.mopesbox.Animals.Tier14.Mammoth;
import io.mopesbox.Animals.Tier9.SnowLeopard;
import io.mopesbox.Animals.Tier15.Bigfoot;
import io.mopesbox.Animals.Tier15.Dragon;
import io.mopesbox.Animals.Tier15.KingCrab;
import io.mopesbox.Animals.Tier15.Kraken;
import io.mopesbox.Animals.Tier15.Phoenix;
import io.mopesbox.Animals.Tier15.Pterodactyl;
import io.mopesbox.Animals.Tier15.Santa;
import io.mopesbox.Animals.Tier15.Rudolph;
import io.mopesbox.Animals.Tier15.Snowman;
import io.mopesbox.Animals.Tier15.Trex;
import io.mopesbox.Animals.Tier15.Yeti;
import io.mopesbox.Animals.Tier16.DinoMonster;
import io.mopesbox.Animals.Tier16.GiantScorpion;
import io.mopesbox.Animals.Tier16.IceMonster;
import io.mopesbox.Animals.Tier16.LandMonster;
import io.mopesbox.Animals.Tier16.SeaMonster;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Animals.Tier5.BulletAnt;
import io.mopesbox.Animals.Tier5.Deer;
import io.mopesbox.Animals.Tier5.Flamingo;
import io.mopesbox.Animals.Tier5.Gazelle;
import io.mopesbox.Animals.Tier5.Reindeer;
import io.mopesbox.Animals.Tier6.ArcticFox;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Animals.Tier6.Fox;
import io.mopesbox.Animals.Tier6.Hedgehog;
import io.mopesbox.Animals.Tier6.Peacock;
import io.mopesbox.Animals.Tier7.Donkey;
import io.mopesbox.Animals.Tier7.Macaw;
import io.mopesbox.Animals.Tier7.Turtle;
import io.mopesbox.Animals.Tier7.Warthog;
import io.mopesbox.Animals.Tier7.Zebra;
import io.mopesbox.Animals.Tier7.Muskox;
import io.mopesbox.Animals.Tier8.Camel;
import io.mopesbox.Animals.Tier8.Cobra;
import io.mopesbox.Animals.Tier6.Duck;
import io.mopesbox.Animals.Tier6.FennecFox;
import io.mopesbox.Animals.Tier8.Snail;
import io.mopesbox.Animals.Tier8.Snowyowl;
import io.mopesbox.Animals.Tier8.Stingray;
import io.mopesbox.Animals.Tier8.Giraffe;
import io.mopesbox.Animals.Tier9.Cheetah;
import io.mopesbox.Animals.Tier9.Frog;
import io.mopesbox.Animals.Tier9.Gorilla;
import io.mopesbox.Animals.Tier9.Toucan;
import io.mopesbox.Animals.Tier9.PufferFish;
import io.mopesbox.Animals.Tier9.RattleSnake;
import io.mopesbox.Animals.Tier8.Walrus;

public enum AnimalType {
    NULL(Animal.class),
    MOUSE(Animal.class),
    RABBIT(Rabbit.class),
    PIG(Pig.class),
    FOX(Fox.class),
    DEER(Deer.class),
    MOLE(Mole.class),
    ZEBRA(Zebra.class),
    LION(Lion.class),
    BIGCAT(Cheetah.class),
    BEAR(Bear.class),
    CROC(Crocodile.class),
    RHINO(Rhino.class),
    HIPPO(Hippo.class),
    DRAGON(Dragon.class),
    SHRIMP(Animal.class),
    TROUT(Trout.class),
    CRAB(Crab.class),
    SQUID(Squid.class),
    SHARK(Shark.class),
    STINGRAY(Stingray.class),
    TURTLE(Turtle.class),
    SEAHORSE(SeaHorse.class),
    JELLYFISH(Animal.class),
    KRAKEN(Kraken.class),
    PUFFERFISH(PufferFish.class),
    KILLERWHALE(KillerWhale.class),
    SWORDFISH(SwordFish.class),
    GORILLA(Gorilla.class),
    OCTOPUS(Octopus.class),
    WOLF(Wolf.class),
    ARCTICHARE(Hare.class),
    YETI(Yeti.class),
    CHIPMUNK(Animal.class),
    MUSKOX(Muskox.class),
    PENGUIN(Penguin.class),
    POLARBEAR(PolarBear.class),
    SEAL(Seal.class),
    SNOWLEOPARD(SnowLeopard.class),
    WALRUS(Walrus.class),
    REINDEER(Reindeer.class),
    ARCTICFOX(ArcticFox.class),
    WOLVERINE(Wolverine.class),
    MAMMOTH(Mammoth.class),
    DONKEY(Donkey.class),
    SNAIL(Snail.class),
    BLACKDRAGON(BlackDragon.class),
    SABERTOOTHTIGER(SabertoothTiger.class),
    ELEPHANT(Elephant.class),
    BLUEWHALE(BlueWhale.class),
    // BLUEWHALE(Animal.class),
    COBRA(Cobra.class),
    BOACONSTRICTOR(BoaConstrictor.class),
    GIANTSPIDER(GiantSpider.class),
    TREX(Trex.class),
    TIGER(Tiger.class),
    GIRAFFE(Giraffe.class),
    EAGLE(Eagle.class),
    HEDGEHOG(Hedgehog.class),
    DUCK(Duck.class),
    DUCKLING(Duckling.class),
    LEMMING(Animal.class),
    KINGCRAB(KingCrab.class),
    FROG(Frog.class),
    OSTRICH(Ostrich.class),
    PELICAN(Pelican.class),
    FALCON(Falcon.class),
    SNOWYOWL(Snowyowl.class),
    HONEYBEE(Bee.class),
    PHOENIX(Phoenix.class),
    OSTRICHBABY(OstrishBaby.class),
    SEAMONSTER(SeaMonster.class),
    LANDMONSTER(LandMonster.class),
    ICEMONSTER(IceMonster.class),
    DINOMONSTER(DinoMonster.class),
    PIGEON(Pigeon.class),
    TOUCAN(Toucan.class),
    MACAW(Macaw.class),
    FLAMINGO(Flamingo.class),
    CASSOWARY(Cassowary.class),
    CHICKEN(Chicken.class),
    WOODPECKER(Woodpecker.class),
    PEACOCK(Peacock.class),
    KANGAROORAT(Animal.class),
    DESERTCHIPMUNK(DesertChipmunk.class),
    MEERKAT(Meerkat.class),
    GAZELLE(Gazelle.class),
    ARMADILLO(Armadillo.class),
    FENNECFOX(FennecFox.class),
    CAMEL(Camel.class),
    WARTHOG(Warthog.class),
    HYENA(Hyena.class),
    RATTLESNAKE(RattleSnake.class),
    VULTURE(Vulture.class),
    BISON(Bison.class),
    KOMODODRAGON(KomodoDragon.class),
    GIANTSCORPION(GiantScorpion.class),
    PTERODACTYL(Pterodactyl.class),
    GOBIBEAR(GobiBear.class),
    BLACKWIDOW(BlackwidowSpider.class),
    BULLETANT(BulletAnt.class),
    KINGDRAGON(KingDragon.class),
    SNOWMAN(Snowman.class),
    SANTA(Santa.class),
    RUDOLPH(Rudolph.class),
    DEATHWORM(Animal.class),
    MARKHOR(Markhor.class),
    BIGFOOT(Bigfoot.class),
    AIBOSS(Animal.class),
    RAVEN(Animal.class),
    KAKAPO(Animal.class),
    PHEASANT(Animal.class),
    WHITEGIRAFFE(Animal.class),
    TURKEY(Animal.class);

    private Class<? extends Animal> animalClass;

    AnimalType(Class<? extends Animal> animalClass) {
        this.animalClass = animalClass;
    }

    public Class<? extends Animal> getAnimalClass() {
        return animalClass;
    }

    public boolean hasCustomClass() {
        return !animalClass.equals(Animal.class);
    }
}
