package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Tier17.DragonAbilities;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;
import io.mopesbox.Utils.Timer;

/**
 * The visible part of the dragons' Wing Slash, Wingspan and Shoot Meteor (DragonAbilities).
 *
 * The client (object type 175, o_kingDragonFx) plays a frame sequence from web/img/kingdragon/<kind>/, made by
 * tools/kingdragon_fx/make_fx_frames.py, rotated to the angle and sized to the radius.
 * Damage is done by DragonAbilities; this object only shows it, except the meteor, which
 * flies forward and tells the dragon where it landed.
 */
public class KingDragonFx extends GameObject {
    public static final int TYPE = 175; // 160-162 are outer space

    public static final int WINGSLASH = 0;
    public static final int WINGSPAN = 1;
    public static final int METEOR = 2;
    public static final int IMPACT = 3;
    public static final int INFERNO = 4;
    public static final int DIVE = 5;
    public static final int ERUPTION = 6;
    public static final int PHOENIX = 7;

    private final int kind;
    private final int angleDeg;
    private final int lifeMs;
    private final Room room;
    private final Timer life;

    // meteor only
    private DragonAbilities owner;
    private double dirX, dirY, speed;
    private int travelTicks;

    public KingDragonFx(int id, double x, double y, int radius, int kind, double angleDeg, int lifeMs, Room room) {
        super(id, x, y, radius, TYPE);
        this.kind = kind;
        this.angleDeg = (int) Math.round(((angleDeg % 360) + 360) % 360);
        this.lifeMs = lifeMs;
        this.room = room;
        this.life = new Timer(lifeMs);
        this.setCollideable(false);
    }

    /** Makes this a flying meteor: it moves `speed` pixels a tick for `ticks` ticks, then lands. */
    public KingDragonFx flyAsMeteor(DragonAbilities owner, double speed, int ticks) {
        this.owner = owner;
        this.speed = speed;
        this.travelTicks = ticks;
        this.dirX = Math.cos(Math.toRadians(angleDeg));
        this.dirY = Math.sin(Math.toRadians(angleDeg));
        return this;
    }

    @Override
    public void update() {
        if (kind == METEOR && owner != null) {
            // GameObject.update() moves by the velocity, then slows it; set it again each tick
            this.setVelocityX(dirX * speed);
            this.setVelocityY(dirY * speed);
            super.update();
            if (--travelTicks <= 0) {
                owner.meteorLanded(this.getX(), this.getY());
                room.removeObj(this, null);
            }
            return;
        }
        super.update();
        life.update(Constants.TICKS_PER_SECOND);
        if (life.isDone()) room.removeObj(this, null);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(kind);
        writer.writeUInt16(angleDeg);
        writer.writeUInt16(Math.min(65535, lifeMs));
    }
}
