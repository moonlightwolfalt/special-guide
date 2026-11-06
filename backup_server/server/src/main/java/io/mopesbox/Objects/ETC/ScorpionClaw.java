package io.mopesbox.Objects.ETC;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class ScorpionClaw extends Ability {
    private Timer liveTimer = new Timer(5000);
    private Room room;
    public Animal owner;
    public Animal gotcha;
    public ScorpionClaw(int id, double x, double y, int radius, Room room, Animal owner) {
        super(id, x, y, 0, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && (gotcha == null || gotcha == (Animal)o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            ((Animal)o).setX(this.getX());
            ((Animal)o).setY(this.getY());
            ((Animal)o).setStun(5);
            if(this.gotcha == null) this.gotcha = (Animal)o;
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

        double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius()*2.5)));
        double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius()*2.5)));
        this.setX(this.owner.getX()+x);
        this.setY(this.owner.getY()+y);
    }
}