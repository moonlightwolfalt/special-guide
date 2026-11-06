package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class BigWhirlpool extends Ability {
    private Timer liveTimer = new Timer(5000);
    private Room room;
    public Animal owner;
    private List<GameObject> damaged = new ArrayList<>();
    private int damage = 120;
    public BigWhirlpool(int id, double x, double y, int radius, Room room, Animal owner) {
        super(id, x, y, 63, radius, 0);
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
        if(o instanceof Animal && !((Animal) o).isInHole() && !((Animal)o).isDiveActive() && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
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
            double dx = this.getX() - o.getX();
            double dy = this.getY() - o.getY();

            double theta = Math.atan2(dy, dx);

            if (theta < 0)
                theta += Math.PI * 2;

            double velocityX = Math.cos(theta)*5;
            double velocityY = Math.sin(theta)*5;
            ((Animal)o).addVelocityX(velocityX);
            ((Animal)o).addVelocityY(velocityY);
            ((Animal)o).setStun(3);
        }
            
        if(!(o instanceof Animal)){
            double dx = this.getX() - o.getX();
            double dy = this.getY() - o.getY();

            double theta = Math.atan2(dy, dx);

            if (theta < 0)
                theta += Math.PI * 2;

                // if(o.isMovable()){
            double velocityX = Math.cos(theta)*5;
            double velocityY = Math.sin(theta)*5;
            o.addVelocityX(velocityX);
            o.addVelocityY(velocityY);
            // ((Animalo.setStun(3);
                // }
            
        }
        
    }

    @Override
    public void update() {
        super.update();

        liveTimer.update(Constants.TICKS_PER_SECOND);

        if (liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}