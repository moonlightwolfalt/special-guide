package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.Snowball;
import io.mopesbox.Objects.ETC.YetiRoar;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class Yeti extends Animal {
    public Yeti(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setArcticAnimal(true);
        this.setSlidingOnIce(false);
        this.setMaxHealth(300);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(false); // is transforming
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(false); // is transforming
    }

    
    public void onHurt() {
        if(this.isRunning) this.disableAbility();
    }  

    private boolean isRunning = false;

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



    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.setSpecies(0);
        this.isRunning = false;
        if(!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    private Timer downTimer = new Timer(2000);

    private void runningUpdate() {
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
 
    }

    

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.isDiveMain() && !this.isDiveActive()) {
            if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning) {
                YetiRoar tailslap = new YetiRoar(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) Math.floor(this.getRadius()*3), this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies());
                this.getClient().getRoom().addObj(tailslap);
                int ang = -30;
                for(int i = 0; i < 2; i++) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle()+ang)) * (this.getRadius()*1.2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle()+ang)) * (this.getRadius()*1.2)));
                    Snowball snowball = new Snowball(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 20, this.getClient().getRoom(), this.getAngle(), 10, this, this.getInfo().getAnimalSpecies());
                    ang += 60;
                    this.getClient().getRoom().addObj(snowball);
                }
                if(this.getInfo().getAnimalSpecies() == 3) {
                    ang = -60;
                    double tangle = this.getAngle();
                    tangle -= 180;
                    if(tangle < 0) tangle += 360;
                    for(int i = 0; i < 3; i++) {
                        double x = ((Math.cos(Math.toRadians(tangle+ang)) * (this.getRadius()*1.2)));
                        double y = ((Math.sin(Math.toRadians(tangle+ang)) * (this.getRadius()*1.2)));
                        Snowball snowball = new Snowball(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 20, this.getClient().getRoom(), tangle+ang, 10, this, this.getInfo().getAnimalSpecies());
                        ang += 60;
                        this.getClient().getRoom().addObj(snowball);
                    }
                }
                // this.getClient().getRoom().chat(this.getClient(), "ROAR!");
                this.isRunning = true;
            }
        }
    }
}
