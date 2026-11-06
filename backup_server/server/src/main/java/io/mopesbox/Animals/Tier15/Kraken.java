package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.Whirpool;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class Kraken extends Animal {
    private int abilT = 0;

    public Kraken(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(300);
        this.setWaterfowl(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
        this.setDiveDuration(22);

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

    public void checknAbil() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(this.getBiome() != 1) {
            abil.setUsable(false);
        } else {
            if(!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole()) {
                abil.setUsable(true);
                // abil.setPossible(divePossible);
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
        super.update();
        abilityRecharge();
        checknAbil();
 
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 7, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && abil.isUsable() && !abil.isRecharging() && this.getBiome() == 1) {
            // double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            // double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            // FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 10, 0, this.getAngle(), 12, this.getId(), this.getClient().getRoom(), this.getClient(),1);
            // this.getClient().getRoom().addObj(newFire);
            Whirpool whirpool = new Whirpool(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius() + 100, this.getClient().getRoom(), this);
            this.getClient().getRoom().addObj(whirpool);
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 15;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
        }
    }
}
