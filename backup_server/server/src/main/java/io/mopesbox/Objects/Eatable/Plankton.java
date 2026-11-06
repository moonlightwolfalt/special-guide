package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Objects.Static.PlanktonBush;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Plankton extends GameObject{
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Timer liveTimer = new Timer(15000);
    public Plankton(int id, double x, double y, int biome, Room room) {
        super(id, x, y, 12, 26);
        this.setMovable(true);
        this.xp = Utils.randomInt(1, 3);
        this.room = room;
        this.setBiome(biome);
    }

    private PlanktonBush origin;

    public Plankton(int id, double x, double y, int biome, Room room, PlanktonBush origin) {
        super(id, x, y, 12, 26);
        this.setMovable(true);
        this.origin = origin;
        this.xp = Utils.randomInt(1, 3);
        this.room = room;
        this.setBiome(biome);
        this.liveTimer = null;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            return false;
        }
        if(o instanceof Plankton || o instanceof Water) {
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
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() <= 8) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            this.isDone = true;
            this.room.removeObj(this, o);
            if(this.origin != null) this.origin.plankton--;
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            this.isDone = true;
            if (origin != null)
                origin.plankton--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }

    @Override
    public void update() {
        super.update();
        if(liveTimer != null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                if(this.origin != null) this.origin.plankton--;
                liveTimer.reset();
                return;
            }
        }
    }
}
