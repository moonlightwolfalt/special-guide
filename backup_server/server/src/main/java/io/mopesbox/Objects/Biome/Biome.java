package io.mopesbox.Objects.Biome;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.AI.AIFrog;
import io.mopesbox.Client.AI.AISnail;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Objects.ETC.PumpkinBall;
import io.mopesbox.Objects.Eatable.AloeVera;
import io.mopesbox.Objects.Eatable.BeeHive;
import io.mopesbox.Objects.Eatable.Cactus;
import io.mopesbox.Objects.Eatable.DateFruit;
import io.mopesbox.Objects.Eatable.Healstone;
import io.mopesbox.Objects.Eatable.Meat;
import io.mopesbox.Objects.Eatable.Mushroom;
import io.mopesbox.Objects.Eatable.MushroomBush;
import io.mopesbox.Objects.Eatable.RedMushroom;
import io.mopesbox.Objects.Eatable.SeaWeed;
import io.mopesbox.Objects.Eatable.Kelp;
import io.mopesbox.Objects.Eatable.StarFish;
import io.mopesbox.Objects.Eatable.Clam;
import io.mopesbox.Objects.Eatable.ConchShell;
import io.mopesbox.Objects.Static.AntHill;
import io.mopesbox.Objects.Static.Berryspot;
import io.mopesbox.Objects.Static.DeathLake;
import io.mopesbox.Objects.Static.HidingBush;
import io.mopesbox.Objects.Static.ArcticBush;
import io.mopesbox.Objects.Static.Hill;
import io.mopesbox.Objects.Static.IsLand;
import io.mopesbox.Objects.Static.Hole;
import io.mopesbox.Objects.Static.Lake;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Objects.Static.Mudspot;
import io.mopesbox.Objects.Static.Oasis;
import io.mopesbox.Objects.Static.PlanktonBush;
import io.mopesbox.Objects.Static.Rock;
import io.mopesbox.Objects.Static.Tree;
import io.mopesbox.Objects.Static.Waterspot;
import io.mopesbox.Objects.Static.FlyTrap;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Biome extends Rectangle {
    protected Room room;
    public ArrayList<GameObject> objects;
    public List<LavaLake> lavaLakes = new ArrayList<>();
    public int mushrooms = 0;
    public int healstones = 0;
    public int redmushrooms = 0;
    public int cactuses = 0;
    public int datefruits = 0;
    public int aloevera = 0;
    public int mushroombushs = 0;
    public int beehives = 0;
    public int snails = 0;
    public int ducks = 0;
    public int frogs = 0;
    public int seaweeds = 0;
    public int kelps = 0;
    public int starfishs = 0;
    public int clams = 0;
    public int conchshells = 0;
    public int pumking = 0;
    public int goldenpumking = 0;

    public Biome(int id, int type, double x, double y, double width, double height, int biome, Room room) {

        super(id, x, y, width, height, type);
        this.setBiome(biome);
        this.room = room;

        this.objects = new ArrayList<>();

        this.preSpawn();
        this.spawn();
        this.postSpawn();
    }

    @Override
    public void onCollision(GameObject object) {
        if (object.isMovable()) {
            if (object instanceof Animal) {
                if (!((Animal) object).ignoreBiomes) {
                    int abobus = 0;
                    for (Map.Entry<Integer, GameObject> entry : object.getCollidedList().entrySet()) {
                        GameObject a = entry.getValue();
                        if (a instanceof VolcanoBiome) {
                            abobus++;
                        }
                        if (abobus > 0) {
                            object.setBiome(BiomeType.VOLCANO.ordinal());
                            ((Animal) object).flag_inLava = true;
                        } else {
                            object.setBiome(this.getBiome());
                            if (((Animal) object).flag_inLava)
                                ((Animal) object).flag_inLava = false;
                        }

                    }
                }
            } else
                object.setBiome(this.getBiome());
        }
    }

    public void spawnFlytraps(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 30;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            FlyTrap trap = new FlyTrap(this.room.getID(), x, y, this.room);
            this.room.addObj(trap);
        }
    }

    public void spawnLavaLakes(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 150, maxX - 150);
            double y = Utils.randomDouble(minY + 150, maxY - 150);
            LavaLake lavaLake = new LavaLake(this.room.getID(), x, y, 150, this.room);
            this.room.addObj(lavaLake);
            lavaLakes.add(lavaLake);
        }
    }

    public void preSpawn() {
        // Main.log.info("Started generating biome: " +
        // this.getClass().getSimpleName().toUpperCase());
    }

    public void postSpawn() {
        // Main.log.info("Finished generating biome: " +
        // this.getClass().getSimpleName().toUpperCase());
    }

    // overriden
    public void spawn() {

    }

    // method that used by many biome classes

    public void spawnHills(int amount) {

        // /*
        // * attempting to make this even better!
        // * spawn hill groups
        // *
        // */
        int minX = (int) (this.getX() - this.getWidth() / 2) + 20;
        int minY = (int) (this.getY() - this.getHeight() / 2) + 20;

        int maxX = (int) (minX + this.getWidth()) - 20;
        int maxY = (int) (minY + this.getHeight()) - 20;

        // for (int i = 0; i < amount; i++) {
        // boolean canSpawn = false;
        // ArrayList<GameObject> objBunch = new ArrayList<>();
        // while (!canSpawn) {
        // int rad = Utils.randomInt(70, 100);
        // int x = Utils.randomInt(minX, maxX);
        // int y = Utils.randomInt(minY, maxY);

        // for (int j = 0; j < (i % 2 + 1); j++) {
        // double angle = (Math.random()) * (Math.PI * 2);
        // double distance = 40;

        // int tempRadius = j > 0 ? rad = Utils.randomInt(70, 100) : rad;
        // if (Math.floor(tempRadius / 2) > 20)
        // distance = Utils.randomInt(20, tempRadius / 2);
        // int addX = (int) (j > 0 ? Math.cos(angle) * (distance) : 0);
        // int addY = (int) (j > 0 ? Math.sin(angle) * (distance) : 0);

        // x += addX + tempRadius * 2;

        // y += addY + tempRadius * 2;

        // Hill tempHill = new Hill(this.room.getID(), x, y, tempRadius,
        // this.getBiome());

        // objBunch.add(tempHill);

        // }

        // if (Math.random() < 0.25) {

        // int rockRad = Utils.randomInt(70, 100);
        // double rockAngle = (Math.random()) * (Math.PI * 2);

        // double rockDistance = Utils.randomInt((int) Math.floor(rockRad / 2),
        // (int) Math.floor(rockRad / 1.2));

        // int rockX = (int) (x + Math.cos(rockAngle) * (rockDistance));
        // int rockY = (int) (y + Math.sin(rockAngle) * (rockDistance));

        // Rock rock = new Rock(this.room.getID(), rockX, rockY, rockRad,
        // this.getBiome());
        // objBunch.add(rock);
        // }
        // if (this.objects.size() <= 0) {
        // for (GameObject o : objBunch) {
        // this.objects.add(o);
        // this.room.addObj(o);
        // }
        // canSpawn = true;

        // } else {
        // ArrayList<GameObject> checkObjects = new ArrayList<>();

        // for (GameObject obj : this.objects) {
        // if (obj instanceof Hill || obj instanceof Rock)
        // checkObjects.add(obj);
        // }

        // boolean failedCheck = false;
        // for (GameObject o : checkObjects) {
        // // if (objBunch.contains(o))
        // // continue;
        // for (GameObject b : objBunch) {
        // if (Utils.testCollision(o, b)) {
        // // failedCheck = true;
        // /*
        // * Main.instance.log.info("Failed check for: " + o.toString() + " and " +
        // * b.toString());
        // */
        // }

        // }
        // }

        // if (failedCheck)
        // continue;

        // for (GameObject o : objBunch) {
        // this.objects.add(o);
        // this.room.addObj(o);
        // }
        // canSpawn = true;
        // }
        // }
        // }
        for (int i = 0; i < amount; i++) {
            int rad = Utils.randomInt(60, 80);
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Hill hill = new Hill(this.room.getID(), x, y, rad, this.getBiome());
            this.room.addObj(hill);
            if (Utils.randomBoolean()) {
                int radd = Utils.randomInt(40, 60);
                int ang = Utils.randomInt(0, 360);
                double ax = ((Math.cos(Math.toRadians(ang)) * (rad)));
                double ay = ((Math.sin(Math.toRadians(ang)) * (rad)));
                Rock rock = new Rock(this.room.getID(), x + ax, y + ay, radd,
                        this.getBiome());
                this.room.addObj(rock);
            }
        }
    }

    public void spawnWaterspot(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 80;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Waterspot spot = new Waterspot(this.room.getID(), x, y, rad, this.getBiome(), this.room);
            this.room.addObj(spot);
        }
    }

    public void spawnBerrybushes(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 50;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Berryspot spot = new Berryspot(this.room.getID(), x, y, rad, this.getBiome(), this.room);
            this.room.addObj(spot);
        }
    }

    public void pumking(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 80;

            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            PumpkinBall pump = new PumpkinBall(this.room.getID(), x, y, 0, this.room);
            this.room.pumpDots.add(pump);

            this.room.addObj(pump);

        }

        pumking += amount;

    }

    public void golden(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;
        for (int i = 0; i < amount; i++) {
            int rad = 80;

            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            PumpkinBall pump = new PumpkinBall(this.room.getID(), x, y, 1, this.room);
            this.room.pumpDots.add(pump);

            this.room.addObj(pump);
        }
        goldenpumking += amount;

    }

    public void spawnPlanktonBushs(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 50;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            PlanktonBush spot = new PlanktonBush(this.room.getID(), x, y, rad, this.getBiome(), this.room);
            this.room.addObj(spot);
        }
    }

    public void spawnHidingBushs(int amount, boolean spawnPears) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 70;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            HidingBush bush = new HidingBush(this.room.getID(), x, y, rad, this.getBiome(), this.room, spawnPears); // так
                                                                                                                    // надо
            // int id, double x, double y, int rad, int biome, int Room
            this.room.addObj(bush);
        }
    }

    public void spawnArcticBushs(int amount, boolean spawnNudes) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 70;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            ArcticBush bush = new ArcticBush(this.room.getID(), x, y, rad, this.getBiome(), this.room, spawnNudes); // так
                                                                                                                    // надо
            // int id, double x, double y, int rad, int biome, int Room
            this.room.addObj(bush);
        }
    }

    public void spawnAntHills(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 40;
        double maxY = (minY + this.getHeight()) - 40;

        for (int i = 0; i < amount; i++) {
            int rad = 40;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            AntHill antHill = new AntHill(this.room.getID(), x, y, rad, this.getBiome(), this.room);
            this.room.addObj(antHill);
        }
    }

    public void spawnLakesForLand(int amount, int mudsAmount) {
           int maps = Utils.randomInt(1, 5);
        // int maps = 5;
        // System.out.print(maps);
//3689 1828
          for (int i = 0; i < mudsAmount; i++) {
        double x = 0, y = 0;
        if(maps == 1){
        if(i == 0){
         x = 2471;
         y = 3979;
        }
        if(i == 1){
             x = 3689;
             y = 1828;
        }
        if(i == 2){
            x = 2438;
            y = 5673;
        }

            
        }else if(maps == 2){
        if(i == 0){
         x = 5849;
         y = 4396;
        }
        if(i == 1){
             x = 3968;
             y = 1986;
        }
        if(i == 2){
            x = 2417;
            y = 3943;
        }
        }else if(maps == 3){
        if(i == 0){
         x = 6072;
         y = 3389;
        }
        if(i == 1){
             x = 2485;
             y = 3725;
        }
        if(i == 2){
            x = 4855;
            y = 1952;
        }
        }else if(maps == 4){
        if(i == 0){
         x = 3634;
         y = 5701;
        }
        if(i == 1){
             x = 6187;
             y = 3528;
        }
        if(i == 2){
            x = 2270;
            y = 5680;
        }
        }
        else if(maps == 5){
        if(i == 0){
         x = 3726;
         y = 4490;
        }
        if(i == 1){
             x = 3063;
             y = 3424;
        }
        if(i == 2){
            x =  4103;
            y = 5686;
        }
        }
            
            int rad = 280;
            Mudspot spot = new Mudspot(this.room.getID(), x, y, rad, room);
            this.room.addObj(spot);
        }
        for (int i = 0; i < amount+1; i++) {

            double x = 0, y = 0;
        if(maps == 1){
        if(i == 0){
         x = 3255;
         y = 3203;
        }
        if(i == 1){
             x = 5310;
             y = 2041;
        }
        if(i == 2){
            x = 5466;
            y = 5569;
        }

            
        }else if(maps == 2){
        if(i == 0){
         x = 2284;
         y = 5589;
        }
        if(i == 1){
             x = 5986;
             y = 1968;
        }
        if(i == 2){
            x = 2315 ;
            y = 1910;
        }
        }else if(maps == 3){
        if(i == 0){
         x = 5714;
         y = 5679;
        }
        if(i == 1){
             x = 2645;
             y = 1912;
        }
        if(i == 2){
            x = 5560;
            y = 4381;
        }
        }else if(maps == 4){
        if(i == 0){
         x = 5475;
         y = 4212;
        }
        if(i == 1){
             x = 3025;
             y = 4114;
        }
        if(i == 2){
            x = 2451;
            y = 1922;
        }
        }
        else if(maps == 5){
        if(i == 0){
         x = 2434;
         y = 4313;
        }
        if(i == 1){
             x = 6010;
             y = 1907;
        }
        if(i == 2){
            x =  5999;
            y = 5683;
        }
        }

            if(i == amount && this.getBiome() != 2) {
                int rad = 280;
                DeathLake lake = new DeathLake(this.room.getID(), x, y, rad, this.room);
                lake.spawnHills();
                this.room.addObj(lake);
                this.room.deathlake = lake;
            } else {
                int rad = 280;
                Lake lake = new Lake(this.room.getID(), x, y, rad, this.room);
                this.room.addObj(lake);
                lake.spawnIslands();

            }
        }
    }
    public void spawnLakesForArctic(int amount) {
        int maps = Utils.randomInt(1, 3);
        if(this instanceof Arctic){
            ((Arctic)this).spawnArcticIce(maps);
        }
        // int maps = 3;
        for (int i = 0; i < amount; i++) {

            double x = 0, y = 0;
        if(maps == 1){
        if(i == 0){
         x = 1491;
         y = 648;
        }
        if(i == 1){
             x = 3558;
             y = 843;
        }
        if(i == 2){
            x = 5968;
            y = 707;
        }

            
        }else if(maps == 2){
        if(i == 0){
         x =  7718;
         y = 415;
        }
        if(i == 1){
             x = 6195;
             y = 621;
        }
        if(i == 2){
            x = 3533;
            y = 730;
        }
        }else if(maps == 3){
        if(i == 0){
         x = 2260;
         y = 473;
        }
        if(i == 1){
             x = 3824 ;
             y = 869;
        }
        if(i == 2){
            x = 5987;
            y = 863;
        }
    }
       

          
                int rad = 280;
                Lake lake = new Lake(this.room.getID(), x, y, rad, this.room);
                this.room.addObj(lake);
                lake.spawnIslands();

            
        
}
    }



    public void spawnOasis(int amount) {

        for (int i = 0; i < amount; i++) {
            int rad = 280;
        int maps = Utils.randomInt(1, 3);

            double x = 0, y = 0;
        if(maps == 1){
        if(i == 0){
         x = 4833;
         y = 6940;
        }
        if(i == 1){
             x = 5639;
             y = 6937;
        }
        if(i == 2){
            x = 2007;
            y = 6785;
        }

            
        }else if(maps == 2){
        if(i == 0){
         x =  1178;
         y = 6958;
        }
        if(i == 1){
             x = 2750;
             y = 6817;
        }
        if(i == 2){
            x = 6541;
            y = 7010;
        }
        }else if(maps == 3){
        if(i == 0){
         x = 7488;
         y = 6840;
        }
        if(i == 1){
             x = 5487 ;
             y = 7161;
        }
        if(i == 2){
            x = 3942;
            y = 6959;
        }
    }
    
       

            Oasis oasis = new Oasis(this.room.getID(), x, y, rad, this.room);
            this.room.addObj(oasis);
        }
    }



    public void spawnIsLand(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 300;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            IsLand island = new IsLand(this.room.getID(), x, y, rad, room, 1);
            this.room.addObj(island);
        }
    }

    public void spawnHealstones(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 24, maxX - 24);
            double y = Utils.randomDouble(minY + 24, maxY - 24);
            Healstone hs = new Healstone(this.room.getID(), x, y, 24, this.getBiome(), 100, room, null, 5);
            this.room.addObj(hs);
        }
        healstones += amount;
    }

    public void spawnMushrooms(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            Mushroom mushroom = new Mushroom(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(mushroom);
        }
        mushrooms += amount;
    }

    public void spawnSeaWeeds(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            SeaWeed seaweed = new SeaWeed(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(seaweed);
        }
        seaweeds += amount;
    }

    public void spawnKelps(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            Kelp kelp = new Kelp(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(kelp);
        }
        kelps += amount;
    }

    public void spawnStarFishs(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            StarFish starFish = new StarFish(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(starFish);
        }
        starfishs += amount;
    }

    public void spawnClams(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            Clam clam = new Clam(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(clam);
        }
        clams += amount;
    }

    public void spawnConchShells(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            ConchShell conchshell = new ConchShell(this.room.getID(), x, y, this, this.room);
            this.room.addObj(conchshell);
        }
        conchshells += amount;
    }

    public void spawnDateFruits(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 12, maxX - 12);
            double y = Utils.randomDouble(minY + 12, maxY - 12);
            DateFruit dateFruit = new DateFruit(this.room.getID(), x, y, this.room, this);
            this.room.addObj(dateFruit);
        }
        datefruits += amount;
    }

    public void spawnCactuses(int amount, int addRad) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 30;
        double maxY = (minY + this.getHeight()) - 30;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 30, maxX - 30);
            double y = Utils.randomDouble(minY + 30, maxY - 30);
            Cactus cactus = new Cactus(this.room.getID(), x, y, this, this.room, Utils.randomInt(15, 25));
            this.room.addObj(cactus);
        }
        cactuses += amount;
    }

    public void spawnMushroomBushs(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 45;
        double maxY = (minY + this.getHeight()) - 45;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 45, maxX - 45);
            double y = Utils.randomDouble(minY + 45, maxY - 45);
            MushroomBush mushroomBush = new MushroomBush(this.room.getID(), x, y, this, this.room);
            this.room.addObj(mushroomBush);
        }
        mushroombushs += amount;
    }

    public void spawnSnails(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 14;
        double maxY = (minY + this.getHeight()) - 14;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 14, maxX - 14);
            double y = Utils.randomDouble(minY + 14, maxY - 14);
            AISnail snail = new AISnail(this.room, this);
            snail.spawnAnimal(x, y);
            room.addAI(snail);
        }
        snails += amount;
    }

    public void spawnFrogs(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 12;
        double maxY = (minY + this.getHeight()) - 12;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 12, maxX - 12);
            double y = Utils.randomDouble(minY + 12, maxY - 12);
            AIFrog frog = new AIFrog(this.room, this);
            frog.spawnAnimal(x, y);
            room.addAI(frog);
        }
        frogs += amount;
    }

    public void spawnBeeHives(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 40;
        double maxY = (minY + this.getHeight()) - 40;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 40, maxX - 40);
            double y = Utils.randomDouble(minY + 40, maxY - 40);
            BeeHive beeHive = new BeeHive(this.room.getID(), x, y, this, this.room);
            this.room.addObj(beeHive);
        }
        beehives += amount;
    }

    public void spawnAloevera(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 30;
        double maxY = (minY + this.getHeight()) - 30;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 30, maxX - 30);
            double y = Utils.randomDouble(minY + 30, maxY - 30);
            AloeVera aloeverae = new AloeVera(this.room.getID(), x, y, this, this.room);
            this.room.addObj(aloeverae);
        }
        aloevera += amount;
    }

    public void spawnRedMushrooms(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 16, maxX - 16);
            double y = Utils.randomDouble(minY + 16, maxY - 16);
            RedMushroom mushroom = new RedMushroom(this.room.getID(), x, y, this.getBiome(), this.room, this);
            this.room.addObj(mushroom);
        }
        redmushrooms += amount;
    }

    public void spawnTrees(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = Utils.randomInt(80, 90);
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Tree tree = new Tree(this.room.getID(), x, y, rad, this.getBiome(), 0, Constants.CHRISTMAS);
            this.room.addObj(tree);
        }
    }

    public void spawnHoles(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int type = Utils.randomInt(0, 1);
            int rad = 0;
            if (type == 0)
                rad = 20;
            else
                rad = 30;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Hole hole = new Hole(this.room.getID(), x, y, type, false, this.getBiome());
            this.room.addObj(hole);
        }
    }

    public void spawnMeat(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = Utils.randomInt(60, 80);
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            Meat meat = new Meat(this.room.getID(), x, y, 10, 5000, this.room);
            this.room.addObj(meat);
        }
    }
}
