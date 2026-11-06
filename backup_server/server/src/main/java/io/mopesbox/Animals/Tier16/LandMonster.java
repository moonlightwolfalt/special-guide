package io.mopesbox.Animals.Tier16;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.SinkHole;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class LandMonster extends Animal {
    public LandMonster(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.flag_fliesLikeDragon = true;
        this.setCanBeUnderTree(false);
        this.setMaxHealth(450);
        this.setFlyingOver(true);
        this.setBarType(2);
        this.biteDamage = 60;
        this.setCanShootFire(true);
        this.setLavaAnimal(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.fireShootSpecType = this.getInfo().getAnimalSpecies() == 1 ? 1 : 0;
        this.setSpeed(12);
        getClient().setCamzoom(1300);
    }

    private int abilT = 0;
    
    private Timer abilTimer = new Timer(950);

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() != 16 && ((Animal)o).getInfo().getTier() > 5 && !this.flag_flying) {
            return true;
        }
        if(o instanceof Animal){
            return false;
        }
        return true;
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

    private Timer coolDownTimer = new Timer(1000);

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if (coolDownTimer.isDone()) {
            this.flag_fliesLikeDragon = true;
         } else {
            coolDownTimer.update(Constants.TICKS_PER_SECOND);
            this.flag_fliesLikeDragon = false;
         }// /ban deleted user bc why not
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && abil.isUsable() && !abil.isRecharging()) {
            if (this.getWater() > this.maxwater / 4){
            this.takeWater(15);
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            SinkHole sinkHole = new SinkHole(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getRadius() + 4, this.getClient().getRoom(), this);
            this.getClient().getRoom().addObj(sinkHole);
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
        }
    }
    }
}
