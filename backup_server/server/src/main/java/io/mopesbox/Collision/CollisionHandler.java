package io.mopesbox.Collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
// import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import net.openhft.affinity.Affinity;


public class CollisionHandler extends Thread {
    public final Room room;

    public final boolean debug = false;
    private Timer leaderBoardTimer = new Timer(2000);
    private Timer stressTestTimer = new Timer(1000);
    public int stressTestSeed = 0;
    public final Main main;
    public QuadTree<GameObject> qt;
    public List<CollisionMath> collisionMaths = new ArrayList<>();
    public List<CollisionSeparator> collisionSeparators = new ArrayList<>();
    public int numThreads = 2;
    public int numSeparatorThreads = 2;


    public CollisionHandler(Room room, Main main) {
        this.room = room;
        this.main = main;
        this.qt = new QuadTree<GameObject>(5, room.getBounds());
        Affinity.setAffinity(2);
        // for (int i = 0; i < numThreads; i++) {
        //     CollisionMath thread = new CollisionMath(room, main, this, i);
        //     collisionMaths.add(thread);
        //     // thread.start();
        //     executorService.submit(thread);
        //     this.room.lticks.put(i, 0);
        //     this.room.hticks.put(i, 0);
        // }
        // for (int i = 0; i < numSeparatorThreads; i++) {
        //     CollisionSeparator thread = new CollisionSeparator(room, main, this, i + numThreads);
        //     collisionSeparators.add(thread);
        //     // thread.start();
        //     executorService.submit(thread);
        //     this.room.lticks.put(i + numThreads, 0);
        //     this.room.hticks.put(i + numThreads, 0);
        // }
        loadHandlers();
    }

    public void insert(GameObject o) {
        this.qt.insert(o);
    }
    private void loadHandlers() {

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {


            CollisionMath mathThread = new CollisionMath(room, main, this, i);
            collisionMaths.add(mathThread);

            executorService.submit(mathThread);
            this.room.lticks.put(i, 0);
            this.room.hticks.put(i, 0);
        }

        ExecutorService executorService1 = Executors.newFixedThreadPool(numSeparatorThreads);

        for (int i = 0; i < numSeparatorThreads; i++) {

            CollisionSeparator mathThread = new CollisionSeparator(room, main, this, i+numThreads);
            collisionSeparators.add(mathThread);

            executorService1.submit(mathThread);
            this.room.lticks.put(i + numThreads, 0);
            this.room.hticks.put(i + numThreads, 0);
        }

    }

    
    @Override
    public void run() {
        while (true) {
            try {
            // if (!room.isThreadReady)
                this.update();
                Thread.sleep(this.room.tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void update() {

        

        long startTime = System.nanoTime();

        if (Constants.STRESSTEST) {
            stressTestTimer.update(Constants.TICKS_PER_SECOND);
            if (stressTestTimer.isDone()) {
                stressTestSeed++;
            }
        }

     this.qt.clear();

ConcurrentHashMap<Integer, GameObject> objects = this.room.getObjects().gameMap;

List<Set<GameObject>> threadSets = new ArrayList<>(numSeparatorThreads);
for (int i = 0; i < numSeparatorThreads; i++) {
    threadSets.add(Collections.newSetFromMap(new ConcurrentHashMap<>()));
}

objects.forEach((key, gameObject) -> {
    int threadIndex = key % numSeparatorThreads;
    threadSets.get(threadIndex).add(gameObject);
});
IntStream.range(0, numSeparatorThreads).parallel().forEach(i ->
    this.collisionSeparators.get(i).math(threadSets.get(i))
);
        for (Map.Entry<Integer, GameObject> entry : objects.entrySet()) {
            GameObject o = entry.getValue();
            if (o.getCollideable() || o.isCollideCallback() || o instanceof PvPArena) {
                qt.insert(o);
            }
        }
        

List<Set<GameObject>> threadSets1 = new ArrayList<>(numThreads);
for (int i = 0; i < numThreads; i++) {
    threadSets1.add(Collections.newSetFromMap(new ConcurrentHashMap<>()));
}

objects.forEach((key, gameObject) -> {
    int threadIndex = key % numThreads;
    threadSets1.get(threadIndex).add(gameObject);
});

IntStream.range(0, numThreads).parallel().forEach(i ->
    this.collisionMaths.get(i).math(threadSets1.get(i))
);



        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;

        if (debug)
            this.main.guiThread.toLog(
                    "Collision took " + duration + "ms" + " with " + room.getObjects().size() + " objects on the map.");
        this.room.latestTick = duration;
        if (this.room.higherTick < duration)
            this.room.higherTick = duration;

        // leaderboard
        leaderBoardTimer.update(Constants.TICKS_PER_SECOND);
        if (leaderBoardTimer.isDone()) {
           List<GameClient> clients1 = new ArrayList<>(this.room.clients);
            for (GameClient a : clients1) {
                a.send(Networking.leaderBoardPacket(this.room, a));
                a.send(Networking.playerCountPacket(this.room));
                if (Constants.GAMEMODE == 2) {
                    // battle royale
                    if (this.room.br_gamestate != 4) {
                        a.send(Networking.br_playerCount(this.room, this.room.getAlivePlayers(),
                                this.room.br_gamestate == 2));
                    }
                    a.send(Networking.br_gameState(this.room, this.room.getAlivePlayers()));
                }
            }
            leaderBoardTimer.reset();
        }
        // room.isThreadReady = true;
    }


    public boolean testCollision(GameObject o, GameObject obj){
        double orad2 = o.isHasCustomCollisionRadius() ? o.getCustomCollisionRadius() : o.getRadius();
        double objrad2 = obj.isHasCustomCollisionRadius() ? obj.getCustomCollisionRadius()
                : obj.getRadius();
        double distance = Utils.distance(o.getX(), obj.getX(), o.getY(), obj.getY());

        if (distance > orad2 + objrad2) {     
return false;
        }

        if (distance <= orad2 + objrad2) {
            return true;
        }
        
        return false;
        
    }
        public boolean testCollision_with1(GameObject o, double x, double y, int rad){

        double distance = Utils.distance(o.getX(), x, o.getY(), y);

        if (distance > o.getRadius() + rad) {     
return false;
        }

        if (distance <= o.getRadius() + rad) {
            return true;
        }
        
        return false;
        
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

        if (entity1 instanceof Animal && entity2 instanceof Animal && (((Animal) entity1).getInfo().isEdible((Animal) entity2)
        || ((Animal) entity2).getInfo().isEdible((Animal) entity1))) {
    // Animal biter
    // Animal prey = null;
    if (((Animal) entity1).getInfo().getTier() > ((Animal) entity2).getInfo().getTier()) {
        biter = (Animal) entity1;
        prey = (Animal) entity2;
    } else{
        if(((Animal)entity1).getInfo().getTier() == ((Animal)entity2).getInfo().getTier()) return;
        biter = (Animal) entity2;
        prey = (Animal) entity1;
    }
   

    if (this.isLookingAt(biter, prey) && biter.biteCooldown < 1) {
        double calculatedXPGiven = Math.min(Math.max(40.0, 0), biter.getClient().getXP());
        biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
        prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));
        prey.hurt(biter.biteDamage, 2, biter);
        if (biter.canSetBiteCooldown(prey, false))
            biter.setBiteCooldown(1);
        // biter.getHealAfterBite(prey, hurthp);
    }
}

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
        double overlap = ((ent1Radius + ent2Radius) - distance);
        double normalX = (entity2.getX() - entity1.getX()) / distance;
        double normalY = (entity2.getY() - entity1.getY()) / distance;

        double impulse = (entity1.getRadius() + entity2.getRadius()) / 2;

        double impulseX = impulse * normalX;
        double impulseY = impulse * normalY;

        if (entity1.isMovable()) {
            entity1.remVelocityX(impulseX / entity1.getRadius());
            entity1.remVelocityY(impulseY / entity1.getRadius());
        }

        if (entity2.isMovable()) {
            entity2.addVelocityX(impulseX / entity2.getRadius());
            entity2.addVelocityY(impulseY / entity2.getRadius());
        }

        if (entity2.isMovable()) {
            if (Utils.isValidDouble(entity2.getX() + normalX * overlap / 2))
                entity2.setX(entity2.getX() + normalX * overlap / 2);
            if (Utils.isValidDouble(entity2.getY() + normalY * overlap / 2))
                entity2.setY(entity2.getY() + normalY * overlap / 2);
        }

        if (entity1.isMovable() || (entity1 instanceof PvPArena)) {
            if (Utils.isValidDouble(entity1.getX() - normalX * overlap / 2))
                entity1.setX(entity1.getX() - normalX * overlap / 2);
            if (Utils.isValidDouble(entity1.getY() - normalY * overlap / 2))
                entity1.setY(entity1.getY() - normalY * overlap / 2);
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

    public void biteCollision(double distance, Animal prey, Animal biter, PvPArena pvp) {
        if (biter.biteCooldown < 1 && !biter.flag_flying && !biter.isInvincible() && !biter.isInHole()
                && !biter.isDiveActive() && !prey.flag_flying && !prey.isInvincible() && !prey.isInHole()
                && !prey.isDiveActive()) {
            double difference = prey.getAngle() - biter.getAngle();
            while (difference < -180)
                difference += 360;
            while (difference > 180)
                difference -= 360;

            if (difference < 0) {
                difference = -difference;
            }
            if (difference < 26 && Utils.distance(biter.getX(), prey.getX(), biter.getY(),
                    prey.getY()) <= biter.getRadius() + prey.getRadius()) {
                double en1x = ((Math.cos(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en1y = ((Math.sin(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en2x = ((Math.cos(Math.toRadians(prey.getAngle())) * prey.getRadius()));
                double en2y = ((Math.sin(Math.toRadians(prey.getAngle())) * prey.getRadius()));
                if (Utils.distance(biter.getX() + en1x, prey.getX() - en2x, biter.getY() + en1y,
                        prey.getY() - en2y) <= biter.getRadius() * 1.2) {
                    if (biter.getInfo().getTier() >= prey.getInfo().getTier()) {
                        double calculatedXPGiven = Math.min(40.0 + prey.getClient().getXP() / 100 * 2,
                                biter.getClient().getXP());
                        biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
                        prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));
                        if (biter.getInfo().getTier() >= 17 && biter.getInfo().getTier() == prey.getInfo().getTier()) {
                            double addHp1 = prey.maxHealth / 25;
                            if (biter.health + addHp1 < biter.maxHealth) {
                                biter.health += addHp1;
                            }
                        }
                        prey.hurt(prey.maxHealth / 10, 2, biter);
                        this.biteCollisionDynamic(biter.getRadius(), biter, prey);
                        if (pvp != null)
                            pvp.addBite(biter);
                        biter.setBiteCooldown(1);
                    } else {
                        double calculatedXPGiven = Math.min(Math.max(40.0, 0), biter.getClient().getXP());
                        biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
                        prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));
                        prey.hurt(prey.maxHealth / 10, 2, biter);
                        this.biteCollisionDynamic(biter.getRadius(), biter, prey);
                        if (pvp != null)
                            pvp.addBite(biter);
                        biter.setBiteCooldown(1);
                    }
                }
            }
        }
    }
}