package io.mopesbox.Animals.Tier14;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.Snowball;
import io.mopesbox.Utils.Timer;


public class Mammoth extends Animal {
    public Mammoth(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(false);
        this.setArcticAnimal(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setMaxHealth(300);
        this.biteDamage = 60;
        this.setSlidingOnIce(false);
        this.setSpeed(8);
        this.setCamzoom(1500);
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

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            Snowball snowball = new Snowball(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 20, this.getClient().getRoom(), this.getAngle(), 10, this, this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(snowball);
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 5;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
        }
    }
}
