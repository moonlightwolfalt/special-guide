package io.mopesbox.Objects.ETC;



import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier5.Flamingo;

import io.mopesbox.Utils.Timer;

import io.mopesbox.Objects.Eatable.Plankton;
import io.mopesbox.Objects.Eatable.Water;
import io.mopesbox.Utils.Utils;

import io.mopesbox.World.Room;

import io.mopesbox.Client.*;

public class findFood extends Ability {
    private Room room;
    private Animal owner;
    public findFood(int id, double x, double y, int radius, Room room, Animal owner){
        super(id, x, y, 72, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setCollideable(false);
        
        
        
    }
         int times = 0;

    Timer cooldownTimer = new Timer(2000);
                int ang = Utils.randomInt(0, 360);

                            double a = Utils.randomDouble(0, 20);


    @Override
    public void update(){
    super.update();

    cooldownTimer.update(Constants.TICKS_PER_SECOND);

    if(!this.owner.getInfo().getAbility().isActive()){
        this.owner.setMove(true);
        this.room.removeObj(this, null);

        return;

    }
    if(cooldownTimer.isDone() ){

        if(times < 3){


                        times++;

               for(int i = 0; i < 8; i++) {

                this.owner.setMove(false);


            double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius()+a)));
            double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius()+a)));
            Plankton mushroom = new Plankton(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
            Water redmushroom = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);






            
         
            room.addObj(mushroom);
            
            room.addObj(redmushroom);

                 Water water = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Plankton berry = new Plankton(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water1 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Plankton berry1 = new Plankton(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water2 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Plankton berry2 = new Plankton(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water3 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Plankton berry3 = new Plankton(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);




            
            //int id, double x, double y, int rad, int biome, int foodtype, boolean xmas "tree structure "
            //this.room.getID(), x, y, rad, this.getBiome(), 0, Constants.CHRISTMAS
            room.addObj(water);
                room.addObj(berry);

                 room.addObj(water1);
                room.addObj(berry1);

                 room.addObj(water2);
                room.addObj(berry2);
                 room.addObj(water3);
                room.addObj(berry3);

            ang += 20;
            if (ang > 360) {
                ang -= 360;
            }
        
            }
        
        cooldownTimer.reset();
        }else{
            this.owner.setMove(true);
            ((Flamingo)this.owner).disableAbility();
            this.owner.getInfo().getAbility().setActive(false);
            this.room.removeObj(this, this.owner);
        }
    }

    }

    @Override
    public boolean canBeVisible(GameClient a){
        return false;
    }

   

    
}
