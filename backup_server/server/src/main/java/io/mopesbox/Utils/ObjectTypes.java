package io.mopesbox.Utils;

public enum ObjectTypes {
    Beach(33);
    private int type;
        ObjectTypes(int type){

            this.type = type;
        }

    public int getType() {
        return type;
    }
}
