package io.mopesbox.Animals.Tier5;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.findFood;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;

public class Flamingo extends Animal {
    public Flamingo(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.getInfo().getAbility().setHoldAbility(true);
        this.getInfo().getAbility().setPossible(true);
        this.getInfo().getAbility().setUsable(true);
        this.setSpeed(21);
        this.setCamzoom(2500);
        this.setMaxHealth(205.5);
    }
    
    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if(this.getInfo().getAbility().isActive()) this.setSpecies(1);
        else if(this.getSpecies() != 0) this.setSpecies(0);
    // timeOutTimer.update(Constants.TICKS_PER_SECOND);

          PlayerAbility abil = this.getInfo().getAbility();

        if(this.getBiome() == 1 && !this.isInHole()) {
            abil.setUsable(true);
        }else{
            abil.setUsable(false);
        }

    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
         
            findFood digfood = new findFood(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) Math.floor(Math.max(this.getRadius(), this.getRadius() * 1.7)), this.getClient().getRoom(), this);
            this.getClient().getRoom().addObj(digfood);
            abil.setActive(true);
            this.setSpeed(3);
            // this.abil = digfood;

           
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
    public void disableAbility(){
                PlayerAbility abil = this.getInfo().getAbility();

                this.setSpeed(8);

         if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }else{
                abil.setRecharging(false);
                abilT = 0;

            }
            abilTimer.reset();

        this.setMove(true);
     
        this.getInfo().getAbility().setActive(false);
       
        
    }



    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(0); // color perc
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(0); // color perc
    }
}
