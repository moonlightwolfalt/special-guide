package io.mopesbox.Animals.Tier13;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.BoaConstrictorShit;
import io.mopesbox.Objects.Eatable.Meat;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Objects.GameObject;

public class BoaConstrictor extends Animal {
    public BoaConstrictor(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(289.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        this.setCamzoom(1700);
        this.setCanClimbHills(true);
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 14, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);
    }

    @Override
    public void onHurt() {
        if(this.abilRunning || this.claw != null) this.disableAbility();
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
        this.abilRunning = false;
        if(this.claw != null) {
            if(this.claw.gotcha != null) {
                this.claw.gotcha.setBleeding(5, this, 10);
            }
            this.getClient().getRoom().removeObj(this.claw, null);
            this.claw = null;
        }
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    private Timer runAbilTimer = new Timer(1);

    private Timer runAbilTimer2 = new Timer(3000);

    private Timer hurtAbilTimer = new Timer(800);
    
    private BoaConstrictorShit claw = null;

    private int stage = 0;

    private void abilUpdate() {
        if(this.abilRunning) {
            if(stage == 0) {
                this.getInfo().getAbility().setActive(true);
                if(claw == null) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                    claw = new BoaConstrictorShit(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getRadius(), this.getClient().getRoom(), this, this.getInfo().getAnimalSpecies());
                    this.getClient().getRoom().addObj(claw);
                }
                runAbilTimer.update(Constants.TICKS_PER_SECOND);
                if(runAbilTimer.isDone()) {
                    stage = 1;
                    runAbilTimer.reset();
                    this.flag_eff_aniInClaws = true;
                }
            } else {
                runAbilTimer2.update(Constants.TICKS_PER_SECOND);
                if(this.claw != null && this.claw.gotcha != null) {
                    hurtAbilTimer.update(Constants.TICKS_PER_SECOND);
                    if(hurtAbilTimer.isDone()) {
                        this.claw.gotcha.hurt(75, 14, this);
                        double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                        double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
                        int xp = Math.min(this.claw.gotcha.getClient().getXP(), 2500);
                        Meat meat = new Meat(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 0, xp, this.claw.gotcha.getId(), this.getClient().getRoom(), this.claw.gotcha);
                        this.claw.gotcha.getClient().takeXp(xp);
                        this.getClient().getRoom().addObj(meat);
                        hurtAbilTimer.reset();
                    }
                }
                if(runAbilTimer2.isDone() || (this.claw == null || this.claw.gotcha == null)) {
                    runAbilTimer2.reset();
                    abilRunning = false;
                    this.flag_eff_aniInClaws = false;
                    this.stage = 0;
                    disableAbility();
                }
            }
        }
    }

   

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public void update() {
        // if(this.abilRunning) {
        //     if(this.claw != null) {
        //         this.setMouseX(this.claw.getX());
        //         this.setMouseY(this.claw.getY());
        //     }
        // }
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
                // this.getClient().getRoom().sendChat(this.getClient(), "");
            }
        } else {
            // if(isDivePossible() && !isDiveRecharging() && isDiveUsable() && !isDiveActive() && this.getBiome() == 1) {
            //     this.setDiveActive(true);
            // }
        }
    }
}

