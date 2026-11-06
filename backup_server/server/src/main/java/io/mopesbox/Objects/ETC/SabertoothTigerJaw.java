package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.AloeVera;
import io.mopesbox.Objects.Eatable.BeeHive;
import io.mopesbox.Objects.Eatable.Cactus;
import io.mopesbox.Objects.Eatable.HoneyComb;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;
import io.mopesbox.Objects.Eatable.MushroomBush;

public class SabertoothTigerJaw extends Ability {
    private Timer liveTimer = new Timer(1000);
    private Room room;
    private Animal owner;
    private List<GameObject> damaged = new ArrayList<>();
    private int damage = 100;

    public Animal getOwner() {
        return this.owner;
    }

    public SabertoothTigerJaw(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 14, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSpecies(species);
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
            if (((Animal) o).getInfo().getTier() != this.owner.getInfo().getTier()) {
                if (((Animal) o).getInfo().getTier() >= 6) {

                    while (damage > ((Animal) o).maxHealth) {
                        damage *= 0.5;
                    }
                    ((Animal) o).hurt(o instanceof Bee ? o.health : (damage + (((Animal) o).maxHealth / 18)), 1, this.owner);
                }
            }
            ((Animal) o).setStun(2);
            ((Animal) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (15))));
            ((Animal) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (15))));
            damaged.add((Animal) o);
        } else if (o instanceof BeeHive && !damaged.contains((BeeHive) o)) {
            ((BeeHive) o).damage(damage + (((BeeHive) o).maxHealth / 20), this.owner);
            ((BeeHive) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (10))));
            ((BeeHive) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (10))));
            damaged.add((BeeHive) o);
        } else if (o instanceof AloeVera && !damaged.contains((AloeVera) o)) {
            ((AloeVera) o).damage(damage + (((AloeVera) o).maxHealth / 20), this.owner);
            ((AloeVera) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (10))));
            ((AloeVera) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (10))));
            damaged.add((AloeVera) o);
        } else if (o instanceof Cactus && !damaged.contains((Cactus) o)) {
            ((Cactus) o).damage(damage + (((Cactus) o).maxHealth / 20), this.owner);
            ((Cactus) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (10))));
            ((Cactus) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (10))));
            damaged.add((Cactus) o);
        } else if (o instanceof MushroomBush && !damaged.contains((MushroomBush) o)) {
            ((MushroomBush) o).damage(damage + (((MushroomBush) o).maxHealth / 20), this.owner);
            ((MushroomBush) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (10))));
            ((MushroomBush) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (10))));
            damaged.add((MushroomBush) o);
        } else if (o instanceof HoneyComb && !damaged.contains((HoneyComb) o)) {
            ((HoneyComb) o).damage(((HoneyComb) o).health, this.owner);
            ((HoneyComb) o).addVelocityX(((Math.cos(Math.toRadians(this.owner.getAngle())) * (10))));
            ((HoneyComb) o).addVelocityY(((Math.sin(Math.toRadians(this.owner.getAngle())) * (10))));
            damaged.add((HoneyComb) o);
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