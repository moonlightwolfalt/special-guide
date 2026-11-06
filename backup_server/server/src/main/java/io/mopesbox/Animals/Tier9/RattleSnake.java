package io.mopesbox.Animals.Tier9;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.Rattle;
import io.mopesbox.Utils.Timer;

public class RattleSnake extends Animal {

    public RattleSnake(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(247.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        this.setCamzoom(2100);
        info.getAbility().setHoldAbility(true);
        this.setDesertAnimal(true);
    }
    private int time = 0;
    private Timer cooldownT = new Timer(1000);
    public boolean holdingW = false;

  


    // public boolean isHiding = false;

    public void cancelHold(){
        holdingW = false;
        cooldownT.reset();
    }

    @Override
   public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && cooldownT.isDone()) {
            if(time < 4){
         

            time++;
            this.getInfo().getAbility().setActive(true);
            Rattle rattle = new Rattle(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius()* 2, this.getClient().getRoom(), this, this.getInfo().getAnimalSubSpecies());
            this.getClient().getRoom().addObj(rattle);
            this.getClient().getRoom().chat(this.getClient(), "SSSSSSSS!");
            cooldownT.reset();
            // if() abil.setActive(true);

            }else this.disableAbility();

        
        }
    }

    public void disableAbility(){

                PlayerAbility abil = this.getInfo().getAbility();
                this.holdingW = false;
                this.cooldownT.reset();
                this.setSpecies(0);
this.getInfo().getAbility().setActive(false);


            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            abilTimer.reset();
            cooldownT.reset();
            time = 0;
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
        if(holdingW){
            cooldownT.update(Constants.TICKS_PER_SECOND);
            if(cooldownT.isDone()){
                this.useAbility();
            }
        if(!this.getInfo().getAbility().isRecharging() && this.getInfo().getAbility().isPossible() && this.getInfo().getAbility().isUsable()) this.setSpecies(1);

        }
    }
}
