package io.mopesbox.Animals.Tier6;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;

public class Bee extends Animal {
    public Bee(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.flag_fliesLikeDragon = true;
        this.baseRadius = 14;
        this.setRadius(baseRadius);
        this.setCanBeUnderTree(false);
        this.setFlyingOver(true);
        this.setMaxHealth(216);
        this.setCanBeInHole(false);
        this.setCanBeInSmallHole(false);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setSpeed(12);
        getClient().setCamzoom(2400);
    }

    public void checknAbil() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(!this.hasSting) {
            abil.setUsable(false);
        } else {
            if(!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole()) {
                abil.setUsable(true);
            }
        }
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

    public boolean hasSting = true;

    private boolean usingAbil = false;

    private double speed = 0;

    public boolean canAbility() {
        PlayerAbility abil = this.getInfo().getAbility();
        return abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !usingAbil && hasSting;
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(canAbility()) {
            this.setSpecies(1);
            usingAbil = true;
            canCalculateAngle = false;
            canCalculateMovement = false;
            speed = -8;
            abil.setActive(true);
        }
    }

    private int toRegen = 0;

    private Timer regenTimer = new Timer(1000);

    public void disableAbility(int abilt, boolean removeSting) {
        this.getInfo().getAbility().setActive(false);
        this.speed = 0;
        canCalculateAngle = true;
        canCalculateMovement = true;
        usingAbil = false;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = abilt;
            if(removeSting) {
                this.hasSting = false;
                this.toRegen = abilt;
            }
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }



    @Override
    public void update() {
        super.update();
        abilityRecharge();
        checknAbil();
        if(usingAbil) this.setSpecies(1);
        else {
            if(this.hasSting) this.setSpecies(0);
            else this.setSpecies(2);
        }
        if(usingAbil) {
            this.flag_usingAbility = true;
            double ang = this.getAngle();
            ang -= 180;
            if(ang < 0) ang += 360;
            this.addVelocityX(((Math.cos(Math.toRadians(ang)) * (this.speed))));
            this.addVelocityY(((Math.sin(Math.toRadians(ang)) * (this.speed))));
            this.speed += 5;
            if(this.speed > 30) {
                disableAbility(5, false);
            }
        } else {
            this.flag_usingAbility = false;
        }
        if(toRegen > 0) {
            hasSting = false;
            regenTimer.update(Constants.TICKS_PER_SECOND);
            if(regenTimer.isDone()) {
                this.hurt(2, 0, null);
                toRegen--;
                if(toRegen <= 0) hasSting = true;
                regenTimer.reset();
            }
        }
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        super.onCollisionEnter(o);
        if (o instanceof Animal && !(o instanceof Bee) && ((Animal)o).getInfo().getTier() <= 16) {
            if (!((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double ang = this.getAngle();
                ang -= 180;
                if(ang < 0) ang += 360;
                double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.usingAbil && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
                    Animal obj = (Animal) o;
                    if(obj.bleedingSeconds <= 0 && !obj.isStunned()) {
                        obj.setBleeding(6, this, obj.maxHealth/24);
                        disableAbility(15, true);
                    }
                }
            }
        }
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (o instanceof Animal && !(o instanceof Bee) && this.speed > 10 && ((Animal)o).getInfo().getTier() <= 16) {
            if (!((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double ang = this.getAngle();
                ang -= 180;
                if(ang < 0) ang += 360;
                double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.usingAbil && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
                    Animal obj = (Animal) o;
                    if(obj.bleedingSeconds <= 0 && !obj.isStunned()) {
                        obj.setBleeding(6, this, obj.maxHealth/24);
                        disableAbility(15, true);
                    }
                }
            }
        }
        super.onCollision(o);
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
}
