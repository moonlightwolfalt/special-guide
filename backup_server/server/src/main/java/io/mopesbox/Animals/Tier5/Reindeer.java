package io.mopesbox.Animals.Tier5;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Objects.ETC.DigFood;
import io.mopesbox.Utils.Timer;

public class Reindeer extends Animal {
    public Reindeer(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        info.getAbility().setHoldAbility(true);
        this.setSpeed(12);
        this.setCamzoom(2500);
        this.setMaxHealth(205.5);
    }
    Timer timeOutTimer = new Timer(3000);

    Ability abil;


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        timeOutTimer.update(Constants.TICKS_PER_SECOND);

          PlayerAbility abil = this.getInfo().getAbility();

        if(this.getBiome() == 1) {
            abil.setUsable(false);
        }

    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
         
            DigFood digfood = new DigFood(this.getClient().getRoom().getID(), this.getX(), this.getY(), (int) Math.floor(Math.max(this.getRadius(), this.getRadius() * 1.7)), this.getClient().getRoom(), this);
            this.getClient().getRoom().addObj(digfood);
            abil.setActive(true);
            this.setSpeed(3);
            // this.abil = digfood;

           
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
    public void disableAbility(){
                PlayerAbility abil = this.getInfo().getAbility();

                this.setSpeed(8);

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
     @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 6 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
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

  
}
