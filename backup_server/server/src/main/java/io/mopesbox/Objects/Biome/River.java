package io.mopesbox.Objects.Biome;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.World.Room;

import java.util.Map;


public class River extends Biome {
    private final boolean leftSide;
    private double size = 1;
    private int streamHeight = 200;

    public River(int id, double x, double y, double width, double height, boolean leftSide, Room room) {
        super(id, 40, x, y, width, height, BiomeType.OCEAN.ordinal(), room);
        this.leftSide = leftSide;
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        // yes this is all
        writer.writeBoolean(this.leftSide);
        writer.writeUInt16((short) this.streamHeight);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writer.writeUInt16((short) (size * 1000));
    }


    @Override
    public void update(){
        for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
            GameObject object = entry.getValue();
            if(object == null)
            continue;

            
            if(!(object instanceof Animal)){
                if(!(object instanceof PvPArena)){

                       object.addVelocityX(0.8 * (this.leftSide ? -1 : 1));


                }
      
            }
                      if(object instanceof Animal){
                    if((((Animal)object).inArena)){

                    }else if(!(((Animal)object).flag_fliesLikeDragon) && !(((Animal)object).flag_flying) && !((Animal)object).flag_eff_isOnSpiderWeb && !(((Animal)object).flag_isClimbingHill)){
                    
                    object.addVelocityX(1.2 * (this.leftSide ? -1 : 1));
                    }

                    }
                
                    
                }
            
            // if(!(object instanceof Animal && object instanceof PvPArena) || (object instanceof Animal  && !(((Animal)object).inArena) && (((Animal)object).flag_fliesLikeDragon == false))) 
            
            // object.addVelocityX(0.8 * (this.leftSide ? -1 : 1));
        
    }
}
