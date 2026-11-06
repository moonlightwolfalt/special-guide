package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Carrot;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Mudspot extends GameObject
{
    private Room room;
    public Mudspot(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 7);
        this.room = room;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setMovableInCollision(true);
    }

    private Timer carrotTimer = new Timer(5000);

    @Override
    public void update() {
        super.update();
        carrotTimer.update(Constants.TICKS_PER_SECOND);
        if(carrotTimer.isDone() && Constants.FOODSPAWN) {
            double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (Utils.randomInt(14, this.getRadius()))));
            double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (Utils.randomInt(14, this.getRadius()))));
            Carrot carrot = new Carrot(this.room.getID(), this.getX()+x, this.getY()+y, 1, room);
            this.room.addObj(carrot);
            carrotTimer.reset();
        }
    }
}

