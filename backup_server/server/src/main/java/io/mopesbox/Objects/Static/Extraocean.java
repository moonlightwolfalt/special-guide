package io.mopesbox.Objects.Static;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;

public class Extraocean extends GameObject
{
    public Extraocean(int id, double x, double y, int rad, int biome, Room room) {
        super(id, x, y, rad, 34);
        this.setBiome(biome);
        this.setCollideable(false);
    }

    public int waters = 0;

    @Override
    public void onCollision(GameObject object) {
        if(object instanceof Animal) {
            object.setBiome(1);
            ((Animal)object).ignoreBiomes();
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            return false;
        }
        return true;
    }

    @Override
    public void update() {
        super.update();
        /*waterSpawnTimer.update(Constants.TICKS_PER_SECOND);
        if(waterSpawnTimer.isDone()) {
            if(this.waters < 10) {
                this.waters++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                Water wat = new Water(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), room, this);
                this.room.addObj(wat);
            }
            waterSpawnTimer.reset();
        }*/
    }
}
