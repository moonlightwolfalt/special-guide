package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Pear;
import io.mopesbox.Objects.Eatable.Raspberry;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class HidingBush extends GameObject {

    private Room room;
    private boolean spawnPears;
    private boolean LiveTimerEnabled = false;
    public HidingBush(int id, double x, double y, int rad, int biome, Room room, boolean spawnPears) {
        super(id, x, y, rad, 6);
        this.setCollideCallbacking(true);
        this.setCollideable(false);
        this.setBiome(biome);

        this.spawnPears = spawnPears;
        this.room = room;
    }

    public int pear = 0;
    public int raspberry = 0;

    private Timer pearSpawnTimer = new Timer(3000);
    private Timer liveTimer = new Timer(8000);

    public void setTimer(){
        this.LiveTimerEnabled = true;
        pearSpawnTimer.reset();
        pearSpawnTimer = new Timer(5000);
        
    }

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
        if(this.LiveTimerEnabled){
            this.liveTimer.update(Constants.TICKS_PER_SECOND);
            if(this.liveTimer.isDone()){
                this.room.removeObj(this, null);
                this.liveTimer.reset();
            }
        }
        if (this.spawnPears && Constants.FOODSPAWN) {
            pearSpawnTimer.update(Constants.TICKS_PER_SECOND);
            if (pearSpawnTimer.isDone()) {
                if(this.pear < 5) {
                    this.pear++;
                    double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    Pear pear = new Pear(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.room, this);
                    this.room.addObj(pear);
                }
                if(this.raspberry < 5) {
                    this.raspberry++;
                    double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                    Raspberry raspberry = new Raspberry(this.room.getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.room, this);
                    this.room.addObj(raspberry);
                }
                pearSpawnTimer.reset();
            }
        }
    }
}
