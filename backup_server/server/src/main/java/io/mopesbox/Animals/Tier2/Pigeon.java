package io.mopesbox.Animals.Tier2;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;

public class Pigeon extends Animal {
    public Pigeon(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        radiusTimer.isRunning = false;
        flyingTimer.isRunning = false;
        this.getInfo().getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2800);
        this.bypassAbilSpeed = true;
        this.setMaxHealth(165);
    }

    public boolean landing = false;

    private Timer radiusTimer = new Timer(1000);
    private double step = 0;
    private double currentZ = 0;

    @Override
    public void onCollision(GameObject o){
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
        //                     inClaws = (Animal) o;
        //                     runningTimer.isRunning = false;
        //                     // inClaws.getClient().getRoom().chat(inClaws.getClient(),
        //                     // "Oh no! I got picked up by ptero. :insert here epic flying:");

        //                     return;
        //                 }
        //             }
        
        // }
    }
    @Override
    public void update() {
        super.update();
        abilityRecharge();
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

        if (this.flying || this.running || this.landing && !pickedByBird && !isRammed) {
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

    @Override
    public double speedManipulation(double speed) {
        if (this.getInfo().getSkin() == 1) {
            // if(this.flying && )
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

    private boolean flying = false;
    private boolean running = false;

    private Timer runningTimer = new Timer(100);
    private Timer flyingTimer = new Timer(8000);

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {

            abil.setActive(true);
            this.running = true;
           

            // double x = ((Math.cos(Math.toRadians(this.getAngle())) *
            // (this.getRadius())));
            // double y = ((Math.sin(Math.toRadians(this.getAngle())) *
            // (this.getRadius())));
            // WhaleTailSlap tailslap = new
            // WhaleTailSlap(this.getClient().getRoom().getID(), this.getX()-x,
            // this.getY()-y, ((int) Math.round(this.getRadius()*1.5)),
            // this.getClient().getRoom(), this.getAngle(), this,
            // this.getInfo().getAnimalSpecies());
            // this.getClient().getRoom().addObj(tailslap);

        } 

    }

    public void disableAbility() {
      
        this.flying = false;

        landing = true;
        radiusTimer.reset();
        runningTimer.reset();
        flyingTimer.isRunning = false;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 10;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    private int abilT = 0;
    // да. чо это за шыза поч оно вообще работает
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
}
