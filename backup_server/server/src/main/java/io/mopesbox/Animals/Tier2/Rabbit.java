package io.mopesbox.Animals.Tier2;

import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.AbilityHole;
import io.mopesbox.Constants;
public class Rabbit extends Animal {
    public Rabbit(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(45);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        this.setCamzoom(2800);
        this.setMaxHealth(165);
    }
    Timer timeOutTimer = new Timer(7000);



    @Override
    public void update() {
        super.update();
        abilityRecharge();
        timeOutTimer.update(Constants.TICKS_PER_SECOND);

          PlayerAbility abil = this.getInfo().getAbility();

        if(this.getBiome() == 1) {
            abil.setUsable(false);
        } else {
            if(!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole() && timeOutTimer.isDone()) {
                abil.setUsable(true);
            }
        }

    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
         
            AbilityHole hole = new AbilityHole(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) Math.floor(Math.max(this.getRadius(), 20)),false,0,20);
            this.getClient().getRoom().addObj(hole);

            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }else{
                abil.setRecharging(true);
                abilT = 1;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));

            }
            abilTimer.reset();
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

}
