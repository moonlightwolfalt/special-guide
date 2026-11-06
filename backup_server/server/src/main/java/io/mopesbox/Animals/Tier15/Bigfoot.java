package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.FireWood;
import io.mopesbox.Objects.ETC.Spear;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;

public class Bigfoot extends Animal {
    public Bigfoot(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setMaxHealth(300);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
    }

    public boolean holdingW = false;
    public FireWood bigfootfirewood;
    private Timer timetoHold = new Timer(3000);


    public void makeFireUpd(){
        if(holdingW && !(this.getInfo().getAbility().isRecharging())){
            timetoHold.update(Constants.TICKS_PER_SECOND);
            this.setSpecies(1);
            if(timetoHold.isDone()){
                if(canDoFire){
                double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                FireWood fire = new FireWood(this.getClient().getRoom().getID(), this.getX() +x, this.getY()+y, this.getRadius() - 5, this.getClient().getRoom(), this.getClient(), false);
                getClient().getRoom().addObj(fire);
                bigfootfirewood = fire;
                this.canDoFire = false;
                getClient().getRoom().chat(getClient(), "FIRE BOOGA!");
                timetoHold.reset();
                holdingW = false;
                this.disableAbility();
                }else{
                getClient().send(Networking.personalGameEvent(255, "You need to wait "+this.cooldownTimer.tim / 1000 + "s to use Fire again."));
                }

            }

        }
    }
    public void cancelFire(){
        this.holdingW = false;
        timetoHold.reset();
    }
    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        // если абилка не речаржится значит есть копьё эф efff
        writer.writeBoolean(!this.getInfo().getAbility().isRecharging() && !this.getInfo().getAbility().isActive()); // spear in hand
        writer.writeBoolean(canDoFire); // can create fire
    } 



    

    private boolean isRunning = false;

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);
    private boolean canruncooldown = false;
    private boolean canDoFire = true;

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


    public void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isRunning = false;
        if(!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            
        }
        canruncooldown = true;
    }


    private Timer downTimer = new Timer(500);
    private Timer cooldownTimer = new Timer(30000);

    private void runningUpdate() {
        if(this.canruncooldown){
            this.cooldownTimer.update(Constants.TICKS_PER_SECOND);
            if(this.cooldownTimer.isDone()){
            this.canruncooldown = false;
            this.cooldownTimer.reset();
            this.canDoFire = true;

            }
        }
        if(this.isRunning) {
            this.getInfo().getAbility().setActive(true);
            this.setSpecies(1);
            downTimer.update(Constants.TICKS_PER_SECOND);
            if(downTimer.isDone()) {
                disableAbility();
                downTimer.reset();
            }
        }
    }
 
    @Override
    public void update() {
        super.update();
        abilityRecharge();
        runningUpdate();
        makeFireUpd();
        if(this.holdingW) this.setSpecies(1);
    }

    public void useAbil() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.isDiveMain() && !this.isDiveActive()) {
            if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning) {
                double x = ((Math.cos(Math.toRadians(this.getAngle()-90)) * (this.getRadius()/2)));
                double y = ((Math.sin(Math.toRadians(this.getAngle()-90)) * (this.getRadius()/2)));
                double x2 = ((Math.cos(Math.toRadians(this.getAngle()-90)) * (this.getRadius()*1.2)));
                double y2 = ((Math.sin(Math.toRadians(this.getAngle()-90)) * (this.getRadius()*1.2)));
                Spear spear = new Spear(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 12, 0, this.getAngle(), this.getClient().getRoom(), this.getClient(), this.getX()+x2, this.getY()+y2);
                this.getClient().getRoom().addObj(spear);
                this.isRunning = true;
        
            }
        }
    }
}
