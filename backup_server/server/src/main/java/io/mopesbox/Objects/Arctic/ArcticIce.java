package io.mopesbox.Objects.Arctic;

import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class ArcticIce extends GameObject
{
    public ArcticIce(int id, double x, double y, int rad) {
        super(id, x, y, rad, 17);
        this.setBiome(2);
    }

    private Timer liveTimer;
    private Room room;

    public ArcticIce(int id, double x, double y, int rad, int liveTime, Room room) {
        super(id, x, y, rad, 17);
        this.setBiome(2);
        this.liveTimer = new Timer(liveTime);
        this.room = room;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o.isMovable()) {
            return false;
        }
        return true;
    }

    @Override
    public void update() {
        super.update();
        if(liveTimer != null) {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                if(this.room != null) {
                    this.room.removeObj(this, null);
                }
                liveTimer.reset();
                this.liveTimer = null;
            }
        }
    }
}
