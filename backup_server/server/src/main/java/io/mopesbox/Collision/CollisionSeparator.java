package io.mopesbox.Collision;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.mopesbox.Main;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.World.Room;
import net.openhft.affinity.Affinity;

public class CollisionSeparator extends Thread {
    public final Room room;

    public final boolean debug = false;
    public final Main main;
    public Set<GameObject> objects = null;
    public CollisionHandler collisionHandler = null;
    public final int id;
    public final AtomicBoolean neededMathFlag = new AtomicBoolean(false);

    public CollisionSeparator(Room room, Main main, CollisionHandler collisionHandler, int id) {
        Affinity.setAffinity(3);

        this.room = room;
        this.main = main;
        this.id = id;
        this.collisionHandler = collisionHandler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(this.neededMathFlag.compareAndSet(true, false)) this.update();
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
                o.update();

                if (o.getCollideable() || o.isCollideCallback() || o instanceof PvPArena) {
                    o.resetCollidedList();
                    // collisionHandler.qt.insert(o);
                }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            int durationCeiled = (int) Math.ceil(duration);
            this.room.lticks.put(id, durationCeiled);
        if (this.room.hticks.get(id) < (int) Math.ceil(duration))
                this.room.hticks.put(id, (int) Math.ceil(duration));
        
    }
}