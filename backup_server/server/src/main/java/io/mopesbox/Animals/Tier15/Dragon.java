package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Client.GameClient;

public class Dragon extends Animal {
    private int abilT = 0;

    public Dragon(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.flag_fliesLikeDragon = true;
        this.setCanBeUnderTree(false);
        this.setFlyingOver(true);
        this.setMaxHealth(300);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() != 15 && ((Animal)o).getInfo().getTier() > 5 && !this.flag_flying) {
            return true;
        }
        if(o instanceof Animal){
            return false;
        }
        return true;
    }

    private Timer abilTimer = new Timer(950);

    // private Timer coolDownTimer = new Timer(1000);

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

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        // if (coolDownTimer.isDone()) {
        //     this.flag_fliesLikeDragon = true;
        //  } else {
        //     coolDownTimer.update(Constants.TICKS_PER_SECOND);
        //     this.flag_fliesLikeDragon = false;
        //  }// /ban deleted user bc why not lol
        // coolDownTimer.update(Constants.TICKS_PER_SECOND);
        // if(coolDownTimer.isDone()) {
        //     abilTimer.reset();
        //     this.flag_fliesLikeDragon = true;
        // }
    
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 23, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);
    }

    // private void disableAbility() {
    //     this.getInfo().getAbility().setActive(false);
    //     this.setSpecies(0);
    //     this.getClient().getRoom().removeObj(this.newFire, null);
    //     this.newFire = null;
    //     if (!this.getClient().instantRecharge) {
    //         this.getInfo().getAbility().setRecharging(true);
    //         abilT = 1;
    //         this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
    //     }
    // }

    // private FireBall newFire = null;

    

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && abil.isUsable() && !abil.isRecharging()) {
            double ran = +Utils.randomDouble(-12, 12);
            double x = ((Math.cos(Math.toRadians(this.getAngle()+ran)) * (this.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.getAngle()+ran)) * (this.getRadius())));
            FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 13, this.getInfo().getAnimalSpecies() == 1 ? 1 : 0, this.getAngle(), 22, this.getId(), this.getClient().getRoom(), this.getClient(), 10);
            this.getClient().getRoom().addObj(newFire);
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 1;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
            // this.abilSpeed.reset();
        }
    }
    // @Override
    // public void onCollisionEnter(GameObject o) {
    //     super.onCollisionEnter(o);
    //     if (o instanceof Animal) {
    //         if (!((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
    //                 && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
    //                 && !((Animal) o).isInvincible()
    //                 && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
    //             // double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
    //         //     double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
    //         //     xx = this.getX() + xx;
    //         //     yy = this.getY() + yy;
    //         //     // if (((Animal) o).getInfo().getTier() > this.getInfo().getTier())
    //         //         // return;
    //         //     if (this.isRunning
    //         //             && ((o instanceof Bison && this.runSpeed > ((Bison) o).runSpeed - 1)
    //         //                     || !(o instanceof Bison))
    //         //             && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)
    //         //             && this.runSpeed >= 30) {
    //         //         Animal obj = (Animal) o;
    //         //         double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 10)));
    //         //         double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 10)));
    //         //         obj.makeRam(x, y, ((Animal) this), 5);
    //         //         disableAbility();
    //         //     }
    //         // }
    //             Animal ani = ((Animal) o);

    //             double multipl = this.getInfo().getSkin() == 1 || this.getInfo().getSkin() == 2 ? 2 : 1;

    //             // if (multipl < 1)
    //                 // multipl = 1;

    //             disableAbility();
                

    //             // ani.hurt()
    //             // Main.log.info("COLLISIONS DONE");
    //         }
    //     }
    // }
}
