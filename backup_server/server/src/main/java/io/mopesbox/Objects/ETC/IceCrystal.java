package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Arctic.ArcticIce;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class IceCrystal extends GameObject {

    private double speed;
    private Room room;
    private double mainAngle;
    private GameClient owner;
    private double uspeed = 2;

    public IceCrystal(int id, double x, double y, int radius, int type, double angle, double speed, int spawnFrom, Room room, GameClient owner) {
        super(id, x, y, radius, 80);
        this.setSpecies(type);
        this.owner = owner;
        this.mainAngle = angle;
        angle -= 180;
        if(angle < 0) angle += 360;
        this.setAngle(angle);
        this.room = room;
        this.speed = speed;
        this.setCollideable(false);
        this.setSendsAngle(true);
        this.setCollideCallbacking(true);
        this.setSpawnID(spawnFrom);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(radius * 1.8);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && o != this.owner.getPlayer() && o.getCollideable(this)) {
            ((Animal)o).setFrozen(3, this.owner.getPlayer(), this.owner.getPlayer().getInfo().getTier() > ((Animal)o).getInfo().getTier() ? (int) ((Animal)o).maxHealth / 6 : (int) ((Animal)o).maxHealth / 8);
            this.room.removeObj(this, o);
        }
    }

    private Timer liveTimer = new Timer(1500);

    @Override
    public void update() {
        this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.uspeed))));
        this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.uspeed))));
        if(this.uspeed < this.speed) this.uspeed += 1;
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            ArcticIce ice = new ArcticIce(this.room.getID(), this.getX(), this.getY(), 80, 5000, this.owner.getRoom());
            this.room.addObj(ice);
            this.room.removeObj(this, null);
            return;
        }
        super.update();
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(this.getSpecies());
    }
}
