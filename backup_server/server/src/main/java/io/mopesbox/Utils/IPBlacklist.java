package io.mopesbox.Utils;

import java.util.LinkedHashMap;
import java.util.Map;


import io.mopesbox.Constants;
import io.mopesbox.Client.GameClient;
import io.mopesbox.World.Room;


public class IPBlacklist {
    private Timer timer = new Timer(60000);
    private LinkedHashMap<String, Integer> blacklisted = new LinkedHashMap<String, Integer>();
    private Room room;
    // public final Logger log;

    public IPBlacklist(Room room) {
        this.room = room;
        // this.log = Main.log;
    }

    public void update() {
        timer.update(Constants.TICKS_PER_SECOND);
        if (timer.isDone()) {
            for (Map.Entry<String, Integer> a : blacklisted.entrySet()) {
                String ip = a.getKey();
                int time = a.getValue();
                if(time > 1) a.setValue(time--);
                else {
                    blacklisted.remove(ip);
                }
            }
            timer.reset();
        }
        for(GameClient a: this.room.clients){
            for (Map.Entry<String, Integer> e : blacklisted.entrySet()) {
                String ip = e.getKey();
                if(a.ip.equals(ip)){
                    if(a.socket.isOpen())
                a.sendDisconnect(false, "MOPERR_691", false);
                }
            }
        // for(GameClient a : this.room)
    }
}

    public void addIP(String ip, int time) {
        blacklisted.put(ip, time);
        // StringBuilder strBuilder = new StringBuilder("");
        // strBuilder.append("New blocked IP: ").append(ip).append(" | Time: ").append(time).append(" minutes");
        // Main.log.info(strBuilder.toString());
    }

    
    public void removeIP(String ip) {
        blacklisted.remove(ip);
        // StringBuilder strBuilder = new StringBuilder("");
        // strBuilder.append("New blocked IP: ").append(ip).append(" | Time: ").append(time).append(" minutes");
        // Main.log.info(strBuilder.toString());
    }
    public boolean checkIP(String ip) {
        if(blacklisted.containsKey(ip)) {
            return true;
        } else {
            return false;
        }
    }
 
}