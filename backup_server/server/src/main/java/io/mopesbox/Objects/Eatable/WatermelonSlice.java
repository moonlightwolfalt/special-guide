package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class WatermelonSlice extends GameObject{
    private int xp;
    private Room room;
    private boolean isDone = false;
    private LavaLake origin;
    private Timer liveTimer = new Timer(2800);
    public WatermelonSlice(int id, double x, double y, int xp, Room room, LavaLake origin) {
        super(id, x, y, 24, 51);
        this.origin = origin;
        this.xp = xp == 0 ? Utils.randomInt(1000, 2500) : xp;
        this.room = room;
        this.setMovable(true);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).getInfo().getTier() >= 6) return false;
        }
        if(o instanceof WatermelonSlice || o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 6) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            biter.addWater(4);
            this.isDone = true;
            if(origin != null) origin.watermelonSlices--;
            this.room.removeObj(this, o);
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            ((ElephantTrunk)o).getOwner().addWater(4);
            this.isDone = true;
            if (origin != null)
                origin.watermelonSlices--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }

    @Override
    public void update() {
        super.update();
        if(origin == null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                liveTimer.reset();
                return;
            }
        }
    }
}
