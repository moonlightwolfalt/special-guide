package io.mopesbox.Animals.Tier4;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
//copyied from rhino btw its ok!
public class Armadillo extends Animal {
    private double mainSpeed = this.speed;
    private int damage = 3;
    private boolean canHit = true;

    public Armadillo(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        this.setMaxHealth(195);
        this.setSpecies(0);
        this.bypassAbilSpeed = true;
        this.canHit = true;
        this.setCamzoom(2600);
        info.getAbility().setHoldAbility(true);

        runTimer.isRunning = false;
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
    
    public void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isRunning = false;
        runSpeed = 0;
        this.speed = mainSpeed;
            this.setSpecies(0);

        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }
    @Override
    public void onHurt(){
        this.disableAbility();
    }
public Timer specTimer = new Timer(100);
public Timer abilityTimer = new Timer(20);
    private void runningUpdate() {
        if (this.isRunning) {
            this.getInfo().getAbility().setActive(true);
            this.setSpecies(1);
            this.speed = mainSpeed + runSpeed;
                             if (this.isRunning) {
                                specTimer.update(Constants.TICKS_PER_SECOND);
                             }

                             if (abilityTimer.isDone()) {
                                 this.setSpecies(0);
                                 disableAbility();
                            
                             } else {
                               abilityTimer.update(Constants.TICKS_PER_SECOND);
                             }
            if (specTimer.isDone()) {
                this.setSpecies(this.getSpecies() + 1);
         specTimer.reset();
                 
            }
            if (runSpeed < 30) {
                runSpeed++;
            } else {
                if (runTimer.isDone()) {
                     this.setSpecies(0);
                    disableAbility();
                } else {
                    runTimer.update(Constants.TICKS_PER_SECOND);
                }
            }
        }else {
            if(this.getSpecies() == 1) this.setSpecies(0);
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
                Animal ani = ((Animal) o);

                double multipl = this.getInfo().getSkin() == 1 || this.getInfo().getSkin() == 2 ? 2 : 0.5;

                // if (multipl < 1)
                    // multipl = 1;

                ani.hurt(((int) (damage + (this.runSpeed * multipl))), 1, this);
                ani.setStun(3);
                ani.addVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * ((40)+(this.runSpeed / 4)))));
                ani.addVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * ((40)+(this.runSpeed / 4)))));
                if (ani.getInfo().getTier() > 8) {
                    this.setStun(3);
                }

                disableAbility();

                // ani.hurt()
                // Main.log.info("COLLISIONS DONE");
            }
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

                       if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.isRunning) {
 abilityTimer.reset();
            }
        }
    }
}
