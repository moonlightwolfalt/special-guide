package io.mopesbox.Objects.Eatable;


import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class SeaWeed extends GameObject {
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Biome origin;
    public SeaWeed(int id, double x, double y, int biome, Room room, Biome origin) {
        super(id, x, y, 20, 35);
        this.setMovable(true);
        this.xp = Utils.randomInt(1, 450);
        this.room = room;
        this.origin = origin;
        this.setBiome(biome);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).getInfo().getTier() >= 4) return false;
        }
        if(o instanceof SeaWeed || o instanceof Water) {
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
        if(!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 4) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            this.isDone = true;
            if(origin != null) origin.seaweeds--;
            this.room.removeObj(this, o);
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            this.isDone = true;
            if (origin != null)
                origin.seaweeds--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }
    @Override
    public void update() {
        if (this.health <= 0) {
            this.room.removeObj(this, null);
            return;
        }
    }
}
