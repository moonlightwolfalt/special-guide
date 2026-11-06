package io.mopesbox.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.BattleRoyale.SafeArea;
import io.mopesbox.Objects.Juggernaut.Meteor;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;

public class BattleRoyaleHandler {

    public Room room;

    public boolean canStart = false;
    public boolean started = false;
    public SafeArea area;
    public boolean safePeriod = false;
    public boolean restartSafe = false;
    public boolean shrinkDone = true;
    public boolean ending = false;

    public int totalPlayers = 0;
    ArrayList<GameClient> deadPlayers = new ArrayList<>();

    public BattleRoyaleHandler(Room room) {
        this.room = room;

        this.beginTimer.isRunning = false;
        this.shrinkTimer.isRunning = false;
        this.safePeriodTimer.isRunning = false;
    }

    public Timer forceStartTimer = new Timer(600000);
    public Timer aliveTimer = new Timer(1000);
    public Timer meteorTimer = new Timer(120000);
    //  public Timer meteorTimer = new Timer(20000);

    public Timer restartTime = new Timer(30000);
    public Timer waitEndTimer = new Timer(15000);
    public Timer closeTimer = new Timer(1800000);

    public GameClient endWinner = null;

    public void update() {

        for (GameClient d : deadPlayers) {
            if (d == null)
                deadPlayers.remove(d);
        }
        for (GameClient d : deadPlayers) {
            if (d.getPlayer() != null && !d.getPlayer().isGhost)
                d.getPlayer().isGhost = true;
        }

        aliveTimer.update(Constants.TICKS_PER_SECOND);

        if (aliveTimer.isDone()) {
            for (GameClient c : room.clients) {
                if (c.getPlayer() != null && !c.getPlayer().isGhost)
                    c.stat.timeAlive++;
            }
            aliveTimer.reset();
        }

        if (room.getAlivePlayers() >= Constants.MIN_START_PLAYER && canStart == false && !ending) {
            this.beginTimer.reset();
            canStart = true;
            room.br_gamestate = 1;
        }
        if (room.getAlivePlayers() < Constants.MIN_START_PLAYER && canStart == true && !started && !ending) {
            canStart = false;
            this.beginTimer.isRunning = false;
            room.br_gamestate = 0;
        }

        if (ending) {
            waitEndTimer.update(Constants.TICKS_PER_SECOND);
            if (waitEndTimer.isDone()) {
                restartTime.update(Constants.TICKS_PER_SECOND);
                updateRoomRestart();
                if (restartTime.isDone())
                    room.resetRoom();
            }
        }

        closeTimer.update(Constants.TICKS_PER_SECOND);
        if (closeTimer.tim <= 60000) {
            updateClosetime();

            started = false;
            canStart = false;
            ending = true;
            this.room.br_gamestate = 4;
            GameClient winner = endWinner;
            if (winner == null) {
                for (GameClient c : room.clients) {
                    if (c.getPlayer() != null && !c.getPlayer().isGhost) {
                        winner = c;
                        break;
                    }
                }
                if(winner != null) winner.addCoins(Constants.BR_COINSAFTERWIN);
            }

            ArrayList<GameClient> players = new ArrayList<>(deadPlayers);
            players.add(winner);

            for (GameClient client : players) {
                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.GAMEMODEHANDLE);
                writer.writeUInt8(42);
                writer.writeBoolean(client == winner);
                if(client != null)client.send(writer);
            }

            sendEnd();

        }

        if (canStart && started == false && !ending) {
            beginTimer.update(Constants.TICKS_PER_SECOND);
            updateTime();
            if (beginTimer.isDone()) {
                started = true;
                room.br_gamestate = 2;
                totalPlayers = this.room.getAlivePlayers();
                shrinkTimer.reset();
                for (GameClient client : this.room.clients) {
                    if (client.getPlayer() != null) {
                        if (client.getPlayer().inEgg) {
                            this.room.removeObj(client.getPlayer().egg, client.getPlayer());
                            client.getPlayer().inEgg = false;
                            client.getPlayer().egg = null;
                        }
                    }
                }

                spawnArea();
            }
        }

        safePeriodTimer.update(Constants.TICKS_PER_SECOND);

        if (started && area != null && !area.isShrinking() && safePeriodTimer.isDone()) {
            shrinkTimer.update(Constants.TICKS_PER_SECOND);
            updateExpandTime();
            meteorTimer.update(Constants.TICKS_PER_SECOND);
            if(meteorTimer.isDone()){
                for(int index = 0; index < 20; index++){
                    Meteor meteor = new Meteor(this.room.getID(), Utils.randomDouble(100, Constants.WIDTH), Utils.randomDouble(100, Constants.HEIGHT), 50, Utils.randomDouble(100, Constants.WIDTH), Utils.randomDouble(100, Constants.HEIGHT), room, false);
                    this.room.addObj(meteor);
                
                }
                    for(GameClient client : this.room.clients){
                        client.send(Networking.popup("Big rocks are passing through the atmosphere!", "cantafford", 5));
                    }
                    meteorTimer.reset();
            }

            if (shrinkTimer.isDone()) {
                if (shrinkDone) {
                    area.setShrinking(true);
                    shrinkTimer.reset();

                    this.notifyArea();
                }

            }
        }

        if (started && !ending && room.getAlivePlayers() <= 1) {
            started = false;
            canStart = false;
            ending = true;
            this.room.br_gamestate = 4;
            GameClient winner = endWinner;
            if (winner == null) {
                for (GameClient c : room.clients) {
                    if (c.getPlayer() != null && !c.getPlayer().isGhost) {
                        winner = c;
                        break;
                    }
                }
                if(winner != null) winner.addCoins(Constants.BR_COINSAFTERWIN);
            }

            ArrayList<GameClient> players = new ArrayList<>(deadPlayers);
            players.add(winner);

            for (GameClient client : players) {
                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.GAMEMODEHANDLE);
                writer.writeUInt8(42);
                writer.writeBoolean(client == winner);
                client.send(writer);
            }

            sendEnd();
        }

    }

    public void sendEnd() {

        for (GameClient client : this.room.clients) {
            if(client.getPlayer() != null){
            if (client.getPlayer().isGhost) {
                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.GAMEMODEHANDLE);
                writer.writeUInt8(48); // endScreen;

                this.writeStat(writer, client);

                client.send(writer);
            }
        }
        }
    }

    public void writeStat(MsgWriter writer, GameClient client) {

        writer.writeUInt16(totalPlayers);
        writer.writeUInt16(client.stat.rank);
        writer.writeUInt16(client.stat.timeAlive);
        writer.writeUInt16(client.stat.totalKills);
        writer.writeUInt16(client.stat.topRank);
        writer.writeUInt32(client.stat.maxXP);
    }

    public void sendRank(GameClient player) {
        List<GameClient> plrs = new ArrayList<>();
        for (GameObject object : room.objects) {
            if (object instanceof Animal) {
                if (((Animal) object).getClient() != null && !((Animal) object).getClient().isBot()
                        && !((Animal) object).isGhost)
                    plrs.add(((Animal) object).getClient());
            }
        }
        Collections.sort(plrs, new Comparator<GameClient>() {
            @Override
            public int compare(GameClient o1, GameClient o2) {
                return Integer.valueOf(o2.getXP()).compareTo(o1.getXP());
            }
        });
        int a = -1;
        int b = 0;
        for (GameClient pl : plrs) {
            b++;
            if (pl == player) {
                a = b;
                break;
            }
        }
        if(!player.isBot()){

        player.stat.rank = (short) (a + 1);

        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(33); // eliminated!
        writer.writeUInt16(a + 1);
        player.send(writer);
        }

    }

    int shrinks = 0;

    public void onShrinkDone() {
        safePeriodTimer.setMaximumTime((int) (safePeriodTimer.getMaximumTime() / 1.5));
        if (safePeriodTimer.getMaximumTime() < 20000)
            safePeriodTimer.setMaximumTime(20000);

        shrinkTimer.setMaximumTime((int) (shrinkTimer.getMaximumTime() / 1.5));
        if (shrinkTimer.getMaximumTime() < 20000)
            shrinkTimer.setMaximumTime(20000);
        safePeriodTimer.reset();

        shrinks++;
        if (shrinks % 3 == 0)
            area.speed += 1;
        if (area.getRadius() > 1500)
            area.addShrink(area.getRadius() + 800);
        else if (area.getRadius() < 200)
            area.addShrink(area.getRadius());
        else
            area.addShrink(area.getRadius() + 400);
        shrinkDone = true;
    }

    public void spawnArea() {
        double ang = Math.random() * 2;
        double x = (this.room.getWidth() / 2) + (Math.cos(ang) * 2000);
        double y = (this.room.getHeight() / 2) + (Math.sin(ang) * 2000);
        SafeArea area = new SafeArea(this.room.getID(), x, y, 5000, this.room);
        this.area = area;
        this.room.addObj(area);
        this.room.safeArea = area;
    }

    public SafeArea getSafeArea() {
        return area;
    }

    public Timer beginTimer = new Timer(120000);
    public Timer shrinkTimer = new Timer(120000);
    public Timer safePeriodTimer = new Timer(60000);

    public void updateRoomRestart() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(37); // restart time!
        writer.writeUInt16((short) Math.round(restartTime.tim / 1000));
        for (GameClient client : this.room.clients) {

            client.send(writer);
        }
    }

    public void updateClosetime() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(46); // restart time!
        writer.writeUInt16((short) Math.round(closeTimer.tim / 1000));
        for (GameClient client : this.room.clients) {

            client.send(writer);
        }
    }

    public void updateExpandTime() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(39); // shrink time
        writer.writeUInt16((short) Math.round(shrinkTimer.tim / 1000));
        for (GameClient client : this.room.clients) {

            client.send(writer);
        }
    }

    public void updateTime() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(37); // time remain msg;
        writer.writeUInt16((short) Math.round(beginTimer.tim / 1000));
        for (GameClient client : this.room.clients) {

            client.send(writer);
        }
    }

    public void notifyDeath(GameClient client) {

        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(44); // death notify
        writer.writeString(client.getPlayer().getPlayerName());
        this.sendRank(client);
        this.notifySelfDeath(client);
        for (GameClient c : this.room.clients) {
            if (c.getPlayer() != null && !c.isBot())
                c.send(writer);
        }
    }

    public void notifyArea() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(40); // death notify

        for (GameClient c : this.room.clients) {
            if (c.getPlayer() != null && !c.getPlayer().isGhost)
                c.send(writer);
        }
    }

    public void notifySelfDeath(GameClient client) {
        if(!client.isBot()){
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(40); // death notify
        client.send(writer);
        deadPlayers.add(client);
        }

    }

}
