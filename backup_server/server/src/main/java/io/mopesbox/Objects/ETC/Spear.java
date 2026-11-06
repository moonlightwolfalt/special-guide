package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Spear extends Ability {

    private Room room;
    private GameClient owner;
    private double mainAngle;
    private double uspeed = 20;
    private Animal gotcha;
    private boolean flies = false;
    private Timer throwTimer = new Timer(250);
    private double x2;
    private double y2;

    public Spear(int id, double x, double y, int radius, int type, double angle, Room room, GameClient owner, double x2, double y2) {
        super(id, x, y, 112, radius, 0);
        this.setSpecies(type);
        this.owner = owner;
        this.x2 = x2;
        this.y2 = y2;
        this.mainAngle = angle;
        angle -= 90;
        if(angle < 0) angle += 360;
        this.setAngle(angle);
        this.room = room;
        this.setCollideable(false);
        this.setSendsAngle(true);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(this.gotcha == null && o instanceof Animal && o != this.owner.getPlayer() && !((Animal)o).flag_flying && !((Animal)o).flag_speared) {
            ((Animal)o).setSpeared(3, this.owner.getPlayer(), 20);
            this.gotcha = (Animal)o;
            liveTimer.reset();
            liveTimer = new Timer(3000);
            return;
        }
                if(this.gotcha == null && o instanceof Animal && o != this.owner.getPlayer() && ((Animal)o).flag_flying && !((Animal)o).flag_speared && this.owner.getPlayer().flag_isClimbingHill) {
                    ((Animal)o).setSpeared(3, this.owner.getPlayer(), 20);
                    this.gotcha = (Animal)o;
                    liveTimer.reset();
                    liveTimer = new Timer(3000);
                    return;
                }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt32(0);
        writer.writeBoolean(true);
    }

    private Timer liveTimer = new Timer(2500);

    @Override
    public void update() {
        if(throwTimer != null) {
            throwTimer.update(Constants.TICKS_PER_SECOND);
            if(throwTimer.isDone()) {
                this.flies = true;
                this.room.removeVisObj3(this, null);
                throwTimer.reset();
                throwTimer = null;
                this.setX(x2);
                this.setY(y2);
            }
        }
        if(flies) {
            this.specType = 0;
            double eangle = this.mainAngle-90;
            if(eangle < 0) eangle += 360;
            this.setAngle(eangle);
            this.setRadius(12);
            if(this.gotcha == null) {
                this.setSpecies(0);
                this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.uspeed))));
                this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.uspeed))));
            } else {
                this.setSpecies(1);
                this.setX(this.gotcha.getX());
                this.setY(this.gotcha.getY());
                if(this.gotcha.isDead()) {
                    this.room.removeObj(this, null);
                    return;
                }
            }
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.room.removeObj(this, null);
                return;
            }
        } else {
            this.specType = 2;
            double eangle = this.mainAngle+90;
            if(eangle > 360) eangle -= 360;
            this.setAngle(eangle);
            this.setRadius(this.owner.getPlayer().getRadius());
        }
        super.update();
    }
}
