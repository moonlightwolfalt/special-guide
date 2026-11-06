package io.mopesbox.Animals.Tier12;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;

public class Bison extends Animal {
    private double mainSpeed = 7;

    public Bison(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.speed = mainSpeed;

        this.biteDamage = 60;
        this.setMaxHealth(279);
        this.bypassAbilSpeed = true;
        this.setDesertAnimal(true);
        this.setCamzoom(1800);

        runTimer.isRunning = false;
    }

    public void onHurt() {
        if (this.isRunning)
            this.disableAbility();
    }

    private boolean isRunning = false;
    private double runSpeed = 0;

    private int abilT = 0;

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

    public Timer runTimer = new Timer(3000);

    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isRunning = false;
        runSpeed = 0;
        this.speed = mainSpeed;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 16;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }  

    private void runningUpdate() {
            this.getInfo().getAbility().setActive(true);
            this.setSpecies(1);
            this.speed = mainSpeed + runSpeed;
            if (runSpeed < 7) {
                runSpeed++;
            } else {
                if (runTimer.isDone()) {
                    disableAbility();
                } else {
                    runTimer.update(Constants.TICKS_PER_SECOND);
                }
            }
        }
    

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (o instanceof Animal) {
            if (!((Animal) o).flag_flying && !((Animal) o).isInHole() && !this.isInHole() && !this.flag_flying
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.runSpeed <= 1)
                    return;
                // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
                    // return;
                if (this.isRunning && this.runSpeed >= 2
                        && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
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
                if (this.runSpeed <= 2)
                    return;
                // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
                    // return;
                if (this.isRunning
                        && ((o instanceof Bison && this.runSpeed > ((Bison) o).runSpeed - 1)
                                || !(o instanceof Bison))
                        && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)
                        && this.runSpeed >= 1) {
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
        if(this.isRunning)runningUpdate();
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
