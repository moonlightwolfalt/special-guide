package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Plankton;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class PlanktonBush extends GameObject
{
    private Room room;
    public PlanktonBush(int id, double x, double y, int rad, int biome, Room room) {
        super(id, x, y, rad, 28);
        this.setBiome(biome);
        this.room = room;
        this.setCollideable(true);
        this.setCollideCallbacking(true);
    }

    public int plankton = 0;

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
            if(this.plankton < 10) {
                this.plankton++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (5)));
                Plankton plankton = new Plankton(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), room, this);
                this.room.addObj(plankton);
            }
            berrySpawnTimer.reset();
        }
    }
}
