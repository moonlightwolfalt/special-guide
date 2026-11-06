package io.mopesbox.Objects.Static;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Client.AI.AIDuck;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.World.Room;

public class LakeIsland extends GameObject {
    public List<AIDuck> ducks = new ArrayList<>();
    public int duckCount = 2;
    public Room room;
    public Lake lake;

    public LakeIsland(int id, double x, double y, int rad, Room room, int duckCount, Lake lake) {
        super(id, x, y, rad, 11);
        this.duckCount = duckCount;
        this.lake = lake;
        this.room = room;
        for (int i = 0; i < duckCount; i++) {
            AIDuck duck = new AIDuck(room, this);
            ducks.add(duck);
            room.addAI(duck);
        }
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public boolean getCollideable(GameObject o) {
        return false;
    }
}
