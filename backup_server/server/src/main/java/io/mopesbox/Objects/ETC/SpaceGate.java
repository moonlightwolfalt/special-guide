package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

// The force field between the right ocean and outer space. While it's open anyone can
// cross; an animal that crossed keeps its pass for as long as it stays in space, and
// loses it once it's fully back on the classic map, so it has to wait for the next opening.
public class SpaceGate extends Rectangle {
    public static final int OBJECT_TYPE = 162;

    private boolean open = false;
    private int secondsLeft = 0;

    public SpaceGate(int id, double x, double y, double width, double height, Room room) {
        super(id, x, y, width, height, OBJECT_TYPE);
    }

    public void setState(boolean open, int secondsLeft) {
        this.open = open;
        this.secondsLeft = Math.max(0, Math.min(65535, secondsLeft));
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        return false;
    }

    // Keeps animals without a pass out of space. Called every tick for every animal.
    public void constrain(Animal a) {
        double edge = Constants.CLASSICWIDTH;
        GameClient c = a.getClient();
        double r = a.getRadius();
        if (c != null && c.spacePass) {
            if (a.getX() + r < edge) {
                c.spacePass = false; // back on the classic map
            } else if (a.getX() > edge) {
                // there's no water in space, so explorers don't get thirsty there
                a.setWater(Double.MAX_VALUE);
            }
            return;
        }
        if (a.getX() + r <= edge) return;
        if (open && c != null && !c.isBot()) {
            c.spacePass = true;
            return;
        }
        a.setX(edge - r - 1);
        if (a.getVelocityX() > 0) a.setVelocityX(0);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        writer.writeUInt8(open ? 1 : 0);
        writer.writeUInt16(secondsLeft);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writer.writeUInt8(open ? 1 : 0);
        writer.writeUInt16(secondsLeft);
    }
}
