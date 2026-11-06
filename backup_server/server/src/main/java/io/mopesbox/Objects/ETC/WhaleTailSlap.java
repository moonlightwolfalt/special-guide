package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class WhaleTailSlap extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room;
    private int speed = 7;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 20;
    public WhaleTailSlap(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 32, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            if (((Animal)o).getInfo().getTier() != this.owner.getInfo().getTier()) {
            ((Animal)o).hurt(o instanceof Bee ? o.health : (damage + (((Animal)o).maxHealth / 18)), 1, this.owner);
            ((Animal)o).setStun(2);
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            double x = ((Math.cos(Math.toRadians(theta)) * (this.getRadius()/3)));
            double y = ((Math.sin(Math.toRadians(theta)) * (this.getRadius()/3)));
            o.addVelocityX(x);
            o.addVelocityY(y);
            damaged.add((Animal)o);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.damage > 10) damage--;
        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed--;
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}