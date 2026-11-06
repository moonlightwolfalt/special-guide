package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class SandboxArena extends GameObject
{
    public List<GameClient> canWalk = new ArrayList<>();
    private GameClient owner;
    public boolean attaching = false;
    private Room room;
    private boolean static1 = false;
    public SandboxArena(int id, double x, double y, int rad, GameClient owner, Room room) {
        super(id, x, y, rad < 40 ? 40 : rad > 1000 ? 1000 : rad, 126);
        this.owner = owner;
        this.room = room;
        // this.setHasCustomCollisionRadius(true);
        // this.setCustomCollisionRadius(rad);
    }

    public void setStatic(boolean status) {
        this.static1 = status;
    }

    public boolean isStatic() {
        return this.static1;
    }

    @Override
    public boolean getCollideable(GameObject o) {

        if(o instanceof Animal) {
            if(this.canWalk.contains(((Animal)o).getClient())) return false;
            if((((Animal)o).getClient()) == owner) return false;
            if((((Animal)o).flag_flying) && this.canWalk.contains(((Animal)o).getClient())) return false;

            if((((Animal)o).flag_flying) && !this.canWalk.contains(((Animal)o).getClient())) return true;

        if((((Animal)o).diveActive) && !this.canWalk.contains(((Animal)o).getClient())) return true;



            return true;
        }
        return false;
    }
  
        

        
    @Override
    public void update() {
        super.update();
        if(this.owner == null || this.owner.getPlayer() == null) {
            this.owner.sandboxArena = null;
            this.room.removeObj(this, null);
            return;
        }
        if (!this.isStatic()) {
            this.setX(this.owner.getPlayer().getX());
            this.setY(this.owner.getPlayer().getY());
        }
        List<GameClient> todel = new ArrayList<>();
        for(GameClient client : this.canWalk) {
            if(client == null) {
                todel.add(client);
            }
        }
        this.canWalk.removeAll(todel);

      
            
        }


        
    
    
    public void innerCollision(double distance, GameObject entity1, GameObject entity2) {
        double dx = entity2.getX() - entity1.getX();
        double dy = entity2.getY() - entity1.getY();
        double nx = dx / distance;
        double ny = dy / distance;
        double newX2 = ((entity1.getX() + nx * (entity1.getRadius() - (entity2.getRadius() + 15))));
        double newY2 = ((entity1.getY() + ny * (entity1.getRadius() - (entity2.getRadius() + 15))));

        if (entity2.isMovable() && entity1.getRadius() - (entity2.getRadius() + 15) <= distance) {
            if (Utils.isValidDouble(newX2))
                entity2.setX(newX2);
            if (Utils.isValidDouble(newY2))
                entity2.setY(newY2);
        }
    }
}
