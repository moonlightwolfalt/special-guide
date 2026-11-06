package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Tailslap extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room;
    private int speed = 10;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 200; // 200
    public Tailslap(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 95, radius, 0);
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
        if (o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal)o).isUnderTree()) {
            ((Animal)o).hurt(damage, 1, this.owner);
            ((Animal)o).setStun(5);
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            double x = ((Math.cos(Math.toRadians(theta)) * (this.getRadius()/2)));
            double y = ((Math.sin(Math.toRadians(theta)) * (this.getRadius()/2)));
            o.setVelocityX(x);
            o.setVelocityY(y);
            damaged.add((Animal)o);
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
            if(this.speed < 5) {
                int sang = -30;
                for(int i = 0; i < 3; i++) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle()+sang)) * (this.getRadius()*1.2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle()+sang)) * (this.getRadius()*1.2)));
                    DustWave wave = new DustWave(this.owner.getClient().getRoom().getID(), this.getX() + x, this.getY() + y, (int) (this.getRadius()/2.5), this.getAngle()+(sang/4), 8, this.getId(), this.owner.getClient().getRoom(), this.owner.getClient());
                    sang += 30;
                    this.owner.getClient().getRoom().addObj(wave);
                }
            }
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}
