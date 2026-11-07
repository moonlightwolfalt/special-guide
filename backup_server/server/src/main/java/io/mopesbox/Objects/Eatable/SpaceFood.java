package io.mopesbox.Objects.Eatable;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Biome.Space;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

// A planet, star, element, photon... floating in outer space. Any animal can eat it for
// xpBase + xpShare x its current XP (see SpaceFoodKind), more than any pumpkin gives.
// Atoms are eaten on the first touch; everything else takes several separate touches
// (SpaceFoodKind.touches), and the XP comes with the last one.
public class SpaceFood extends GameObject {
    public static final int OBJECT_TYPE = 161;

    private final int kind;
    private final Space space;
    private final Room room;
    private boolean eaten = false;
    private int touchesLeft;
    // animals touching it now, by id; a new touch only counts once an animal has moved off it
    private final java.util.Map<Integer, Animal> touching = new java.util.HashMap<>();

    public SpaceFood(int id, double x, double y, int kind, Space space, Room room) {
        super(id, x, y, (int) Math.round(SpaceFoodKind.KINDS[kind].radius), OBJECT_TYPE);
        this.kind = kind;
        this.space = space;
        this.room = room;
        this.touchesLeft = Math.max(1, SpaceFoodKind.KINDS[kind].touches);
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
        // collisions repeat while touching; only a fresh touch counts
        if (touching.put(eater.getId(), eater) != null) return;
        if (--this.touchesLeft > 0) return;
        SpaceFoodKind k = SpaceFoodKind.KINDS[kind];
        long xp = k.xpBase + (long) Math.floor(eater.getClient().getXPLong() * k.xpShare);
        eater.getClient().addXp(xp);
        this.eaten = true;
        this.room.removeObj(this, o);
        this.space.foodEaten(kind);
    }

    @Override
    public void update() {
        super.update();
        // forget animals that have moved off (or died), so their next touch counts
        if (touching.isEmpty()) return;
        java.util.Iterator<Animal> it = touching.values().iterator();
        while (it.hasNext()) {
            Animal a = it.next();
            double reach = this.getRadius() + a.getRadius() + 4;
            double dx = a.getX() - this.getX(), dy = a.getY() - this.getY();
            if (a.isDead() || dx * dx + dy * dy > reach * reach) it.remove();
        }
    }

    // kind, touches left, touches in all
    private void writeState(MsgWriter writer) {
        writer.writeUInt8(kind);
        writer.writeUInt8(Math.max(0, Math.min(255, touchesLeft)));
        writer.writeUInt8(Math.max(1, Math.min(255, SpaceFoodKind.KINDS[kind].touches)));
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        writeState(writer);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writeState(writer);
    }
}
