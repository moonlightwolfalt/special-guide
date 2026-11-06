package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.Hole;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import io.mopesbox.Networking.*;
import io.mopesbox.Client.GameClient;
public class pullfromhole extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room; 

    private Animal owner;
    private Hole hole;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 20;
    public pullfromhole(int id, double x, double y, int radius, Room room, Animal owner, Hole hole) {
        super(id, x, y, 23, radius, 0);
        this.room = room;
        this.owner = owner;
        this.hole = hole;
    }

    @Override
    public void onCollision(GameObject o) {
       
    }
    @Override 
    public boolean getCollideable(GameObject o) {
           if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && ((Animal) o).isInHole()) {
            if (((Animal)o).getInfo().getTier() < 15 && ((Animal)o).isInHole()) {
                return true;
            }
        }
        return false;
    
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
        for(Animal o : this.hole.players){
             if(this.owner == null) return;
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && ((Animal) o).isInHole()) {
            if (((Animal)o).getInfo().getTier() < 15 && ((Animal)o).isInHole()) {

                while (damage > ((Animal)o).maxHealth) { 
                    damage *= 0.8;
                }

            ((Animal)o).hurt(damage, 0, this.owner);
            // ((Animal)o).getClient().send(Networking.personalGameEvent(17, null));
            ((Animal)o).getClient().send(Networking.personalGameEvent(255, "You've been barked out by a fox!"));
            }
            ((Animal)o).setStun(5);
            double dX = o.getX() - this.getX();
            double dY = o.getY() - this.getY();
            double ang = Math.atan2(dY,dX);
            double s = Math.abs(this.getRadius() - Utils.distance(this.getX(),o.getX(),this.getY(),o.getY()));
            if(s < 10) s = 10;
            if(s > 100) s = 30;
            GameClient client = ((Animal)o).getClient();
            client.send(Networking.personalGameEvent(11, null));
            ((Animal)o).addVelocityX((Math.cos(ang)) * s);
            ((Animal)o).addVelocityY((Math.sin(ang)) * s);
            this.room.chat(client, "AHHHH!");
            damaged.add((Animal)o);
        }
        }
    }
}