package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;

import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class FlyTrap extends GameObject {

    private Room room;

    private List<FlyTrapMouth> mouths = new ArrayList<>();

    public FlyTrap(int id, double x, double y, Room room) {
        super(id, x, y, 35, 87);
        this.setCollideCallbacking(true);
        this.setCollideable(true);
        this.setBiome(BiomeType.DESERT.ordinal());
        this.room = room;
        this.init();
    }

    public void init() {
        int count = Utils.randomInt(3, 5);
        int angleStep = 360 / count;
        int startAngle = Utils.randomInt(0, 360);
        for (int i = 0; i < count; i++) {
            int angg = startAngle + (angleStep * i);
            double rd = Utils.randomDouble(1.5, 2);
            double x = ((Math.cos(Math.toRadians(angg)) * (this.getRadius() * rd)));
            double y = ((Math.sin(Math.toRadians(angg)) * (this.getRadius() * rd)));
            FlyTrapMouth mouth = new FlyTrapMouth(this.room.getID(), this.getX() + x, this.getY() + y, room, this);
            mouth.setAngle(angg);
            mouths.add(mouth);
            this.room.addObj(mouth);
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal && ((Animal) o).getInfo().getTier() >= 15) {
            return false;
        }
        return true;
    }
    @Override 
    public void onCollision(GameObject o){
        if(getCollideable(o)){
            for(FlyTrapMouth head : this.mouths){
                head.onCollision(o);
            }
        }
    }

    @Override
    public void update() {
        super.update();
    }
    @Override
    public boolean canBeVisible(GameClient a){
        return true;
    }
}
