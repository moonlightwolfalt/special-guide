package io.mopesbox.Objects.Static;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;

public class Hill extends GameObject
{
    public Hill(int id, double x, double y, int rad, int biome) {
        super(id, x, y, rad, 3);
        this.setBiome(biome);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(rad * 1.1);
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            Animal a = (Animal)o;
            if(a.getFlyingOver()) return false;
            if(a.getCanClimbHills()) return false;
            if(a.isDiveActive()) return false;
            return true;
        }
        return true;
    }
}
