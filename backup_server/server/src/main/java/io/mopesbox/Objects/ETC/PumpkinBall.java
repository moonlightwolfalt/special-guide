package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;

import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Water;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class PumpkinBall extends GameObject {

    public int is1v1Target;
    private boolean isRolling = false;
    public Animal owner;
    private Timer bonusTimer = new Timer(5000);
    private Timer liveTimer = new Timer(100000);
    private Room room;
    public Animal oldOwner;


    public PumpkinBall(int id, double x, double y, int type, Room room) {
        super(id, x, y, 25, 131);
        this.setSpecies(type);
        this.setMovable(true);
        this.room = room;
        
        
         
        
    }

    
    

    

    


    public void takeBonus() {
        if(bonusTimer.isDone()) {
            if(this.owner != null && this.owner.getClient() != null) {
                int xp = 5;
                if (this.getSpecies() == 0) xp = this.owner.getClient().getXP()/500;
                else if (this.getSpecies() == 1) xp = this.owner.getClient().getXP()/50;
                else if (this.getSpecies() == 2) xp = this.owner.getClient().getXP()/100;
                if (xp < 5) xp = 5;
                int coins = 5;
                if (this.getSpecies() == 0) coins = 2;
                else if (this.getSpecies() == 1) coins = 5;
                else if (this.getSpecies() == 2) coins = 5;
                if (coins < 5) coins = 5;
                this.owner.getClient().addXp(xp);
                this.owner.getClient().addCoins(coins);
                this.owner.getClient().addEXP(coins);
                bonusTimer.reset();
            }
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(this.getObjectFlying()) return false;
        if(o instanceof Animal) {
            if(getCollideableIs(o)) return true;
            return false;
        }
        if(o instanceof PumpkinBall || o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
            if(((Animal)o) == this.owner) return false;
            if(((Animal)o).getClient().isBot()) return false;

            



        }
        return true;
    }

    private int speed = 0;
    private boolean isKicked = false;

    public void Throw() {
        this.oldOwner = this.owner;
        this.setOwner(null);
        this.speed = 20;
        this.abandonTimer.reset();
        
    }

    public void Kick() {
        this.setOwner(null);
        this.speed = 30;
        this.isUp = true;
        this.isKicked = true;
        this.abandonTimer.reset();
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        super.onCollisionEnter(o);
        if(abandonTimer.isDone() && this.owner == null && getCollideableIs(o) && o instanceof Animal && ((Animal)o).canHandleMorePumpkins()) {
            Animal biter = (Animal) o;
            this.setOwner(biter);
            if(this.owner.pumpkinBalls.size() > 1){
                for(PumpkinBall balls : this.owner.pumpkinBalls){
                    balls.Throw();
                }
            }
        }
        if(o.getType() == 9 || o.getType() == 13){
            if(this.owner != null){
                this.room.chat(this.owner.getClient(), "GOALLLLLLLLLLLL!");
                                this.room.goals++;

                this.room.chat(this.owner.getClient(), "You were the goal #" +this.room.goals);
                                this.room.chat(this.owner.getClient(), "BONUS: 150 Coins and 50Exp!");


                if(this.getSpecies() == 1){this.room.land.goldenpumking--;}else if(this.getSpecies() == 0){this.room.land.pumking--;}
                this.owner.getClient().addCoins(150);
                this.owner.getClient().addXp(this.owner.getClient().getXP() / 5);
                this.owner.getClient().addEXP(50);
                this.room.pumpDots.remove(this);
                this.room.removeObj(this, o);
                
                liveTimer.reset();
            }
        }
   
    }


    public void setOwner(Animal owner) {
        if(owner == null)
            if(this.owner != null && this.owner.pumpkinBalls != null) {
            this.owner.pumpkinBalls.remove(this);

                this.isRolling = false;
            }
        this.owner = owner;
        if(this.owner != null && this.owner.pumpkinBalls != null)
            owner.pumpkinBalls.add(this);
    }

    private Timer abandonTimer = new Timer(500);
    private boolean isUp = false;
    private int height = 0;

    @Override
    public void update() {
        super.update();
        if(this.isKicked) {
            this.setObjectFlying(true);
        } else {
            this.setObjectFlying(false);
        }
        if(this.owner != null){
        if(this.owner.flag_flying || this.owner.isRammed || this.owner.inArena){
            this.Throw();
            
        }
    }
        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed--;
            this.isRolling = true;
            if(this.isKicked) {
                if(isUp) {
                    height++;
                    if(height > 15) isUp = false;
                } else {
                    if(height > 0) height--;
                }
                this.setZ(height);
            }
            if(this.speed <= 0) {
                this.isKicked = false;
            }
        }
        abandonTimer.update(Constants.TICKS_PER_SECOND);
        bonusTimer.update(Constants.TICKS_PER_SECOND);
        if(this.owner != null) {
            double speedCoefficient = Utils
                    .normalize(Utils.constrain(Utils.distance(this.owner.getX(), this.owner.getMouseX(), this.owner.getY(), this.owner.getMouseY()),
                            0.00001, this.owner.getRadius()), 0.00001, this.owner.getRadius());
            if (speedCoefficient < 0)
                speedCoefficient = 0;
            if (speedCoefficient > 0.15)
                this.isRolling = true;
            else
                this.isRolling = false;
            
            if(this.owner.isDead()) {
                this.setOwner(null);
            }
        } else if(this.speed < 1) {
            this.isRolling = false;
        }

            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.pumpDots.remove(this);


                if(this.getSpecies() == 0){
                this.room.land.pumking--;
                }else{              
this.room.land.goldenpumking--;


                }
                this.room.removeObj(this, null);

       
                liveTimer.reset();
            }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writer.writeUInt8((int) (this.getAngle()/3));
        writer.writeBoolean(this.isRolling);
        writer.writeUInt8(this.getSpecies());
        writer.writeUInt16((short) this.getX());
        writer.writeUInt16((short) this.getY());
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        writer.writeUInt8((int) (this.getAngle()/3));
        writer.writeBoolean(this.isRolling);
        writer.writeUInt8(this.getSpecies());

        writer.writeUInt16((short) this.getX());
        writer.writeUInt16((short) this.getY());
    }
}
