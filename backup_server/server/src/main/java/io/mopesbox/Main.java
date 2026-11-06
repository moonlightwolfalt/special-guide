package io.mopesbox;

import io.mopesbox.Client.GameClient;

import io.mopesbox.Networking.Networking;
import io.mopesbox.Server.GameServer;
import io.mopesbox.Utils.GUIThread;
import io.mopesbox.Utils.Sessions;
import io.mopesbox.World.Room;
import io.mopesbox.Utils.IPCheck;

public class Main {
    // public static final Logger log = LoggerFactory.getLogger("SERVER");
    public GUIThread guiThread;
    public static Main instance;
    public Room room;
    public GameServer server;
    public Sessions sessionManager = new Sessions();
    public IPCheck ipChecker = new IPCheck();

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        // System.setProperties()
        Main.instance = new Main();
        Main.instance.run();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // pre close we notify every client about close.
                Room room = Main.instance.room;
                if (Constants.GAMEMODE == 2 && room.br_gamestate == 2) {
                    // give coins to best player before closing
                    GameClient winner = null;
                    for (GameClient c : room.clients) {
                        if (c.getPlayer() != null && !c.getPlayer().isGhost && (winner == null || c.getXP() > winner.getXP())) {
                            winner = c;
                        }
                    }
                    if(winner != null) winner.addCoins(Constants.BR_COINSAFTERWIN);
                }
                for (GameClient client : room.clients) {
                    client.send(Networking.popup("SERVER RESTARTING!", "cantafford", 10));
                    client.updateCoins(client.getCoins());
                }
                // then we save coins so everyone stops crying.

            }
        });
    }

    public void run() {
        this.server = new GameServer(Constants.GAMEPORT);
        this.room = new Room(this);
        this.guiThread = new GUIThread(this);

        this.guiThread.start(); // start gui

        this.room.start(); // start room

        this.server.start(); // start websocket server

    }
    // finish giving out stuff on close so players wont lose coins.

}
