package io.mopesbox.Objects.ETC;

import java.util.Arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class FireTornado extends GameObject{

    public Animal owner;
    public Room room;
    public double aimAngle;
 
    public int speed = 6;
    public Timer canShoot = new Timer(5000);
    public Timer liveTimer = new Timer(30000);
    public FireTornado (final int id,final double x,final double y,final int radius,Animal owner) {
        super(id, x, y, radius, 71);
        this.owner = owner;
        this.setSpecies(owner.getInfo().getSkin());
        this.sendsAngle = false;
        this.room = Main.instance.room;
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(this.getRadius()*10);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setMovable(true);
    }

    @Override
    public void update() {
        super.update();
        canShoot.update(Constants.TICKS_PER_SECOND);
        liveTimer.update(Constants.TICKS_PER_SECOND);
        checkShoot();
        if (liveTimer.isDone()) {
            room.removeObj(this,null);
        }
    }

    public void checkShoot(){
        HashMap<Double,Animal> animals = new HashMap<>();
        // this.room.chat(this.owner.getClient(),"Collided with: " + collided.size());
        for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
            GameObject o = entry.getValue();
             if(o instanceof Animal){
                Animal ani = (Animal) o;
                if(ani == owner) continue;
                animals.put(Utils.distance(ani.getX(),owner.getX(),ani.getY(),owner.getY()),ani);
            }
        }
        if (animals.size() <= 0) {
            followOwner();
            return;
        }
        Double[] key = new Double[animals.size()];
        int i = 0;
        for (Entry<Double, Animal> entry : animals.entrySet()) {
            Double keye = entry.getKey();
            key[i] = keye != null ? keye : 10000;
            i++;
        }
        Arrays.sort(key);
        Animal closestAnimal = animals.get(key[key.length-1]); 

        double ang = Math.atan2(closestAnimal.getY() - this.getY(), closestAnimal.getX() - this.getX());
        ang *= 180 / Math.PI; // rads to degs, range (-180, 180]
        if (ang < 0) {
            ang += 360;
        }
        this.aimAngle = ang;
        if (Utils.distance(this.getX(),closestAnimal.getX() ,this.getY() ,closestAnimal.getY()) > this.getRadius()/2) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.aimAngle)) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.aimAngle)) * (this.speed))));
        }
        if (canShoot.isDone()) {
            shoot(closestAnimal);
            canShoot.reset();
        }
    }

    public void followOwner() {
        double ang = Math.atan2(owner.getY()-this.getY(), owner.getX()-this.getX());
        this.aimAngle = ang;
        if (Utils.distance(this.getX(),owner.getX(),this.getY(),owner.getY()) > this.getRadius()*4){
            this.setVelocityX(((Math.cos(this.aimAngle) * (this.speed))));
            this.setVelocityY(((Math.sin(this.aimAngle) * (this.speed))));
	    }
    }

	public void shoot(Animal ani){
        double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
        FireBall newFire = new FireBall(this.room.getID(), this.getX()+x, this.getY()+y, 10, 0, this.getAngle(), 12, this.getId(), this.room, this.owner.getClient(),1);
        this.room.addObj(newFire);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(0); // species
    }
}