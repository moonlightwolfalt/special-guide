package io.mopesbox.Utils;


import io.mopesbox.Client.GameClient;

public class Apex {
    private int type = 0;
    private GameClient gid = null;
    public Apex(int type, GameClient gid) {
        this.type = type;
        this.gid = gid;
    }
    public int getType() {
        return type;
    }
    public GameClient getClient() {
        return gid;
    }
}
