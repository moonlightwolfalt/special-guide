package io.mopesbox.Objects.Static;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;

public class Rock extends GameObject
{
    public Rock(int id, double x, double y, int rad, int biome) {
        super(id, x, y, rad, 8);
        this.setBiome(biome);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(rad * 1.2);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            Animal a = (Animal)o;
            if(a.getFlyingOver()) return false;
            if(a.getCanClimbRocks()) return false;
            return true;
        }
        return true;
    }
}
