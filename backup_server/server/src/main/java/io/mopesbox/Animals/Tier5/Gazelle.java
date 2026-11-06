package io.mopesbox.Animals.Tier5;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;

public class Gazelle extends Animal {
    public Gazelle(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        // this.baseRadius = 12;
        this.setRadius(Tier.byOrdinal(this.getInfo().getTier()).getBaseRadius());
        this.setSpeed(12);
        this.setMaxHealth(205.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        info.getAbility().setHoldAbility(true);
        this.setCamzoom(2500);
    }

    private boolean isUp = true;
    private int timesThatJump = 0;

    public void disableAbility(){
        this.setZ(0);
        this.getInfo().getAbility().setActive(false);
                    this.flag_usingAbility = false;
                    this.flag_flying = false;
                    canCalculateAngle = true;
                    canCalculateMovement = true;
                    if (!this.getClient().instantRecharge) {
                        this.getInfo().getAbility().setRecharging(true);
                        abilT = 8;
                        this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
    }
    }
    @Override
    public void update() {
        super.update();
        abilityRecharge();
  
  
        if(this.getInfo().getAbility().isActive()) {
            this.addVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (3))));
            this.addVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (3))));
            
            this.setZ(this.height);
            if(this.isUp) {
                this.height++;
                if(this.height > 10){
                     this.isUp = false;
                     timesThatJump++;
                }
            } else {
                if(this.height > 0 && this.timesThatJump < 5) {
                this.height--;
                if(this.height == 0) this.isUp = true;
                
                }
                else {
                    this.getInfo().getAbility().setActive(false);
                    this.flag_usingAbility = false;
                    this.flag_flying = false;
                    canCalculateAngle = true;
                    canCalculateMovement = true;
                    if (!this.getClient().instantRecharge) {
                        this.getInfo().getAbility().setRecharging(true);
                        abilT = 8;
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
            this.getInfo().getAbility().setActive(true);
            this.height = 0;
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
