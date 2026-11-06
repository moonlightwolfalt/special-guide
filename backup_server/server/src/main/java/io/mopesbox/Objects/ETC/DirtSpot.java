package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class DirtSpot extends GameObject
{
    private Room room;
    public DirtSpot(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 7);
        this.room = room;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }     
    private Timer liveTimer =  new Timer(5000);

    @Override
    public void update(){
        super.update();
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()){
            this.room.removeObj(this, null);

        }
    }
}
