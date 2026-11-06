package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class CrabSmash extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 15;

    public CrabSmash(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species, boolean isLeft) {
        super(id, x, y, 51, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSpecies(species);
        this.specType = isLeft ? 1 : 0;
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if (this.owner == null)
            return;
        if (o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena
                && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
            if (((Animal) o).getInfo().getTier() >= 6) {

                while (damage > ((Animal) o).maxHealth) {
                    damage *= 0.8;
                }

                ((Animal) o).hurt(damage + (((Animal) o).maxHealth / 6), 1, this.owner);
            }
            ((Animal) o).setStun(2);
            ((Animal) o).setVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (40))));
            ((Animal) o).setVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (40))));
            damaged.add((Animal) o);
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.damage > 10)
            damage--;
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if (liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }
}