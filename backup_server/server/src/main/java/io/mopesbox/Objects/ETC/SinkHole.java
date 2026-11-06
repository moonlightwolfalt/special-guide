package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class SinkHole extends GameObject {
    private Timer liveTimer = new Timer(5000);
    private Room room;
    public Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 10;
    public SinkHole(int id, double x, double y, int radius, Room room, Animal owner) {
        super(id, x, y, radius, 72);
        this.room = room;
        this.owner = owner;
        // this.setObjAngle(angle);
        // this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(radius * 1.7);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            if(!damaged.contains((Animal) o)) {
                if (damage + 30 < ((Animal)o).maxHealth) {
                    ((Animal)o).hurt(damage, 1, this.owner);
                }
                else {
                    ((Animal)o).hurt(5 + (((Animal)o).maxHealth / 4), 1, this.owner);
                }
                damaged.add((Animal)o);
                ((Animal)o).setStun(5);
            }
            double distance = Utils.distance(o.getX(),this.getX(),o.getY(),this.getY());
            if(distance > this.getRadius()-5) {
                double dx = this.getX() - o.getX();
                double dy = this.getY() - o.getY();

                double theta = Math.atan2(dy, dx);

                if (theta < 0)
                    theta += Math.PI * 2;

                double velocityX = Math.cos(theta)*10;
                double velocityY = Math.sin(theta)*10;
                ((Animal)o).addVelocityX(velocityX);
                ((Animal)o).addVelocityY(velocityY);
                ((Animal)o).setFire(1, owner, 0);
            } else {
                ((Animal)o).setStun(3);
                if(distance <= this.getRadius()/4) {
                    ((Animal)o).setFire(2, owner, 0);
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        // if(this.damage > 10) damage--;
        // if (this.speed > 0) {
            // ((Animal)o).addVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (-this.speed))));
            // ((Animal)o).addVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (-this.speed))));
            // this.speed--;
        // }

        liveTimer.update(Constants.TICKS_PER_SECOND);

        if (liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}