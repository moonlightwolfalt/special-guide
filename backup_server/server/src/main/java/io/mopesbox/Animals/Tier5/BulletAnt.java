package io.mopesbox.Animals.Tier5;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.AntBite;
import io.mopesbox.Utils.Timer;
public class BulletAnt extends Animal {
    public BulletAnt(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.baseRadius = 9;
        this.mainSpeed = 14;
        this.setCamzoom(2500);
        if (this.getInfo().getSkin() == 1) this.mainSpeed = 12;
        this.setSpeed(this.mainSpeed);
        this.setDesertAnimal(true);
        this.setRadius(baseRadius);
        if (this.getInfo().getSkin() == 1) 
        this.setMaxHealth(411);
        else
        this.setMaxHealth(205.5);
        this.setCanClimbHills(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
    }

    private int abilT = 0;

    public void disableAbility(int abilt) {
        if(this.bite != null)
        this.bite.delete();
        this.bite = null;
        this.getInfo().getAbility().setActive(false);
        this.setSpeed(this.mainSpeed);
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = abilt;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    public AntBite bite;

    public boolean canAbility() {
        PlayerAbility abil = this.getInfo().getAbility();
        return abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive() && bite == null;
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.isDiveMain() && !this.isDiveActive()) {
            if(canAbility()) {
                double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
                AntBite bite = new AntBite(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getRadius(), this.getClient().getRoom(), this);
                this.getClient().getRoom().addObj(bite);
                this.bite = bite;
                abil.setActive(true);
            } else if(abil.isActive() && bite != null) {
                disableAbility(this.bite.gotcha == null ? 5 : 10);
            }
        }
    }



    @Override
    public void update() {
        super.update();
        if(this.bite != null) {
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*2)));
            this.bite.setX(this.getX()+x);
            this.bite.setY(this.getY()+y);
        }
        abilityRecharge();
        if(!this.getInfo().getAbility().isActive() && bite != null) {
            this.disableAbility(this.bite.gotcha == null ? 5 : 10);
        }
    }

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
