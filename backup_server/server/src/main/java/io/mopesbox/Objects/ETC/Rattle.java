package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;

import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Rattle extends Ability {
    private Timer liveTimer = new Timer(7000);
    private Timer radius = new Timer(400);

    private Room room;
    private List<Animal> damaged = new ArrayList<>();

    public Animal owner;
    public boolean canSee = true;
    public Rattle(int id, double x, double y, int radius, Room room, Animal owner, int species) {
        super(id, x, y, 87, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
                   if(this.owner == null) return;
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying) {
            if (((Animal)o).getInfo().getTier() <= 15) {

        
                if(owner.getInfo().getAnimalSpecies() == 2){
                ((Animal)o).hurt(3, 1, this.owner);

                }

                ((Animal)o).hurt(2, 1, this.owner);
            }
            ((Animal)o).setStun(3);
         
                        damaged.add((Animal)o);

            
                 
            }
        }
    

       @Override
    public void update() {
        super.update();
        radius.update(Constants.TICKS_PER_SECOND);
        if(radius.isDone()){
            this.setRadius(getRadius() + 15);
            radius.reset();
        }
        
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}