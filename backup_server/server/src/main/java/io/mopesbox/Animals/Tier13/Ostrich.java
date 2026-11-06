package io.mopesbox.Animals.Tier13;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Client.AI.AIOstrishBaby;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.PvP.Target;
import io.mopesbox.Objects.GameObject;

public class Ostrich extends Animal {
    public Ostrich(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(289.5);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        this.setCamzoom(1700);
    }

    public int babyOrEgg = 0;

    public void checknAbil() {
        PlayerAbility abil = this.getInfo().getAbility();
    
            if (!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole()) {
                abil.setUsable(true);
            
        }
    }

    public boolean getCollideableIs(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).isInHole())
                return false;
            if (((Animal) o).isDiveActive())
                return false;
        }
        return true;
    }


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        checknAbil();
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            if (babyOrEgg < 6) {

                double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
                double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
                AIOstrishBaby duckling = new AIOstrishBaby(this.getRoom(), this, getX()+x,
                        getY()+y);
                this.getRoom().addAI(duckling);
                this.babyOrEgg++;
                             if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 12;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                abilTimer.reset();
               

            } else {
                if (this.getClient().ostrichTarget == null) {
                    this.getClient().ostrichTarget = new Target(getRoom().getID(), getX(), getY(), getClient(), 2, 1);
                    this.getRoom().addObj(this.getClient().ostrichTarget);
                    return;
                } else {
                    if(this.getClient().ostrichTarget != null && this.getClient().ostrichTarget.attachedTo != null){
                            this.getRoom().removeObj(this.getClient().ostrichTarget, null);
                            this.getClient().ostrichTarget = null;
                    }
                    if (this.getClient().ostrichTarget != null && this.getClient().ostrichTarget.attachedTo == null && this.getClient().ostrichTarget.targ1 != null) {
                                this.getClient().ostrichTarget.setAttach(this.getClient().ostrichTarget.targ1);

                            
                        }
                        // return;
                    }

                }
            
             if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 12;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                abilTimer.reset();

        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if (abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if (abilTimer.isDone()) {
                abilT--;
                if (abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                }
                abilTimer.reset();
            }
        }
    }

}
