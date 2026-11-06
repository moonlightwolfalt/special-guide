package io.mopesbox.Objects.ETC;


import io.mopesbox.Objects.Static.Hole;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Constants;
import io.mopesbox.Main;
public class AbilityHole extends Hole {
    private Timer dissapearTimer;
    public AbilityHole(int id, double x, double y, int radius,boolean isWater, int biome, double time) {
        super(id, x, y, 0, isWater, biome);
        this.setRadius(radius);
        dissapearTimer = new Timer(90000);

        
        
        
    }

    @Override
    public void update(){
        super.update();
        dissapearTimer.update(Constants.TICKS_PER_SECOND);
        if(dissapearTimer.isDone()){
            if(this.players.size() <= 0)
                Main.instance.room.removeObj(this,null);
                
        }
    }

  
}
