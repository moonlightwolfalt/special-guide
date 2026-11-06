package io.mopesbox.Utils;

import io.mopesbox.Client.GameClient;

public class PvPRequest {
    private GameClient enemy;
    private int duration;
    private int id;
    public PvPRequest(GameClient enemy, int duration, int id) {
        this.enemy = enemy;
        this.id = id;
        this.duration = duration;
    }
    public GameClient getEnemy() {
        return this.enemy;
    }
    public int getDuration() {
        return this.duration;
    }
    public void updateDuration() {
        this.duration--;
    }
    public int getID() {
        return id;
    }
}
