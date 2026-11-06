package io.mopesbox.Animals.Tier3;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.Jump;


public class Chicken extends Animal {

    public Chicken(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(184.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        info.getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2700);

    }
    private boolean isRunning;

    @Override
    public double speedManipulation(double speed) {
        if (this.isRunning)
            return speed * 1.50;
        else
            return speed;
        }


    // public boolean isHiding = false;
    public Timer hidingTimer = new Timer(8000);
    public Timer boostShowTimer = new Timer(500);
    public void stopAbil() {
        this.getInfo().getAbility().setActive(false);
        this.isRunning = false;
        this.flag_usingAbility = false;
        PlayerAbility abil = this.getInfo().getAbility();


          if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 5;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
    }
    

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning && !abil.isActive()) {
            this.getInfo().getAbility().setActive(true);
          
            hidingTimer.reset();
            this.flag_usingAbility = true;

            this.isRunning = true;
            
          
        }
       
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(405);


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

    public void hidingUpd() {
        if (this.isRunning) {
            hidingTimer.update(Constants.TICKS_PER_SECOND);
            if (hidingTimer.isDone()) {
                this.isRunning = false;
                this.stopAbil();
                hidingTimer.reset();
            }
            boostShowTimer.update(Constants.TICKS_PER_SECOND);
            if(boostShowTimer.isDone() && !hidingTimer.isDone()) {

            Jump tailslap = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap);
            boostShowTimer.reset();
                        }
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        hidingUpd();
    }


  

         @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
writer.writeUInt8(this.getInfo().getAbility().isActive() ? 1 : 0); // Attack?
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(this.getInfo().getAbility().isActive() ? 1 : 0); // Attack?
    }

} 
