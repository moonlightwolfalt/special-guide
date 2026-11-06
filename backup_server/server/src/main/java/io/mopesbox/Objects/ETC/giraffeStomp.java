package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class giraffeStomp extends Ability {
    private Timer liveTimer = new Timer(800);
    private Room room;
    private int speed = 7;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 1;
    public giraffeStomp(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 42, radius, 0);
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
            double dmg = (((Animal)o).maxHealth / 10);
            ((Animal)o).hurt(damage + dmg, 1, this.owner);
            ((Animal)o).setStun(2);
            ((Animal)o).setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (18))));
            ((Animal)o).setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (18))));
            damaged.add((Animal)o);
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed--;
        }
               liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()){
            this.room.removeObj(this, null);
            
        }
        }
 
    }