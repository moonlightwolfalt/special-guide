package io.mopesbox.Objects.Biome;

import io.mopesbox.Constants;
import io.mopesbox.Objects.ETC.Wave;
import io.mopesbox.Objects.Static.Hole;
import io.mopesbox.Objects.Static.IsLand;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Ocean extends Biome{
    public Ocean(int id,  double x, double y, double width, double height, Room room) {
        super(id, 12, x, y, width, height, BiomeType.OCEAN.ordinal(), room);
    }

    @Override
    public void spawnHoles(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 200;

        for(int i = 0;i < amount;i++){
            int type = 1;
            int rad = 30;
            double x = Utils.randomDouble(minX+rad,maxX-rad);
            double y = Utils.randomDouble(minY+rad,maxY-rad);
            Hole hole = new Hole(this.room.getID(), x, y, type, true, 1);
            this.room.addObj(hole);
        }
    }


    @Override
    public void spawnIsLand(int amount) {
        double minX = (this.getX() - this.getWidth() / 2);
        double minY = (this.getY() - this.getHeight() / 2);

        double maxX = (minX + this.getWidth()) - 20;
        double maxY = (minY + this.getHeight()) - 20;

        for (int i = 0; i < amount; i++) {
            int rad = 100;
            double x = Utils.randomDouble(minX + rad, maxX - rad);
            double y = Utils.randomDouble(minY + rad, maxY - rad);
            IsLand island = new IsLand(this.room.getID(), x, y, rad, room, 1);
            this.room.addObj(island);
        }
    }





    private Timer oceanWaveTimer = new Timer(5000);

    @Override
    public void update() {
        super.update();
        oceanWaveTimer.update(Constants.TICKS_PER_SECOND);
        if(oceanWaveTimer.isDone()) {
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
                Wave wave = new Wave(this.room.getID(), wx, wy, 60, angle+(sang/4), 4, this.getId(), this.room, null);
                sang += 30;
                this.room.addObj(wave);
            }
            oceanWaveTimer.reset();
        }
    }
}
