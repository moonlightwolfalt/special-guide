package io.mopesbox.Animals.Tier9;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Objects.Eatable.Banana;
import io.mopesbox.Objects.Eatable.Coconut;

public class Gorilla extends Animal {
    public Gorilla(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(247.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.bypassAbilSpeed = false;
        this.setSpeed(12);
        this.setCamzoom(2100);
    }
    
    @Override
    public void update(){
        super.update();
        abilityRecharge();
    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
        
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()+25)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()+25)));
            boolean banan = Utils.randomBoolean();
            if(banan) {
                Banana banana = new Banana(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.getClient().getRoom(), this, 30, this.getAngle());
                this.getClient().getRoom().addObj(banana);
            } else {
                Coconut coconut = new Coconut(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.getClient().getRoom(), this, 30, this.getAngle());
                this.getClient().getRoom().addObj(coconut);
            }

            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 3;
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
