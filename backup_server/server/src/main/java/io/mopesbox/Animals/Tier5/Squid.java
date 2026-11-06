package io.mopesbox.Animals.Tier5;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.ETC.SquidInk;

public class Squid extends Animal {
    public Squid(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
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


    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }
    @Override
    public void onHurt(){
        SquidInk SquidInk = new SquidInk(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), this.getClient().getRoom(), this, this.getInfo().getAnimalSubSpecies());
        this.getClient().getRoom().addObj(SquidInk);
        abilTimer.reset();
        abilT = 8;
        if(!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
        }
}