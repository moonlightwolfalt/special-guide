package io.mopesbox.Animals.Tier13;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.Wave;
import io.mopesbox.Objects.GameObject;

public class KillerWhale extends Animal {
    public KillerWhale(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(289.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        this.setCamzoom(1700);
        this.setDiveDuration(30);
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
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 14 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
        }
    }
    
    @Override
    public void update(){
        super.update();
        abilityRecharge();
        checknAbil();
    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            int sang = -30;
            for(int i = 0; i < 3; i++) {
                double x = ((Math.cos(Math.toRadians(this.getAngle()+sang)) * (this.getRadius()*1.2)));
                double y = ((Math.sin(Math.toRadians(this.getAngle()+sang)) * (this.getRadius()*1.2)));
                Wave wave = new Wave(this.getClient().getRoom().getID(), this.getX() + x, this.getY() + y, (int) (this.getRadius()*1.2), this.getAngle()+(sang/4), 8, this.getId(), this.getClient().getRoom(), this.getClient());
                sang += 30;
                this.getClient().getRoom().addObj(wave);
            }
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 10;
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
