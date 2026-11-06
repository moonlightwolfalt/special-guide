package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.FireTornado;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class Phoenix extends Animal {
    public Phoenix(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.flag_fliesLikeDragon = true;
        this.setCanBeUnderTree(false);
        this.setFlyingOver(true);
        this.setMaxHealth(300);
        this.setBarType(2);
        this.setCanShootFire(true);
        this.setLavaAnimal(true);
        this.fireShootSpecType = this.getInfo().getAnimalSpecies() == 1 ? 1 : this.getInfo().getAnimalSpecies() == 2 ? 3 : 0;
        this.setSpeed(8);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        getClient().setCamzoom(1400);
    }

    
    public int abilT = 0;
    private Timer abilTimer = new Timer(950);

    @Override
    public void update(){
        super.update();
        abilityRecharge();
    }
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
        if(this.getBiome() == 1) {
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
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && abil.isUsable() && !abil.isRecharging()) {
       
            // FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY(    )+y, 10, 0, this.getAngle(), 12, this.getId(), this.getClient().getRoom(), this.getClient(),1);
            // this.getClient().getRoom().addObj(newFire);
           FireTornado tornado = new FireTornado(this.getClient().getRoom().getID(), this.getX(), this.getY(), 20, this);
           this.takeWater(12);

       
           this.getClient().getRoom().addObj(tornado);
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                
            }
        }
    }
}
