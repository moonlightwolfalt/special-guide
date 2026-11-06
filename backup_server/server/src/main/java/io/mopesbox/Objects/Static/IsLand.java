package io.mopesbox.Objects.Static;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;
import io.mopesbox.Constants;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Utils;

public class IsLand extends GameObject
{
    public IsLand(int id, double x, double y, int radius, Room room,int biome) {
        super(id, x, y, radius, 11);
        this.setBiome(BiomeType.OCEAN.ordinal());
        int ang = Utils.randomInt(0, 360);
        for(int i = 0; i < 1; i++) {
            double a = Utils.randomDouble(0, 1);
            int rad = Utils.randomInt(80, 90);

            double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius()+a)));
            double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius()+a)));
            Tree tree = new Tree(room.getID(), this.getX()+xx, this.getY()+yy, rad,this.getBiome(), 29, Constants.CHRISTMAS);
            //int id, double x, double y, int rad, int biome, int foodtype, boolean xmas "tree structure "
            //this.room.getID(), x, y, rad, this.getBiome(), 0, Constants.CHRISTMAS
            room.addObj(tree);
            ang += 150;
            if (ang > 360) {
                ang -= 360;
            }
        }
    }

    @Override
    public void onCollision(GameObject object) {
        super.onCollision(object);
        if(object instanceof Animal && !(((Animal)object).isIslandCollided) && !((Animal)object).inArena && !((Animal)object).isRammed && !((Animal)object).flag_flying) {
            object.setBiome(0);
            ((Animal)object).ignoreBiomes();
        }
    }
    @Override
    public boolean getCollideable(GameObject o) {
        return false;
    }
}
