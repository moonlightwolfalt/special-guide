package io.mopesbox.Objects.Biome;

import io.mopesbox.Utils.BiomeType;
import io.mopesbox.World.Room;

// Outer space: the strip added along the right edge of the map (Constants.SPACEW).
// The client draws it as a starfield (object type 160, o_biome_space). For gameplay it
// counts as land, so animals keep the land rules (water, home biome) while there.
public class Space extends Biome {
    public static final int OBJECT_TYPE = 160;

    public Space(int id, double x, double y, double width, double height, Room room) {
        super(id, OBJECT_TYPE, x, y, width, height, BiomeType.LAND.ordinal(), room);
    }
}
