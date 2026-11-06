package io.mopesbox.Objects.Fun;


import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.World.Room;

public class FireForCircle extends GameObject { // dary00_ fire
    public GameClient owner;
    public Room room;
    public double coordY;
    public double coordX;

    public FireForCircle(int id, double x, double y, Room room, GameClient owner, double coordX, double coordY) {
        super(id, x, y, 10, 18);
        this.owner = owner;
        this.room = room;
        this.coordY = coordY;
        this.coordX = coordX;
        this.isSummonedFire = true;
        
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        // Main.log.info("collision");
        if(o instanceof Animal && !((Animal) o).inArena && ((Animal) o) != this.owner.getPlayer() && !((Animal) o).isInvincible()) {
            // Main.log.info("FIRE PROTECTS DARY00_ AND ATTACKS AMOGUS!!!~");
            ((Animal)o).setFire(3, null, 1);
        }
    }

    @Override
    public void update() {
        super.update();


        // if(this.owner == null || this == null || this.owner.getPlayer() == null || Objects.equals(this.owner.getPlayer().getX(), null) || Objects.equals(this.owner.getPlayer().getY(), null)) this.room.removeObj(this);
        if(this.owner == null || this.owner.getPlayer() == null) {
            this.room.removeObj(this, null);
            return;
        }

        this.setX(this.owner.getPlayer().getX()+coordX);
        this.setY(this.owner.getPlayer().getY()+coordY);
    }

@Override
public void writeCustomData_onAdd(MsgWriter writer){
    super.writeCustomData_onAdd(writer);
    writer.writeUInt8(this.getSpecies());
}
}

// public class Amogus {
    
// }
