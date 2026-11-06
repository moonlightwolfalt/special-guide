package io.mopesbox.Animals.Tier11;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.SquidInk;
import io.mopesbox.Server.MsgWriter;

public class Octopus extends Animal {
    public Octopus(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setMaxHealth(226.5);
        this.getInfo().getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2100);
        this.setDiveDuration(20);

        this.bypassAbilSpeed = true;
    }

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

                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
            }

        // if (inClaws != null && !inClaws.isDead()) {
        // inClaws.birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed
        // / 4)));
        // inClaws.birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed
        // / 4)));
        // }



    public int animals;
    public int animal;
    public int specie;
    public boolean isAnimal = false;

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();

        if (!abil.isActive() && abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            abil.setActive(true);
                isHided = true;
                animals = Utils.randomInt(1, 5);
                isAnimal = Utils.randomBoolean();
                if(isAnimal){
                if (animals == 1)
                    animal = 45;
                if (animals == 2)
                    animal = 62;
                if (animals == 3)
                    animal = 15;
                if (animals == 4)
                    animal = 19;
                if (animals == 5)
                    animal = 18;
                }else{
                if (animals == 1)
                    animal = 39;
                if (animals == 2)
                    animal = 35;
                if (animals == 3)
                    animal = 13;
                if (animals == 4)
                    animal = 37;
                if (animals == 5)
                    animal = 38;
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

    public void disableAbility() {
        if (this.isHided && getClient() != null && this != null) {
            SquidInk roar = new SquidInk(getRoom().getID(), getX(), getY(), this.getRadius() * (int) 1.5, getRoom(), this, this.getInfo().getAnimalSpecies());
            getClient().getRoom().addObj(roar);
        }
        this.getInfo().getAbility().setActive(false);
        this.isHided = false;
        this.animal = 0;
        this.specie = 0;

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
            writer.writeUInt8(isAnimal ? 1 : 0);
            writer.writeUInt16(animal);

        }
    }
}
