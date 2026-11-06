package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier5.BulletAnt;
import io.mopesbox.Client.AI.AIBulletAnt;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class AntHill extends GameObject
{
    private Room room;
    
    public AntHill(int id, double x, double y, int rad, int biome, Room room) {
        super(id, x, y, rad, 98);
        this.room = room;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setBiome(biome);
    }

    
    public List<Animal> players = new ArrayList<>();

    public void addPreyToHole(GameObject o) {
        if(o instanceof Animal && o.getCollideable(this) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
            if(!players.contains((Animal)o)) players.add((Animal)o);
            ((Animal)o).setHole(this);
        }
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        if(o instanceof BulletAnt && !((BulletAnt)o).getClient().isBot()) {
            if(o instanceof Animal && o.getCollideable(this) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
                if(!players.contains((Animal)o)) players.add((Animal)o);
                ((Animal)o).setHole(this);
            }
        }
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(o instanceof BulletAnt) return false;
            if(((Animal)o).flag_fliesLikeDragon) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    private Timer spawnAntsTimer = new Timer(120000);

    private Timer sTimer = new Timer(1000);

    @Override
    public void update() {
        if(players.size() > 0) {
            for(Animal ani : players) {
                if(!(ani instanceof BulletAnt)) {
                    ani.setStun(10);
                    ani.setBleeding(10, null, 2);
                }
            }
        }
        if(spawnCooldown > 0) {
            sTimer.update(Constants.TICKS_PER_SECOND);
            if(sTimer.isDone()) {
                spawnCooldown--;
                sTimer.reset();
            }
        }
        spawnAntsTimer.update(Constants.TICKS_PER_SECOND);
        if(spawnAntsTimer.isDone()) {
            int ants = Utils.randomInt(1, 3);
            for(int i = 0; i < ants; i++) {
                AIBulletAnt ant = new AIBulletAnt(this.room, this.getX(), this.getY(), this, 60000);
                ant.spawnAnimal(this.getX(), this.getY(), Utils.randomInt(0, 1));
                room.addAI(ant);
            }
            spawnAntsTimer.reset();
        }
        if(this.finishOffTimer != null && this.finishOffTarget != null) {
            finishOffTimer.update(Constants.TICKS_PER_SECOND);
            if(finishOffTimer.isDone()) {
                int ants = Utils.randomInt(1, 2);
                for(int i = 0; i < ants; i++) {
                    AIBulletAnt ant = new AIBulletAnt(this.room, finishOffTarget, this.getX(), this.getY(), this);
                    ant.spawnAnimal(this.getX(), this.getY(), Utils.randomInt(0, 1));
                    room.addAI(ant);
                }
                finishOffTimer.reset();
                finishOffTimer = null;
                finishOffTarget = null;
            }
        }
        if(this.spawnAnts != null && this.bitertoants != null) {
            spawnAnts.update(Constants.TICKS_PER_SECOND);
            if(spawnAnts.isDone()) {
                int ants = Utils.randomInt(2, 5);
                for(int i = 0; i < ants; i++) {
                    AIBulletAnt ant = new AIBulletAnt(this.room, bitertoants, this.getX(), this.getY(), this);
                    int skin = Utils.randomInt(0, 1);
                    ant.spawnAnimal(this.getX(), this.getY(), skin);
                    room.addAI(ant);
                }
                spawnAnts.reset();
                spawnAnts = null;
                bitertoants = null;
            }
        }
    }

    private Timer finishOffTimer = null;
    private Animal finishOffTarget = null;

    @Override
    public void onCollisionExit(GameObject o) {
        if(o instanceof Animal && ((Animal)o).isInHole()) {
            if(players.contains((Animal)o)) players.remove((Animal)o);
            ((Animal)o).setHole(null);
            if(!(o instanceof BulletAnt) && getCollideableIs(o) && !(o instanceof BulletAnt) && ((Animal)o).getInfo().getTier() <= 15) {
                if(finishOffTarget == null) {
                    finishOffTimer = new Timer(Utils.randomInt(1200, 1500));
                    finishOffTarget = (Animal)o;
                }
            }
        }
    }

    private int spawnCooldown = 0;

    private Animal bitertoants = null;

    private Timer spawnAnts = null;

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(o instanceof BulletAnt) {
            if(!((BulletAnt)o).getClient().isBot()) {
                if(o instanceof Animal && o.getCollideable(this) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
                    if(!players.contains((Animal)o)) players.add((Animal)o);
                    ((Animal)o).setHole(this);
                } else if(o instanceof Animal && o.getCollideable(this) && ((Animal) o).flag_flying && ((Animal)o).isInHole()) {
                    if(players.contains((Animal)o)) players.remove((Animal)o);
                    ((Animal)o).setHole(null);
                }
            }
        }
        if(getCollideableIs(o) && o instanceof Animal && this.spawnCooldown <= 0 && !(o instanceof BulletAnt) && ((Animal)o).getInfo().getTier() <= 15) {
            this.spawnCooldown = 5;
            if(bitertoants == null) {
                spawnAnts = new Timer(Utils.randomInt(800, 1000));
                bitertoants = (Animal)o;
            }
        }
    }
}
