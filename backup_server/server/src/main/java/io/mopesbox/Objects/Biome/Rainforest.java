package io.mopesbox.Objects.Biome;

import io.mopesbox.Utils.BiomeType;
import io.mopesbox.World.Room;

public class Rainforest extends Biome{
    public Rainforest(int id, double x, double y, double width, double height, Room room) {
        super(id, 118, x, y, width, height, BiomeType.FOREST.ordinal(), room);
    }
}
