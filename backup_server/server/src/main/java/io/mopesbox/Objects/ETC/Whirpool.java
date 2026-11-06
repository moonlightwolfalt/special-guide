package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Whirpool extends Ability {
    private Timer liveTimer = new Timer(5000);
    private Room room;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 125;
    public Whirpool(int id, double x, double y, int radius, Room room, Animal owner) {
        super(id, x, y, 5, radius, 0);
        this.room = room;
        this.owner = owner;
        // this.setObjAngle(angle);
        // this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            if(!damaged.contains((Animal) o) && !((Animal)o).isStunned()) {
                if (damage + 1000 < ((Animal)o).maxHealth) {
                    ((Animal)o).hurt(damage, 1, this.owner);
                }
                else {
                    ((Animal)o).hurt(this.owner.getInfo().getTier() > ((Animal)o).getInfo().getTier() ? ((Animal)o).maxHealth / 8 : ((Animal)o).maxHealth / 12, 1, this.owner);
                }
                damaged.add((Animal)o);
                ((Animal)o).setStun(5);
            }
            double dx = this.getX() - o.getX();
            double dy = this.getY() - o.getY();

            double theta = Math.atan2(dy, dx);

            if (theta < 0)
                theta += Math.PI * 2;

            double velocityX = Math.cos(theta)*5;
            double velocityY = Math.sin(theta)*5;
            ((Animal)o).addVelocityX(velocityX);
            ((Animal)o).addVelocityY(velocityY);
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