package io.mopesbox.Objects.Dangerous;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class MopeCoin extends GameObject {
    private int speed = 0;
    private Room room;
    private double aangle;
    private Timer liveTimer = new Timer(30000);
    private boolean collided = false;

    public MopeCoin(int id, double x, double y, int speed, double angle, Room room) {
        super(id, x, y, 12, 132);
        this.setMovable(true);
        this.speed = speed;
        this.room = room;
        this.aangle = angle;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        // if(o instanceof Animal) {
        //     if(((Animal)o).flag_flying) return false;
        // }
        // if(o instanceof Water) {
        //     return false;
        // }
        return false;
    }

    public boolean getCollideableIs(GameObject o) {
        if (o instanceof Animal) {
            if(((Animal)o).flag_flying) 
                return false;
            if(((Animal)o).isInHole()) 
                return false;
            if(((Animal)o).isDiveActive()) 
                return false;
            if(this.speed >= 10) 
                return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && this.collided == false) {
            this.collided = true;
            Animal killer = (Animal) o;

            killer.getClient().addCoins(Utils.randomInt(1, 3));
            this.room.removeObj(this, killer);
        }
    }

    @Override
    public void update() {
        super.update();

        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.aangle)) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.aangle)) * (this.speed))));
            this.speed--;
        } else {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                liveTimer.reset();
            }
        }
    }
}
