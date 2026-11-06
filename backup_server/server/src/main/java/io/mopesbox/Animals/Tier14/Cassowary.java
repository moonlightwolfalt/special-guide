package io.mopesbox.Animals.Tier14;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.CassowaryIdk;
import io.mopesbox.Utils.Timer;

public class Cassowary extends Animal {
    private double mainSpeed = 7;
    private boolean canHit = true;

    public Cassowary(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.speed = mainSpeed;
        this.setCamzoom(1500);
        this.setMaxHealth(125);
        this.bypassAbilSpeed = true;
        this.canHit = true;
        runTimer.isRunning = false;
        this.setMaxHealth(300);
    }

    // public void onHurt() {
    //     if (this.isRunning)
    //         this.disableAbility();
    // }

    private boolean isRunning = false;
    private double runSpeed = 0;

    private int abilT = 0;
    private Timer abilTimer = new Timer(950);
    private Timer canHitTimer = new Timer(150);

     @Override
    public double speedManipulation(double speed) {
        if (this.isRunning)
            return speed * 1.5;
        else
            return speed;
        }

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



    public Timer runTimer = new Timer(1000);

    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isRunning = false;
        runSpeed = 0;
        this.speed = mainSpeed;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    private void runningUpdate() {
        if (this.isRunning) {
            this.getInfo().getAbility().setActive(true);
            this.setSpecies(1);
            this.speed = mainSpeed + runSpeed;
            if (runSpeed < 30) {
                runSpeed++;
            } else {
                if (runTimer.isDone()) {
                    // disableAbility();
                } else {
                    runTimer.update(Constants.TICKS_PER_SECOND);
                }
            }
        }
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
            if (this.canHit && !((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                // double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            //     double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            //     xx = this.getX() + xx;
            //     yy = this.getY() + yy;
                if (this.runSpeed <= 2)
                    return;

                    this.canHit = false;
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

                // if (multipl < 1)
                    // multipl = 1;
                // ani.hurt()
                // Main.log.info("COLLISIONS DONE");
            }
        }
    }


    public void push(){
                  PlayerAbility abil = this.getInfo().getAbility();
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.isRunning) {
                double x = ((Math.cos(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
                double y = ((Math.sin(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
                CassowaryIdk tailslap = new CassowaryIdk(this.getClient().getRoom().getID(), this.getX() - x,
                this.getY() - y, ((int) Math.round(this.getRadius() * 1.8)), this.getClient().getRoom(),
                this.getAngle(), this, this.getInfo().getAnimalSpecies());
                this.getClient().getRoom().addObj(tailslap);
                this.setSpecies(2);
                this.isRunning = false;
                if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 10;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                this.abilSpeed.reset();
                disableAbility();
                this.setSpeed(8);
            }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        runningUpdate();
        canHitTimer.update(Constants.TICKS_PER_SECOND);

        if (canHitTimer.isDone()) {
            canHit = true;
            canHitTimer.reset();
        }
        if (runTimer.isDone()){
            PlayerAbility abil = this.getInfo().getAbility();
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.isRunning) {
            this.isRunning = false;
            this.setSpecies(2);
                double x = ((Math.cos(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
                double y = ((Math.sin(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
                CassowaryIdk tailslap = new CassowaryIdk(this.getClient().getRoom().getID(), this.getX() - x,
                this.getY() - y, ((int) Math.round(this.getRadius() * 1.8)), this.getClient().getRoom(),
                this.getAngle(), this, this.getInfo().getAnimalSpecies());
                this.getClient().getRoom().addObj(tailslap);
                if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 10;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                this.abilSpeed.reset();
                disableAbility();
            }
        }
        if(this.flag_flying){
            disableAbility();
        }
    }


    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (!this.isDiveMain() && !this.isDiveActive()) {
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning) {
                runTimer.reset();
                this.isRunning = true;
            }
        }
    }
}