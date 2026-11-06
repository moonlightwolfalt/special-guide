package io.mopesbox.Animals.Tier10;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.PelicanMouth;
import io.mopesbox.Objects.Static.WaterDrop;

public class Pelican extends Animal {
    public Pelican(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        radiusTimer.isRunning = false;
        flyingTimer.isRunning = false;
        this.setMaxHealth(258);
        this.setCamzoom(2000);
        this.setSpeed(12);
        this.bypass_waterani_slowness = true;

        this.bypassAbilSpeed = true;
    }

    public boolean landing = false;

    private Timer radiusTimer = new Timer(1000);

    private double step = 0;
    private double currentZ = 0;

    private boolean pickedUp = false;
    public Animal inClaws;
    public PelicanMouth mouth;
    @Override
    public void onHurt(){
        if(this.getInfo().getAbility().isActive() && !this.getInfo().getAbility().isRecharging()){
            this.disableAbility();
        }
    }

    @Override
    public void update() {
        
        super.update();
        abilityRecharge();

        if(this.getClient().getPlayer() != null){
        if(this.isRammed){
            this.flag_usingAbility = false;
            this.running = false;
            this.getClient().getPlayer().setSpeed(7);
        }


        }

        if(this.pickedByBird && this.getSpecies() > 0){
            this.setSpecies(0);
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

        if(this.isDead()) this.disableAbility();

        if (pickedUp && inClaws != null) {
            double x = ((Math.cos(Math.toRadians(this.getAngle()))
                    * (inClaws.getRadius() / 2 + (this.getRadius() / 1.5))));
            double y = ((Math.sin(Math.toRadians(this.getAngle()))
                    * (inClaws.getRadius() / 2 + (this.getRadius() / 1.5))));
            inClaws.setX(this.getX() - x);
            inClaws.setY(this.getY() - y);
            inClaws.setZ((int) currentZ);
            inClaws.flag_flying = true;
            inClaws.pickedByBird = true;
            inClaws.pickedBy = this;

            // inClaws.stun(1);
        }

        if (this.running && !this.isRammed && !pickedByBird && !pickedByPelican) {
            this.setSpecies(1);
        } else {
          
            if (this.flag_flying && !this.isRammed && !this.pickedByBird && inClaws == null && !pickedByBird && !pickedByPelican){
                this.setSpecies(2);
            }
                if(this.flag_flying && !this.isRammed && inClaws != null && !pickedByBird && !pickedByPelican){
                this.setSpecies(3);
                }
                if(!this.flag_flying && !this.isRammed){
                    this.setSpecies(0);
                }
        

        }

        if (this.flying && !this.isRammed && !this.pickedByBird) {
            flyingTimer.update(Constants.TICKS_PER_SECOND);
            if (flyingTimer.isDone() || this.water <= 25) {
                if (inClaws != null && pickedUp == true) {
                     double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed /4)));
                        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 4)));
                        inClaws.makeFall(x, y, this);
                          WaterDrop water = new WaterDrop(this.getRoom().getID(), getX(), getY(), 5, 0, this.getAngle(), this.getId(), getRoom(), this);
                        this.getRoom().addObj(water);
                        
                        pickedUp = false;
                        inClaws = null;
                }

                this.disableAbility();
                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom())));
                // this.getClient().setCamzoom((this.getClient().getBaseZoom()));
            }
        }

        if (inClaws != null) {
            inClaws.birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 4)));
            inClaws.birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 4)));
        }
   

        radiusTimer.update(Constants.TICKS_PER_SECOND);
        if (!radiusTimer.isDone()) {

            if (landing && !this.isRammed) {
                // decrease size to before size
                currentZ -= step;
                if (currentZ >= 0)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = 0;

                // this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));
            } else {
                currentZ += step;
                if (currentZ < this.getRadius() * 2)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = this.getRadius() * 2;

                // this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));

            }

        } else {
            if (landing) {
                
                this.setZ(0);
                this.flag_flying = false;
                this.landing = false;
                this.getInfo().getAbility().setActive(false);
                // чо за скам поч
                // this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom())));
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
        if (this.getInfo().getSkin() == 4) {
        if (this.flying)
            return speed * 1.5 * 4;
        else if (this.running)
            return speed * 1.3 * 2;
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
    public boolean running = false;

    public Timer runningTimer = new Timer(2000);
    private Timer flyingTimer = new Timer(8000);
    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRammed && this.getBiome() == 1) {

            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));

            mouth = new PelicanMouth(this.getRoom().getID(), getX()+x, getY()+y, 15, getRoom(), this, 0);
            this.getRoom().addObj(mouth);

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

        } else if (this.flying && radiusTimer.isDone() && !this.isRammed && !this.pickedByBird) {
           
                        disableAbility();
                        // this.getClient().getRoom().chat(this.getClient(), "ABILITY DISABLED 1. RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                    }
        

    }

    public void disableAbility() {
      
        this.flying = false;
        this.getInfo().getAbility().setActive(false);
                          WaterDrop water = new WaterDrop(this.getRoom().getID(), getX(), getY(), 15, 0, this.getAngle(), this.getId(), getRoom(), this);
                        this.getRoom().addObj(water);

            if(mouth != null)mouth.makeFall();


        this.getRoom().removeObj(mouth, null);
        mouth = null;

        landing = true;
        runningTimer.isRunning = false;
        running = false;
        radiusTimer.reset();
        runningTimer.reset();
        flyingTimer.isRunning = false;
        pickedUp = false;
        this.setZ(0);
        this.setSpecies(0);

        this.getClient().setCamzoom((this.getClient().getBaseZoom()));

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
