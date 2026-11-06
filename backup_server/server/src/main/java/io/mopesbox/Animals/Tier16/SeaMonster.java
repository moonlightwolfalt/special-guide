package io.mopesbox.Animals.Tier16;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.BigWhirlpool;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Client.GameClient;

public class SeaMonster extends Animal {
    public SeaMonster(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setBarType(1);
        this.setWaterfowl(true);
        this.setDivePossible(true);
        this.setDiveUsable(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.biteDamage = 60;
        this.setMaxHealth(450);
        usingAbility.isRunning = false;
        this.setSpeed(12);
        this.setCamzoom(1300);
        this.bypass_waterani_slowness = false;

    }

    private Timer notifyTimer = new Timer(12000);
    private Timer onSurfaceTimer = new Timer(24000);

    private Timer exhaustTimerFire = new Timer(8000);

    @Override
    public void onWaterLose() {
        notifyTimer.update(Constants.TICKS_PER_SECOND);
        exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
        if(this.getBiome() == 1 && !this.isDiveActive() && !this.isInHole()) {
            waterTimer.update(Constants.TICKS_PER_SECOND);
            if(waterTimer.isDone()) {
                this.addWater(2);
                if(this.fireSeconds > 0) this.fireSeconds--;
                waterTimer.reset();
            }
            if(!this.isDiveActive()) {
                onSurfaceTimer.update(Constants.TICKS_PER_SECOND);
                if(onSurfaceTimer.isDone()) {
                    this.takeWater(5);
                    this.setFire(2, null, 12);
                    this.getClient().send(Networking.personalGameEvent(255, "Ouch! You can't stay on the surface for too long!"));
                    onSurfaceTimer.reset();
                }
            } else {
                onSurfaceTimer.reset();
            }
        } else {
            if(exhaustTimerFire.isDone() && !this.isDiveActive()) {
                this.setFire(2, null, 4);
                this.getClient().send(Networking.personalGameEvent(255, "Ouch! It's hot outside water!"));
                exhaustTimerFire.reset();
            }
            if(notifyTimer.isDone()) {
                this.getClient().send(Networking.personalGameEvent(255, "You regen health only in water!"));
                notifyTimer.reset();
            }
        }
    }

    private int abilT = 0;
    private int diveT = 0;

    private Timer abilTimer = new Timer(950);
    private Timer diveTimer = new Timer(1000);

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
        if(diveT > 0) {
            diveTimer.update(Constants.TICKS_PER_SECOND);
            if(diveTimer.isDone()) {
                diveT--;
                if(diveT < 1) {
                    this.setDiveRecharging(false);
                }
                diveTimer.reset();
            }
        }
    }

    public void checkDiving() {
        if(this.isDiveActive()) {
            if(this.getBiome() != 1) {
                this.disableDiving();
            }
        } else {
            if(this.getBiome() == 1 && !this.isInHole()) {
                this.setDiveUsable(true);
            } else {
                this.setDiveUsable(false);
            }
        }
    }

    private Timer usingAbility = new Timer(5000);

    public void disableDiving() {
        this.setDiveActive(false);
        if(!this.getClient().instantRecharge) {
            setDiveRecharging(true);
            diveT = 2;
            this.getClient().send(Networking.rechargingAbility(true, diveT, this.getClient().getRoom()));
        }
    }

    public void checknAbil() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(this.getBiome() != 1) {
            abil.setUsable(false);
        } else {
            if(!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole()) {
                abil.setUsable(true);
            }
        }
    }

    public double speedManipulation(double speed) {
        if(this.getInfo().getAbility().isActive()) speed /= 2;
        return speed;
    }

    public void damage(int damage, Animal biter) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        killer = biter;
    }

    private Animal killer;

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() > 16 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
        }
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        checkDiving();
        checknAbil();
        if(this.health <= 0) {
            if(this.killer != null){
            if(this.killer.getClient() != null){
                this.killer.getClient().addXp(this.getClient().getXP());
                if(this.killer instanceof BlackDragon){
                ((BlackDragon) this.killer).addApex(this.getInfo().getAnimalType(), this.getClient());
                }
            }
        }
    }

        if(usingAbility.isRunning) {
            usingAbility.update(Constants.TICKS_PER_SECOND);
            if(!usingAbility.isDone()) {
                this.isInBigWhirlpool = true;
                this.flag_underWater = true;

                this.getInfo().getAbility().setActive(true);
            } else {
                this.getInfo().getAbility().setActive(false);
                this.isInBigWhirlpool = false;
                usingAbility.reset();
                usingAbility.isRunning = false;
                PlayerAbility abil = this.getInfo().getAbility();
                if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 10;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
            }
        }
    }

    @Override
    public void onAbilityDisable() {
        if(isDiveActive()) {
            this.disableDiving();
        }
    }

    @Override
    public void useAbility() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.isDiveMain() && !this.isDiveActive()) {
            if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive() && this.getBiome() == 1) {
                BigWhirlpool sinkHole = new BigWhirlpool(this.getClient().getRoom().getID(), this.getX(), this.getY(), ((int) Math.round(this.getRadius() * 4.5)), this.getClient().getRoom(), this);
                this.getClient().getRoom().addObj(sinkHole);
                this.abilSpeed.reset();
                this.takeWater(this.maxwater/4);
                this.usingAbility.isRunning = true;
            }
        } else {
            if(isDivePossible() && !isDiveRecharging() && isDiveUsable() && !isDiveActive() && this.getBiome() == 1 && !abil.isActive()) {
                this.setDiveActive(true);
            }
        }
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 7, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);
    }
}
