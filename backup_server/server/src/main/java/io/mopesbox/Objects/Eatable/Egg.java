package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Animals.Tier3.Duckling;
import io.mopesbox.Animals.Tier4.OstrishBaby;
import io.mopesbox.Animals.Tier6.Duck;
import io.mopesbox.Client.AI.AIDuckling;
import io.mopesbox.Client.AI.AIOstrishBaby;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Egg extends GameObject {

    public boolean isBrEgg = false;
    public Animal owner;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(100);
    private int xp = Utils.randomInt(4000, 5000);
    private Room room = null;
    private AIDuckling duckling = null;
    private OstrishBaby ostrishBaby = null;

    public Egg(int id, double x, double y, int radius) {
        super(id, x, y, radius, 63);
        this.setMovable(true);
    }

    public Egg(int id, double x, double y, Animal owner, Room room, AIDuckling duckling) {
        super(id, x, y, 8, 63);
        this.setMovable(true);
        this.showHP = false;
        this.duckling = duckling;
        this.room = room;
        this.health = 100;
        this.maxHealth = 100;
        this.owner = owner;
    }

        public Egg(int id, double x, double y, Ostrich owner, Room room, OstrishBaby ostrishBaby) {
        super(id, x, y, 15, 66);
        this.setMovable(true);
        this.showHP = false;
        this.ostrishBaby = ostrishBaby;
        this.room = room;
        this.health = 100;
        this.maxHealth = 100;
        this.owner = owner;
    }


    public Egg(int id, double x, double y, int radius, Animal owner) {
        super(id, x, y, radius, 63);
        this.isBrEgg = true;
        this.owner = owner;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
        }
        if (o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).isInHole())
                return false;
            if (((Animal) o).isDiveActive())
                return false;
        }
        return true;
    }

    public void damage(double damage, Animal biter) {
        if (this.health > 0)
            this.health -= damage;
        if (this.health < 0)
            this.health = 0;
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
        if (!this.isBrEgg) {
            if (getCollideableIs(o) && o instanceof Animal && ((Animal) o).getInfo().getTier() >= 3
                    && this.biteCooldown <= 0 && !(o instanceof Duck || o instanceof Duckling || o instanceof OstrishBaby) && !(this.ostrishBaby != null && ((Animal)o) instanceof Ostrich && this.ostrishBaby.mom == ((Ostrich)o))) {
                Animal biter = (Animal) o;
                double damage = biter.biteDamage;
                if (biter instanceof BlackDragon || biter instanceof KingDragon)
                    damage = this.maxHealth;
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
                if (theta < 0)
                    theta += 360;
                double x = ((Math.cos(Math.toRadians(theta)) * (6)));
                double y = ((Math.sin(Math.toRadians(theta)) * (6)));
                this.addVelocityX(x);
                this.addVelocityY(y);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isBrEgg) {
            if (owner != null && owner.isDead()) {
                owner = null;
                Main.instance.room.removeObj(this, null);
            }
        } else {
            if (biteCooldown > 0) {
                bTimer.update(Constants.TICKS_PER_SECOND);
                if (bTimer.isDone()) {
                    biteCooldown--;
                    bTimer.reset();
                }
            }
            if (this.showHP) {
                showHPTimer.update(Constants.TICKS_PER_SECOND);
                if (showHPTimer.isDone()) {
                    this.showHP = false;
                    showHPTimer.reset();
                }
            }
            if (this.isHurted) {
                biteTimer.update(Constants.TICKS_PER_SECOND);
                if (biteTimer.isDone()) {
                    this.isHurted = false;
                    biteTimer.reset();
                }
            }
            if (this.health <= 0) {
                if(this.ostrishBaby != null){
                    if (this.ostrishBaby != null){
                    ((AIOstrishBaby)this.ostrishBaby.getClient()).mom.babyOrEgg--;
                    ((AIOstrishBaby)this.ostrishBaby.getClient()).egg = null;

                    }
                }
                this.room.removeObj(this, killer);
                if (this.duckling != null)
                    this.duckling.killEgg();
                if (this.killer != null && this.killer.getClient() != null) {
                    this.killer.getClient().addXp(this.xp);
                }

            }
        }
    }

    public Animal getOwner() {
        return owner;
    }
    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        super.writeCustomData_onAdd(writer);
        if(this.ostrishBaby != null){
            writer.writeUInt32(this.ostrishBaby.mom.getId());
            writer.writeUInt8(this.ostrishBaby.mom.getInfo().getAnimalSpecies());
        }
    }

}
