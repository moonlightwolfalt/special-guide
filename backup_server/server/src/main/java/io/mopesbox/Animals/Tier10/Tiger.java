package io.mopesbox.Animals.Tier10;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.TigerJump;
import io.mopesbox.Objects.Static.HidingBush;
import io.mopesbox.Utils.Timer;

public class Tiger extends Animal {
    public boolean holdingW = false;
    private Timer holdTimer = new Timer(2000);

    // private boolean canHit = true;
    public Animal grabbedAni = null;

    // private int xp = 

    public Tiger(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.isAbilityDone = false; // абилка недоделана, юзать НЕ в сандбоксе нельзя. гейммод 1 онли
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);


        this.setMaxHealth(258);
        this.setCanClimbHills(true);
        this.setSpeed(12);
        this.setCamzoom(2000);
    }

    public void onHurt() {
        if(this.getSpecies() > 0) this.disableAbility();
    }

    private boolean isUsingAbility = false;
    // private double runSpeed = 0;s

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);
    private Timer ungrabTimer = new Timer(3000);
    private Timer abTimer = new Timer(2000);

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

    public void damage(int damage, Animal biter) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        killer = biter;
    }

    private Animal killer;

    private Timer dmgTimer = new Timer(1000);
   

    // public Timer runTimer = new Timer(1000);

    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isUsingAbility = false;

        // if (!this.getClient().instantRecharge) {
            // this.getInfo().getAbility().setRecharging(true);
            // abilT = 5;
            // this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        // }
    }

    // @Override
    // public void onCollision(GameObject o) {
    //     super.onCollision(o);
    //     if (o instanceof Animal) {
    //         if (!((Animal) o).flag_flying && !((Animal) o).isInHole() && !this.isInHole() && !this.flag_flying
    //                 && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
    //                 && !((Animal) o).isInvincible()
    //                 && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
    //             double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
    //             double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
    //             xx = this.getX() + xx;
    //             yy = this.getY() + yy;
    //             if (this.runSpeed <= 20)
    //                 return;
    //             // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
    //                 // return;
    //             if (this.isRunning && this.runSpeed >= 30
    //                     && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
    //                 Animal obj = (Animal) o;
    //                 double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 10)));
    //                 double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 10)));
    //                 obj.makeRam(x, y, ((Animal) this), 5);
    //                 disableAbility();
    //             }
    //         }
    //     }
    // }

    @Override
    public void onCollisionEnter(GameObject o) {
        super.onCollisionEnter(o);
        if (o instanceof Animal) {
            // Main.log.info("USING: " + this.isUsingAbility);
            if (this.isUsingAbility && !((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                        // Main.log.info("AAAAAAA");
                // double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                // double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            //     xx = this.getX() + xx;
            //     yy = this.getY() + yy;
                // if (this.runSpeed <= 2)
                //     return;

                    // this.canHit = false;
            //     // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
            //         // return;
            //     if (this.isRunning
            //             && ((o instanceof Bison && this.runSpeed > ((Bison) o).runSpeed - 1)
            //                     || !(o instanceof Bison))
            //             && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)
            //             && this.runSpeed >= 30) {
            //         Animal obj = (Animal) o;
            //         double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 10)));
            //         double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 10)));
            //         obj.makeRam(x, y, ((Animal) this), 5);
            //         disableAbility();
            //     }
            // }
                this.ungrabTimer.reset();

                Animal ani = ((Animal) o);

                this.grabbedAni = ani;



                // double multipl = this.getInfo().getSkin() == 1 || this.getInfo().getSkin() == 2 ? 3.6 : 0.69;


                // if (multipl < 1)
                    // multipl = 1;

                // ani.hurt(((int) (damage)), 1, this);
                // ani.setStun(3);
                // ani.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * ((40)+(this.runSpeed / 4)))));
                // ani.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * ((40)+(this.runSpeed / 4)))));

                disableAbility();

                // ani.hurt()
                // Main.log.info("COLLISIONS DONE");
            }
        }
    }

    public void cancelAmbush(){
        this.holdingW = false;
        holdTimer.reset();
        // makeFire = false;
    }
    @Override
    public void update() {
        super.update();

        if(grabbedAni != null){
            this.setSpecies(4);
        }
        // if(this)

        if(holdingW){
            holdTimer.update(Constants.TICKS_PER_SECOND);
            if(holdTimer.isDone()){
                HidingBush bush = new HidingBush(this.getRoom().getID(), getX(), getY(), (int) 2.3 * getRadius(), this.getBiome(), getRoom(), false);
                getRoom().addObj(bush);
                bush.setTimer();
                holdTimer.reset();
                this.holdingW = false;
                this.disableAbility();
            if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
            }
        }

        // Main.log.info("GRABBED: " + this.grabbedAni);

        if (abTimer.isDone()) {
            if (this.isUsingAbility) {
                disableAbility();
                this.isUsingAbility = false;
            } 
            abTimer.reset();
         } else {
            abTimer.update(Constants.TICKS_PER_SECOND);
         }// /ban yesd amogus 65496845y
    

        ungrabTimer.update(Constants.TICKS_PER_SECOND);
        dmgTimer.update(Constants.TICKS_PER_SECOND);

        if (ungrabTimer.isDone() || (grabbedAni != null && this.grabbedAni.isDead())) {
            if (this.grabbedAni != null) 
                this.grabbedAni = null;

                this.setSpecies(0);
                ungrabTimer.reset();
            
        }

        if (this.grabbedAni != null) {
            if(dmgTimer.isDone()){
                this.grabbedAni.hurt(((int) this.getTier() <= this.grabbedAni.getTier() ? (this.grabbedAni.maxHealth / 20) : (this.grabbedAni.maxHealth / 12)), 1, this);
                dmgTimer.reset();
            }
                if(!grabbedAni.isStunned())
                this.grabbedAni.setStun(2);
                double dy = this.getY() - this.grabbedAni.getY();
                double dx = this.getX() - this.grabbedAni.getX();
                double theta = Math.atan2(dy, dx);
                theta *= 180 / Math.PI;
                if (theta < 0) {
                    theta += 360;
                }
                double x = ((Math.cos(Math.toRadians(theta)) * (5)));
                double y = ((Math.sin(Math.toRadians(theta)) * (5)));
                this.grabbedAni.addVelocityX(x);
                this.grabbedAni.addVelocityY(y);
        }

        // this.grabbedAni.addV
        
        abilityRecharge();
        checkDiving();
        // canHitTimer.update(Constants.TICKS_PER_SECOND);

        // if (canHitTimer.isDone()) {
        //     canHit = true;
        //     canHitTimer.reset();
        // }
        if(this.health <= 0) {
            if(this.killer != null && this.killer.getClient() != null)
                this.killer.getClient().addXp(this.getClient().getXP());
        }
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (!this.isDiveMain() && !this.isDiveActive()) {
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
                // Main.log.info("isP");
                // this.getClient().getRoom().chat(this.getClient(), "isPossible: " + abil.isPossible() + " isRecharging: " + abil.isRecharging() + " isUsable: " + abil.isUsable());
                // runTimer.reset();
                // this.isRunning = true;
                this.isUsingAbility = true;

                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (3)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (3)));

                TigerJump tigerJump = new TigerJump(this.getClient().getRoom().getID(), this.getX() + xx, this.getY() + yy, (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies());
                this.getClient().getRoom().addObj(tigerJump);
                // this.disableAbility();

            if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
            }
        }
    }
}