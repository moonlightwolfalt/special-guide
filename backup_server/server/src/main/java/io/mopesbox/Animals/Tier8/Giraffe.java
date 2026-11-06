package io.mopesbox.Animals.Tier8;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.giraffeStomp;

public class Giraffe extends Animal {

    private Timer spawnStompTimer = new Timer(500);
    private int times = 0;

    public Giraffe(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(237);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(12);
        info.getAbility().setHoldAbility(true);
        this.setCamzoom(1500);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            double x = ((Math.cos(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            double y = ((Math.sin(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            giraffeStomp giraffeStomp = new giraffeStomp(this.getClient().getRoom().getID(), this.getX() - x,
                    this.getY() - y, ((int) Math.round(this.getRadius() * 1.5)), this.getClient().getRoom(),
                    this.getAngle(), this, this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(giraffeStomp);

            this.abilSpeed.reset();
            abil.setActive(true);
        }
    }
    private void abilityUpdate(){
        if(this.getInfo().getAbility().isActive()){
            spawnStompTimer.update(Constants.TICKS_PER_SECOND);
            if(spawnStompTimer.isDone()){
                if(times < 6){
                times++;
            double x = ((Math.cos(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            double y = ((Math.sin(Math.toRadians(this.getAngle()) + Math.PI) * (this.getRadius() * 1.25)));
            giraffeStomp giraffeStomp = new giraffeStomp(this.getClient().getRoom().getID(), this.getX() - x,
                    this.getY() - y, ((int) Math.round(this.getRadius() * 1.5)), this.getClient().getRoom(),
                    this.getAngle(), this, this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(giraffeStomp);
            spawnStompTimer.reset();
                }else{
                    this.disableAbility();
                }
            }
        }
    }

    public void disableAbility() {
        PlayerAbility abil = getInfo().getAbility();

        spawnStompTimer.reset();
        times = 0;

        abil.setActive(false);

        if (!this.getClient().instantRecharge) {
            abil.setRecharging(true);
            abilT = 10;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
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

    @Override
    public void update() {
        super.update();
        abilityUpdate();
        abilityRecharge();
    }

    // @Override
    // public void onDeath() {
    // AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(),
    // this.getX(), this.getY(), this.getRadius(), 16, true,
    // this.getClient().getNickname(), this.getClient().getRoom(),
    // this.getClient().getXP()/2);
    // this.getClient().getRoom().addObj(carcass);
    // }
}
