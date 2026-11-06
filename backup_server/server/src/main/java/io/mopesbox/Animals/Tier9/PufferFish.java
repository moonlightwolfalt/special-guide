package io.mopesbox.Animals.Tier9;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Cactus;

public class PufferFish extends Animal {
    public PufferFish(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(247.5);
        this.baseRadius = Tier.byOrdinal(info.getTier()).getBaseRadius();
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        info.getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2100);
    }
    public boolean canTouch = true;
    public Timer timeAfterAbil = new Timer(900);
    private boolean canRunTimer  = false;

    @Override
    public void onCollision(GameObject o){
        if(((o instanceof Animal))){

        if(this.getCollideableIs(o) && this.getInfo().getAnimalSpecies() == 2 && canTouch && !((Animal)o).isStunned()){
            ((Animal)o).setStun(3);
            ((Animal)o).hurt(((Animal)o).maxHealth / 9, 10, this);
        }
        if(this.getCollideableIs(o) && !canTouch && !((Animal)o).isStunned()){
         

            Animal biter = (Animal) o;
            double dmg = 0;

            if(this.getInfo().getAnimalSpecies() == 2) dmg = biter.maxHealth / 6;
            else dmg = biter.maxHealth / 12;

            biter.hurt(dmg, 0, this);
            biter.setStun(4);
            
            if(this.getInfo().getAnimalSpecies() == 2) biter.setBleeding(3, this, biter.maxHealth / 10);

       

            // biter.hurt(10, 14, null);
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            xx = this.getX() + xx;
            yy = this.getY() + yy;
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            theta -= 180;
            if(theta < 0) theta += 360;
            double x = ((Math.cos(Math.toRadians(theta)) * (8)));
            double y = ((Math.sin(Math.toRadians(theta)) * (8)));
            this.addVelocityX(x);
            this.addVelocityY(y);
            theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            x = ((Math.cos(Math.toRadians(theta)) * (8)));
            y = ((Math.sin(Math.toRadians(theta)) * (8)));
            o.addVelocityX(x);
            o.addVelocityY(y);
        }else if(o instanceof Cactus){
            if(this.getInfo().getAbility().isActive())
            this.disableAbility();
        }
         
        }
    }
    public void onHurt(){
        this.disableAbility();
    }
    public void timeAfterAbilEnd(){
        if(canRunTimer){
             timeAfterAbil.update(Constants.TICKS_PER_SECOND);
            //  this.setMove(true);
             if(timeAfterAbil.isRunning){ 
             }else{ 
                timeAfterAbil.reset();
                canRunTimer = false;
                this.setMove(true);
             }
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
  
    @Override
    public void update(){
super.update();
abilityRecharge();
timeAfterAbilEnd();

if(this.getInfo().getAbility().isActive()) this.setRadius(this.baseRadius);




    }
 
    public void disableAbility(){
        PlayerAbility abil = this.getInfo().getAbility();
        timeAfterAbil.reset();
        canRunTimer = true;
        canTouch = true;
        this.baseRadius = Tier.byOrdinal(getInfo().getTier()).getBaseRadius();
        this.setRadius(baseRadius);
          
                        
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
            abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
        timeAfterAbil.reset();
        abil.setActive(false);
           
    }

    

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
            this.baseRadius = 50;
            canTouch = false;
            abil.setActive(true);
            
            abilTimer.reset();
        }
    }

    
    


        

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if(abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if(abilTimer.isDone()) {
                abilT--;
                if(abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                }
                abilTimer.reset();
            }
        }
    }
    
    
        

}
