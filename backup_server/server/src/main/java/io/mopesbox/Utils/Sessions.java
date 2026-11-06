package io.mopesbox.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Client.GameClient;

public class Sessions {
     public ConcurrentHashMap<String, GameClient> savedClients = new ConcurrentHashMap<>();

    public Sessions() {
        System.out.print("Started Sessions System!\n");
    }

    public ConcurrentHashMap<String, GameClient> getSessions() {
        return savedClients;
    }

    public void saveSession(GameClient a) {
        if (a.saveSession) {
            this.savedClients.put(a.session, a);
        }
    }

    public void removeSession(GameClient a, boolean expiredTime) {
        if(expiredTime){
            if(a.player != null){
            Main.instance.room.removeObj(a.player, null);
            a.player = null;
            }

        }
        this.savedClients.remove(a.session);
    }

    public void update() {
        for (Map.Entry<String, GameClient> entry : savedClients.entrySet()) {
            GameClient o = entry.getValue();
            o.removeSession.update(Constants.TICKS_PER_SECOND);
            if (o.removeSession.isDone()) {
                this.savedClients.remove(o.session);

            }
        }

    }


}
