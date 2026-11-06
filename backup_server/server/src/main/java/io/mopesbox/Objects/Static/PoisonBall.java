package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.World.Room;

public class PoisonBall extends GameObject{
    private Animal owner = null;
    private int speed = 20;
    private Room room;
    private Waterspot origin;
    private Animal author;
    private double mainAngle;
    public PoisonBall(int id, double x, double y, int biome, Room room, Animal Author, double angle, double mainAngle) {
        super(id, x, y, 10, 59);
        this.setBiome(biome);
        this.setMovable(true);
        this.room = room;
        this.author = Author;
        this.mainAngle = mainAngle;
    }


    
    

    private Timer liveTimer = new Timer(9000);


    public boolean collideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
            if(((Animal)o).getInfo().getTier()  < 15) return true;
            if(((Animal)o).getInfo().getType() == AnimalType.COBRA) return false;
            if(((Animal)o).getInfo().getType() == AnimalType.GIANTSPIDER) return false;
            if(((Animal)o) == this.author) return false;
    
        }
        return false;
    }

    @Override
    public boolean getCollideable(GameObject o) {
     
        return false;
    }
    @Override
    public boolean canBeVisible(GameClient c){
        return true;
    }
    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(o instanceof Animal){
            if(o == this.author){
            }else if(this.collideableIs(o) && !((Animal)o).flag_eff_poison){
                ((Animal)o).setPoisoned(3, this.owner, ((Animal)o).maxHealth / 12);
                this.room.removeObj(this, ((Animal)o));
            }
        }
       
    }

    @Override
    public void update() {
        super.update();
        
        if(this.origin == null) {
            if(this.speed > 0) {
                this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.speed))));
                this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.speed))));
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
