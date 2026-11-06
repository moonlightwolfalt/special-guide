package io.mopesbox.Animals.Tier14;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.SpiderWeb;
import io.mopesbox.Utils.Timer;

public class GiantSpider extends Animal {
private SpiderWeb actualSpiderWeb;

    public GiantSpider(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(300);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(8);
        this.setCamzoom(1500);
        this.setCanClimbHills(true);
        info.getAbility().setHoldAbility(true);
        this.setCanClimbRocks(true);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
                        abil.setActive(true);

            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
            SpiderWeb tailslap = new SpiderWeb(this.getClient().getRoom().getID(), this.getX()-x, this.getY()-y, 10, this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies());
            this.actualSpiderWeb = tailslap;
            this.getClient().getRoom().addObj(tailslap);
            this.flag_eff_isOnSpiderWeb = true;

        }
    }
    public void disableAbility(){
        if(this.actualSpiderWeb != null && this.actualSpiderWeb.isBuilding){
            this.actualSpiderWeb.isBuilding = false;
            this.actualSpiderWeb = null;
            this.setSpeed(8);
            PlayerAbility abil = getInfo().getAbility();
            abil.setActive(false);
                if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
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

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if(this.actualSpiderWeb != null){
            if(this.actualSpiderWeb.isBuilding){
                this.setSpeed(4);
            }
        }
    }
          @Override
        public void onHurt(){
            if(this.getInfo().getAbility().isActive() && this.actualSpiderWeb != null){
                this.disableAbility();
            }
        }
}