package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;

public class Hole extends GameObject {
    public List<Animal> players = new ArrayList<>();
    public Hole(int id, double x, double y, int type, boolean isWater, int biome) {
        super(id, x, y, type == 0 && !isWater ? 20 : 30, isWater ? 13 : type == 0 ? 5 : 9);
        this.setCollideable(false);
        this.setBiome(biome);
        this.setCollideCallbacking(true);
        
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        if(o instanceof Animal && o.getCollideable(this) && ((Animal) o).canBeInHole() && (this.getRadius() == 30 || ((Animal) o).canBeInSmallHole()) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
            if(!players.contains((Animal)o)) players.add((Animal)o);
            ((Animal)o).setHole(this);
        }
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && o.getCollideable(this) && ((Animal) o).canBeInHole() && (this.getRadius() == 30 || ((Animal) o).canBeInSmallHole()) && !((Animal) o).flag_flying && (((Animal) o).getInfo().getAbility() == null || !((Animal) o).getInfo().getAbility().isActive())) {
            if(!players.contains((Animal)o)) players.add((Animal)o);
            ((Animal)o).setHole(this);
        } else if(o instanceof Animal && o.getCollideable(this) && ((Animal) o).flag_flying && ((Animal)o).isInHole()) {
            if(players.contains((Animal)o)) players.remove((Animal)o);
            ((Animal)o).setHole(null);
        }
    }

    @Override
    public void onCollisionExit(GameObject o) {
        if(o instanceof Animal && ((Animal)o).isInHole()) {
            if(players.contains((Animal)o)) players.remove((Animal)o);
            ((Animal)o).setHole(null);
        }
    }
}
