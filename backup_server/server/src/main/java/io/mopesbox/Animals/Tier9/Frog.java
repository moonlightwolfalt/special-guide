package io.mopesbox.Animals.Tier9;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
public class Frog extends Animal {
    public Frog(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.baseRadius = 12;
        this.setRadius(baseRadius);
        this.setSpeed(0);
        this.setMaxHealth(247.5);
        this.setCanClimbHills(true);
        this.setCanClimbRocks(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCamzoom(2100);
    }

    private boolean isUp = true;

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if(soundTimer != null) {
            soundTimer.update(Constants.TICKS_PER_SECOND);
            if(soundTimer.isDone()) {
                this.getClient().getRoom().sendChat(this.getClient(), this.getClient().getRoom().getRandomItem(new String[] {"CROAK!","RIBBIT!"}));
                soundTimer.reset();
                soundTimer = new Timer(Utils.randomInt(8000, 12000));
            }
        }
        if(this.isInHole()) {
            this.flag_usingAbility = true;
            this.isUp = true;
            this.flag_flying = true;
            canCalculateAngle = false;
            canCalculateMovement = false;
            this.getInfo().getAbility().setActive(true);
            this.height = 0;
        }
        if(this.getInfo().getAbility().isActive()) {
            this.addVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (5))));
            this.addVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (5))));
            this.setZ(this.height);
            if(this.isUp) {
                this.height++;
                if(this.height > 5) this.isUp = false;
            } else {
                if(this.height > 0) this.height--;
                else {
                    this.getInfo().getAbility().setActive(false);
                    this.flag_usingAbility = false;
                    this.flag_flying = false;
                    canCalculateAngle = true;
                    canCalculateMovement = true;
                    if (!this.getClient().instantRecharge) {
                        this.getInfo().getAbility().setRecharging(true);
                        abilT = 3;
                        this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                    }
                }
            }
        }
    }
    
    private int height = 0;

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
            this.flag_usingAbility = true;
            this.isUp = true;
            this.flag_flying = true;
            canCalculateAngle = false;
            canCalculateMovement = false;
            this.getInfo().getAbility().setActive(true);
            this.height = 0;
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    private Timer soundTimer = new Timer(10000);

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
