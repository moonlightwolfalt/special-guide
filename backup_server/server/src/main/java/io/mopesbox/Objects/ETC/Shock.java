package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;
import io.mopesbox.Networking.*;
public class Shock extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room; 

    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 10;
    public Shock(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 3, radius, 0);
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
        if(this.owner == null) return;
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            // if ((Animal)o).getType() != this.owner.getType()) 
            // ((Animal)o).hurt(damage + (((Animal)o).maxHealth / 4), 1, this.owner);
                // if (((Animal)o).getInfo().getTier() < this.owner.getInfo().getTier()) {
                //     int dmg = damage - (((Animal)o).maxHealth / 4);
                //     if (dmg <= 10) dmg += 15;
                //     ((Animal)o).hurt((dmg) , 1, this.owner);
             
                    ((Animal)o).hurt(((Animal)o).getInfo().getTier() < this.owner.getInfo().getTier() ? ((Animal)o).maxHealth / 6 : ((Animal)o).maxHealth / 12, 1, this.owner);
                        
            ((Animal)o).setStun(3);
            ((Animal)o).getClient().send(Networking.personalGameEvent(3, null));
            damaged.add((Animal)o);
                }
                
        
        }   
    

    @Override
    public void update() {
        super.update();
        if(this.damage > 10) damage--;
        
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    
}
}