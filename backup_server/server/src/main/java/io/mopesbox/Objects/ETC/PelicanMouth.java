package io.mopesbox.Objects.ETC;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier10.Pelican;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;

public class PelicanMouth extends Ability {
    private Room room;
    public Animal owner;
    public Animal gotcha;
    public PelicanMouth(int id, double x, double y, int radius, Room room, Animal owner, int species) {
        super(id, x, y, 55, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setSendsAngle(true);
        this.setAngle(this.owner.getAngle());
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setSpecies(0);
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        if (o instanceof Animal && (gotcha == null || gotcha == (Animal) o) && ((Animal) o) != this.owner
                && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying
                && !((Animal) o).isInHole() && ((Animal) o).bleedingSeconds <= 0 && !((Animal) o).isStunned() && !this.owner.flag_flying ) {
                    if(this.owner.getTier() >= ((Animal)o).getTier()){

            if (((Animal) o).getInfo().getTier() > 16 || ((Animal) o).isLavaAnimal()) {
                ((Animal) owner).flag_usingAbility = false;
                this.owner.setFire(5, this.owner, 3);
                this.room.removeObj(this, this.owner);

                return;
            }

            if (this.gotcha == null) {
                this.gotcha = (Animal) o;
                this.gotcha.hurt(5, 0, this.owner);
                
        this.gotcha.setZ((int) this.owner.getZ());
        this.gotcha.flag_flying = true;
        ((Pelican)this.owner).runningTimer.isRunning = false;
        this.gotcha.pickedByPelican = true;
        this.gotcha.pickedBy = this.owner;
            }
            }
        }
    }

    @Override
    public boolean canBeVisible(GameClient client) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (this != null && this.gotcha != null) {

            this.angle = this.owner.getAngle();

         

            if (this.gotcha != null && this != null) {
                
        this.gotcha.setX(this.getX());
        this.gotcha.setY(this.getY());
        this.gotcha.setZ((int) this.owner.getZ());
        this.gotcha.flag_flying = true;

            }
            }
            if(this.owner != null){
                this.setAngle(this.owner.getAngle());
               double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (this.getRadius()*2)));
            double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (this.getRadius()*2)));

            this.setX(((Animal) owner).getX() + x);
            this.setY(((Animal) owner).getY() + y);
            }
    
        

                if(this.getRadius() < 25){
                this.setRadius(this.getRadius()+2);
            }

    }

    public void makeFall(){
        
        if(this.gotcha != null){
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (3)));
              double y = ((Math.sin(Math.toRadians(this.getAngle())) * (3)));

              gotcha.setStun(5);
             gotcha.makeFall(x, y, this.owner);
             gotcha.pickedByPelican = false;
             gotcha = null;
}
    }
    
}