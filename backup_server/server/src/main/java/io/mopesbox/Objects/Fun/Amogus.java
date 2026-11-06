package io.mopesbox.Objects.Fun;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Client.GameClient;
import io.mopesbox.World.Room;

public class Amogus extends GameObject {
    public GameClient owner;
    public Room room;
    public double coordY;
    public double coordX;

    public Amogus(int id, double x, double y, Room room, GameClient owner, double coordX, double coordY) {
        super(id, x, y, 10, 151);
        this.owner = owner;
        this.room = room;
        this.coordY = coordY;
        this.coordX = coordX;
        
        this.setCollideable(false);
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
}

// public class Amogus {
    
// }
