package io.mopesbox.Animals.Tier6;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.pullfromhole;
import io.mopesbox.Objects.Static.Hole;

public class ArcticFox extends Animal {

    public ArcticFox(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(216);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        getClient().setCamzoom(2400);
    }

    public void stopAbil() {
        this.getInfo().getAbility().setActive(false);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.isInHole()) {
            // // this.setSpecies(1);
            // // double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
            // // double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
            // // DonkeyKick tailslap = new DonkeyKick(this.getClient().getRoom().getID(), this.getX()-x, this.getY()-y, ((int) Math.round(this.getRadius()*1.2)), this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies());
            // // this.getClient().getRoom().addObj(tailslap);
            
            pullfromhole slap = new pullfromhole(this.getClient().getRoom().getID(), this.getX(), this.getY(), ((int)Math.round(this.getHole().getRadius() * 4)), this.getClient().getRoom(), this, ((Hole)this.getHole()));
            this.getClient().getRoom().addObj(slap);
            if (!this.getClient().instantRecharge) {
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
        if(!this.isInHole()){
            this.getInfo().getAbility().setUsable(false);
        }else if(this.getInfo().getAbility().isRecharging() && !this.getInfo().getAbility().isUsable()){
            this.getInfo().getAbility().setUsable(true);

        }
      
    }
}
