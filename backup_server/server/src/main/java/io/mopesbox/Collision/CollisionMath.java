package io.mopesbox.Collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.awt.Point;
import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier10.Pelican;
import io.mopesbox.Animals.Tier10.Tiger;
import io.mopesbox.Animals.Tier11.Vulture;
import io.mopesbox.Animals.Tier12.Eagle;
import io.mopesbox.Animals.Tier14.BlackwidowSpider;
import io.mopesbox.Animals.Tier14.GiantSpider;
import io.mopesbox.Animals.Tier15.Pterodactyl;
import io.mopesbox.Animals.Tier16.GiantScorpion;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Objects.Static.SandboxArena;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import net.openhft.affinity.Affinity;
import io.mopesbox.Networking.*;

public class CollisionMath extends Thread {
    public final Room room;

    public final boolean debug = false;
    public final Main main;
    public final AtomicBoolean neededMathFlag = new AtomicBoolean(false);
    public Set<GameObject> objects = null;
    private AtomicReference<QuadTree<GameObject>> qt;
    private CollisionHandler collisionHandler = null;
    public final int id;

    public CollisionMath(Room room, Main main, CollisionHandler collisionHandler, int id) {
        Affinity.setAffinity(3);
        this.room = room;
        this.main = main;
        this.id = id;
        this.collisionHandler = collisionHandler;
        this.qt = new AtomicReference<>(collisionHandler.qt);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.neededMathFlag.compareAndSet(true, false)) 
                    this.update();
                    
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void math(Set<GameObject> objs) {
        this.objects = objs;
        this.neededMathFlag.set(true);
    }

    public void update() {
            long startTime = System.nanoTime();
            for (GameObject o : objects) {
                if (o == null)
                    continue;
                if (o instanceof PvPArena) {
                    if (((PvPArena) o).arenaState != 0) {
                        Animal obj1 = ((PvPArena) o).getPlayer1();
                        double distance;
                        if (obj1 != null) {
                            distance = Utils.distance(o.getX(), obj1.getX(), o.getY(), obj1.getY());
                            this.innerCollision(distance, o, obj1);
                        }
                        Animal obj = ((PvPArena) o).getPlayer2();
                        if (obj != null) {
                            distance = Utils.distance(o.getX(), obj.getX(), o.getY(), obj.getY());
                            this.innerCollision(distance, o, obj);
                        }

                        if (obj1 != null && obj != null) {
                            double distance2 = Utils.distance(obj1.getX(), obj.getX(), obj1.getY(), obj.getY());
                            this.biteCollision(distance2, obj1, obj, (PvPArena) o, false);
                            this.biteCollision(distance2, obj, obj1, (PvPArena) o, false);
                        }

                    }
                }
                if (o instanceof SandboxArena) {
                    if (((SandboxArena) o).attaching) {
                        for (GameClient client : ((SandboxArena) o).canWalk) {
                            if (client != null && client.getPlayer() != null) {
                                double distance = Utils.distance(o.getX(), client.getPlayer().getX(), o.getY(),
                                        client.getPlayer().getY());
                                this.innerCollision(distance, o, client.getPlayer());
                            }
                        }
                    }
                }
                if (!o.isMovable() && !(o instanceof PvPArena)
                        || (o instanceof Animal && (((Animal) o).isGhost || ((Animal) o).inEgg)))
                    continue;

                List<GameObject> objectsNear = qt.get().retrieve(new ArrayList<>(), o);
                List<GameObject> rectss = room.rectangles;

                for (GameObject rect : rectss) {
                    objectsNear.add(rect);
                }

                if (objectsNear.size() <= 1)
                    continue;
                for (GameObject obj : objectsNear) {
                    if (o == obj || o == null || obj == null || (obj instanceof Animal && (((Animal) obj).isGhost || ((Animal) obj).inEgg)))
                        continue;

                    try {
                        if (Constants.STRESSTEST && !(obj instanceof Animal) && !(obj instanceof Biome)) {
                            double velocityX = Math
                                    .cos(Utils.seedAngle(obj.hashCode() + this.collisionHandler.stressTestSeed));
                            double velocityY = Math
                                    .sin(Utils.seedAngle(obj.hashCode() + this.collisionHandler.stressTestSeed));

                            obj.addVelocityX(velocityX * 4);
                            obj.addVelocityY(velocityY * 4);
                        }

                        if (obj instanceof Rectangle) {
                            if (obj instanceof Biome) {
                                Rectangle biome = (Rectangle) obj;
                                if (o.getX() >= (biome.getX() - (biome.getWidth() / 2))
                                        && o.getX() <= (biome.getX() + (biome.getWidth() / 2))
                                        && o.getY() >= (biome.getY() - (biome.getHeight() / 2))
                                        && o.getY() <= (biome.getY() + (biome.getHeight() / 2))) {
                                    obj.onCollision(o);
                                    o.addCollided(obj);
                                    obj.addCollided(o);
                                    continue;
                                }
                            } else
                                continue;
                        }

                        double orad2 = o.isHasCustomCollisionRadius() ? o.getCustomCollisionRadius() : o.getRadius();
                        double objrad2 = obj.isHasCustomCollisionRadius() ? obj.getCustomCollisionRadius()
                                : obj.getRadius();
                        double distance = Utils.distance(o.getX(), obj.getX(), o.getY(), obj.getY());

                        if (distance > orad2 + objrad2)
                            continue;

                        if ((o instanceof Animal || obj instanceof Animal)
                                && ((o instanceof Animal && ((Animal) o).inArena)
                                        || (obj instanceof Animal && ((Animal) obj).inArena)))
                            continue;

                        o.addCollided(obj);
                        obj.addCollided(o);
                        o.onCollision(obj);
                        obj.onCollision(o);

                        if (distance <= orad2 + objrad2) {
                            // if(o instanceof Lake && obj instanceof Mudspot)
                            // this.collisionDynamic(distance, obj, o);
                            // if(o instanceof VolcanoBiome && obj instanceof Mudspot)
                            // this.collisionDynamic(distance, obj, o);
                            // if(o instanceof River && obj instanceof Mudspot)
                            // this.collisionDynamic(distance, obj, o);
                            // if(o instanceof River && obj instanceof VolcanoBiome)
                            // this.collisionDynamic(distance, obj, o);
                            // if(o instanceof DeathLake && obj instanceof Lake){
                            // for(GameObject hill : ((DeathLake)o).hills){
                            // if(hill.getCollidedList().contains(obj)){
                            // if(obj != o){
                            // this.collisionDynamic(distance, obj, o);
                            // }
                            // }
                            // }
                            // }
                            // if(o instanceof DeathLake && obj instanceof VolcanoBiome){
                            // for(GameObject hill : ((DeathLake)o).hills){
                            // if(hill.getCollidedList().contains(obj)){
                            // if(obj != o){
                            // this.collisionDynamic(distance, obj, o);
                            // }
                            // }
                            // }
                            // }
                            if (!(o instanceof Animal) || !(obj instanceof Animal)) {
                                if (obj.getCollideable() && o.getCollideable() && obj.getCollideable(o)
                                        && o.getCollideable(obj))
                                    this.collisionDynamic(distance, obj, o);
                            }
                            if (o instanceof PvPArena && obj instanceof PvPArena) {
                                this.collisionDynamic(distance, obj, o);
                            }
                            if (o instanceof Animal && obj instanceof Animal) {
                                if (obj.getCollideable(o) && o.getCollideable(obj)) {
                                    this.collisionDynamic(distance, obj, o);
                                }else if(((Animal)obj).getInfo().isDanger(((Animal)o).getInfo()) || ((Animal)o).getInfo().isDanger(((Animal)obj).getInfo())){
                                if(((Animal)obj).getInfo().isDanger(((Animal)o).getInfo())) this.collisionDynamic(distance, o, obj);
                                if(((Animal)o).getInfo().isDanger(((Animal)obj).getInfo())) this.collisionDynamic(distance, obj, o);
                                
                                }
                                if (((Animal) o).getInfo().isBiteable((Animal) obj)) {

                                    List<Point> tails = new ArrayList<>(((Animal)obj).tails);

                                    for (Point a : tails) {
                                        if (((Animal) o).mouth != null) {

                                            double distance__ = Utils.getDistance2D(((Animal) o).mouth.x,
                                                    ((Animal) o).mouth.y, a.x, a.y);

                                            if (distance__ <= ((Animal) o).getRadius() + ((Animal) obj).getRadius() * 2
                                                    && ((Animal) o).biteCooldown <= 0) {

                                                     this.bite((Animal) obj, (Animal) o, distance__);
                                                

                                            }
                                        }

                                        double distance2 = Utils.distance(obj.getX(), o.getX(), obj.getY(),
                                                o.getY());

                                                            if (((Animal) o).getInfo().isBiteable((Animal) obj)) {

                                        this.biteCollision(distance2, ((Animal) o), ((Animal) obj), null, false);
                                        this.biteCollision(distance2, ((Animal) obj), ((Animal) o), null, false);
                                                            }

                             
                                    }
                                    

                                    }
                                    if (((Animal) obj).getInfo().isBiteable((Animal) o)) {

                                    List<Point> tails = new ArrayList<>(((Animal)o).tails);

                                    for (Point a : tails) {
                                            if (((Animal) obj).mouth != null) {

                                                double distance__ = Utils.getDistance2D(((Animal) obj).mouth.x,
                                                        ((Animal) obj).mouth.y, a.x, a.y);

                                                if (distance__ <= ((Animal) obj).getRadius()
                                                        + ((Animal) o).getRadius() * 2
                                                        && ((Animal) obj).biteCooldown <= 0) {

                                                        this.bite((Animal) o, (Animal) obj, distance__);
                                                    
                                                }

                                            }

                                           

                                        }
                                     

                                  
                                    }
                                                                    if (((Animal) o).getInfo().isBiteable((Animal) obj)) {

                                        double distance2 = Utils.distance(obj.getX(), o.getX(), obj.getY(),
                                                    o.getY());
                                              this.biteCollision(distance2, ((Animal) o), ((Animal) obj), null,
                                                    false);
                                            this.biteCollision(distance2, ((Animal) obj), ((Animal) o), null,
                                                    false);
                                                                    }
                                
                            }
                        }
                    } catch (NullPointerException e) {
                        //
                        e.printStackTrace();
                    }
                }

                if (o != null)
                    o.updateCollided();
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            this.room.lticks.put(id, (int) Math.ceil(duration));
            if (this.room.hticks.get(id) < (int) Math.ceil(duration))
                this.room.hticks.put(id, (int) Math.ceil(duration));
        }
    }

    public boolean isLookingAt(Animal biter, Animal prey) {
        double dx = prey.getX() - biter.getX();
        double dy = prey.getY() - biter.getY();
        double angle = Math.atan2(dy, dx);
        double angleInDegrees = Math.toDegrees(angle);
        double difference = angleInDegrees - biter.getAngle();
        while (difference < -180)
            difference += 360;
        while (difference > 180)
            difference -= 360;

        if (difference < 0) {
            difference = -difference;
        }
        return difference < 26;
    }

    public void collisionDynamic(double distance, GameObject entity1, GameObject entity2) {
        double ent1Radius = entity1.isHasCustomCollisionRadius() ? entity1.getCustomCollisionRadius()
                : entity1.getRadius();
        double ent2Radius = entity2.isHasCustomCollisionRadius() ? entity2.getCustomCollisionRadius()
                : entity2.getRadius();

        if (distance < 2)
            distance = 2;
        double overlap = (((ent1Radius + ent2Radius) - distance) / 2);

        double newX2 = ((entity2.getX() - entity1.getX()) / distance) * overlap;
        double newY2 = ((entity2.getY() - entity1.getY()) / distance) * overlap;
        double newX1 = ((entity1.getX() - entity2.getX()) / distance) * overlap;
        double newY1 = ((entity1.getY() - entity2.getY()) / distance) * overlap;
        Animal prey;
        Animal biter;

        if (entity1 instanceof Animal && entity2 instanceof Animal
                && (((Animal) entity1).getInfo().isEdible((Animal) entity2)
                        || ((Animal) entity2).getInfo().isEdible((Animal) entity1))) {
            // Animal biter
            // Animal prey = null;
            if (((Animal) entity1).getInfo().getTier() > ((Animal) entity2).getInfo().getTier()) {
                biter = (Animal) entity1;
                prey = (Animal) entity2;
            } else {
                if (((Animal) entity1).getInfo().getTier() == ((Animal) entity2).getInfo().getTier()) {
                    main.guiThread.toLog("a");

                }

                biter = (Animal) entity2;
                prey = (Animal) entity1;

            }

            if (this.isLookingAt(biter, prey) && prey.biteCooldown < 1) {
                if(biter instanceof Pelican){
                    if(((Pelican)biter).flying) return;
                }
                if(prey.isInHole() || biter.isInHole()) return;
                if(prey.isDiveActive() || biter.isDiveActive()) return;
                if(prey.flag_flying && !biter.flag_flying)
                return;
                if(prey instanceof Eagle){
                if(((Eagle)prey).running || ((Eagle)prey).flying || ((Eagle)prey).landing) return;
                }
                if(prey instanceof Vulture){
                    if(((Vulture)prey).running || ((Vulture)prey).flying || ((Vulture)prey).landing) return;
                }
                if(!prey.flag_flying && biter.flag_flying) return;
                double calculatedXPGiven = Math.min(Math.max(40.0, 0), biter.getClient().getXP());
                biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
                prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));
                // prey.damage(biter);
                prey.hurt(biter.biteDamage, 1, biter);
                prey.biteCooldown = 1;
                if(prey.flag_flying && biter.flag_flying)
                prey.getClient().send(Networking.personalGameEvent(255, "Ouch!"));

                prey.biteTimer.reset();
                if (biter instanceof GiantScorpion)
                    prey.setShivering(2, biter);
                    if(biter instanceof GiantSpider || biter instanceof BlackwidowSpider)
                    prey.setPoisoned(2, biter, 5);
                    if(biter.isLavaAnimal() && !(biter instanceof GiantScorpion))
                    prey.setFire(1, biter, 5);
                    if(biter instanceof Pterodactyl)
                    biter.addWater(2);
                // biter.hurt(10, 14, null);
                double dy = biter.getY() - prey.getY();
                double dx = biter.getX() - prey.getX();
                double xx = ((Math.cos(Math.toRadians(prey.getAngle())) * (prey.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(prey.getAngle())) * (prey.getRadius() * 2)));
                xx = prey.getX() + xx;
                yy = prey.getY() + yy;
                double theta = Math.atan2(dy, dx);
                theta *= 180 / Math.PI;
                if (theta < 0) {
                    theta += 360;
                }
                theta -= 180;
                if (theta < 0)
                    theta += 360;
                double x = ((Math.cos(Math.toRadians(theta)) * (8)));
                double y = ((Math.sin(Math.toRadians(theta)) * (8)));
                prey.addVelocityX(x);
                prey.addVelocityY(y);
                theta = Math.atan2(dy, dx);
                theta *= 180 / Math.PI;
                if (theta < 0) {
                    theta += 360;
                }
                x = ((Math.cos(Math.toRadians(theta)) * (8)));
                y = ((Math.sin(Math.toRadians(theta)) * (8)));
                biter.addVelocityX(x);
                biter.addVelocityY(y);
                return;

            }
        }

                if (entity1 instanceof Animal && ((Animal)entity1).flag_flying) {
                return;
            }
                if (entity2 instanceof Animal && ((Animal)entity2).flag_flying) {

                    return;
            }
            if(entity1 instanceof Animal && ((Animal)entity1).isInHole()) return;
            if(entity2 instanceof Animal && ((Animal)entity2).isInHole()) return;
            

        if (entity2.isMovable() || (entity2 instanceof PvPArena)) {
            if ((!(entity2 instanceof Animal) || !(((Animal) entity2).inArena))
                    && (!(entity1 instanceof Animal) || !(((Animal) entity1).inArena))) {
                if (Utils.isValidDouble(entity2.getX() + newX2))
                    entity2.setX(entity2.getX() + newX2);
                if (Utils.isValidDouble(entity2.getY() + newY2))
                    entity2.setY(entity2.getY() + newY2);
            }
        }


        if (entity1.isMovable() || (entity1 instanceof PvPArena)) {
            if ((!(entity1 instanceof Animal) || !(((Animal) entity1).inArena))
                    && (!(entity2 instanceof Animal) || !(((Animal) entity2).inArena))) {
                if (Utils.isValidDouble(entity1.getX() + newX1))
                    entity1.setX(entity1.getX() + newX1);
                if (Utils.isValidDouble(entity1.getY() + newY1))
                    entity1.setY(entity1.getY() + newY1);
            }
        }
    }

    public void biteCollisionDynamic(double distance, GameObject entity1, GameObject entity2) {
        double ent1Radius = entity1.isHasCustomCollisionRadius() ? entity1.getCustomCollisionRadius()
                : entity1.getRadius();
        double ent2Radius = entity2.isHasCustomCollisionRadius() ? entity2.getCustomCollisionRadius()
                : entity2.getRadius();
        if (distance < 2)
            distance = 2;
            double modifier = 1;
            if(entity1 instanceof Animal && entity2 instanceof Animal && ((Animal)entity1).inArena && ((Animal)entity2).inArena){
                modifier = 1.8;
            }
        double overlap = (((ent1Radius + ent2Radius) - distance) / 3) * 3.3 * modifier;

        double newX1 = ((entity1.getX() - entity2.getX()) / distance) * overlap;
        double newY1 = ((entity1.getY() - entity2.getY()) / distance) * overlap;

        if (entity1.isMovable()) {
            if (Utils.isValidDouble(entity1.getX() + newX1))
                entity1.setX(entity1.getX() + newX1);
            if (Utils.isValidDouble(entity1.getY() + newY1))
                entity1.setY(entity1.getY() + newY1);
        }
    }

    public void innerCollision(double distance, GameObject entity1, GameObject entity2) {
        double dx = entity2.getX() - entity1.getX();
        double dy = entity2.getY() - entity1.getY();
        double nx = dx / distance;
        double ny = dy / distance;
        double newX2 = ((entity1.getX() + nx * (entity1.getRadius() - (entity2.getRadius() + 15))));
        double newY2 = ((entity1.getY() + ny * (entity1.getRadius() - (entity2.getRadius() + 15))));

        if (entity2.isMovable() && entity1.getRadius() - (entity2.getRadius() + 15) <= distance) {
            if (Utils.isValidDouble(newX2))
                entity2.setX(newX2);
            if (Utils.isValidDouble(newY2))
                entity2.setY(newY2);
        }
    }

    public void biteCollision(double distance, Animal prey, Animal biter, PvPArena pvp, boolean diffAniCollision) {
        if (biter.biteCooldown < 1 && !biter.flag_flying && !biter.isInvincible() && !biter.isInHole()
                && !biter.isDiveActive() && !biter.flag_flying && !prey.flag_flying && !prey.flag_flying
                && !prey.isInvincible() && !prey.isInHole() && !prey.isDiveActive()) {
            double difference = prey.getAngle() - biter.getAngle();
            while (difference < -180)
                difference += 360;
            while (difference > 180)
                difference -= 360;

            if (difference < 0) {
                difference = -difference;
            }

            if ((difference < 26 && Utils.distance(biter.getX(), prey.getX(), biter.getY(),
                    prey.getY()) <= (biter.getRadius() + prey.getRadius()) / 2)) {
                double en1x = ((Math.cos(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en1y = ((Math.sin(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en2x = ((Math.cos(Math.toRadians(prey.getAngle())) * (prey.getRadius())));
                double en2y = ((Math.sin(Math.toRadians(prey.getAngle())) * (prey.getRadius())));
                if (Utils.distance(biter.getX() + en1x, prey.getX() - en2x, biter.getY() + en1y,
                        prey.getY() - en2y) <= (biter.getRadius() * 1.2)) {
                    // 40 base xp + 2% of prey xp but can't be more than double biter xp
                    if (pvp == null) {
                        double littleXP = prey.getClient().getXP() / 100 * 2;
                        double calculatedXPGiven = Math.min(Math.max(40.0 + littleXP, 0), biter.getClient().getXP());
                        biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
                        prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));

                        if (biter.getInfo().getTier() >= 17 && biter.getInfo().getTier() == prey.getInfo().getTier()) {
                            double addHp1 = prey.maxHealth / 25;
                            if (biter.health + addHp1 < biter.maxHealth)
                                biter.health += addHp1;
                        }
                    }
                    prey.hurt(prey.maxHealth / 10, 2, biter);
                    this.biteCollisionDynamic(biter.getRadius(), biter, prey);
                    if (pvp != null)
                        pvp.addBite(biter);
                    biter.setBiteCooldown(2);
                    prey.collisionBite = 1;
                    prey.collisionTimer.reset();
                    // double enx = ((Math.cos(Math.toRadians(biter.getAngle())) *
                    // (prey.getRadius())));
                    // double eny = ((Math.sin(Math.toRadians(biter.getAngle())) *
                    // (prey.getRadius())));
                    // prey.forcedBoost(biter.getX()+enx, biter.getY()+eny, 180
                    // Main.log.info("Collision. prey: " + prey.getPlayerName() + " biter: " +
                    // biter.getPlayerName());
                    prey.getClient().send(Networking.personalGameEvent(2, null)); //
                    biter.getClient().send(Networking.personalGameEvent(18, null)); //

                }
            }
        }
    }

    Animal biters;
    Animal os;

    public void bite(Animal entity1, Animal entity2, double distancemouth) {


        if (entity1.getInfo().getTier() < entity2.getInfo().getTier()) {
            biters = entity1;
            os = entity2;

        }
        if (entity1.getInfo().getTier() > entity2.getInfo().getTier()) {
            biters = entity2;
            os = entity1;

        }
        if (entity1.getInfo().getTier() == entity2.getInfo().getTier()) {
            return;
        }
        if(biters.getInfo().getTier() > os.getInfo().getTier()){
            return;
        }

        double difference = biters.getAngle() - os.getAngle();
        if (difference < -180)
            difference += 360;
        if (difference > 180)
            difference -= 360;

        if (difference < 0) {
            difference = -difference;
        }

                if (biters.biteCooldown < 1 && !biters.flag_flying && !biters.isInvincible() && !biters.isInHole()
                && !biters.isDiveActive() && !biters.flag_flying && !os.flag_flying && !os.flag_flying
                && !os.isInvincible() && !os.isInHole() && !os.isDiveActive()) {
        

        if (difference < 22) {
            if (distancemouth <= biters.getRadius() / 6 && biters.biteCooldown <= 0) {
                if((biters.flag_flying && os.flag_flying && biters.getInfo().getTier() > 14 && os.getInfo().getTier() > 14) || (!biters.flag_flying && !os.flag_flying)){

                biters.biteCooldown = 2;
                if(biters instanceof Tiger && ((Tiger)biters).grabbedAni != null) return;
                if(biters instanceof Tiger) biters.biteCooldown = 3;
                biters.biteTimer.reset();
                double factor = 12;
                os.hurt(os.maxHealth / factor, 0, biters);
                if (biters instanceof GiantScorpion)
                    os.setShivering(1, biters);
                int a = 0;
                if(os instanceof BlackDragon || os instanceof KingDragon){
                    a = 25;
                }
                this.biteCollisionDynamic(os.getRadius() + a, biters, os);

                int littleXP = os.getClient().getXP() / 100 * 2;
                double calculatedXPGiven = Math.min(Math.max(40.0 + littleXP, 0), biters.getClient().getXP());
                biters.getClient().addXp((int) Math.floor(calculatedXPGiven));
                os.getClient().takeXp((int) Math.floor(calculatedXPGiven));

                if (biters.getInfo().getTier() >= 17 && biters.getInfo().getTier() == os.getInfo().getTier()) {
                    double addHp1 = os.maxHealth / 25;
                    if (biters.health + addHp1 < biters.maxHealth)
                        biters.health += addHp1;
                }

                os.getClient().send(Networking.personalGameEvent(2, null)); //
                biters.getClient().send(Networking.personalGameEvent(18, null)); //
            }

            }
        }
    }

    }
}