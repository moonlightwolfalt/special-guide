package io.mopesbox.Objects.Juggernaut;

import io.mopesbox.Constants; // твой цыган фикс не работает эффф
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class MeteorCrater extends GameObject {
    public byte lavaLevel = 0;
    private Room room;
    private Timer liveTimer = new Timer(15000);

    public MeteorCrater(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 153);
        this.room = room;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        return false;
    }

    @Override
    public void onCollision(GameObject o) {
        if(o.isMovable()) {
            if(o instanceof Animal) {
                if(!((Animal) o).isInHole() && !((Animal)o).flag_fliesLikeDragon && !((Animal)o).isDiveActive() && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()) {
                    double dx = this.getX() - o.getX();
                    double dy = this.getY() - o.getY();

                    double theta = Math.atan2(dy, dx);

                    if (theta < 0)
                        theta += Math.PI * 2;
                    
                    if(Utils.distance(this.getX(), o.getX(), this.getY(), o.getY()) < (this.getRadius()/100)*this.lavaLevel) {
                        if(((Animal)o).fireSeconds <= 2) ((Animal)o).setFire(8, null, ((Animal)o).maxHealth / 12);
                    }

                    double speed = Math.max(0, this.getRadius()/200);
                    if(speed > 0) {
                        double velocityX = Math.cos(theta)*speed;
                        double velocityY = Math.sin(theta)*speed;
                        ((Animal)o).addVelocityX(velocityX);
                        ((Animal)o).addVelocityY(velocityY);
                    }
                }
            } else {
                double dx = this.getX() - o.getX();
                double dy = this.getY() - o.getY();

                double theta = Math.atan2(dy, dx);

                if (theta < 0)
                    theta += Math.PI * 2;

                double velocityX = Math.cos(theta)*2;
                double velocityY = Math.sin(theta)*2;
                o.addVelocityX(velocityX);
                o.addVelocityY(velocityY);
            }
        }
    }

    public Timer raiseTimer = new Timer(500);

    @Override
    public void update() {
        super.update();
        if (this.lavaLevel < 100) {
            raiseTimer.update(Constants.TICKS_PER_SECOND);
            if (raiseTimer.isDone()) {
                this.lavaLevel += 1;
                raiseTimer.reset();
            }
        } else {
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if (liveTimer.isDone()) {
                this.room.removeObj(this, null);
                liveTimer.reset();
            }
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(lavaLevel);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(lavaLevel);
    }

}