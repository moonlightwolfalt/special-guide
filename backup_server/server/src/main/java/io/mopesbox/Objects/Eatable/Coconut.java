package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.Tree;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Coconut extends GameObject {
    private int xp;
    private Room room;
    private boolean isDone = false;
    private Timer liveTimer = new Timer(5000);
    public Coconut(int id, double x, double y, int biome, Room room, Tree origin) {
        super(id, x, y, 24, 30);
        this.setMovable(true);
        this.liveTimer = null;
        this.speed = 0;
        this.origin = origin;
        this.xp = Utils.randomInt(700, 1000);
        this.room = room;
        this.setBiome(biome);
    }

    private Tree origin;
    private Animal owner;
    private int speed = 0;

    public Coconut(int id, double x, double y, int biome, Room room, Animal owner, int speed, double angle) {
        super(id, x, y, 24, 30);
        this.setMovable(true);
        this.owner = owner;
        this.secondaryType2 = 1;
        this.speed = speed;
        this.setAngle(angle);
        this.xp = Utils.randomInt(700, 1000);
        this.room = room;
        this.setBiome(biome);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).getInfo().getTier() >= 9)
                return false;
        }
        if(o instanceof Coconut || o instanceof Banana || o instanceof Berry || o instanceof Tree || o instanceof Water) {
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
        if (!this.isDone && this.speed <= 0 && getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 9) {
            Animal biter = (Animal) o;
            biter.getClient().addXp(this.xp);
            this.isDone = true;
            this.room.removeObj(this, o);
            if(this.origin != null) this.origin.foods--;
        } else if (this.speed > 0 && getCollideableIs(o) && o instanceof Animal && o != this.owner) {
            ((Animal)o).setStun(2);
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            double x = ((Math.cos(Math.toRadians(theta)) * (o.getRadius()/4)));
            double y = ((Math.sin(Math.toRadians(theta)) * (o.getRadius()/4)));
            o.setVelocityX(x);
            o.setVelocityY(y);
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.speed > 0) {
            this.secondaryType2 = 1;
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed -= 2;
        } else this.secondaryType2 = 0;
        if(this.liveTimer != null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                if(this.origin != null) this.origin.foods--;
                liveTimer.reset();
                return;
            }
        }
    }
}
