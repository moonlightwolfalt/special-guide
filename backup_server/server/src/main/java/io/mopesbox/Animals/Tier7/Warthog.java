package io.mopesbox.Animals.Tier7;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Objects.Static.HidingBush;
import io.mopesbox.Constants;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Networking.Networking;



public class Warthog extends Animal {
    public Warthog(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setSpeed(12);
        this.setMaxHealth(226.5);
        this.getInfo().getAbility().setHoldAbility(true);
        this.getInfo().getAbility().setUsable(true);
        this.getInfo().getAbility().setPossible(true);

    }
    private boolean isCharging = false;

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);
    private Timer chargingTimer = new Timer(6000);
    private Timer runningTimer = new Timer(3000);
    private boolean canRunTimer = false;
    private int speedX = 3;
    public void chargingUpdate(){
        if(this.isCharging){
            if(this.getSpeed() == 6) this.setSpeed(2);
            this.chargingTimer.update(Constants.TICKS_PER_SECOND);
            if(this.chargingTimer.isDone()){
               doAbility();
               speedX += 8;
                
            }
        }
    }
    public void doAbility(){
         this.setSpeed(8);
                HidingBush bush = new HidingBush(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius() + 25, this.getBiome(), this.getClient().getRoom(), true);
                bush.setTimer();
                this.getClient().getRoom().addObj(bush);
                this.makeBoost(2);
                this.isCharging = false;
                this.setSpeed(speedX);
                this.chargingTimer.reset();
                canRunTimer = true;
                this.disableAbility();
    }
    public void runningUpdate(){

        if(canRunTimer){
            this.runningTimer.update(Constants.TICKS_PER_SECOND);
            if(this.runningTimer.isDone()){
                this.setSpeed(6);
                this.disableAbility();
                this.canRunTimer = false;
                this.runningTimer.reset();
                // this.disableAbility();
            }
        }
    }


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        chargingUpdate();
        runningUpdate();
        // timeOutTimer.update(Constants.TICKS_PER_SECOND);

        //   PlayerAbility abil = this.getInfo().getAbility();

        // if(this.getBiome() == 1) {
            // abil.setUsable(false);
        // }

    }

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

    public void disableAbility(){
        PlayerAbility abil = this.getInfo().getAbility();

 if(!this.getClient().instantRecharge) {
        abil.setRecharging(true);
        abilT = 8;
        this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
    }else{
        abil.setRecharging(false);
        abilT = 0;

    }
    abilTimer.reset();

// this.setMove(true);

this.getInfo().getAbility().setActive(false);
        this.isCharging = false;
                this.setSpeed(6);
                this.chargingTimer.reset();
                this.canRunTimer = false;
                this.runningTimer.reset();

}

    @Override
    public void useAbility(){
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isCharging) {
            this.isCharging = true;
            abil.setActive(true);

        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(isCharging ? 1 : 0); // is charging
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(isCharging ? 1 : 0); // is charging
    }
}
