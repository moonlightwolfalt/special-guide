package io.mopesbox.Objects.Eatable;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.AI.AIBee;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class BeeHive extends GameObject {
    private Room room;
    private Timer showHPTimer = new Timer(2000);
    private Timer biteTimer = new Timer(100);
    private Timer liveTimer = new Timer(300000);
    private Biome origin;
    public int honeycombs = 0;
    private int xp = Utils.randomInt(40000, 80000);
    public BeeHive(int id, double x, double y, Biome origin, Room room) {
        super(id, x, y, 55, 68);
        this.room = room;
        this.origin = origin;
        this.health = 400;
        this.maxHealth = 400;
        this.showHP = false;
        this.setMovable(true);
        this.setBiome(0);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(o instanceof Bee) return false;
        }
        if(o instanceof Water) {
            return false;
        }
        return true;
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(o instanceof Bee) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    private Animal bitertobees = null;

    private Timer spawnBeesTimer = new Timer(120000);

    public void damage(double damage, Animal biter) {
        if(this.health > 0) this.health -= damage;
        if(this.health < 0) this.health = 0;
        this.showHP = true;
        this.isHurted = true;
        this.showHPTimer.reset();
        killer = biter;
    }

    private Animal killer;

    private Timer spawnBees = null;

    public List<Animal> players = new ArrayList<>();

    @Override
    public void onCollisionEnter(GameObject o) {
        if(o instanceof Bee && !((Bee)o).getClient().isBot()) {
            if(o instanceof Animal && o.getCollideable(this) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
                if(!players.contains((Animal)o)) players.add((Animal)o);
                ((Animal)o).setHole(this);
            }
        }
    }

    @Override
    public void onCollisionExit(GameObject o) {
        if(o instanceof Bee && !((Bee)o).getClient().isBot()) {
            if(o instanceof Animal && ((Animal)o).isInHole()) {
                if(players.contains((Animal)o)) players.remove((Animal)o);
                ((Animal)o).setHole(null);
            }
        }
    }

    private int biteCooldown = 0;

    private int spawnCooldown = 0;

    private Timer bTimer = new Timer(1000);

    private Timer sTimer = new Timer(1000);

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (o instanceof Bee) {
            if (!((Bee)o).getClient().isBot()) { // не перемещать, иначе пчелы-боты будут кусать или толкать улей!!!!
                if (o instanceof Animal && o.getCollideable(this) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
                    if (!players.contains((Animal)o)) players.add((Animal)o);
                    ((Animal)o).setHole(this);
                } else if (o instanceof Animal && o.getCollideable(this) && ((Animal) o).flag_flying && ((Animal)o).isInHole()) {
                    if (players.contains((Animal)o)) players.remove((Animal)o);
                    ((Animal)o).setHole(null);
                }
            }
        } else if (getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 10 && this.biteCooldown <= 0) {
            Animal biter = (Animal) o;
            double damage = biter.biteDamage;
            if (biter instanceof BlackDragon || biter instanceof KingDragon) damage = this.maxHealth;
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
        }
        if(getCollideableIs(o) && o instanceof Animal && this.spawnCooldown <= 0 && !(o instanceof Bee)) {
            this.spawnCooldown = 5;
            if(bitertobees == null) {
                spawnBees = new Timer(Utils.randomInt(1500, 2500));
                bitertobees = (Animal)o;
            }
            int ran = Utils.randomInt(1, 2);
            for(int i = 0; i < ran; i++) {
                this.honeycombs++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                HoneyComb comb = new HoneyComb(this.room.getID(), this.getX()+x, this.getY()+y, this, room);
                this.room.addObj(comb);
            }
        }
    }

    public int bees = 0;
    
    private Timer honeyCombTimer = new Timer(60000);

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
        if(spawnCooldown > 0) {
            sTimer.update(Constants.TICKS_PER_SECOND);
            if(sTimer.isDone()) {
                spawnCooldown--;
                sTimer.reset();
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
        if(this.spawnBees != null) {
            spawnBees.update(Constants.TICKS_PER_SECOND);
            if(spawnBees.isDone()) {
                int bees = Utils.randomInt(2, 5);
                for(int i = 0; i < bees; i++) {
                    AIBee bee = new AIBee(this.room, bitertobees, this.getX(), this.getY(), this);
                    bee.spawnAnimal(this.getX(), this.getY());
                    room.addAI(bee);
                }
                spawnBees.reset();
                spawnBees = null;
                bitertobees = null;
            }
        }
        honeyCombTimer.update(Constants.TICKS_PER_SECOND);
        if(honeyCombTimer.isDone()) {
            if(this.honeycombs < 2) {
                this.honeycombs++;
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (this.getRadius()+14)));
                HoneyComb comb = new HoneyComb(this.room.getID(), this.getX()+x, this.getY()+y, this, room);
                this.room.addObj(comb);
            }
            honeyCombTimer.reset();
        }
        spawnBeesTimer.update(Constants.TICKS_PER_SECOND);
        if(spawnBeesTimer.isDone()) {
            int bees = Utils.randomInt(1, 3);
            for(int i = 0; i < bees; i++) {
                AIBee bee = new AIBee(this.room, this.getX(), this.getY(), this, 60000);
                bee.spawnAnimal(this.getX(), this.getY());
                room.addAI(bee);
            }
            spawnBeesTimer.reset();
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone() || this.health <= 0) {
            this.room.removeObj(this, this.health <= 0 ? killer : null);
            if(this.origin != null) this.origin.beehives--;
            if(this.health <= 0) {
                if(this.killer != null && this.killer.getClient() != null)
                    this.killer.getClient().addXp(this.xp);
            }
            return;
        }
    }
}
