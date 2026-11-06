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

public class Cactus extends GameObject{
    private Room room;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(100);
    private Timer pearTimer = new Timer(3500);
    private Timer liveTimer = new Timer(300000);
    private Biome origin;
    private int xp = Utils.randomInt(80000, 100000);
    public Cactus(int id, double x, double y, Biome origin, Room room, int addRad) {
        super(id, x, y, 30 + addRad, 82);
        this.setMovable(true);
        this.room = room;
        this.origin = origin;
        this.health = 50;
        this.maxHealth = 50;
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

    private Timer bTimer = new Timer(1000);

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && this.biteCooldown <= 0) {
            Animal biter = (Animal) o;
            double damage = 15;
            if(biter instanceof BlackDragon || biter instanceof KingDragon) damage = this.maxHealth;
            if(((Animal)o).getInfo().getTier() >= 12 ) this.damage(damage, biter);
            if(((Animal)o).getInfo().getTier() >= 12 ) this.biteCooldown = 1;
            biter.setStun(3);
            biter.hurt(10, 14, null);
            double dy = o.getY() - this.getY();
            double dx = o.getX() - this.getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            theta -= 180;
            if(theta < 0) theta += 360;
            double x = ((Math.cos(Math.toRadians(theta)) * (8)));
            double y = ((Math.sin(Math.toRadians(theta)) * (8)));
             if(((Animal)o).getInfo().getTier() >= 12 ) this.addVelocityX(x);
             if(((Animal)o).getInfo().getTier() >= 12 ) this.addVelocityY(y);
            theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            x = ((Math.cos(Math.toRadians(theta)) * (8)));
            y = ((Math.sin(Math.toRadians(theta)) * (8)));
            o.addVelocityX(x);
            o.addVelocityY(y);
        }
    }

    public int pears = 0;

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
        if(pearTimer.isDone() && Constants.FOODSPAWN) {
            if(this.pears < 8) {
                this.pears++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                CactusPear pear = new CactusPear(this.room.getID(), this.getX()+x, this.getY()+y, room, this);
                this.room.addObj(pear);
            }
            pearTimer.reset();
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone() || this.health <= 0) {
            this.room.removeObj(this, this.health <= 0 ? killer : null);
            if(this.origin != null) this.origin.cactuses--;
            if(this.health <= 0) {
                if(this.killer != null && this.killer.getClient() != null)
                    this.killer.getClient().addXp(this.xp);
            }
            return;
        }
    }
}
