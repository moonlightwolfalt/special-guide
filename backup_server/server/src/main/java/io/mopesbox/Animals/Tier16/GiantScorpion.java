package io.mopesbox.Animals.Tier16;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.ScorpionClaw;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class GiantScorpion extends Animal {
    private int poison = 100;
    public GiantScorpion(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setDesertAnimal(true);
        this.biteDamage = 60;
        this.setLavaAnimal(true);
        this.setMaxHealth(450);
        this.setCanClimbRocks(true);
        this.setSlidingOnIce(false);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        getClient().setCamzoom(1300);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(this.poison); // poison
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(this.poison); // poison
    }
    
    public void onHurt() {
        if(this.abilRunning || this.claw != null || this.getInfo().getAbility().isActive()) this.disableAbility();
    }

    private boolean abilRunning = false;

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
        if(this.claw != null) {
            this.getClient().getRoom().removeObj(this.claw, null);
            this.claw = null;
        }
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    private Timer runAbilTimer = new Timer(1000);
    
    private ScorpionClaw claw = null;

    private int stage = 0;

    private void abilUpdate() {
        if(this.abilRunning) {
            if(stage == 0) {
                this.getInfo().getAbility().setActive(true);
                this.setSpecies(1);
                if(claw == null) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    claw = new ScorpionClaw(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getRadius(), this.getClient().getRoom(), this);
                    this.getClient().getRoom().addObj(claw);
                }
                runAbilTimer.update(Constants.TICKS_PER_SECOND);
                if(runAbilTimer.isDone()) {
                    stage = 1;
                    runAbilTimer.reset();
                    this.flag_eff_aniInClaws = true;
                }
            } else {
                runAbilTimer.update(Constants.TICKS_PER_SECOND);
                if(runAbilTimer.isDone()) {
                    runAbilTimer.reset();
                    abilRunning = false;
                    this.setSpecies(0);
                    if(this.claw != null && this.claw.gotcha != null) {
                        this.claw.gotcha.hurt(40, 14, this);
                        this.claw.gotcha.setShivering(10, this);
                    }
                    this.flag_eff_aniInClaws = false;
                    this.stage = 0;
                    disableAbility();
                }
            }
        }
    }

   

    @Override
    public void update() {
        if(this.abilRunning) {
            if(this.claw != null) {
                this.setMouseX(this.claw.getX());
                this.setMouseY(this.claw.getY());
            }
        }
        super.update();
        abilityRecharge();
        abilUpdate();
 
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.isDiveMain() && !this.isDiveActive()) {
            if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.abilRunning) {
                this.abilRunning = true;
            }
        } else {
            // if(isDivePossible() && !isDiveRecharging() && isDiveUsable() && !isDiveActive() && this.getBiome() == 1) {
            //     this.setDiveActive(true);
            // }
        }
    }
}
