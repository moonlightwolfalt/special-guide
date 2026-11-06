package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier14.BlackwidowSpider;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class blackWidowAbility extends Ability {
    private Room room;
    private int speed = 14;
    public boolean isBuilding = true;
    public Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 20;
    private Animal gotcha = null;
    private Timer sizeTimer = new Timer(2000);
    private double globalAlpha = 20;
    public blackWidowAbility(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 92, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(true);
        this.setCollideCallbacking(true);
        // this.setSpeed();
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal)o).flag_eff_poison) {
            if (((Animal)o).getInfo().getTier() < this.owner.getInfo().getTier()) {
            ((Animal)o).hurt(o instanceof Bee ? o.health : (damage + (((Animal)o).maxHealth / 18)), 1, this.owner);
            this.gotcha = ((Animal)o);
            ((Animal)o).setStun(2);
            ((Animal)o).setPoisoned(4, owner, damage);
            ((Animal)o).flag_webStuck = true;
            ((Animal)o).webStuckType = this.getSpecies();
            ((Animal)o).setSpeed(2);
            }else{
                ((Animal)o).flag_webStuck = true;
            ((Animal)o).webStuckType = this.getSpecies();
            ((Animal)o).setSpeed(3);
            if(this.owner instanceof BlackwidowSpider){
                
                ((BlackwidowSpider)this.owner).webState = 0;
                
                ((BlackwidowSpider)this.owner).web = null;
            }
            this.room.removeObj(this, o);
                ((BlackwidowSpider)this.owner).disableAbility();

            }
        }
                if(o instanceof Animal && ((Animal)o) == this.owner && this.gotcha != null){
                        if(this.owner instanceof BlackwidowSpider){
                ((BlackwidowSpider)this.owner).webState = 0;
                
                ((BlackwidowSpider)this.owner).web = null;
                ((BlackwidowSpider)this.owner).disableAbility();

            }
                this.room.removeObj(this, o);
            }
    }
    @Override
    public boolean getCollideable(GameObject o){
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()){
            return true;
        }
        return false;

    }
    @Override
    public void update() {

        super.update();
                if(this.gotcha != null && owner != null){
                    double diffX = owner.getX() - getX();
                    double diffY = owner.getY() - getY();
                    
                    double distance = Math.sqrt(diffX * diffX + diffY * diffY);
                    
                    if (distance != 0) {
                        double normalizedDiffX = diffX / distance;
                        double normalizedDiffY = diffY / distance;
                    
                        double velocidad = 12;
                    
                        double webVelocityX = normalizedDiffX * velocidad;
                        double webVelocityY = normalizedDiffY * velocidad;
                    

                        this.setVelocityX(webVelocityX);
                        this.setVelocityY(webVelocityY);
                        gotcha.setX(this.getX());
                        gotcha.setY(getY());
                    }
        }

        if(this.damage > 10) damage--;
        if(this.speed > 0) {
            this.setVelocityX(((Math.cos(Math.toRadians(this.getAngle())) * (this.speed))));
            this.setVelocityY(((Math.sin(Math.toRadians(this.getAngle())) * (this.speed))));
            this.speed--;
        }
         sizeTimer.update(Constants.TICKS_PER_SECOND);
        if(sizeTimer.isDone()){
            if(globalAlpha > 3){
                                sizeTimer.isRunning = false;

                return;
            }
                globalAlpha -= 5;
                sizeTimer.reset();

            }

           

            }
            
    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(1); // web state
        writer.writeUInt8((int) globalAlpha); // transparency 
        writer.writeUInt8(((BlackwidowSpider)this.owner).getInfo().getAnimalSpecies()); // species



        }
    

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
          writer.writeUInt8(1); // web state
        writer.writeUInt8((int) globalAlpha); // web state
        writer.writeUInt8(((BlackwidowSpider)this.owner).getInfo().getAnimalSpecies()); // web state


        }
}
        


