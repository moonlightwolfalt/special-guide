package io.mopesbox.Animals.Tier8;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.Shock;

public class Stingray extends Animal {

    public Stingray(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(237);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        groanTimer.isRunning = false;
        this.setSpeed(12);
        this.setCamzoom(2200);
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        groanTimer.update(Constants.TICKS_PER_SECOND);
    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            Shock tailslap = new Shock(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                    (int) Math.floor(this.getRadius() * 5), this.getClient().getRoom(), this.getAngle(), this,
                    this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(tailslap);


            groanTimer.reset();
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 10;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            abilTimer.reset();
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);
    private Timer groanTimer = new Timer(1000);

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
}
