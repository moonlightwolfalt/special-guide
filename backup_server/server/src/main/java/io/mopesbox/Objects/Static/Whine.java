package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.World.Room;

public class Whine extends Ability{
    private Animal owner = null;
    private int speed = 8;
    private int speed2 = 15;
    private Room room;
    private Animal author;
    private double mainAngle;
    public Whine(int id, double x, double y, int biome, Room room, Animal Author, double angle, double mainAngle) {
        super(id, x, y, 81, 20, 0);
        this.setMovable(true);
        this.room = room;
        this.author = Author;
        this.mainAngle = mainAngle;
    }


    
    

    private Timer liveTimer = new Timer(4000);


    public boolean collideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
            if(((Animal)o).getInfo().getTier()  < 15) return true;
            if(((Animal)o).getInfo().getType() == AnimalType.FENNECFOX) return false;
            if(((Animal)o) == this.author) return false;
    
        }
        return false;
    }

    @Override
    public boolean getCollideable(GameObject o) {
     
        return false;
    }
    @Override
    public boolean canBeVisible(GameClient a){
        return true;
    }
  
    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(o instanceof Animal){
            if(o == this.author){
            }else if(this.collideableIs(o) && !((Animal)o).isStunned()){
                ((Animal)o).setStun(4);
            }
        }
       
    }

    @Override
    public void update() {
        super.update();
        
            if(this.speed2 > 0) {
                this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.speed))));
                this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.speed))));
                this.speed2--;
            }
                 liveTimer.update(Constants.TICKS_PER_SECOND);
                            if(liveTimer.isDone()) {
                                this.room.removeObj(this, null);
                                liveTimer.reset();
                            }
    }

    public Animal getOwner() {
        return owner;
    }
}
