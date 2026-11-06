package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class DeathLake extends Lake
{
    // private Room room;
    public DeathLake(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, room);
        // this.room = room;
        this.isDeath = true;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(lakeSize);
    }

    public List<GameObject> hills = new ArrayList<>();
    private Timer stageTimer = new Timer(10);
    private int stage = 0;
    private int staged = 0;

    public void spawnHills(){
                int ang = Utils.randomInt(0, 360);
        for(int i = 0; i < 15; i++) {
            double a = Utils.randomDouble(0, 20);
            double xx = ((Math.cos(Math.toRadians(ang)) * (this.getRadius()+a)));
            double yy = ((Math.sin(Math.toRadians(ang)) * (this.getRadius()+a)));
            Hill hill = new Hill(room.getID(), this.getX()+xx, this.getY()+yy, Utils.randomInt(60, 80), 0);
            room.addObj(hill);
            hills.add(hill);
            ang += 20;
            if (ang > 360) {
                ang -= 360;
            }
        }
    }

    @Override
    public void update() {
        super.update();
        stageTimer.update(Constants.TICKS_PER_SECOND);
        if(stageTimer.isDone()) {
            if(stage == 0) {
                staged++;
                staged++;

                if(staged <= 50) {
                lakeSize++;
            }
                else lakeSize = staged;
                if(staged >= 1000) {
                    stage = 1;
                    staged = 0;
                }
            } else if(stage == 1) {
                staged++;
                staged++;

                poison = staged;
                if(poison < 0) poison = 0;
                if(poison > 1000) poison = 1000;
                if(staged >= 1000) {
                    stage = 2;
                }
            } else if(stage == 2) {
                staged--;
                staged--;

                poison = staged;
                if(poison < 0) poison = 0;
                if(poison > 1000) poison = 1000;
                if(staged <= 0) {
                    stage = 3;
                    staged = lakeSize;
                }
            } else if(stage == 3) {
                staged--;
                staged--;

                if(staged <= 0) stage = 0;
                if(staged <= 50) lakeSize = 50;
                else lakeSize = staged;
            }
            stageTimer.reset();
        }
        if(lakeSize <= 0) lakeSize += 5;
        this.setCustomCollisionRadius(300/(1000/lakeSize));
    }

    @Override
    public void onCollision(GameObject object) {
        if(object instanceof Animal && !((Animal)object).inArena && !((Animal)object).isRammed && !((Animal)object).flag_flying) {
            object.setBiome(1);
            ((Animal)object).ignoreBiomes();
            int dmg = ((int) Math.round(((Animal)object).maxHealth / 6));

            while (dmg >= object.maxHealth) dmg /= 2;

           Animal ani = ((Animal) object);

            if (this.poison > 150 && !((Animal)object).flag_eff_poison && (ani.getInfo().getType() != AnimalType.GIANTSCORPION && ani.getInfo().getType() != AnimalType.GIANTSPIDER && ani.getInfo().getType() != AnimalType.PTERODACTYL)) ((Animal)object).setPoisoned(3, null, dmg);
        }
    }

    private int lakeSize = 0;
    private int poison = 0;

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt16(lakeSize);
        writer.writeUInt16(poison);
    }
}
