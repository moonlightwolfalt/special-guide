package io.mopesbox.Animals.Tier9;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.Jump;

public class SnowLeopard extends Animal {
    public SnowLeopard(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(247.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.bypassAbilSpeed = true;
        this.setSpeed(12);
        this.setCamzoom(2100);
    }
    
    @Override
    public void update(){
super.update();
abilityRecharge();

    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
        
            Jump tailslap = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap);

            if (this.getInfo().getSkin() == 4) {
                Jump tailslap1 = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap1);
            Jump tailslap2 = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap2);
            }
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 3;
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
