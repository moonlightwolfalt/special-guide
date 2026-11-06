package io.mopesbox.Utils;

import io.mopesbox.Constants;

import io.mopesbox.Objects.Biome.Arctic;
import io.mopesbox.Objects.Biome.Desert;
import io.mopesbox.Objects.Biome.Land;
import io.mopesbox.Objects.Biome.Ocean;
import io.mopesbox.Objects.Biome.VolcanoBiome;
import io.mopesbox.Objects.Eatable.Healstone;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.World.Room;
import net.openhft.affinity.Affinity;

public class FoodGenerator extends Thread {
    private Room room;
    private Timer spawnTimer = new Timer(4000);
    private Timer spawnRedMushroomsTimer = new Timer(8000);
    private Timer spawnSeaWeedsTimer = new Timer(8000);
    private Timer spawnKelpsTimer = new Timer(8000);
    private Timer spawnStarFishsTimer = new Timer(8000);
    private Timer spawnClamsTimer = new Timer(8000);
    private Timer spawnConchShellsTimer = new Timer(10000);
    private Timer spawnSnailsTimer = new Timer(4000);
    private Timer spawnBeehives = new Timer(8000);
    private Timer spawnCactusTimer = new Timer(2000);
    private Timer spawnDateFruitTimer = new Timer(4000);
    private Timer spawnHealstonesTimer = new Timer(12000);
    private Timer spawnLavaLakesTimer = new Timer(10000);
    private Timer spawnMushroomBushsTimer = new Timer(10000);
    private Timer spawnFrogsTimer = new Timer(30000);
      private Timer PumpTimer = new Timer(30000);


    private Timer PumpGoldenTimer = new Timer(100000);
    private boolean isInitialized = false;

    public FoodGenerator(Room room) {
        this.room = room;
        Affinity.setAffinity(1);
    }

    Arctic arctic;
    Land land;
    Ocean oleft;
    Ocean oright;
    Desert desert;
    VolcanoBiome volcanoBiome;

    public void initialize() {
        arctic = this.room.arctic;
        land = this.room.land;
        oleft = this.room.oleft;
        oright = this.room.oright;
        desert = this.room.desert;
        volcanoBiome = this.room.volcanoBiome;
        isInitialized = true;
        // this.log.info("Food Generator Initialized.");
    }

    @Override
    public void run() {
        while (Constants.FOODSPAWN) {
            try {
                this.update();
   Thread.sleep(this.room.tick);
//    Thread.sleep(1); 
           } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (isInitialized) {
            spawnTimer.update(Constants.TICKS_PER_SECOND);
            spawnLavaLakesTimer.update(Constants.TICKS_PER_SECOND);
            spawnRedMushroomsTimer.update(Constants.TICKS_PER_SECOND);
            spawnSeaWeedsTimer.update(Constants.TICKS_PER_SECOND);
            spawnKelpsTimer.update(Constants.TICKS_PER_SECOND);
            spawnStarFishsTimer.update(Constants.TICKS_PER_SECOND);
            spawnClamsTimer.update(Constants.TICKS_PER_SECOND);
            spawnConchShellsTimer.update(Constants.TICKS_PER_SECOND);
            spawnHealstonesTimer.update(Constants.TICKS_PER_SECOND);
            spawnCactusTimer.update(Constants.TICKS_PER_SECOND);
            spawnDateFruitTimer.update(Constants.TICKS_PER_SECOND);
            spawnMushroomBushsTimer.update(Constants.TICKS_PER_SECOND);
            spawnBeehives.update(Constants.TICKS_PER_SECOND);
            spawnSnailsTimer.update(Constants.TICKS_PER_SECOND);
            spawnFrogsTimer.update(Constants.TICKS_PER_SECOND);
            PumpTimer.update(Constants.TICKS_PER_SECOND);
            PumpGoldenTimer.update(Constants.TICKS_PER_SECOND);


            if (spawnTimer.isDone()) {
                if (arctic.mushrooms < 160)
                    arctic.spawnMushrooms(Math.min(5, 160 - arctic.mushrooms));
                if (land.mushrooms < 160)
                    land.spawnMushrooms(Math.min(5, 160 - land.mushrooms));
                spawnTimer.reset();
            }
            if (spawnFrogsTimer.isDone()) {
                if (land.frogs < 30)
                    land.spawnFrogs(Math.min(6, 30 - land.frogs));
                spawnFrogsTimer.reset();
            }
    
            if (spawnSnailsTimer.isDone()) {
                if (oleft.snails < 20)
                    oleft.spawnSnails(Math.min(6, 20 - oleft.snails));
                if (oright.snails < 20)
                    oright.spawnSnails(Math.min(6, 20 - oright.snails));
                spawnSnailsTimer.reset();
            }
            // if (spawnDucksTimer.isDone()) {
            //     if (oleft.ducks < 20)
            //         oleft.spawnDucks(Math.min(6, 20 - oleft.ducks));
            //     if (oright.ducks < 20)
            //         oright.spawnDucks(Math.min(6, 20 - oright.ducks));
            //     spawnDucksTimer.reset();
            // }
            if (spawnSeaWeedsTimer.isDone()) {
                if (oleft.seaweeds < 50)
                    oleft.spawnSeaWeeds(Math.min(5, 50 - oleft.seaweeds));
                if (oright.seaweeds < 50)
                    oright.spawnSeaWeeds(Math.min(5, 50 - oright.seaweeds));
                spawnSeaWeedsTimer.reset();
            }
            if (spawnKelpsTimer.isDone()) {
                if (oleft.kelps < 50)
                    oleft.spawnKelps(Math.min(5, 50 - oleft.kelps));
                if (oright.kelps < 50)
                    oright.spawnKelps(Math.min(5, 50 - oright.kelps));
                spawnKelpsTimer.reset();
            }
            if (spawnStarFishsTimer.isDone()) {
                if (oleft.starfishs < 30)
                    oleft.spawnStarFishs(Math.min(5, 30 - oleft.starfishs));
                if (oright.starfishs < 30)
                    oright.spawnStarFishs(Math.min(5, 30 - oright.starfishs));
                spawnStarFishsTimer.reset();
            }
            if (spawnClamsTimer.isDone()) {
                if (oleft.clams < 30)
                    oleft.spawnClams(Math.min(5, 30 - oleft.clams));
                if (oright.clams < 30)
                    oright.spawnClams(Math.min(5, 30 - oright.clams));
                spawnClamsTimer.reset();
            }
            if(Constants.GAMEMODE != 2 && Constants.canSpawnPump){
                if (PumpTimer.isDone()) {
                if (land.pumking < 2)
                    land.pumking(1);
                PumpTimer.reset();
            }
            if (PumpGoldenTimer.isDone() && Constants.canSpawnPump) {
                if (land.goldenpumking < 1)
                    land.golden(1);
                PumpGoldenTimer.reset();
            }
            }
            if (spawnConchShellsTimer.isDone()) {
                if (oleft.conchshells < 50)
                    oleft.spawnConchShells(Math.min(5, 50 - oleft.conchshells));
                if (oright.clams < 50)
                    oright.spawnConchShells(Math.min(5, 50 - oright.conchshells));
                spawnConchShellsTimer.reset();
            }
            if (spawnDateFruitTimer.isDone()) {
                if (desert.datefruits < 75)
                    desert.spawnDateFruits(Math.min(5, 75 - desert.datefruits));
                spawnDateFruitTimer.reset();
            }
            if (spawnMushroomBushsTimer.isDone()) {
                if (land.mushroombushs < 100)
                    land.spawnMushroomBushs(Math.min(10, 100 - land.mushroombushs));
                spawnMushroomBushsTimer.reset();
            }
            if (spawnBeehives.isDone()) {
                if (land.beehives < 10)
                    land.spawnBeeHives(Math.min(4, 20 - land.beehives));
                spawnBeehives.reset();
            }
            if (spawnCactusTimer.isDone()) {
                if (desert.cactuses < 25)
                    desert.spawnCactuses(Math.min(2, 25 - desert.cactuses), Utils.randomInt(1, 25));
                if (desert.aloevera < 25)
                    desert.spawnAloevera(Math.min(2, 25 - desert.aloevera));
                spawnCactusTimer.reset();
            }
            if (spawnHealstonesTimer.isDone()) {
                if (arctic.healstones < 5)
                    arctic.spawnHealstones(Math.min(1, 10 - arctic.healstones));
                if (land.healstones < 8)
                    land.spawnHealstones(Math.min(1, 15 - land.healstones));
                if (oleft.healstones < 6)
                    oleft.spawnHealstones(Math.min(1, 10 - oleft.healstones));
                if (oright.healstones < 6)
                    oright.spawnHealstones(Math.min(1, 10 - oright.healstones));
                if (desert.healstones < 8)
                    desert.spawnHealstones(Math.min(1, 10 - desert.healstones));
                spawnHealstonesTimer.reset();
            }
            if (spawnLavaLakesTimer.isDone()) {
                if (volcanoBiome.healstone == null) {
                    Healstone hs = new Healstone(this.room.getID(), volcanoBiome.getX() + Utils.randomInt(-400, 400), volcanoBiome.getY() + Utils.randomInt(-400, 400), 60, 4,
                            200, room, volcanoBiome, 20);
                    this.room.addObj(hs);
                    volcanoBiome.healstone = hs;
                } else {
                    if (!volcanoBiome.healstone.getCollidedList().contains(volcanoBiome)) {
                        volcanoBiome.healstone.setX(volcanoBiome.getX() + Utils.randomInt(-200, 250));
                        volcanoBiome.healstone.setY(volcanoBiome.getY()+ Utils.randomInt(-200, 250));
                    }
                }
                for (LavaLake lavaLake : volcanoBiome.lavaLakes) {
                    if (lavaLake.watermelons < 3)
                        lavaLake.spawnWatermelons(Math.min(1, 3 - lavaLake.watermelons));
                    if (lavaLake.watermelonSlices < 4)
                        lavaLake.spawnWatermelonSlices(Math.min(1, 4 - lavaLake.watermelonSlices));
                }
                for (LavaLake lavaLake : desert.lavaLakes) {
                    if (lavaLake.watermelons < 3)
                        lavaLake.spawnWatermelons(Math.min(1, 3 - lavaLake.watermelons));
                    if (lavaLake.watermelonSlices < 3)
                        lavaLake.spawnWatermelonSlices(Math.min(1, 3 - lavaLake.watermelonSlices));
                }
                spawnLavaLakesTimer.reset();
            }
            if (spawnRedMushroomsTimer.isDone()) {
                if (arctic.redmushrooms < 80)
                    arctic.spawnRedMushrooms(Math.min(5, 80 - arctic.mushrooms));
                if (land.redmushrooms < 120)
                    land.spawnRedMushrooms(Math.min(5, 120 - land.mushrooms));
                spawnRedMushroomsTimer.reset();
            }
        }
    }
}