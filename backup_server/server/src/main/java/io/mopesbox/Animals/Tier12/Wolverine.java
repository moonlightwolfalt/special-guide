package io.mopesbox.Animals.Tier12;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Objects.ETC.Roar;
import io.mopesbox.Objects.GameObject;

public class Wolverine extends Animal {

    public String[] abilityMessage = new String[] { "ROAAR!" };

    public Wolverine(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setMaxHealth(279);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        groanTimer.isRunning = false;
        this.setSpeed(12);
        this.setCamzoom(1800);
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        groanTimer.update(Constants.TICKS_PER_SECOND);
        this.setSpecies(!groanTimer.isDone() ? 1 : 0);
        if(this.health <= 0) {
            if(this.killer != null && this.killer.getClient() != null)
                this.killer.getClient().addXp(this.getClient().getXP());
        }
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 14 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
        }
    }


    private Animal killer;

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            Roar tailslap = new Roar(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                    (int) Math.floor(this.getRadius() * 5), this.getClient().getRoom(), this.getAngle(), this,
                    this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(tailslap);

            this.getClient().getRoom().sendChat(this.getClient(),
                    abilityMessage[Utils.randomInt(0, abilityMessage.length - 1)]);
            groanTimer.reset();
            if (!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 10;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            abilTimer.reset();
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);
    private Timer groanTimer = new Timer(1000);

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
