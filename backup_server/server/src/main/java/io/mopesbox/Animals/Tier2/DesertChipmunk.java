package io.mopesbox.Animals.Tier2;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.Jump;
import io.mopesbox.Objects.ETC.Spawnfood;
public class DesertChipmunk extends Animal {
    public DesertChipmunk(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(165);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.bypassAbilSpeed = true;
        this.setSpecies(1);
        this.setSpeed(12);
        this.setCamzoom(2800);
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
        
            Jump tailslap = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap);

            Spawnfood spawnfood = new Spawnfood(
                this.getClient().getRoom().getID(),
                this.getX(),
                this.getY() - 100,
                (int) Math.floor(Math.max(this.getRadius(), this.getRadius() * 1.7)),
                this.getClient().getRoom(),
            this
);

this.getClient().getRoom().addObj(spawnfood);
abil.setActive(true);
this.setMove(false);

            if (this.getInfo().getSkin() == 4) {
                Jump tailslap1 = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap1);
            Jump tailslap2 = new Jump(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) this.getRadius(), this.getClient().getRoom(), this.getAngle(), this);
            this.getClient().getRoom().addObj(tailslap2);
            }
            if(!this.getClient().instantRecharge) {
                            this.setSpecies(0);
                abil.setRecharging(true);
                abilT = 3;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            abilTimer.reset();
        }
    }
    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

        public void checkspec() {
        PlayerAbility abil = this.getInfo().getAbility();
                    if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
                        this.setSpecies(1);
                    } else {
                        this.setSpecies(0);
                    }

        }


        public void disableAbility(){
            PlayerAbility abil = this.getInfo().getAbility();

     if(!this.getClient().instantRecharge) {
            abil.setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }else{
            abil.setRecharging(false);
            abilT = 0;

        }
        abilTimer.reset();

    this.setMove(true);
 
    this.getInfo().getAbility().setActive(false);
   
    
}



    public void abilityRecharge() {
        if(abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if(abilTimer.isDone()) {
                abilT--;
                if(abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                                    this.setSpecies(1);
                }
                abilTimer.reset();
            }
        }
    }

}
