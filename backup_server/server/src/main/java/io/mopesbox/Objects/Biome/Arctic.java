package io.mopesbox.Objects.Biome;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Objects.Arctic.ArcticIce;
import io.mopesbox.Objects.ETC.ColdWave;
import io.mopesbox.Objects.Static.Lake;
import io.mopesbox.Objects.Static.Volcano;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Arctic extends Biome {
    public Arctic(int id, double x, double y, double width, double height, Room room) {
        super(id, 16, x, y, width, height, BiomeType.ARCTIC.ordinal(), room);
    }

    public Volcano volcano;
    public List<ArcticIce> ices = new ArrayList<>();
    public List<Lake> lakes = new ArrayList<>();

    public void spawnArcticIce(int maps) {
       
            for (int i = 0; i < 2; i++) {
                
            double x = 0, y = 0;
            if(maps == 1){
            if(i == 0){
             x = 2550;
             y = 648;
            }
            if(i == 1){
                 x = 7742;
                 y = 670;
            }
    
                
            }else if(maps == 2){
            if(i == 0){
             x =  1430;
             y = 702;
            }
            if(i == 1){
                 x = 5224;
                 y = 911;
            }
            }else if(maps == 3){
            if(i == 0){
             x = 7553;
             y = 479;
            }
            if(i == 1){
                 x = 1016 ;
                 y = 495;
            }
  
        }

                int rad = 280;

                                ArcticIce ice = new ArcticIce(this.room.getID(), x, y, rad);
              

                this.room.addObj(ice);
                ices.add(ice);
            }
    }

    public void spawnVolcano() {

        this.volcano = new Volcano(this.room.getID(), this.getX(), this.getY(), this.room, BiomeType.ARCTIC.ordinal());
        this.room.addObj(this.volcano);
    }


    private Timer coldWaveTimer = new Timer(5000);

    @Override
    public void update() {
        super.update();
        coldWaveTimer.update(Constants.TICKS_PER_SECOND);
        if (coldWaveTimer.isDone()) {
            int sang = -30;
            double minX = (this.getX() - this.getWidth() / 2);
            double minY = (this.getY() - this.getHeight() / 2);

            double maxX = (minX + this.getWidth()) - 60;
            double maxY = (minY + this.getHeight()) - 60;

            double x = Utils.randomDouble(minX + 60, maxX - 60);
            double y = Utils.randomDouble(minY + 60, maxY - 60);
            double angle = Utils.randomInt(0, 360);
            for (int i = 0; i < 3; i++) {
                double wx = x + ((Math.cos(Math.toRadians(angle + sang)) * (60)));
                double wy = y + ((Math.sin(Math.toRadians(angle + sang)) * (60)));
                ColdWave wave = new ColdWave(this.room.getID(), wx, wy, 60, angle + (sang / 4), 4, this.getId(),
                        this.room, null);
                sang += 30;
                this.room.addObj(wave);
            }
            coldWaveTimer.reset();
        }
    }
}
