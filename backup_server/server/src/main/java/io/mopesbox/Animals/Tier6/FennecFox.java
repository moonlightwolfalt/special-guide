package io.mopesbox.Animals.Tier6;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Static.Whine;
import io.mopesbox.Utils.Timer;

public class FennecFox extends Animal {
    public FennecFox(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(216);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCanBeInHole(true);
        this.setCanBeInSmallHole(true);
        this.setSpeed(12);
        this.setDesertAnimal(true);
        this.setCamzoom(2200);
    }
    private Timer abilTimer = new Timer(950);
    public int times;

    @Override
    public void useAbility() {
        if(!this.getInfo().getAbility().isActive() && !this.getInfo().getAbility().isRecharging() && this.getInfo().getAbility().isUsable()){
        int ang = -30;
                for(int i = 0; i < 2; i++) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle()+ang)) * (this.getRadius()*1.2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle()+ang)) * (this.getRadius()*1.2)));
                    Whine tailslap = new Whine(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getBiome(), this.getClient().getRoom(), this, ang, this.getAngle()+(ang/4));
                    ang += 60;

                    this.getClient().getRoom().addObj(tailslap);
                    this.setSpecies(1);
                    this.stopAbil();

                    }
                }
            }
    private int abilT;
        public void stopAbil() {
        this.getInfo().getAbility().setActive(false);
        this.times = 0;
        this.flag_usingAbility = false;
        PlayerAbility abil = this.getInfo().getAbility();
        this.setSpecies(0);


          if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
    }

      @Override
    public void update(){
     abilityRecharge();
     super.update();
    }

  
    public void abilityRecharge() {
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
