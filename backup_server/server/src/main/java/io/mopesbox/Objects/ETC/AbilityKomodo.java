package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;

import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class AbilityKomodo extends Ability {
    private Timer liveTimer = new Timer(2500);
    private Timer seeTimer = new Timer(2500);
    private Room room;
    public Animal owner;
    public Animal gotcha;
    public int ang = Utils.randomInt(0, 90);
    public double a = Utils.randomDouble(0, 20);

    public double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius() + a)));
    public double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius() + a)));

    public boolean canSee = true;
    public boolean stillAlive = true;
    public boolean alrChangedAngle = false;

    public AbilityKomodo(int id, double x, double y, int radius, Room room, Animal owner, int species) {
        super(id, x, y, 900, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if (o instanceof Animal && (gotcha == null || gotcha == (Animal) o) && ((Animal) o) != this.owner
                && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying
                && !((Animal) o).isInHole() && ((Animal) o).bleedingSeconds <= 0 && !((Animal) o).isStunned()) {

            if (((Animal) o).getInfo().getTier() > 16 || ((Animal) o).isLavaAnimal()) {
                ((Animal) owner).flag_usingAbility = false;
                this.owner.setFire(5, this.owner, 3);
                this.room.removeObj(this, this.owner);
                stillAlive = false;

                return;
            }
            if (((Animal) o).getInfo().getTier() > 15) {
                ((Animal) owner).flag_usingAbility = false;

                this.owner.hurt(5, 0, o);
                this.room.removeObj(this, this.owner);
                stillAlive = false;
                return;
            }

            ((Animal) o).setStun(5);

            if (this.gotcha == null) {
                this.gotcha = (Animal) o;
                this.gotcha.hurt(10, 0, this.owner);
                this.gotcha.flag_eff_sweatPoisoned = true;
                this.gotcha.setSpeed(this.gotcha.getSpeed() - 3);

                this.gotcha.getClient()
                        .send(Networking.personalGameEvent(255, "Oh no, you're sweating. Go drink loads of water! "));
                ((Animal) owner).flag_usingAbility = false;
                this.room.removeObj(this, this.owner);
                stillAlive = false;

            }
        }
    }

    @Override
    public void onCollisionExit(GameObject o) {
        if (gotcha == o) {

            gotcha = null;
            this.room.removeObj(this, null);
        }
    }

    @Override
    public boolean canBeVisible(GameClient client) {// Just static
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (this != null && !this.liveTimer.isDone() && stillAlive) {
            double x = ((Math.cos(Math.toRadians(((Animal) owner).getAngle())) * (((Animal) owner).getRadius() * 2))); // Some
                                                                                                                       // math
            double y = ((Math.sin(Math.toRadians(((Animal) owner).getAngle())) * (((Animal) owner).getRadius() * 2)));
            this.setX(((Animal) owner).getX() + x);
            this.setY(((Animal) owner).getY() + y);
            if (this.gotcha != null && this != null && stillAlive && this.liveTimer.isRunning) {

                this.gotcha.setX(this.getX());// Make sure that the position of ability will be the same of animal
                                              // position
                this.gotcha.setY(this.getY());

            }
        }

        liveTimer.update(Constants.TICKS_PER_SECOND);

        if (liveTimer.isDone() || this.owner == null && stillAlive) {
            if (this.owner != null) {
                ((Animal) owner).flag_usingAbility = false;

            }

            this.room.removeObj(this, this.owner != null ? ((Animal) owner) : null);
            liveTimer.reset();
            return;
        }
        if (seeTimer != null) {
            seeTimer.update(Constants.TICKS_PER_SECOND);
            if (seeTimer.isDone()) {

                canSee = false;
                seeTimer.reset();
                seeTimer = null;
                this.owner.getClient().getRoom().removeVisObj(this, null, null);
            }
        }

    }
}