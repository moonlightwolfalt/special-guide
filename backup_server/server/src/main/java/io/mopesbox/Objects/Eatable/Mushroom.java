package io.mopesbox.Objects.Eatable;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Mushroom extends GameObject {
    private int xp;
    private Room room;
    private int water = 0;
    private boolean isDone = false;
    private Biome origin;

    public Mushroom(int id, double x, double y, int biome, Room room, Biome origin) {
        super(id, x, y, 16, 22);
        this.setMovable(true);
        this.xp = Utils.randomInt(10, 50);
        this.room = room;
        this.origin = origin;
        this.setBiome(biome);
    }

    public Mushroom(int id, double x, double y, int biome, Room room, Biome origin, int xp) {
        this(id, x, y, biome, room, origin);
        this.xp = xp;
    }

    public Mushroom(int id, double x, double y, int biome, Room room, MushroomBush origin) {
        super(id, x, y, 16, 22);
        this.setMovable(true);
        this.xp = Utils.randomInt(10, 50);
        this.room = room;
        this.setSpawnID(origin.getId());
        this.setBiome(biome);
    }

    public void addWater() {
        if (!isDone) {
            this.water++;
            this.setRadius(this.getRadius() + this.water * 2);
            if (this.water >= 3) {
                isDone = true;
                int count = Utils.randomInt(3, 5);
                for (int i = 0; i < count; i++) {
                    int ang = Utils.randomInt(0, 360);
                    double x = ((Math.cos(Math.toRadians(ang)) * (this.getRadius())));
                    double y = ((Math.sin(Math.toRadians(ang)) * (this.getRadius())));
                    Berry berry = new Berry(this.room.getID(), this.getX() + x, this.getY() + y, this.getBiome(), room);
                    this.room.addObj(berry);
                }
                if (origin != null)
                    origin.mushrooms--;
                this.room.removeObj(this, null);
            }
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
        }
        if (o instanceof Mushroom || o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).isInHole())
                return false;
            if (((Animal) o).isDiveActive())
                return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (!this.isDone && getCollideableIs(o) && o instanceof Animal && ((Animal) o).getInfo().getTier() >= 4) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            this.isDone = true;
            if (origin != null)
                origin.mushrooms--;
            this.room.removeObj(this, o);
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().getClient().addXp(this.xp);
            this.isDone = true;
            if (origin != null)
                origin.mushrooms--;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }
}
