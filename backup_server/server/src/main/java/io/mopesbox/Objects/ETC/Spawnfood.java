package io.mopesbox.Objects.ETC;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier2.DesertChipmunk;

import io.mopesbox.Utils.Timer;
import io.mopesbox.Objects.Eatable.DateFruit;

import io.mopesbox.Objects.Eatable.Water;
import io.mopesbox.Utils.Utils;

import io.mopesbox.World.Room;

import io.mopesbox.Client.*;

public class Spawnfood extends Ability {
    private Room room;
    private Animal owner;
    public Spawnfood(int id, double x, double y, int radius, Room room, Animal owner){
        super(id, x, y, 79, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setCollideable(false);
        
        
        
    }
         int times = 0;

    Timer cooldownTimer = new Timer(0);
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

        if(times < 1){


                        times++;

               for(int i = 0; i < 1; i++) {


                double random = Math.floor(Math.random() * 4);
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
            //double xx = ((Math.cos(Math.toRadians(angle)) * (this.getRadius()+a)));
           //double yy = ((Math.sin(Math.toRadians(angle)) * (this.getRadius()+a)));
         
            
            //room.addObj(mushroom);
           // this.room.addObj(bush);
               // room.addObj(redmushroom);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
                 Water water = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
                 DateFruit dateFruit = new DateFruit(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.room, this.room.desert);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
                    Water water1 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
             DateFruit dateFruit1 = new DateFruit(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.room, this.room.desert);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
                    Water water2 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
             DateFruit dateFruit2 = new DateFruit(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.room, this.room.desert);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
                    Water water3 = new Water(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.getBiome(), this.room);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);
             DateFruit dateFruit3 = new DateFruit(this.room.getID(), this.owner.getX()+xx, this.owner.getY()+yy, this.room, this.room.desert);
             xx += Utils.randomDouble(-8, 8);
yy += Utils.randomDouble(-8, 8);




            
            //int id, double x, double y, int rad, int biome, int foodtype, boolean xmas "tree structure "
            //this.room.getID(), x, y, rad, this.getBiome(), 0, Constants.CHRISTMAS

            room.addObj(water);
                room.addObj(dateFruit);
if (random >= 1) {
                 room.addObj(water1);
                room.addObj(dateFruit1);
}
if (random >= 2) {
                 room.addObj(water2);
                room.addObj(dateFruit2);
}
if (random >= 3) {
                 room.addObj(water3);
                room.addObj(dateFruit3);
}
                           // this.room.addObj(bush1);

            ang += 20;
            if (ang > 360) {
                ang -= 360;
            }
        
            }
        
            this.owner.setMove(true);
            ((DesertChipmunk)this.owner).disableAbility();
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
