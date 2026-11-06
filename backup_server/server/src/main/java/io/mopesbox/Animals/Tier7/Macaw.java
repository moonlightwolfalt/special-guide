package io.mopesbox.Animals.Tier7;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Roar;
import io.mopesbox.Server.MsgWriter;

public class Macaw extends Animal {
    public Macaw(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setMaxHealth(226.5);
        radiusTimer.isRunning = false;
        flyingTimer.isRunning = false;
        this.getInfo().getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2100);

        this.bypassAbilSpeed = true;
    }

    public boolean landing = false;

    private Timer radiusTimer = new Timer(1000);
    private double step = 0;
    private double currentZ = 0;

    private boolean isHided;

    @Override
    public void onCollision(GameObject o) {
        // if(this.running){
        // if (o instanceof Animal) {
        // if (!((Animal) o).pickedByBird && !((Animal) o).flag_flying
        // && !((Animal) o).isStunned()) {
        // Animal ani = (Animal) o;
        // int tier = ani.getInfo().getTier();

        // if((this.getInfo().getSkin() == 0 && tier > this.getInfo().getTier()) ||
        // (this.getInfo().getSkin() == 1 && tier > 16))
        // return;

        // if ((ani.getInfo().getType() == this.getInfo().getType()) &&
        // ani.getInfo().getAbility().isActive())
        // return;

        // pickedUp = true;
        // inClaws = (Animal) o;
        // runningTimer.isRunning = false;
        // // inClaws.getClient().getRoom().chat(inClaws.getClient(),
        // // "Oh no! I got picked up by ptero. :insert here epic flying:");

        // return;
        // }
        // }

        // }
        if (this.isHided) {
            if (o instanceof Animal) {
                if (((Animal) o).getCollideable(this) && this.getCollideable(((Animal) o))) {
                    this.disableAbility();
                    Roar roar = new Roar(getClient().getRoom().getID(), getX(), getY(), getRadius() * 2, getClient().getRoom(), angle, this, specie);
                    getClient().getRoom().addObj(roar);

                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        // if(this.isHi
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

        if (this.flying || this.flag_flying ||this.running || this.landing && !this.pickedByBird && !isRammed) {
            this.setSpecies(1);
        } else {
            this.setSpecies(0);

            // if (this.flag_flying)
            // this.setSpecies(2);
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
        // inClaws.birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed
        // / 4)));
        // inClaws.birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed
        // / 4)));
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

    private boolean flying = false;
    private boolean running = false;

    private Timer runningTimer = new Timer(100);
    private Timer flyingTimer = new Timer(8000);

    public int animals;
    public int animal;
    public int specie;

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            abil.setActive(true);
            if (!this.flag_isClimbingHill){
                this.running = true;
                this.setSpecies(1);
            }else {
                isHided = true;
                animals = Utils.randomInt(1, 5);
                if (animals == 1)
                    animal = 56;
                if (animals == 2)
                    animal = 1;
                if (animals == 3)
                    animal = 2;
                if (animals == 4)
                    animal = 3;
                if (animals == 5)
                    animal = 4;

                if (this.getInfo().getAnimalSpecies() == 2) {
                    if (animal == 56)
                        specie = Utils.randomInt(1, 3);
                    else
                        specie = 0;

                }

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

        }

    }

    public void disableAbility() {
        if(this.flying){ this.setSpecies(0);

        this.flying = false;

        landing = true;
        radiusTimer.reset();
        }
        if (this.isHided && getClient() != null && this != null) {
            Roar roar = new Roar(this.getClient().getRoom().getID(), getX(), getY(), getRadius() * 3, getClient().getRoom(), angle, this, specie);
            getClient().getRoom().addObj(roar);
        }
        this.getInfo().getAbility().setActive(false);
        this.isHided = false;
        this.animal = 0;
        this.specie = 0;
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

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        if (this.isHided) {
            writer.writeUInt16(animal);
            writer.writeUInt8(specie);

        }
    }
}
