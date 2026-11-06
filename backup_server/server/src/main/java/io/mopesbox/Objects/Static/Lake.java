package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.LilyPad;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Lake extends GameObject
{
    public Room room;
    protected boolean isDeath = false;
    public Lake(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 10);
        this.room = room;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    public void spawnIslands() {
        int islands = Utils.randomInt(1, 2);
        double xmin = this.getX() + 100 - this.getRadius();
        double xmax = this.getX() - 100 + this.getRadius();
        double ymin = this.getY() + 100 - this.getRadius();
        double ymax = this.getY() - 100 + this.getRadius();
        for (int i = 0; i < islands; i++) {
            LakeIsland island = new LakeIsland(room.getID(), Utils.randomDouble(xmin, xmax),
                    Utils.randomDouble(ymin, ymax), 100, this.room, islands == 1 ? 6 : 3, this);
            room.addObj(island);
        }
    }

    private Timer lilyTimer = new Timer(5000);

    @Override
    public void update() {

        super.update();
        if(!this.isDeath) {
            lilyTimer.update(Constants.TICKS_PER_SECOND);
            if(lilyTimer.isDone() && Constants.FOODSPAWN) {
                double x = ((Math.cos(Math.toRadians(Utils.randomInt(0, 360))) * (Utils.randomInt(14, this.getRadius()))));
                double y = ((Math.sin(Math.toRadians(Utils.randomInt(0, 360))) * (Utils.randomInt(14, this.getRadius()))));
                LilyPad lily = new LilyPad(this.room.getID(), this.getX()+x, this.getY()+y, 1, room);
                this.room.addObj(lily);
                lilyTimer.reset();
            }
        }
    }

    @Override
    public void onCollision(GameObject object) {
        if(object instanceof Animal && !(((Animal)object).isIslandCollided) && !((Animal)object).inArena && !((Animal)object).isRammed && !((Animal)object).flag_flying) {
            object.setBiome(1);
            ((Animal)object).ignoreBiomes();
        }
    }

    protected int lakeStream = 0;

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(isDeath); // is death lake
        if(isDeath) writer.writeUInt16(lakeStream);
    }
}
