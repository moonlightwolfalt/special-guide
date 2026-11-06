package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Berry;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Berryspot extends GameObject
{
    private Room room;
    public Berryspot(int id, double x, double y, int rad, int biome, Room room) {
        super(id, x, y, rad, 27);
        this.setBiome(biome);
        this.room = room;
        this.setCollideable(true);
        this.setCollideCallbacking(true);
    }

    public int berries = 0;

    private Timer berrySpawnTimer = new Timer(1000);

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() > 5) {
            return false;
        }
        return true;
    }

    @Override
    public void update() {
        super.update();
        berrySpawnTimer.update(Constants.TICKS_PER_SECOND);
        if(berrySpawnTimer.isDone()) {
            if(this.berries < 15) {
                this.berries++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                Berry berry = new Berry(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), room, this);
                this.room.addObj(berry);
            }
            berrySpawnTimer.reset();
        }
    }
}
