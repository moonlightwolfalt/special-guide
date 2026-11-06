package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Objects.Static.ArcticBush;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Nude extends GameObject{
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Timer liveTimer = new Timer(30000);
    
    public Nude(int id, double x, double y, int biome, Room room) {
        super(id, x, y, 30, 48);
        this.setMovable(true);
        this.xp = Utils.randomInt(400, 2500);
        this.room = room;
        this.setBiome(biome);
    }

    private ArcticBush origin;

    public Nude(int id, double x, double y, int biome, Room room, ArcticBush origin) {
        super(id, x, y, 30, 48);
        this.setMovable(true);
        this.origin = origin;
        this.xp = Utils.randomInt(400, 2500);
        this.room = room;
        this.setBiome(biome);
        this.liveTimer = null;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).getInfo().getTier() >= 7)
                return false;
        }
        if(o instanceof Pear || o instanceof Raspberry || o instanceof Berry || o instanceof Water) {
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

    // @Override
    // public boolean getCollideable(GameObject o) {
    //     if(o instanceof Animal) {
    //         if(((Animal)o).flag_flying) return false;
    //         if(((Animal)o).getInfo().getTier() >= 4) return false;
    //     }
    //     if(o instanceof RedMushroom || o instanceof Water) {
    //         return false;
    //     }
    //     return true;
    // }

    // public boolean getCollideableIs(GameObject o) {
    //     if(o instanceof Animal) {
    //         if(((Animal)o).flag_flying) return false;
    //         if(((Animal)o).isInHole()) return false;
    //         if(((Animal)o).isDiveActive()) return false;
    //     }
    //     return true;
    // }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 7) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            biter.addWater(biter.maxwater/10);
            this.isDone = true;
            this.room.removeObj(this, o);
            if(this.origin != null) this.origin.nude--;
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            ((ElephantTrunk)o).getOwner().addWater(((ElephantTrunk)o).getOwner().maxwater/10);
            this.isDone = true;
            if (origin != null)
                origin.nude--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }

    @Override
    public void update() {
        super.update();
        if (liveTimer != null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                if(this.origin != null) this.origin.nude--;
                liveTimer.reset();
                return;
            }
        }
    }
}
