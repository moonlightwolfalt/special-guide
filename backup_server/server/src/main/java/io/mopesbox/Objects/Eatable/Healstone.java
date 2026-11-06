package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.VolcanoBiome;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import java.util.Map;

public class Healstone extends GameObject{
    private Room room;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(100);
    private Timer damageTimer = new Timer(1500);
    private Timer liveTimer = new Timer(600000);
    private VolcanoBiome origin;
    private int ghp;
    private int mainRadius;
    public Healstone(int id, double x, double y, int rad, int biome, int hp, Room room, VolcanoBiome origin, int ghp) {
        super(id, x, y, rad, 46);
        this.mainRadius = rad;
        this.setMovable(true);
        this.room = room;
        this.origin = origin;
        this.ghp = ghp;
        this.health = hp;
        this.maxHealth = hp;
        this.showHP = false;
        this.setBiome(biome);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_fliesLikeDragon) return false;
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
        }
        return true;
    }

    public void damage(int damage) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        this.showHPTimer.reset();
    }

    private boolean damaging = false;

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal) {
            Animal biter = (Animal) o;
            biter.regenerateHP(ghp);
            damaging = true;
        }
    }

    @Override
    public void update() {
        super.update();

        double radd = this.mainRadius * (((double) this.health) / ((double) this.maxHealth));
        int rad = (int) Math.round(radd);
        
        if(this.showHP) this.setRadius(Math.max(10, rad));

        if (this.showHP) {
            showHPTimer.update(Constants.TICKS_PER_SECOND);
            if(showHPTimer.isDone()) {
                this.showHP = false;
                showHPTimer.reset();
            }
        }
        if (this.isHurted) {
            biteTimer.update(Constants.TICKS_PER_SECOND);
            if(biteTimer.isDone()) {
                this.isHurted = false;
                biteTimer.reset();
            }
        }
        if (this.damaging) {
            damageTimer.update(Constants.TICKS_PER_SECOND);
            if(damageTimer.isDone()) {
                this.damaging = false;
                this.damage(1);
                // int hpPerc = this.health / this.maxHealth;
                // this.setRadius(this.getRadius() * hpPerc); // э ф ден абобус а паша амогус а yesd бобус
                for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
                    GameObject obj = entry.getValue();
                    if (obj instanceof Animal) {
                        ((Animal)obj).getClient().addXp(Utils.randomInt(2, 5));
                    }
                }
                damageTimer.reset();
            }
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if (liveTimer.isDone() || this.health <= 0) {
            this.room.removeObj(this, null);
            if(this.origin != null) this.origin.healstone = null;
            return;
        }
    }
}
