package io.mopesbox.Objects.Eatable;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Watermelon extends GameObject{
    private int xp;
    private Room room;
    private int water = 0;
    private boolean isDone = false;
    private LavaLake origin;
    public Watermelon(int id, double x, double y, Room room, LavaLake origin) {
        super(id, x, y, 24, 50);
        this.xp = Utils.randomInt(2000, 5000);
        this.room = room;
        this.origin = origin;
        this.setMovable(true);
    }

    public void addWater() {
        if(!isDone) {
            this.water++;
            this.setRadius(this.getRadius()+1);
            if(this.water >= 5) {
                isDone = true;
                int count = Utils.randomInt(2, 4);
                int nxp = this.xp/count;
                for(int i = 0; i < count; i++) {
                    int ang = Utils.randomInt(0, 360);
                    double x = ((Math.cos(Math.toRadians(ang)) * (this.getRadius())));
                    double y = ((Math.sin(Math.toRadians(ang)) * (this.getRadius())));
                    WatermelonSlice slice = new WatermelonSlice(this.room.getID(), this.getX()+x, this.getY()+y, nxp, room, null);
                    this.room.addObj(slice);
                }
                if(origin != null) origin.watermelons--;
                this.room.removeObj(this, null);
            }
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).getInfo().getTier() >= 11) return false;
        }
        if(o instanceof Watermelon || o instanceof Water) {
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
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 11) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            biter.addWater(8);
            this.isDone = true;
            if(origin != null) origin.watermelons--;
            this.room.removeObj(this, o);
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            ((ElephantTrunk)o).getOwner().addWater(8);
            this.isDone = true;
            if (origin != null)
                origin.watermelons--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }
}
