package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Nude;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class ArcticBush extends GameObject {

    private Room room;
    private boolean spawnNudes;
    public ArcticBush(int id, double x, double y, int rad, int biome, Room room, boolean spawnNudes) {
        super(id, x, y, rad, 6);
        this.setCollideCallbacking(true);
        this.setCollideable(false);
        this.setBiome(biome);

        this.spawnNudes = spawnNudes;
        this.room = room;
    }

    public int nude = 0;
    public int raspberry = 0;

    private Timer nudeSpawnTimer = new Timer(3000);

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
        if (this.spawnNudes) {
            nudeSpawnTimer.update(Constants.TICKS_PER_SECOND);
            if (nudeSpawnTimer.isDone() && Constants.FOODSPAWN) {
                if(this.nude < 5) {
                    this.nude++;
                    double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    Nude nude = new Nude(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.room, this);
                    this.room.addObj(nude);
                }
                nudeSpawnTimer.reset();
            }
        }
    }
}
