package io.mopesbox.Objects.Fire;

// import java.lang.constant.Constable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier15.Dragon;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.FireWood;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Fire extends GameObject {

    public int is1v1Target;
    private int secondaryType;
    private Timer timerDel = new Timer(1000);
    private Room room;
    private GameClient owner;
    public boolean isDamaged = false;
    private Timer hpTimer = new Timer(1000);

    private FireWood firewood;
    public Fire(int id, double x, double y, int radius, int type, Room room, GameClient owner, int damage, boolean isDamaged) {
        super(id, x, y, radius, 70);
        this.setSpecies(type);
        this.owner = owner;
        this.room = room;
        this.isDamaged = isDamaged;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.showHP = false;
    }

    public void setDamage(int dam) {
        this.health -= dam;
        this.showHP = true;
        hpTimer.reset();
    }

    public void setFireWood(FireWood a){

        this.firewood = a;


    }

 
    @Override
    public void onCollision(GameObject o) {
        if(this.owner != null){
        if (!isDamaged && o instanceof Animal && ((Animal) o) != this.owner.getPlayer() && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal)o).isDiveActive()) {
            ((Animal)o).setFire(6, this.owner.getPlayer() != null ? this.owner.getPlayer() : null, ((Animal)this.owner.getPlayer()) instanceof Dragon ? 2 : 3);
                if(!(this.owner.getPlayer() instanceof Dragon)) 
            
                if (this.getSpecies() < 5) {
                    ((Animal)o).hurt(6, 0, this.owner.getPlayer());
                    } else {
                    ((Animal)o).hurt(1, 0, this.owner.getPlayer());
                    }
                isDamaged = true;
                this.room.removeObj(this, o);
                return;

        }
            

        }else{
            if(o instanceof Animal){
            if(!((Animal)o).isLavaAnimal()){
            ((Animal)o).setFire(6, null, 5);
                   isDamaged = true;
                this.room.removeObj(this, o);
                return;
            }

        }
    }
    }


    @Override
    public void update() {
        timerDel.update(Constants.TICKS_PER_SECOND);
        if (timerDel.isDone()) {
            this.health -= 20;
            timerDel.reset();
        }
        if (this.showHP) {
            hpTimer.update(Constants.TICKS_PER_SECOND);
            if (hpTimer.isDone()) {
                this.showHP = false;
                hpTimer.reset();
            }
        }
        if (this.health <= 0) {
        if(this.firewood != null) this.firewood.fires.remove(this);
            this.room.removeObj(this, null);
            return;
        }
        super.update();
    }

    public int getSecondaryType() {
        return secondaryType;
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(this.getSpecies());
    }
}
