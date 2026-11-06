package io.mopesbox.Animals.Tier4;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;

public class Seal extends Animal {

    public Seal(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(195);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        this.setCamzoom(2600);
        this.getInfo().getAbility().setHoldAbility(true);
    }

  


    // public boolean isHiding = false;

    @Override
   public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.isOnIce && !abil.isActive()) {
            this.setSpecies(1);
            this.setSlidingOnIce(true);
            this.setSpeed(10);
            abil.setActive(true);
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


    public void disableAbility() {
        this.setSlidingOnIce(false);
        this.setSpecies(0);
        this.setSpeed(8);


            this.getInfo().getAbility().setActive(false);

        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }
    public void onHurt(){
        this.disableAbility();
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if(!this.isOnIce && this.isSlidingOnIce() && this.getInfo().getAbility().isActive()){
            this.disableAbility();

        }
    }
}
