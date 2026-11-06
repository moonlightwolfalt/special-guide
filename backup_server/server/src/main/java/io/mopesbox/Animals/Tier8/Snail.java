package io.mopesbox.Animals.Tier8;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;

public class Snail extends Animal {
    public Snail(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.baseRadius = 14;
        this.setWaterfowl(true);
        this.setRadius(baseRadius);
        this.setSpeed(2);
        this.setMaxHealth(237);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCamzoom(2200);
    }

    private boolean isDefending = false;

    @Override
    public void update() {
        super.update();
        if(abilTimer != null) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if(abilTimer.isDone()) {
                this.isDefending = false;
                this.flag_usingAbility = false;
                this.getInfo().getAbility().setActive(false);
                this.setSpeed(2);
                abilTimer.reset();
            }
        }
    }

    @Override
    public void hurt(double damage, int reason, GameObject biter) {
        if(isDefending) damage /= 3;
        super.hurt(damage, reason, biter);
    }

    private Timer abilTimer = null;

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
            this.isDefending = true;
            this.flag_usingAbility = true;
            this.getInfo().getAbility().setActive(true);
            abilTimer = new Timer(8000);
            this.setSpeed(1);
        }
    }

}
