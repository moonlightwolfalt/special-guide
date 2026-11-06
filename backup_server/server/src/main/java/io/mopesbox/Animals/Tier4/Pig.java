package io.mopesbox.Animals.Tier4;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.StinkPig;
import io.mopesbox.Utils.Timer;

public class Pig extends Animal {

    public Pig(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(195);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        this.setCamzoom(2600);
    }

  


    // public boolean isHiding = false;

    @Override
   public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
         

            StinkPig stink = new StinkPig(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius()* 4, this.getClient().getRoom(), this, this.getInfo().getAnimalSubSpecies());
            this.getClient().getRoom().addObj(stink);
            this.getClient().getRoom().chat(this.getClient(), "PFFFFFFFFF");
            if(this.getInfo().getAnimalSpecies() == 2){
                for(int o = 0;o < 3; o++){
                StinkPig stink1 = new StinkPig(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius()* 4, this.getClient().getRoom(), this, this.getInfo().getAnimalSubSpecies());
            this.getClient().getRoom().addObj(stink1);
                }
                
            }


            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
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

    @Override
    public void update() {
        super.update();
        abilityRecharge();
    }
}
