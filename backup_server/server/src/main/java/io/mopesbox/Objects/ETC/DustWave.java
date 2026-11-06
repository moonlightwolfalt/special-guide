package io.mopesbox.Objects.ETC;



import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class DustWave extends Ability {
    private Timer liveTimer = new Timer(2500);
    private Timer radTimer = new Timer(500);
    private Room room;
    private double speed = 10;
    private GameClient owner;
    private double mainAngle;
    private int alpha = 0;
    private boolean upalpha = true;
    public DustWave(int id, double x, double y, int radius, double angle, double speed, int spawnFrom, Room room, GameClient owner) {
        super(id, x, y, 102, radius, 0);
        this.room = room;
        this.speed = speed;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.mainAngle = angle;
        angle -= 180;
        if(angle < 0) angle += 360;
    }

    @Override
    public void onCollision(GameObject o) {
        if (o instanceof Animal && (this.owner == null || ((Animal) o) != this.owner.getPlayer()) && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && ((Animal) o).getCollideable(this) && ((Animal) o).getInfo().getTier() < 17) {
            ((Animal)o).setStun(2);
            // ((Animal)o).setFire(3, this.owner != null ? this.owner.getPlayer() != null ? this.owner.getPlayer() : null : null, 0);
            ((Animal)o).addVelocityX((((Math.cos(Math.toRadians(this.mainAngle)) * (1.5)))));
            ((Animal)o).addVelocityY((((Math.sin(Math.toRadians(this.mainAngle)) * (1.5)))));
            ((Animal)o).onHurt();
        } else if (o.isMovable() && !(o instanceof Animal)) {
            o.addVelocityX((((Math.cos(Math.toRadians(this.mainAngle)) * (1.5)))));
            o.addVelocityY((((Math.sin(Math.toRadians(this.mainAngle)) * (1.5)))));
        }
    }

    @Override
    public void update() {
        super.update();
        if (speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (this.speed))));
        }
        if (upalpha) {
            if(alpha < 100) alpha++;
        } else {
            if(alpha > 0) alpha--;
        }
        radTimer.update(Constants.TICKS_PER_SECOND);
        if (radTimer.isDone()) {
            this.setRadius(this.getRadius()+1);
            radTimer.reset();
        }
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if (liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(alpha);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(alpha);
    }
}
