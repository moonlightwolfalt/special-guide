package io.mopesbox.Objects.Juggernaut;

import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Dangerous.MopeCoinBox;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Objects.Static.LavaDrop;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Meteor extends GameObject {

    public int height = 0;
    public int maxHeight = 0;
    public double landX = 0;
    public double landY = 0;
    public double speed = 1;
    public double fallStep = 4;
    public double radianAngle = 0;

    public Timer fallTimer = new Timer(500);
    public Room room;

    public boolean hasCoins = false;
    public Meteor(int id, double x, double y, int rad, double landX, double landY, Room room, boolean hasCoins) {
        super(id, x, y, rad, 154);
        this.hasCoins = hasCoins;
        this.maxHeight = (int) Math.floor(this.getRadius() * 1.5);
        this.height = this.maxHeight;
        this.room = room;
        this.sendsAngle = true;
        this.landX = landX;
        this.landY = landY;
        this.radianAngle = Math.atan2(this.landY - this.getY(), this.landX - this.getX());
        double angle = Math.toDegrees(this.radianAngle);
        angle -= 180;
        if (angle < 0)
            angle += 360;
        this.setObjAngle(angle);

        // time we have
        double time = ((this.maxHeight / fallStep) * fallTimer.getMaximumTime());
        double distance = Utils.distance(x, landX, y, landY);
        double speed = (distance / time) * (1000 / Constants.TICKS_PER_SECOND);

        if (speed < 1)
            speed = 1;
        this.speed = speed;

    }

    @Override
    public boolean getCollideable(GameObject o) {

        return false;

    }

    @Override
    public void update() {
        super.update(); // Я ОФФ

        this.setZ(this.height);

        if (this.height > 0) {
            fallTimer.update(Constants.TICKS_PER_SECOND);
            this.setX(this.getX() + Math.round(Math.cos(this.radianAngle) * speed));
            this.setY(this.getY() + Math.round(Math.sin(this.radianAngle) * speed));
            if (fallTimer.isDone()) {
                this.height -= fallStep;
                fallTimer.reset();
            }
        } else {
             for(int index = 0; index < 4; index++){
                  FireBall fire = new FireBall(this.room.getID(), getX(), getY(), 45, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);

                      fire = new FireBall(this.room.getID(), getX(), getY(), 45, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);

                     LavaDrop drop = new LavaDrop(this.room.getID(), getX(), getY(), 60, 0, Utils.randomDouble(0, 360), this.getId(), this.room);
                    this.room.addObj(drop);
            }
            this.room.addObj(
                    new MeteorCrater(this.room.getID(), this.getX(), this.getY(), this.getRadius() * 2, this.room));
            this.room.removeObj(this, null);

           

            if(this.hasCoins){
                MopeCoinBox mopeCoinBox = new MopeCoinBox(this.room.getID(), this.getX(),
                        this.getY(), 18, 0, this.room);
                this.room.addObj(mopeCoinBox);
            }
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt16(maxHeight);
        writer.writeUInt16(height);
        writer.writeBoolean(hasCoins);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt16(height);

    }

}