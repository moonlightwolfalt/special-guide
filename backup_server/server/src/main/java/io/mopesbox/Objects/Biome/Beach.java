package io.mopesbox.Objects.Biome;

import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Utils.ObjectTypes;
import io.mopesbox.World.Room;

public class Beach extends Rectangle {


    public Beach(int id, double x, double y, double width, double height, Room room, int direction) {
        super(id, x, y, width, height, ObjectTypes.Beach.getType());
    }

    public void generate(){

    }
}
