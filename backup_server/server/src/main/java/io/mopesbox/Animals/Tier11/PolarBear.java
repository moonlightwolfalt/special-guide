package io.mopesbox.Animals.Tier11;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.BearSlash;
import io.mopesbox.Utils.Timer;

public class PolarBear extends Animal {

    public PolarBear(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(268.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCanClimbHills(true);
        this.setSpeed(13);
        this.setCamzoom(1900);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
            abil.setActive(true);
            double x = ((Math.cos(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            double y = ((Math.sin(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            BearSlash tailslap = new BearSlash(this.getClient().getRoom().getID(), this.getX() - x,
                    this.getY() - y, ((int) Math.round(this.getRadius() * 1.5)), this.getClient().getRoom(),
                    this.getAngle(), this, this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(tailslap);
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
        }
    }

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

    @Override
    public void update() {
        super.update();
        abilityRecharge();
    }

    // @Override
    // public void onDeath() {
    // AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(),
    // this.getX(), this.getY(), this.getRadius(), 16, true,
    // this.getClient().getNickname(), this.getClient().getRoom(),
    // this.getClient().getXP()/2);
    // this.getClient().getRoom().addObj(carcass);
    // }
}
