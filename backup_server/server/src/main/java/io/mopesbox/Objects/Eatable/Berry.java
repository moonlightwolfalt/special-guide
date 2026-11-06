package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.Berryspot;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Berry extends GameObject{
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Timer liveTimer = new Timer(5000);
    public Berry(int id, double x, double y, int biome, Room room) {
        super(id, x, y, 12, 20);
        this.setMovable(true);
        this.xp = Utils.randomInt(5, 10);
        this.room = room;
        this.setBiome(biome);
    }

    private Berryspot origin;

    public Berry(int id, double x, double y, int biome, Room room, Berryspot origin) {
        super(id, x, y, 12, 20);
        this.setMovable(true);
        this.origin = origin;
        this.liveTimer = null;
        this.xp = Utils.randomInt(5, 10);
        this.room = room;
        this.setBiome(biome);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            return false;
        }
        if(o instanceof Berry || o instanceof Water) {
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
            if(this.origin != null) this.origin.berries--;
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.liveTimer != null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                if(this.origin != null) this.origin.berries--;
                liveTimer.reset();
                return;
            }
        }
    }
}
