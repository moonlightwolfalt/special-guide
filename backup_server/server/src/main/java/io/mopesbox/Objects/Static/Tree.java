package io.mopesbox.Objects.Static;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;

public class Tree extends GameObject
{
    public int foodtype = 0;
    private boolean xmas = false;
    private int lights = 0;
    public int foods = 0;
    public Tree(int id, double x, double y, int rad, int biome, int foodtype, boolean xmas) {
        super(id, x, y, rad, 101);
        this.foodtype = foodtype;
        this.xmas = xmas;
        this.setBiome(biome);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(rad/3.5);
        this.setTreeCollide(true);
    }

    private Timer lightTimer = new Timer(400);
    private int stage = 0;
    private int staged = 0;

    @Override
    public void update() {
        super.update();
        if(this.xmas) {
            lightTimer.update(Constants.TICKS_PER_SECOND);
            if(lightTimer.isDone()) {
                if(stage == 0) {
                    if(lights == 2) lights = 1;
                    else lights = 2;
                    staged++;
                } else if(stage == 1) {
                    if(lights == 0) lights = 3;
                    else lights = 0;
                    staged++;
                }
                if(stage == 0 && staged >= 5) {
                    stage = 1;
                    staged = 0;
                }
                if(stage == 1 && staged >= 5) {
                    stage = 0;
                    staged = 0;
                }
                lightTimer.reset();
            }
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            if(!((Animal)o).getCanBeUnderTree()) return false;
        }
        return true;
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        super.writeCustomData_onAdd(writer);
        writer.writeUInt16(foodtype);
        writer.writeBoolean(xmas);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer){
        super.writeCustomData_onUpdate(writer);
        if(this.xmas) writer.writeUInt8(lights);
    }
}
