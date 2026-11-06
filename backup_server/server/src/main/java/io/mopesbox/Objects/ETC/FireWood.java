package io.mopesbox.Objects.ETC;

import java.util.ArrayList;

// import java.lang.constant.Constable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier15.Bigfoot;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Fire.Fire;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class FireWood extends GameObject {

    public int is1v1Target;
    private int secondaryType;
    private Timer timerDel = new Timer(1000);
    private Room room;
    private GameClient owner;
    public boolean isDamaged = false;
    private Timer hpTimer = new Timer(1000);

    public FireWood(int id, double x, double y, int radius, Room room, GameClient owner, boolean isDamaged) {
        super(id, x, y, radius, 121);
        this.owner = owner;
        this.room = room;
        this.isDamaged = isDamaged;
        this.setCollideable(true);
        this.setCollideCallbacking(true);
        this.showHP = false;
    }

        public ArrayList<GameObject> fires = new ArrayList<>();

    private Timer timerPerFire = new Timer(300);
    public void setDamage(int dam) {
        this.health -= dam;
        this.showHP = true;
        hpTimer.reset();
    }

    @Override
    public void onCollision(GameObject o) {
        if(!isDamaged && o instanceof Animal && (this.owner == null || ((Animal) o) != this.owner.getPlayer()) && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && ((Animal)o).fireSeconds <= 0) {
            ((Animal)o).setFire(6, this.owner != null ? this.owner.getPlayer() : null, ((Animal)o).maxHealth / 9);
        }
    }
    @Override
    public boolean getCollideable(GameObject o){
        return true;
    }

    @Override
    public void update() {
        if(this.fires.size() < 8){
            timerPerFire.update(Constants.TICKS_PER_SECOND);
            if(timerPerFire.isDone()){
                Fire fire = new Fire(this.owner.getRoom().getID(), this.getX() + Utils.randomDouble(-20, 20), this.getY() + Utils.randomDouble(-20, 20), (int) this.getRadius() - 25, 0, this.owner.getRoom(), this.owner, 5, true);
                this.owner.getRoom().addObj(fire);
                fire.health = 20;
                fire.setSpawnID(this.getId());
                fire.setFireWood(this);
                this.fires.add(fire);
            }
        }
        timerDel.update(Constants.TICKS_PER_SECOND);
        if (timerDel.isDone()) {
            this.health -= 5;
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
            if(this.owner.getPlayer() instanceof Bigfoot) 
             ((Bigfoot)this.owner.getPlayer()).bigfootfirewood = null;
            this.room.removeObj(this, null);
            return;
        }
        super.update();
    }

    public int getSecondaryType() {
        return secondaryType;
    }

}
