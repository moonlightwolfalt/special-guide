package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class sweatPoison extends GameObject{
    private Animal owner = null;
    private int speed = 0;
    private Room room;
    private double aangle;
    private Waterspot origin;
    private Animal author;
    public sweatPoison(int id, double x, double y, int biome, Room room, Animal Author) {
        super(id, x, y, 12, 85);
        this.setBiome(biome);
        this.setMovable(true);
        this.room = room;
        this.author = Author;
    }


    
    

    private Timer liveTimer = new Timer(9000);


    public boolean collideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
            if(((Animal)o).getBarType() != 0) {
                return false;
            }
            if(((Animal)o) == this.owner && this.speed > 10) {
                return false;
            }
            if(((Animal)o).water == ((Animal)o).maxwater) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean getCollideable(GameObject o) {
     
        return false;
    }
    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(o instanceof Animal){
            if(o == this.author){
                ((Animal)o).getClient().send(Networking.personalGameEvent(255, "You cant eat your own sweat"));
            }else{
                ((Animal)o).addWater(5);
                this.room.removeObj(this, o);
            }
        }
       
    }

    @Override
    public void update() {
        super.update();
        
        if(this.origin == null) {
            if(this.speed > 0) {
                this.setVelocityX(((Math.cos(Math.toRadians(this.aangle)) * (this.speed))));
                this.setVelocityY(((Math.sin(Math.toRadians(this.aangle)) * (this.speed))));
                this.speed--;
            } else {
                liveTimer.update(Constants.TICKS_PER_SECOND);
                if(liveTimer.isDone()) {
                    this.room.removeObj(this, null);
                    liveTimer.reset();
                }
            }
        }
    }

    public Animal getOwner() {
        return owner;
    }
}
