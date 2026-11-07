package io.mopesbox.Objects.Eatable;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Space;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

// A planet, star, element, photon... floating in outer space. Any animal can eat it for
// xpBase + xpShare x its current XP (see SpaceFoodKind), more than any pumpkin gives.
public class SpaceFood extends GameObject {
    public static final int OBJECT_TYPE = 161;

    private final int kind;
    private final Space space;
    private final Room room;
    private boolean eaten = false;

    public SpaceFood(int id, double x, double y, int kind, Space space, Room room) {
        super(id, x, y, (int) Math.round(SpaceFoodKind.KINDS[kind].radius), OBJECT_TYPE);
        this.kind = kind;
        this.space = space;
        this.room = room;
        this.setMovable(false);
    }

    public int getKind() {
        return kind;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        // animals pass over it (and eat it); nothing pushes it around
        return false;
    }

    private static boolean canEat(Animal a) {
        return !a.flag_flying && !a.isInHole() && !a.isDiveActive() && a.getClient() != null;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (this.eaten || !(o instanceof Animal) || !canEat((Animal) o)) return;
        Animal eater = (Animal) o;
        SpaceFoodKind k = SpaceFoodKind.KINDS[kind];
        long xp = k.xpBase + (long) Math.floor(eater.getClient().getXP() * k.xpShare);
        eater.getClient().addXp(xp);
        this.eaten = true;
        this.room.removeObj(this, o);
        this.space.foodEaten(kind);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        writer.writeUInt8(kind);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writer.writeUInt8(kind);
    }
}
