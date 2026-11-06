package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class AloeVeraLeaf extends GameObject{
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Timer liveTimer = new Timer(80000);
    private AloeVera origin;
    public AloeVeraLeaf(int id, double x, double y, Room room, AloeVera origin) {
        super(id, x, y, 14, 97);
        this.setMovable(true);
        this.xp = Utils.randomInt(1, 25);
        this.room = room;
        this.origin = origin;
        this.setBiome(4);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            return false;
        }
        if(o instanceof AloeVeraLeaf || o instanceof Water) {
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
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal) {
            Animal biter = (Animal) o;
            if(biter.getInfo().getTier() <= 14) biter.setAloeVeraLeafHealing(true);
            biter.getClient().addXp(this.xp);
            this.isDone = true;
            this.room.removeObj(this, o);
            if(this.origin != null) this.origin.leafs--;
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            Animal biter = ((ElephantTrunk)o).getOwner();
            if(biter.getInfo().getTier() <= 14) biter.setAloeVeraLeafHealing(true);
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            this.isDone = true;
            if (origin != null)
                origin.leafs--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }

    @Override
    public void update() {
        super.update();
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            if(this.origin != null) this.origin.leafs--;
            liveTimer.reset();
            return;
        }
    }
}
