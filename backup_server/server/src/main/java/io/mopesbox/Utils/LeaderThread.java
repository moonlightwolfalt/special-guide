package io.mopesbox.Utils;
import io.mopesbox.Constants;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.World.Room;

public class LeaderThread extends Thread {
    private Room room;
    private Timer timerz = new Timer(2000);
    // public final Logger log;

    public LeaderThread(Room room) {
        this.room = room;
        // this.log = Main.log;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.update();
                Thread.sleep(this.room.tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        timerz.update(Constants.TICKS_PER_SECOND);
        if (timerz.isDone()) {
            for (GameClient a : this.room.clients) {
                a.send(Networking.leaderBoardPacket(this.room, a));
                a.send(Networking.playerCountPacket(this.room));
                if (Constants.GAMEMODE == 2) {
                    // battle royale
                    if (this.room.br_gamestate != 4) {
                        a.send(Networking.br_playerCount(this.room, this.room.getAlivePlayers(),
                                this.room.br_gamestate == 2));
                    }
                    a.send(Networking.br_gameState(this.room, this.room.getAlivePlayers()));
                }
            }
            timerz.reset();
        }
    }
}