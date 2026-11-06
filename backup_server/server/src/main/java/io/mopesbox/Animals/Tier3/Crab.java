package io.mopesbox.Animals.Tier3;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;

public class Crab extends Animal {

    public Crab(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(184.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.bypass_waterani_slowness = true;
        this.bypassAbilSpeed = true;
        this.setSpeed(23);
        this.setCamzoom(2700);
        info.getAbility().setHoldAbility(true);
    }

    @Override
    public double speedManipulation(double speed) {
        if (this.isHiding)
            return speed * 0.75;
        else
            return speed;
        }


    // public boolean isHiding = false;
    public Timer hidingTimer = new Timer(10000);

    public void stopAbil() {
        this.setSpecies(0);
        this.getInfo().getAbility().setActive(false);
        this.isHiding = false;

                PlayerAbility abil = this.getInfo().getAbility();

              if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 5;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();

    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isHiding) {
            this.setSpecies(1);
            this.getInfo().getAbility().setActive(true);
            // double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
            // double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
            // DonkeyKick tailslap = new DonkeyKick(this.getClient().getRoom().getID(), this.getX()-x, this.getY()-y, ((int) Math.round(this.getRadius()*1.2)), this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies());
            // this.getClient().getRoom().addObj(tailslap);

            hidingTimer.reset();

            this.isHiding = true;
            
      
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

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

    public void hidingUpd() {
        if (this.isHiding) {
            hidingTimer.update(Constants.TICKS_PER_SECOND);
            if (hidingTimer.isDone()) {
                this.isHiding = false;
                this.stopAbil();
                hidingTimer.reset();
            }
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        hidingUpd();
    }
}
