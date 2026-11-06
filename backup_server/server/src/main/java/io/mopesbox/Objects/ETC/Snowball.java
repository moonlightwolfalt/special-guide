package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;


public class Snowball extends GameObject {
    private Timer liveTimer = new Timer(2000);
    private Room room;

    private Animal owner;
    private int damage = 2;
    private double speed;
    private boolean makeStatue = false;
    private boolean snowmanBall = false;

    public Snowball(int id, double x, double y, int radius, Room room, double angle, double speed, Animal owner,int species) {
        super(id, x, y, radius, 19);
        this.room = room;
        this.owner = owner;
        this.angle = angle;
        this.speed = speed;
        if (species == 3) {
            damage += 8;
            makeStatue = true;
        }
        if (owner.getInfo().getAnimalType() == 101) {
            snowmanBall = true;
            damage = 6;
        }
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if (this.owner == null)
            return;
        if (o instanceof Animal && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible()
                && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal) o).isDiveActive()) {
            if (((Animal) o).getInfo().getTier() >= 6) {

                while (damage > ((Animal) o).maxHealth) {
                    damage *= 0.8;
                }

                ((Animal) o).hurt(damage, 1, this.owner);
            }
            ((Animal) o).setStun(2);
            if (makeStatue) {
                ((Animal) o).setFrozen(3, this.owner, 1);
                ((Animal) o).setStatue(5, 1, this.owner);
            } else if (snowmanBall) {
                ((Animal) o).setFrozen(2, this.owner, 1);
                ((Animal) o).setStatue(2, 1, this.owner);
            } else {
                ((Animal) o).setFrozen(2, this.owner, 1);
            }
            this.room.removeObj(this, o);
        }
    }

    @Override
    public void update() {
        super.update();
        if (speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.angle)) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.angle)) * (this.speed)))); // для скама мамонта
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if (liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}