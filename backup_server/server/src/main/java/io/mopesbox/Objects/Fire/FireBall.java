package io.mopesbox.Objects.Fire;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier15.Dragon;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

public class FireBall extends GameObject {

    public int is1v1Target;
    private int secondaryType;
    private double speed;
    private double speed2 = 18;
    private Room room;
    private double mainAngle;
    private GameClient owner;
    private int damage;
    private boolean isDamaged = false;

    public FireBall(int id, double x, double y, int radius, int type, double angle, double speed, int spawnFrom, Room room, GameClient owner, int damage) {
        super(id, x, y, radius, 18);
        this.setSpecies(type);
        this.owner = owner;
        this.damage = damage;
        this.mainAngle = angle;
        angle -= 180;
        if (angle < 0) angle += 360;
        this.setAngle(angle);
        this.room = room;
        this.speed = speed;
        this.setCollideable(false);
        this.setSendsAngle(true);
        this.setCollideCallbacking(true);
        this.setSpawnID(spawnFrom);
    }

    @Override
    public void onCollision(GameObject o) {
        if(this.owner != null){
        if (!isDamaged && o instanceof Animal && ((Animal) o) != this.owner.getPlayer() && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal)o).isDiveActive()) {
            if(!(this.owner.getPlayer() instanceof Dragon) && ((Animal)o).fireSeconds > 0){ 
                if (this.getSpecies() < 5) {
                    ((Animal)o).hurt(6, 0, this.owner.getPlayer());
                    } else {
                    ((Animal)o).hurt(1, 0, this.owner.getPlayer());
                    }
            isDamaged = true;
            this.room.removeObj(this, o);
            return;

              }

            ((Animal)o).setFire(6, this.owner.getPlayer() != null ? this.owner.getPlayer() : null, ((Animal)this.owner.getPlayer()) instanceof Dragon ? 2 : 3);
            isDamaged = true;
                        this.room.removeObj(this, o);

           
            

        }
    }else{
        if(o instanceof Animal){
        if(!((Animal)o).isLavaAnimal()){

                ((Animal)o).setFire(6, null, 5);
            isDamaged = true;
                        this.room.removeObj(this, o);
        }

    }
}
    }

    @Override
    public void update() {
        if(speed2 > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.mainAngle)) * (13))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.mainAngle)) * (13))));
            speed--;
            speed2--;
            if(this.speed < 11) this.speed = 11;

            if(this.owner != null){
                if(this.owner.getPlayer() != null){
            if (this.owner.getPlayer().getInfo().getTier() >= 12) speed--;
                }
            }
        }else{
                        if(this.owner != null){
            Fire newFire = new Fire(this.room.getID(), this.getX(), this.getY(), this.getRadius()+4, this.getSpecies(), this.room, this.owner, damage, isDamaged);
            this.room.addObj(newFire);
            this.room.removeObj(this, newFire);
            return;
            }else{
            Fire newFire = new Fire(this.room.getID(), this.getX(), this.getY(), this.getRadius()+4, this.getSpecies(), this.room, null, damage, isDamaged);
            this.room.addObj(newFire);
            this.room.removeObj(this, newFire);
            }
        }
        // if(this.onCollision() == true){

        // }
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
