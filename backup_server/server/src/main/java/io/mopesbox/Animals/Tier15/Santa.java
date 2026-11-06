package io.mopesbox.Animals.Tier15;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
public class Santa extends Animal {
    public int mainAbility = 0;
    public Santa(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setMaxHealth(300);
        this.setCanClimbRocks(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        radiusTimer.isRunning = false;
        flyingTimer.isRunning = false;
        this.setSpeed(8);
        getClient().setCamzoom(1200);
        this.bypassAbilSpeed = true;
    }

    public boolean landing = false;

    private Timer radiusTimer = new Timer(1000);
    private double step = 0;
    private double currentZ = 0;

    private boolean pickedUp = false;
    private Animal inClaws;

    public void damage(int damage, Animal biter) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        killer = biter;
    }

    private Animal killer;

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }


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
        if(this.health <= 0) {
            if(this.killer != null && this.killer.getClient() != null)
                this.killer.getClient().addXp(this.getClient().getXP());
        }
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

        if (this.flag_flying) {
            this.setSpecies(2);
        } else {
          
            // if (this.flag_flying)
            //     this.setSpecies(2);
            // else
                this.setSpecies(0);

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
        if (this.flying)
            return speed * 6 * 1.25;
        else if (this.running)
            return speed * 3 * 1.25;
        else
            return speed;
        }

    private boolean flying = false;
    private boolean running = false;

    private Timer runningTimer = new Timer(100);
    private Timer flyingTimer = new Timer(694200);

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {

            abil.setActive(true);
            if(this.mainAbility == 0) this.running = true;
            else{
            }
           

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

        } else if (this.flying && radiusTimer.isDone()) {
             if (inClaws != null && pickedUp == true) {
                      
                        // this.getClient().getRoom().chat(this.getClient(), "FALL VELOCITY STUFF. RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                    } else {
                        disableAbility();
                        // this.getClient().getRoom().chat(this.getClient(), "ABILITY DISABLED 1. RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                    }
        }

    }

    public void disableAbility() {
      
        this.flying = false;

        landing = true;
        radiusTimer.reset();
        runningTimer.reset();
        flyingTimer.isRunning = false;
        pickedUp = false;

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
