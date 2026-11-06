package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class HyenaAbil extends Ability {
    private Timer liveTimer = new Timer(3000);
    private Room room; // крутой ток стой
    private int speed;

    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 10;
    public HyenaAbil(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species, int speed) {
        super(id, x, y, 86, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setAngle(angle);
        this.setSendsAngle(true);
        this.speed = speed;
    }

    @Override
    public void onCollision(GameObject o) {
        if(this.owner == null) return;
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            // if ((Animal)o).getType() != this.owner.getType()) 
            // ((Animal)o).hurt(damage + (((Animal)o).maxHealth / 4), 1, this.owner);
            if (((Animal)o).getInfo().getTier() != this.owner.getInfo().getTier()) { 
                // if (((Animal)o).getInfo().getTier() < this.owner.getInfo().getTier()) {
                //     int dmg = damage - (((Animal)o).maxHealth / 4);
                //     if (dmg <= 10) dmg += 15;
                //     ((Animal)o).hurt((dmg) , 1, this.owner);
                // if (((Animal) {

                    while (damage > ((Animal)o).maxHealth) { 
                        damage *= 0.8;
                    }
                    ((Animal)o).getClient().getRoom().chat(((Animal)o).getClient(), "AHHHHHHH!");

                    ((Animal)o).hurt(damage, 10, this.owner);
                
                // }
            }
            ((Animal)o).setStun(3);
            damaged.add((Animal)o);
        }
    }

    @Override
    public void update() {
        super.update();
        if (speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (speed)))); // для скама мамонта
        }
        if(this.damage > 10) damage--;
        
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}