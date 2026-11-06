package io.mopesbox.Objects.Biome;

import java.util.ArrayList;

import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.World.Room;

public class Land extends Biome {
    public Land(int id, double x, double y, double width, double height, Room room) {
        super(id, 1, x, y, width, height, BiomeType.LAND.ordinal(), room);
    }
    public River top;
    public River bottom;

    public ArrayList<GameObject> lakes = new ArrayList<>();
    @Override
    public void spawn() {
        super.spawn();
        top = new River(this.room.getID(), this.getX(), (Constants.HEIGHT/2) - (this.getHeight()/4) + 120, this.getWidth(), 240.0, false, room);
         bottom = new River(this.room.getID(), this.getX(), (Constants.HEIGHT/2) + (this.getHeight()/4) + 120, this.getWidth(), 260.0, true, room);
        this.room.addObj(top);
        this.room.addObj(bottom);
    }
}
