package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class AloeVera extends GameObject {
    private Room room;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(100);
    private Timer pearTimer = new Timer(3500);
    private Timer liveTimer = new Timer(300000);
    private Biome origin;
    private int xp = Utils.randomInt(30000, 60000);
    public AloeVera(int id, double x, double y, Biome origin, Room room) {
        super(id, x, y, 30, 96);
        this.setMovable(true);
        this.room = room;
        this.origin = origin;
        this.health = 150;
        this.maxHealth = 150;
        this.showHP = false;
        this.setBiome(4);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
        }
        if(o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    public void damage(double damage, Animal biter) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        this.showHPTimer.reset();
        killer = biter;
    }

    private Animal killer;

    private int biteCooldown = 0;

    private Timer bTimer = new Timer(500);

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 11 && this.biteCooldown <= 0) {
            Animal biter = (Animal) o;
            double damage = 15;
            if(biter instanceof BlackDragon || biter instanceof KingDragon) damage = this.maxHealth;
            this.damage(damage, biter);
            this.biteCooldown = 1;
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            theta -= 180;
            if(theta < 0) theta += 360;
            double x = ((Math.cos(Math.toRadians(theta)) * (6)));
            double y = ((Math.sin(Math.toRadians(theta)) * (6)));
            this.addVelocityX(x);
            this.addVelocityY(y);
            x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
            y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
            AloeVeraLeaf leaf = new AloeVeraLeaf(this.room.getID(), this.getX()+x, this.getY()+y, room, null);
            this.room.addObj(leaf);
        }
    }

    public int leafs = 0;

    @Override
    public void update() {
        super.update();
        if(biteCooldown > 0) {
            bTimer.update(Constants.TICKS_PER_SECOND);
            if(bTimer.isDone()) {
                biteCooldown--;
                bTimer.reset();
            }
        }
        if(this.showHP) {
            showHPTimer.update(Constants.TICKS_PER_SECOND);
            if(showHPTimer.isDone()) {
                this.showHP = false;
                showHPTimer.reset();
            }
        }
        if(this.isHurted) {
            biteTimer.update(Constants.TICKS_PER_SECOND);
            if(biteTimer.isDone()) {
                this.isHurted = false;
                biteTimer.reset();
            }
        }
        pearTimer.update(Constants.TICKS_PER_SECOND);
        if(pearTimer.isDone()) {
            if(this.leafs < 8) {
                this.leafs++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                AloeVeraLeaf leaf = new AloeVeraLeaf(this.room.getID(), this.getX()+x, this.getY()+y, room, this);
                this.room.addObj(leaf);
            }
            pearTimer.reset();
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone() || this.health <= 0) {
            this.room.removeObj(this, this.health <= 0 ? killer : null);
            if(this.origin != null) this.origin.aloevera--;
            if(this.health <= 0) {
                if(this.killer != null && this.killer.getClient() != null) {
                    this.killer.getClient().addXp(this.xp);
                    if(killer.getInfo().getTier() <= 14) killer.setAloeVeraHealing(true);
                }
            }
            return;
        }
    }
}
