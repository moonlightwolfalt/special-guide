package io.mopesbox.Animals.Tier10;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.HyenaAbil;
public class Hyena extends Animal {


    public Hyena(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(258);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        this.setDesertAnimal(true);
        this.setCamzoom(2000);
    }

    @Override
    public void update() {
        super.update();
        if(this.getInfo().getAbility().isActive()){
            this.setSpecies(1);

        }else if(this.getSpecies() == 1) this.setSpecies(0);
        abilityRecharge();
    }
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            this.getInfo().getAbility().setActive(true);
            for(int index = 0; index < 5; index++){
            HyenaAbil tailslap = new HyenaAbil(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                    (int) Math.floor(this.getRadius() * 1.2), this.getClient().getRoom(), this.getAngle(), this,
                    0, 15 - index);
            this.getClient().getRoom().addObj(tailslap);
            this.setSpecies(1);
            }


       
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            abilTimer.reset();
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if (abilT > 0) {
            this.getInfo().getAbility().setActive(false);

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
}
