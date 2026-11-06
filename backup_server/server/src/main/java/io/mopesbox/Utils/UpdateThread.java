package io.mopesbox.Utils;

import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;
import net.openhft.affinity.Affinity;

public class UpdateThread extends Thread {
    private final Room room;

    public UpdateThread(Room room) {
        this.room = room;
        Affinity.setAffinity(4);
    }

    @Override
    public void run() {
        while (true) {
            try {
                update();
                Thread.sleep(room.tick);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        for (GameObject object : room.objects) {
            object.update();
        }
    }
}
