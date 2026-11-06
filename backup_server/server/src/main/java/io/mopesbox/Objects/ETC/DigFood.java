package io.mopesbox.Objects.ETC;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier5.Deer;
import io.mopesbox.Animals.Tier5.Reindeer;

import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.Eatable.Mushroom;
import io.mopesbox.Objects.Eatable.RedMushroom;
import io.mopesbox.Objects.Eatable.Water;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Objects.Eatable.Berry;

import io.mopesbox.World.Room;
import io.mopesbox.Client.*;

public class DigFood extends Ability {
    private Room room;
    private Animal owner;
    public DigFood(int id, double x, double y, int radius, Room room, Animal owner){
        super(id, x, y, 25, radius, 0);
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

               for(int i = 0; i < 2; i++) {


            double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius()+a)));
            double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius()+a)));
            Mushroom mushroom = new Mushroom(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room, this.room.land);
            RedMushroom redmushroom = new RedMushroom(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room, this.room.land);

            if(this.owner.flag_eff_isInMud){
                Mushroom mushroom1 = new Mushroom(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room, this.room.land);
            RedMushroom redmushroom1 = new RedMushroom(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room, this.room.land);
            this.room.addObj(mushroom1);
            this.room.addObj(redmushroom1);
            }




            
         
            room.addObj(mushroom);
                room.addObj(redmushroom);

                 Water water = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Berry berry = new Berry(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water1 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Berry berry1 = new Berry(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water2 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Berry berry2 = new Berry(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
                    Water water3 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             Berry berry3 = new Berry(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);




            
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

            ang += 60;
            if (ang > 360) {
                ang -= 360;
            }
        
            }
        
        cooldownTimer.reset();
        }else{
            this.owner.setMove(true);
            if(this.owner instanceof Deer)((Deer)this.owner).disableAbility();
            if(this.owner instanceof Reindeer)((Reindeer)this.owner).disableAbility();

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
