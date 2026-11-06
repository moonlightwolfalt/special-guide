package io.mopesbox.Animals.Tier3;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;

public class Mole extends Animal {

    private boolean isDiving = false;

    public Mole(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(184.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.getInfo().getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2700);
    }

    @Override
    public double speedManipulation(double speed) {
        if (this.isDiving)
            return speed * 1.5;
        else
            return speed;
        }


    // public boolean isHiding = false;
    public Timer hidingTimer = new Timer(5000);

    public void stopAbil() {
        this.getInfo().getAbility().setActive(false);
        this.isDiving = false;
        this.setDiveActive(false);

                PlayerAbility abil1 = this.getInfo().getAbility();

        if (!this.getClient().instantRecharge) {
            abil1.setRecharging(true);
            abilT = 5;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
            this.getInfo().getAbility().setActive(true);
          
            this.useDive();
            this.isDiving = true;

     
            

        }
        
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if(abilT > 0 && !this.isDiving) {
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
        if (this.isDiving) {
            hidingTimer.update(Constants.TICKS_PER_SECOND);
            if (hidingTimer.isDone()) {
                this.isDiving = false;
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
