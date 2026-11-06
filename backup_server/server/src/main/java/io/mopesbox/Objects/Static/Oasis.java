package io.mopesbox.Objects.Static;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

public class Oasis extends GameObject
{
    private boolean isOasisWater = false;
    public Oasis(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 100);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        OasisWater oasisWater = new OasisWater(room.getID(), this);
        room.addObj(oasisWater);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(isOasisWater);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(isOasisWater);
    }
}
