package io.mopesbox.Objects.Biome;

import io.mopesbox.Constants;
import io.mopesbox.Objects.ETC.HeatWave;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Desert extends Biome{
    public Desert(int id,  double x, double y, double width, double height, Room room) {
        super(id, 79, x, y, width, height, BiomeType.DESERT.ordinal(), room);
    }

    private Timer heatWaveTimer = new Timer(5000);

    @Override
    public void update() {
        super.update();
        heatWaveTimer.update(Constants.TICKS_PER_SECOND);
        if(heatWaveTimer.isDone()) {
            int sang = -30;
            double minX = (this.getX() - this.getWidth() / 2);
            double minY = (this.getY() - this.getHeight() / 2);

            double maxX = (minX + this.getWidth()) - 60;
            double maxY = (minY + this.getHeight()) - 60;
            
            double x = Utils.randomDouble(minX+60,maxX-60);
            double y = Utils.randomDouble(minY+60,maxY-60);
            double angle = Utils.randomInt(0, 360);
            for(int i = 0; i < 3; i++) {
                double wx = x+((Math.cos(Math.toRadians(angle+sang)) * (60)));
                double wy = y+((Math.sin(Math.toRadians(angle+sang)) * (60)));
                HeatWave wave = new HeatWave(this.room.getID(), wx, wy, 60, angle+(sang/4), 4, this.getId(), this.room, null);
                sang += 30;
                this.room.addObj(wave);
            }
            heatWaveTimer.reset();
        }
    }
}
