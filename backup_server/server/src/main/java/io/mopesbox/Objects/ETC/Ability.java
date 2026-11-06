package io.mopesbox.Objects.ETC;

import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;

public class Ability extends GameObject {

    public int is1v1Target;
    public int secondaryType;

    public Ability(int id, double x, double y, int type, int radius, int is1v1Target) {
        super(id, x, y, radius, 14);
        this.specType = 0;
        this.specType2 = 0;
        this.secondaryType = type;
        this.is1v1Target = is1v1Target;
        this.setSpecies(0);
    }

    public int getSecondaryType() {
        return secondaryType;
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        writer.writeUInt8(this.specType);
        writer.writeUInt8(this.specType2);
        writer.writeUInt8(this.is1v1Target);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer){
        writer.writeUInt8(this.specType);
        writer.writeUInt8(this.specType2);
        writer.writeUInt8(this.getSpecies());
    }
}
