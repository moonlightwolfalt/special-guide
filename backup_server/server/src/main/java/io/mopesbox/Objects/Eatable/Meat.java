package io.mopesbox.Objects.Eatable;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

public class Meat extends GameObject{
    private int xp;
    private Room room;
    private int type;
    public Animal owner;
    public Meat(int id, double x, double y, int type, int xp, Room room) {
        super(id, x, y, type == 0 ? 12 : type == 1 ? 22 : 36, type == 0 ? 52 : type == 1 ? 53 : 54);
        this.setMovable(true);
        this.xp = xp;
        this.type = type;
        this.room = room;
    }

    public Meat(int id, double x, double y, int type, int xp, int spawnFrom, Room room) {
        super(id, x, y, type == 0 ? 12 : type == 1 ? 22 : 36, type == 0 ? 52 : type == 1 ? 53 : 54);
        this.setMovable(true);
        this.setSpawnID(spawnFrom);
        this.xp = xp;
        this.type = type;
        this.room = room;
    }

    public Meat(int id, double x, double y, int type, int xp, int spawnFrom, Room room, Animal owner) {
        super(id, x, y, type == 0 ? 12 : type == 1 ? 22 : 36, type == 0 ? 52 : type == 1 ? 53 : 54);
        this.setMovable(true);
        this.setSpawnID(spawnFrom);
        this.xp = xp;
        this.type = type;
        this.room = room;
        this.owner = owner;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideable(o) && o instanceof Animal) {
            Animal biter = (Animal) o;
            if(this.owner != biter) {
                if((this.type == 0 && biter.getInfo().getTier() > 0) || (this.type == 1 && biter.getInfo().getTier() >= 6) || (this.type == 2 && biter.getInfo().getTier() >= 11)) {
                    biter.getClient().addXp(this.xp);
                    this.room.removeObj(this, o);
                }
            } else {
                biter.getClient().send(Networking.personalGameEvent(255, "You can't eat your own meat!"));
            }
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(0); // spec type
    }
}
