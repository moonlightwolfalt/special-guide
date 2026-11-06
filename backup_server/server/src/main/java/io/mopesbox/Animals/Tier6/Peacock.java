package io.mopesbox.Animals.Tier6;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;

public class Peacock extends Animal {

    public Peacock(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(216);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setHoldAbility(true);
        info.getAbility().setUsable(true);
        radiusTimer.isRunning = false;
        this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom())));
        flyingTimer.isRunning = false;
        this.setSpeed(12);
        this.setCamzoom(2400);
    }

   

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isInHole() && !this.diveActive && !abil.isActive()) {

            if(this.flag_isClimbingHill) {
                this.running = true;
            



            abil.setActive(true);
            return;
            }
            if(!this.flag_isClimbingHill){
                canTouch = false;
                abil.setActive(true);
            }
            

            
         
           

        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    
    public boolean landing = false;

    private Timer radiusTimer = new Timer(1000);
    private Timer runningTimer = new Timer(100);
    private Timer flyingTimer = new Timer(8000);
    private Timer touchTimer = new Timer(5000);
    private boolean flying = false;
    private double step = 0;
    private double currentZ = 0;
    private boolean running = false;
    private boolean canTouch = true;

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
    @Override
    public void onCollision(GameObject o){
        if( o instanceof Animal && !this.isInHole() && !this.flag_flying && !((Animal)o).diveActive && !((Animal)o).isInHole() && !((Animal)o).isOverTree() && !((Animal)o).flag_flying && !((Animal)o).isStunned() && !canTouch) {
            ((Animal)o).setStun(5);
            ((Animal)o).getClient().send(Networking.personalGameEvent(255, "You've been hypnotized by a peacock!"));
            ((Animal)o).hurt(this.getInfo().getTier() > ((Animal)o).getInfo().getTier() ? ((Animal)o).maxHealth / 9 : ((Animal)o).maxHealth / 14, 0, this);

            if(this.getInfo().getTier() < ((Animal)o).getInfo().getTier()){
            ((Animal) o).addVelocityX(((Math.cos(Math.toRadians(((Animal)o).getAngle())) * (15))));
            ((Animal) o).addVelocityY(((Math.sin(Math.toRadians(((Animal)o).getAngle())) * (15))));
            }else if(this.getInfo().getTier() > ((Animal)o).getInfo().getTier()){
                  double dx = this.getX() - o.getX();
            double dy = this.getY() - o.getY();//do i reset? yes, i need to test, but i will give you assets for peacock

            double theta = Math.atan2(dy, dx);

            if (theta < 0)
                theta += Math.PI * 2;

            double velocityX = Math.cos(theta)*5;
            double velocityY = Math.sin(theta)*5;
            ((Animal)o).addVelocityX(velocityX);
            ((Animal)o).addVelocityY(velocityY);
            }

            
        }
  

    }

    public void disableAbility(){
        this.flying = false;
        this.flag_flying = false;
        this.setZ(0);

        canTouch = true;
        touchTimer.reset();
        landing = true;
        radiusTimer.reset();
        runningTimer.reset();
        flyingTimer.isRunning = false;
        this.getInfo().getAbility().setActive(false);

        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
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

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if(!canTouch){
            touchTimer.update(Constants.TICKS_PER_SECOND);
            if(touchTimer.isDone()){
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

        if (this.flying || this.running || this.landing && !isRammed && !pickedByBird) {
            this.setSpecies(1);
        } else {
            this.setSpecies(0);
          
            // if (this.flag_flying)
            //     this.setSpecies(2);
            // else
                // this.setSpecies(0);

        }

        if (this.flying) {
            flyingTimer.update(Constants.TICKS_PER_SECOND);
            if (flyingTimer.isDone() || this.water <= 25) {

                this.disableAbility();
            }
        }

        // if (inClaws != null && !inClaws.isDead()) {
        //     inClaws.birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 4)));
        //     inClaws.birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 4)));
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
            if (landing) {
                
                this.setZ(0);
                this.flag_flying = false;
                this.landing = false;
                this.getInfo().getAbility().setActive(false);
                // чо за скам поч
                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom())));
            }

        }

  
        
      
    }
}
