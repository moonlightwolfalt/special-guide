package io.mopesbox.Objects.Fun;

import java.util.ArrayList;
import io.mopesbox.Objects.GameObject;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier15.Rudolph;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Client.AI.AIDeer;
import io.mopesbox.World.Room;

public class Sleigh extends GameObject {
    public GameClient owner;
    public Room room;
    public double coordY;
    public double coordX;

    public ArrayList<Animal> deers = new ArrayList<>();
    public Sleigh(int id, double x, double y, Room room, GameClient owner) {
        super(id, x, y, 80, 64);
        this.owner = owner;
        this.room = room;
        this.setCollideable(false);
        this.spawnDeers();
    }

    @Override
    public void update() {
        super.update();


        // if(this.owner == null || this == null || this.owner.getPlayer() == null || Objects.equals(this.owner.getPlayer().getX(), null) || Objects.equals(this.owner.getPlayer().getY(), null)) this.room.removeObj(this);
        if(this.owner == null || this.owner.getPlayer() == null) {
            this.room.removeObj(this, null);
            return;
        }

        this.setX(this.owner.getPlayer().getX());
        this.setY(this.owner.getPlayer().getY());
        if(this.owner.getPlayer().flag_flying) this.setObjectFlying(true);
        else
        if(this.isFlyingObject) this.setObjectFlying(false);
    }
    public void spawnDeers(){
        for(int a = 0; a < 10; a++){
            if(this.deers.size() > 3){
            AIDeer deer = new AIDeer(this.room, this, this.deers.get(a - 1), this.deers.get(a-1));
            this.room.addAI(deer);
            if(a == 10){
                ((Rudolph)deer.getPlayer()).setPlayerName("Rudolph");

                deer.setLastest(true);
            }
                      if(a == 9){
                // ((Rudolph)deer.getPlayer()).setPlayerName("Rudolph");

                deer.setLastest(true);
            }

            ((Rudolph)deer.getPlayer()).setOwner(this.owner.getPlayer());

            ((Rudolph)deer.getPlayer()).setSleigh(this);
                ((Rudolph)deer.getPlayer()).setOwner(this.owner.getPlayer());

            ((Rudolph)deer.getPlayer()).setRow(a == 0 ? 1 : a == 2 ? 2 : a == 4 ? 3 : a == 6 ? 4 : a == 8 ? 5 : 0);
            }else{
            AIDeer deer = new AIDeer(this.room, this);
            this.room.addAI(deer);
            ((Rudolph)deer.getPlayer()).setSleigh(this);
            ((Rudolph)deer.getPlayer()).setRow(a == 0 ? 1 : a == 2 ? 2 : a == 4 ? 3 : a == 6 ? 4 : a == 8 ? 5 : 0);
            if(this.deers.size() > 3){
                ((AIDeer)this.deers.get(0).getClient()).setXandY(this.deers.get(2));

                ((AIDeer)this.deers.get(1).getClient()).setXandY(this.deers.get(3));
                

            }

            }
            
            

        }
        
    }
}

// public class Amogus {
    
// }
