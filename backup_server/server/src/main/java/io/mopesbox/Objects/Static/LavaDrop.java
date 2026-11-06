package io.mopesbox.Objects.Static;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;

public class LavaDrop extends GameObject {

    public int is1v1Target;
    private int secondaryType;
    private double speed;
    private double speed2 = 30;
    private Room room;
    private double mainAngle;

    public LavaDrop(int id, double x, double y, int radius, int type, double angle, int spawnFrom, Room room) {
        super(id, x, y, radius, 107);
        this.mainAngle = angle;
        angle -= 180;
        if (angle < 0) angle += 360;
        this.setAngle(angle);
        this.room = room;
        this.setSendsAngle(true);
        this.setCollideCallbacking(false);
        this.setCollideable(false);
        this.setSpawnID(spawnFrom);
    }

    @Override
    public void update() {
        if(speed2 > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.speed))));
            speed--;
            speed2--;
            if(this.speed < 10) this.speed = 10;
        }else{
            LavaLake lake = new LavaLake(this.room.getID(), getX(), getY(), 100, this.room);
            this.room.addObj(lake);
            this.room.removeObj(this, lake);
            lake.setLiveTimer();
            
        }
        // if(this.onCollision() == true){

        // }
        super.update();
    }

    public int getSecondaryType() {
        return secondaryType;
    }
}
