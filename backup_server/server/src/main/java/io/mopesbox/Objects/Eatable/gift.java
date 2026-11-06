package io.mopesbox.Objects.Eatable;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class gift extends GameObject {
    private Room room;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(1);
        private Timer liveTimer = new Timer(10000);

    // private Biome origin;
    int speed = 15;
    private int xp = Utils.randomInt(1200, 48000);
    public gift(int id, double x, double y, int origin, Room room) {
        super(id, x, y, 15, 143);
        this.setMovable(true);
        this.room = room;
        // this.origin = this.room.land
        this.health = 100;
        this.maxHealth = 100;
        this.showHP = false;
        this.setBiome(origin);
    }
    public int mush = 0;


    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isDiveActive()) return false;
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

    public void damage(int damage, Animal biter) {
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
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 1 && (this.biteCooldown <= 0 || ((Animal)o).getInfo().getTier() >= 17)) {
            Animal biter = (Animal) o;
            if(biter instanceof BlackDragon || biter instanceof KingDragon)
            this.damage(100, biter);
            else
            this.damage(25, biter);
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
        }
    }

    public int leafs = 0;

    @Override
    public void update() {
        super.update();
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()){
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
        if(biteCooldown > 0) {
            bTimer.update(Constants.TICKS_PER_SECOND);
            if(bTimer.isDone()) {
                biteCooldown--;
                bTimer.reset();
            }
        }
        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed--;
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
        if(this.health <= 0) {
            this.room.removeObj(this, this.health <= 0 ? killer : null);
            // if(this.origin != null) this.origin.conchshells--;
            if(this.health <= 0) {
                if(this.killer != null && this.killer.getClient() != null) {
                    this.killer.getClient().addXp(this.xp);
                }
            }
            return;
        }
    }

        @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(Utils.randomInt(0, 3));
        writer.writeUInt8(0);

    }
}
