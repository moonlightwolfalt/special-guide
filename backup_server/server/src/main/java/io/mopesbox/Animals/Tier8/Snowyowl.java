package io.mopesbox.Animals.Tier8;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.Target;

public class Snowyowl extends Animal {
    public Snowyowl(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        radiusTimer.isRunning = false;
        flyingTimer.isRunning = false;
        this.setSpeed(12);
        this.bypassAbilSpeed = true;
        this.setCamzoom(1900);
        this.setMaxHealth(268.5);
        this.setArcticAnimal(true);
    }

    public boolean landing = false;
    public boolean attacking = false;
    private Animal inClaws = null;

    private Timer radiusTimer = new Timer(1000);
    private double step = 0;
        private Timer timeAfterPressW = new Timer(4000);

    private double currentZ = 0;

    @Override
    public void onCollisionEnter(GameObject o){
        // if(this.running){
        //      if (o instanceof Animal) {
        //                 if (!((Animal) o).pickedByBird && !((Animal) o).flag_flying
        //                         && !((Animal) o).isStunned()) {
        //                             Animal ani = (Animal) o;
        //                             int tier = ani.getInfo().getTier();
                                  
        //                     if((this.getInfo().getSkin() == 0 && tier > this.getInfo().getTier()) || (this.getInfo().getSkin() == 1 && tier > 16))
        //                         return;

        //                     if ((ani.getInfo().getType() == this.getInfo().getType()) && ani.getInfo().getAbility().isActive())
        //                             return;

        //                     pickedUp = true;
        //                     ((Animal)o) = (Animal) o;
        //                     runningTimer.isRunning = false;
        //                     // ((Animal)o).getClient().getRoom().chat(((Animal)o).getClient(),
        //                     // "Oh no! I got picked up by ptero. :insert here epic flying:");

        //                     return;
        //                 }
        //             }
        
        // }
        
                if(this.landing){
                if(o instanceof Animal && !((Animal)o).isInHole() && !((Animal)o).flag_flying && !((Animal)o).inArena && !((Animal)o).isDiveActive() && !((Animal)o).isStunned() && inClaws == null){
                    ((Animal)o).setStun(3);
                    inClaws = ((Animal)o);
                    ((Animal)o).hurt(this.getInfo().getTier() > ((Animal)o).getInfo().getTier() ? ((Animal)o).maxHealth / 128 : ((Animal)o).maxHealth/128, 0, this);
                }
                
            
        }
    }
    @Override
    public void onCollision(GameObject o){
                if(this.landing){
                if(o instanceof Animal && !((Animal)o).isInHole() && !((Animal)o).flag_flying && !((Animal)o).inArena && !((Animal)o).isDiveActive() && !((Animal)o).isStunned() && inClaws == null){
                    ((Animal)o).setStun(2);
                    inClaws = ((Animal)o);
                    ((Animal)o).hurt(this.getInfo().getTier() > ((Animal)o).getInfo().getTier() ? ((Animal)o).maxHealth / 128 : ((Animal)o).maxHealth/128, 0, this);
                }
                
            
        }
    }
    int count = 0;
    @Override
    public void update() {
        super.update();
        if(inClaws != null){
        double x = ((Math.cos(Math.toRadians(this.getAngle()))
            * (inClaws.getRadius() / 2 + (this.getRadius() / 1.5))));
    double y = ((Math.sin(Math.toRadians(this.getAngle()))
            * (inClaws.getRadius() / 2 + (this.getRadius() / 1.5))));
    inClaws.setX(this.getX() - x);
    inClaws.setY(this.getY() - y);
    inClaws.pickedByBird = true;
    inClaws.flag_flying = true;
    if(!inClaws.isStunned()) inClaws.setStun(2);
        }
      
        abilityRecharge();
        if(this.attacking){
            this.timeAfterPressW.update(Constants.TICKS_PER_SECOND);
            if(this.timeAfterPressW.isDone()){
                this.disableAbility();
            }
          
        }
        if (this.running) {
            runningTimer.update(Constants.TICKS_PER_SECOND);
 
            if (runningTimer.isDone()) {
                this.running = false;
                this.flying = true;
                landing = false;
                step = Math.round((this.getRadius() * 2) / (1000 / Constants.TICKS_PER_SECOND));
                if (step < 1)
                    step = 1;

                radiusTimer.reset();
                flyingTimer.reset();
                this.flag_flying = true;

            }
        }
        
        if(this.attacking){
         
            
            if(this.getClient().snowyowlTarget != null && this.getClient().snowyowlTarget.isFreezed() && !this.getClient().snowyowlTarget.getCollidedList().contains(this)){
                            this.setMove(false);
                                            this.ability_mark = true;


            this.setMouseX(this.getClient().snowyowlTarget.getX());
            this.setMouseY(this.getClient().snowyowlTarget.getY());
            this.setSpecies(3);
            
            }

        }

        if (this.flying || this.running) {
            if(!this.attacking && !this.isRammed && !pickedByBird)
            this.setSpecies(1);
            
        }
         if(!attacking){
            if(!this.flying && !this.landing)
            this.setSpecies(0);
          
        

        }
         if(this.attacking){
            if(this.flying && !this.landing && !isRammed && !pickedByBird)
            this.setSpecies(2);
        }
        if(this.landing && !this.attacking && !isRammed && !pickedByBird){
            this.setSpecies(3);
        }

        if (this.flying) {
            flyingTimer.update(Constants.TICKS_PER_SECOND);
            if(this.getClient().snowyowlTarget == null){

                    this.getClient().snowyowlTarget = new Target(this.getClient().getRoom().getID(), this.getMouseX() + 80, this.getMouseY() + 80, this.getClient(), 2, 1);
                 this.getClient().getRoom().addObj(this.getClient().snowyowlTarget);
                                  this.getClient().snowyowlTarget.setFreeze(false);
            }
            
            if (flyingTimer.isDone() || this.water <= 25) {

                this.disableAbility();
            }
        }

        // if (((Animal)o) != null && !((Animal)o).isDead()) {
        //     ((Animal)o).birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 4)));
        //     ((Animal)o).birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 4)));
        // }

        radiusTimer.update(Constants.TICKS_PER_SECOND);
        if (!radiusTimer.isDone()) {

            if (landing) {
            
                // decrease size to before size
                currentZ -= step;
                if (currentZ >= 0)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = 0;

                    

                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));
                
            } else {
                currentZ += step;
                if (currentZ < this.getRadius() * 2)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = this.getRadius() * 2;

                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));

            }

        } else {
            if (landing && !attacking) {
                
                this.setZ(0);
                this.flag_flying = false;
                this.flying = false;
                this.landing = false;
                // this.disableAbility();
                this.inClaws = null;
                this.getInfo().getAbility().setActive(false);
                // чо за скам поч
                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom())));
            }

        }

    }



    @Override
    public double speedManipulation(double speed) {
        if (this.getInfo().getSkin() == 2) {
        if (this.flying)
            return speed * 1.5 * 1.5;
        else if (this.running)
            return speed * 1.3 * 1.5;
        else
            return speed;
        }
        if (this.getInfo().getSkin() == 3) {
            if (this.flying)
                return speed * 1.5 * 1.25;
            else if (this.running)
                return speed * 1.3 * 1.25;
            else
                return speed;
            }
        if (this.flying)
            return speed * 1.5 * 1.25;
        else if (this.running)
            return speed * 1.3 * 1.25;
        else
            return speed;
        }

    public boolean flying = false;
    private boolean running = false;

    private Timer runningTimer = new Timer(100);
    private Timer flyingTimer = new Timer(8000);

    public void useAbil() {
        
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.attacking) {

                    // if(this.getClient() != null) this.getClient().send(Networking.personalGameEvent(255, "FLYING "+flying + " landing "+landing + " running " + running));

            abil.setActive(true);
            this.running = true;
            this.setCamzoom(1000);

            // if(this.getClient().falconTarget == null){
         

            
           
        //    System.out.print("a\n");

         

        } 
       

    }
    @Override
    public void onAbilityDisable(){
        if(this.getClient().snowyowlTarget != null){
        this.getClient().getRoom().removeObj(this.getClient().snowyowlTarget, null);
        this.getClient().snowyowlTarget = null;
        }
    }
    public void handleWwhenFlying(){
         if(!attacking && this.flying && this.getClient().snowyowlTarget != null){
            this.setSpecies(2);
            attacking = true;
            this.getClient().snowyowlTarget.setFreeze(true);

        }
    }

    public void disableAbility() {
      
        this.flying = false;
                this.attacking = false;
                            this.setMove(true);
                            this.ability_mark_set(false);


                    this.timeAfterPressW.reset();

        count = 0;
        running = false;

        landing = true;
        radiusTimer.reset();
        runningTimer.reset();
        flyingTimer.isRunning = false;
        runningTimer.isRunning = false;

        this.getClient().getRoom().removeObj(this.getClient().snowyowlTarget, null);
        this.getClient().snowyowlTarget = null;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 12;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
        if(inClaws != null){
        inClaws.pickedByBird = false;
    inClaws.flag_flying = false;
                }
                this.inClaws = null;
    }

    private int abilT = 0;
    // да. чо это за шыза поч оно вообще работает
    private Timer abilTimer = new Timer(500);

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
}
