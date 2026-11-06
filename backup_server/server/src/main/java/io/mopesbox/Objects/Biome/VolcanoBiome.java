package io.mopesbox.Objects.Biome;

import java.util.ArrayList;

import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Healstone;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Objects.Static.Volcano;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class VolcanoBiome extends GameObject {
    private Room room;
    private Volcano volcano;
    public List<LavaLake> lavaLakes = new ArrayList<>();
    public Healstone healstone;

    public VolcanoBiome(int id, double x, double y, int radius, Room room, int spawnid) {
        super(id, x, y, radius, 47);
        this.setBiome(BiomeType.VOLCANO.ordinal());
        this.room = room;
        this.setCollideable(false);
        this.setSpawnID(spawnid);
        this.setCollideCallbacking(true);
            }
        

    private Timer spawnLavaLakesTimer = new Timer(120 * 1000);

    public void spawnVolcano() {

        this.volcano = new Volcano(this.room.getID(), this.getX(), this.getY(), this.room, BiomeType.VOLCANO.ordinal());
        this.room.addObj(this.volcano);
        double lavaAngle = 0;

        for (int i = 0; i < 8; i++) {
            lavaAngle = lavaAngle % (Math.PI * 2);

            double angle = lavaAngle + (Math.random() * 2);
            lavaAngle = angle;

            double rad = this.getRadius() - (this.getRadius()/4);//Utils.randomInt(this.getRadius()/2, this.getRadius());
            double tempRadius = rad;

            double x = ((Math.cos(angle) * tempRadius));
            double y = ((Math.sin(angle) * tempRadius));

            /*
             * This improves generation results making it more close to mope.io
             * 
             * - Pasha
             */

            /*
             * this will improve it with some tinkering about distance...
             */
            LavaLake lavaLake = new LavaLake(this.room.getID(), this.getX() + x, this.getY() + y, Utils.randomInt(90, 120), this.room);
            this.room.addObj(lavaLake);
            lavaLakes.add(lavaLake);
        }
    }
    @Override
    public void update(){
        super.update();
        spawnLavaLakesTimer.update(Constants.TICKS_PER_SECOND);
    if (spawnLavaLakesTimer.isDone()) {
        if (this.healstone == null) {
            Healstone hs = new Healstone(this.room.getID(), this.getX() + Utils.randomInt(-400, 400), this.getY() + Utils.randomInt(-400, 400), 60, 4,
                    200, room, this, 14);
            this.room.addObj(hs);
            this.healstone = hs;
        } else {
            if (!this.healstone.getCollidedList().contains(this)) {
                this.healstone.setX(this.getX() + Utils.randomInt(-200, 250));
                this.healstone.setY(this.getY()+ Utils.randomInt(-200, 250));
            }
        }
        spawnLavaLakesTimer.reset();
    }
}

    @Override
    public void onCollision(GameObject object) {
        if (!(object instanceof Animal))
            return;
        Animal ani = (Animal) object;
        if (ani.inArena)
            return;

        if (ani.isLavaAnimal() && ani.waterTimer.isDone()){
            ani.addWater(0.5);
            ani.waterTimer.reset();
        }



            // ani.setBiome(baseRadius);

    }
}