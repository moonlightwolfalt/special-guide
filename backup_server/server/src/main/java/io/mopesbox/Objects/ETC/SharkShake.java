package io.mopesbox.Objects.ETC;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class SharkShake extends Ability {
    private Timer liveTimer = new Timer(5000);
    private Timer seeTimer = new Timer(250);
    private Room room;
    public Animal owner;
    public Animal gotcha;
    public boolean canSee = true;
    public SharkShake(int id, double x, double y, int radius, Room room, Animal owner, int species, double angle) {
        super(id, x, y, 46, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setSpecies(species);
        // this.setAngle(this.getAngle());
        this.setSendsAngle(true);
        this.setObjAngle(angle);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && (gotcha == null || gotcha == (Animal)o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && ((Animal) o).bleedingSeconds <= 0 && !((Animal) o).isStunned()) {
            double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (o.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (o.getRadius())));
            ((Animal)o).setX(this.getX()+x);
            ((Animal)o).setY(this.getY()+y);
            ((Animal)o).setStun(5);
            if(this.gotcha == null) {
                this.gotcha = (Animal)o;
                this.gotcha.hurt(20, 14, this.owner);
            }
        }
    }

    @Override
    public void onCollisionExit(GameObject o) {
        if(gotcha == o) {
            gotcha = null;
            this.room.removeObj(this, null);
        }
    }

    @Override
    public void update() {
        super.update();
        liveTimer.update(Constants.TICKS_PER_SECOND);

        if (liveTimer.isDone() || this.owner == null) {
            this.room.removeObj(this, null);
            liveTimer.reset();
            return;
        }
        if(seeTimer != null) {
            seeTimer.update(Constants.TICKS_PER_SECOND);
            if (seeTimer.isDone()) {
                canSee = false;
                seeTimer.reset();
                seeTimer = null;
                this.owner.getClient().getRoom().removeVisObj(this, null, null);
            }
        }
        double ang = this.owner.getAngle();
        ang -= 180;
        if(ang < 0) ang += 360;
        this.setAngle(ang);

        double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius())));
        double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius())));
        this.setX(this.owner.getX()+x);
        this.setY(this.owner.getY()+y);
    }
}