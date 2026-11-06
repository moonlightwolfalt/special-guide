package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.CrabSmash;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Client.GameClient;

public class KingCrab extends Animal {
    public KingCrab(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setMaxHealth(300);
        this.setCanClimbHills(true);
        this.setCanClimbRocks(true);
        this.setSlidingOnIce(false);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
        this.setDiveDuration(20);
    }
    
    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            boolean isLeft = Utils.randomBoolean();
            double x = ((Math.cos(Math.toRadians(this.getAngle() + (isLeft ? -30 : 30)) + Math.PI) * (this.getRadius()*1.25)));
            double y = ((Math.sin(Math.toRadians(this.getAngle() + (isLeft ? -30 : 30)) + Math.PI) * (this.getRadius()*1.25)));
            CrabSmash tailslap = new CrabSmash(this.getClient().getRoom().getID(), this.getX()-x, this.getY()-y, ((int) Math.round(this.getRadius()*1.5)), this.getClient().getRoom(), this.getAngle(), this, this.getInfo().getAnimalSpecies(), isLeft);
            this.getClient().getRoom().addObj(tailslap);
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 10;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            this.abilSpeed.reset();
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
        // if(this.health <= 0) {
        //     if(this.killer != null && this.killer.getClient() != null)
        //         this.killer.getClient().addXp(this.getClient().getXP());
        //         ((BlackDragon) this.killer).addApex(this.getInfo().getAnimalType(), this.getClient());
        // }
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 5, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);

    }
}