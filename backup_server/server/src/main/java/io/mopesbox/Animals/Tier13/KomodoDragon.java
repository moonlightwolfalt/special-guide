package io.mopesbox.Animals.Tier13;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.AbilityKomodo;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Ability.PlayerAbility;
public class KomodoDragon extends Animal {
    public KomodoDragon(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        this.setCamzoom(1700);
        this.setMaxHealth(289.5);
    }
    private Timer lickT = new Timer(10000);
    private boolean licking;

  

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(licking); // is licking
        writer.writeUInt8(0); // lick side
        writer.writeUInt8(0); // lickedAmt
                writer.writeUInt8(this.flag_usingAbility ? 1 : 0);

    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(licking); // is licking
        writer.writeUInt8(0); // lick side
        writer.writeUInt8(0); // lickedAmt
        writer.writeUInt8(this.flag_usingAbility ? 1 : 0);
    }

       @Override
    public void update() {
        super.update();
        abilityRecharge();
        lickT.update(Constants.TICKS_PER_SECOND);
        if(lickT.isRunning){
            if(licking){
                licking = false;
            }
        }
        if(lickT.isDone()){
            licking = true;
        }
        lickT.reset();


    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
         
       double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    AbilityKomodo bite  = new AbilityKomodo(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getRadius(), this.getClient().getRoom(), this, this.getInfo().getAnimalSpecies());         
                       this.getClient().getRoom().addObj(bite);

                       this.flag_usingAbility = true;

            if(!this.getClient().instantRecharge) {
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
