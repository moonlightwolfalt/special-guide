package io.mopesbox.Animals.Tier12;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import java.util.Map;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.Target;
import io.mopesbox.Objects.Static.Hill;
import io.mopesbox.Objects.Static.Rock;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;

public class Markhor extends Animal {
    private double mainSpeed = 7;

    public Markhor(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.speed = mainSpeed;

        this.biteDamage = 60;
        this.setCanClimbHills(true);
        this.setCanClimbRocks(true);
        this.setMaxHealth(279);
        this.bypassAbilSpeed = true;
        this.setArcticAnimal(true);
        this.setCamzoom(1800);

        runTimer.isRunning = false;
    }

    public void onHurt() {
        if (this.getInfo().getAbility().isActive())
            this.disableAbility();
    }

    private boolean readyToHit = false;
    private boolean isRunning = false;
    private double runSpeed = 0;
    private double backSpeed = 0;

    private int abilT = 0;

    public boolean isUp = false;
    public int height = 0;
    private Timer abilTimer = new Timer(950);


    public void abilityRecharge() {
        if (abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if (abilTimer.isDone()) {
                abilT--;
                if (abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                }
                abilTimer.reset();
            }
        }
    }

    public int countbeforejump = 0;

    public boolean jumping = false;
    public boolean readyToJump = false;
    public Timer runTimer = new Timer(1500);

                    public int collidedHils = 0;

        public Timer abilFinish = new Timer(2000);

    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.ability_mark_set(false);

        this.isRunning = false;
        this.setMove(true);
        this.readyToJump = false;
        this.height = 0;
        this.setZ(0);
        this.readyToHit = false;
        this.setMove(true);
        this.collidedHils = 0;
        runSpeed = 0;
        backSpeed = 0;
        this.speed = mainSpeed;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }
    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }


    private void runningUpdate() {//In this case running will be the "turn back" of markhor

        if (this.isRunning) {
            this.getInfo().getAbility().setActive(true);
            this.setSpecies(1);
            this.setMove(false);
            if(this.runSpeed < 3){
            double backX = backSpeed * Math.cos(this.getAngle());
            double backY = backSpeed * Math.sin(this.getAngle());
            this.setVelocityX(this.getVelocityX()-backX);
            this.setVelocityX(this.getVelocityY()-backY);
            }

            if(backSpeed <= 2){
                this.backSpeed++;
            }

            if (runSpeed < 10) {
                runSpeed++;
                
            } else {
                if (runTimer.isDone()) {
                    this.setSpecies(2);
                    this.isRunning = false;
                    this.setMove(true);
                    this.speed = this.speed + runSpeed;
                    this.readyToHit = true;
                } else {
                    runTimer.update(Constants.TICKS_PER_SECOND);
                }
           
            }
        }
             if(readyToHit){
                    abilFinish.update(Constants.TICKS_PER_SECOND);
                    if(abilFinish.isDone()){
                        disableAbility();
                        abilFinish.reset();
                    }

                    
                }
                if(this.readyToJump){
                
            double backX = backSpeed * Math.cos(this.getAngle());
            double backY = backSpeed * Math.sin(this.getAngle());
            this.setVelocityY(this.getVelocityX()-backX);
            this.setVelocityX(this.getVelocityY()-backY);
            this.countbeforejump++;
            if(this.countbeforejump > 2){
                this.readyToJump = false;
                this.jumping = true;
                this.setVelocityX(0);
                this.setVelocityY(0);
            }
            //double MathStartTargetX = Math.abs(this.getClient().markhorTarget.getX() - this.getX());
//double MathStartTargetY = Math.abs(this.getClient().markhorTarget.getY() - this.getY());
                }
                if (!this.jumping) {
                    if (this.getClient().markhorTarget != null) {
                                        this.speed = 10;
                    }
                }
                if(this.jumping){
                  
                    if(this.getClient().markhorTarget != null){
                    this.isUp = true;
                    //this.speed = Math.abs(this.getClient().markhorTarget.getX() - this.getX())/10;
                    this.flag_flying = true;
                    this.ability_mark_set(true);
                    this.setMove(false);
                    this.setMouseX(this.getClient().markhorTarget.getX());
                    this.setMouseY(this.getClient().markhorTarget.getY());
         //double MathStartTargetX = java.lang.Math.abs(this.getClient().markhorTarget.getX() - this.getX());
         //double MathStartTargetY = java.lang.Math.abs(this.getClient().markhorTarget.getY() - this.getY());
//double TargetDistanceX = this.getClient().markhorTarget.getX() - this.getX();
//double TargetDistanceY = this.getClient().markhorTarget.getY() - this.getY();
                    }
                

              

                     
            }
            
              if(this.isUp) {

                    if(this.height > 30 || (Math.abs(this.getClient().markhorTarget.getX() - this.getX()) + Math.abs(this.getClient().markhorTarget.getY() - this.getY())) < this.speed*10) {

                this.height -= (5 - this.speed/30000 * 10)/2;
              
              
                       if (this.getZ() > 0) {
             this.setZ(this.height);
                       }

               
                    }else{
                    this.height += 5 - this.speed/30000 * 10;
                    this.setZ(this.height);
                    }


                if(this.collidedList.contains(this.getClient().markhorTarget) && this.isUp) {this.isUp = false; this.jumping = false;}
                   if(this.getZ() <= 1) {this.setZ(0); this.isUp = false; this.jumping = false;}
                } else {
                    if(this.getClient().markhorTarget != null && this.height > 0 && (this.collidedList.contains(this.getClient().markhorTarget) || this.getZ() <= 1)){
                      this.getClient().getRoom().removeObj(this.getClient().markhorTarget, null);
                    this.isUp = false;
                    this.getClient().markhorTarget = null;
                     this.jumping = false;
                this.flag_flying = false;
                            this.disableAbility();

                    this.setMove(true);
                    this.ability_mark_set(false);
                    for(int a = 0; a < 10; a++){
                    if(this.height > 0) this.height--;
                    if(this.height < 0) this.height = 0;
                    this.setZ(this.height);

                    }
                    
                    
                    }
                    

                }

                 
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 13 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
        }
        if (o instanceof Animal) {
            if (!((Animal) o).flag_flying && !((Animal) o).isInHole() && !this.isInHole() && !this.flag_flying
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.runSpeed <= 5)
                    return;
                    if(((Animal)o).getInfo().getTier() > this.getInfo().getTier()){
                    ((Animal)o).setStun(3);
                    ((Animal)o).hurt(((Animal)o).maxHealth / 15, 0, this);
                    return;
                    }
                // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
                    // return;
                if (this.readyToHit && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
                    Animal obj = (Animal) o;
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 10)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 10)));
                    obj.makeRam(x, y, ((Animal) this), 5);
                    
                    
                    disableAbility();
                }
            }
        }
    }
    @Override
    public void onCollisionExit(GameObject o){
        boolean isFine = false;
        if(this.getInfo().getAbility().isActive()) return;
        if(o instanceof Rock || o instanceof Hill){
            for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
                GameObject a = entry.getValue();
                 if(a instanceof Rock || a instanceof Hill){
                    isFine = true;

                }
            }
         
            }
            if(isFine == false){
                if(this.getClient().markhorTarget != null){
                    this.getClient().getRoom().removeObj(this.getClient().markhorTarget, null);
                    this.isUp = false;
                    this.getClient().markhorTarget = null;
                    this.disableAbility();
                }
        }
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        super.onCollisionEnter(o);
        if (o instanceof Animal) {
            if (!((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.runSpeed <= 5)
                    return;
                // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
                    // return;
                if (this.readyToHit
                        && ((o instanceof Markhor && this.runSpeed > ((Markhor) o).runSpeed - 1)
                                || !(o instanceof Markhor))
                        && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)
                        && this.runSpeed >= 10) {
                    Animal obj = (Animal) o;
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 10)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 10)));
                    obj.makeRam(x, y, ((Animal) this), 5);
                    disableAbility();
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        runningUpdate();
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (!this.isDiveMain() && !this.isDiveActive()) {
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning && !abil.isActive()) {
                for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
                    GameObject a = entry.getValue();
                      if(a instanceof Hill || a instanceof Rock){
                        this.collidedHils++;
                    }
                }
                if(this.getClient().markhorTarget != null){
                    this.getClient().markhorTarget.setFreeze(true);
                    this.readyToJump = true;
                    this.getInfo().getAbility().setActive(true);
                    return;
                    
            }
                if(collidedHils > 0){
                    this.getClient().markhorTarget = new Target(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getClient(), 2, 1);
                    this.getClient().getRoom().addObj(this.getClient().markhorTarget);
                    this.getClient().markhorTarget.setFreeze(false);

                    return;
                }
                runTimer.reset();
                this.isRunning = true;
            }
        }
    }
      @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);

        writer.writeUInt8(this.getInfo().getAbility() != null ? this.getInfo().getAbility().isActive() && !this.isDiveActive() && !this.isUp? 1 : 0 : 0);

        writer.writeUInt8(this.isUp  ? 1 : 0);//Target

    }
    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);

                writer.writeUInt8(this.getInfo().getAbility() != null ? this.getInfo().getAbility().isActive() && !this.isDiveActive()  && !this.isUp? 1 : 0 : 0);
        writer.writeUInt8(this.isUp  ? 1 : 0);//Target

    }
}
