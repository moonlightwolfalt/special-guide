package io.mopesbox.Objects.Static;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Roar;
import io.mopesbox.World.Room;

public class WaterDrop extends GameObject {

    public int is1v1Target;
    private int secondaryType;
    private double speed;
    private double speed2 = 30;
    private double mainAngle;
    private Animal owner;

    public WaterDrop(int id, double x, double y, int radius, int type, double angle, int spawnFrom, Room room, Animal owner) {
        super(id, x, y, radius, 67);
        this.mainAngle = angle;
        angle -= 180;
        if (angle < 0) angle += 360;
        this.setAngle(angle);
        this.room = room;
        this.setSendsAngle(true);
        this.setCollideCallbacking(true);
        this.setCollideable(false);
        this.owner = owner;
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
            Roar roar = new Roar(this.owner.getRoom().getID(), getX(), getY(), 50, this.owner.getRoom(), this.getAngle(), this.owner, 0);
            roar.setSpawnID(this.getId());
            this.owner.getRoom().addObj(roar);
            this.owner.getRoom().removeObj(this, roar);

        }
        // if(this.onCollision() == true){

        // }
        super.update();
    }

    public int getSecondaryType() {
        return secondaryType;
    }
}
