package io.mopesbox.Objects.Bones;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Meat;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class AnimalCarcass extends GameObject {
    private int type;
    private boolean deadAni;
    private String nick = "mope.io";
    private boolean isFade = true;
    private boolean isFlying = false;
    private int transparency = 250;
    private Room room;
    private int xp;

    private Timer existTimer = new Timer(1000);

    private List<Integer> canBeEaten = new ArrayList<>();

    public AnimalCarcass(int id, double x, double y, int radius, int type, boolean isFromDead, String nick, Room room, int xp) {
        super(id, x, y, radius, 74);
        this.type = type;
        this.health = 50;
        this.maxHealth = 50;
        this.deadAni = isFromDead;
        this.room = room;
        this.xp = Math.min(xp, 360000);
        this.setMovable(true);
        canBeEaten.add(90);
        canBeEaten.add(92);
        canBeEaten.add(11);
        canBeEaten.add(94);
        canBeEaten.add(53);
        canBeEaten.add(96);
        canBeEaten.add(71);
        canBeEaten.add(73);
        canBeEaten.add(70);
        canBeEaten.add(72);
        canBeEaten.add(95);
        canBeEaten.add(46);
        canBeEaten.add(100);
        if(nick != null) this.nick = nick;
    }

    @Override
    public void update() {
        super.update();
        if(this.health <= 0) {
            lastKiller.getClient().addXp(this.xp);
            this.room.removeObj(this, lastKiller);
            return;
        }
        existTimer.update(Constants.TICKS_PER_SECOND);
        if(existTimer.isDone()) {
            transparency -= 10;
            if(transparency <= 0) {
                this.room.removeObj(this, null);
                return;
            }
            existTimer.reset();
        }
        if(this.isHurted) {
            hurtTimer.update(Constants.TICKS_PER_SECOND);
            if(hurtTimer.isDone()) {
                this.isHurted = false;
                hurtTimer.reset();
            }
        }
        if(this.showHP) {
            hpTimer.update(Constants.TICKS_PER_SECOND);
            if(hpTimer.isDone()) {
                this.showHP = false;
                hpTimer.reset();
            }
        }
    }

    private Animal lastKiller;
    private Timer hpTimer = new Timer(1500);
    private Timer hurtTimer = new Timer(150);

    public void hurt(double damage, int reason, Animal biter) {
        if(this.health > 0) this.health -= this.health-damage > 0 ? damage : this.health;
        this.lastKiller = biter;
        this.showHP = true;
        this.isHurted = true;
        this.hpTimer.reset();
        this.hurtTimer.reset();
        if(xp > 0) {
            double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
            double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius())));
            Meat meat = new Meat(this.room.getID(), this.getX()+x, this.getY()+y, this.xp/(damage*10) < 5000 ? 0 : this.xp/(damage*10) < 30000 ? 1 : 2, this.getId(), Math.min(this.xp/(25*10), 56942), this.room);
            this.xp -= this.xp/(damage*10);
            this.room.addObj(meat);
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideable(o) && o instanceof Animal) {
            Animal biter = (Animal) o;
            if(biter.biteCooldown < 1 && !biter.isDead() && canBeEaten.contains(biter.getInfo().getAnimalType())) {
                hurt(biter.biteDamage, 2, biter);
                biter.setBiteCooldown(2);
            }
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(deadAni);
        writer.writeString(nick);
        writer.writeUInt8(type);
        writer.writeBoolean(isFade);
        if(isFade) writer.writeUInt16(transparency);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(isFlying);
        if(isFade) writer.writeUInt16(transparency);
    }
}
