package io.mopesbox.Networking;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Objects.Eatable.Egg;
import io.mopesbox.Objects.Juggernaut.Meteor;
import io.mopesbox.Objects.Static.Tree;
import io.mopesbox.Server.FlagWriter;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.PvPRequest;
import io.mopesbox.Utils.RemoveList;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorldUpdate {
    public static MsgWriter create(GameClient client, Room room) {

        if (client.getPlayer() != null) {
            double playerX = client.getPlayer().getX();
            double playerY = client.getPlayer().getY();
            double playerRadius = client.getPlayer().getRadius();
            double playerAngle = client.getPlayer().getAngle();
        
            client.getPlayer().mouth = Utils.rotate(playerX, playerY, playerX - playerRadius, playerY, playerAngle, false);
        
            if(client.getPlayer() != null)client.getPlayer().tails.clear();
        
            for (int i = 0; i < 10; i++) {
                if(client.getPlayer() != null){
                double radiusFactor = i * (playerRadius / 5);
                double tailX = (i < 5) ? playerX + playerRadius - radiusFactor : playerX - playerRadius + radiusFactor;
                client.getPlayer().tails.add(Utils.rotate(playerX, playerY, tailX, playerY, playerAngle, false));
                }
            }
        }
        
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.WORLDUPDATE);

        writer.writeUInt16((short) (client.getCamX() * 4));
        writer.writeUInt16((short) (client.getCamY() * 4));

        writer.writeUInt32(client.getPlayer() != null
                ? client.getPlayer().isInHole() ? ((int) (Math.round(client.getCamzoom() * 1.5))) : client.getCamzoom()
                : client.getCamzoom());

        // flags

        Animal ani = client.getPlayer();
        AnimalInfo info = ani != null ? ani.getInfo() : null;

        io.mopesbox.Utils.Byte flags = new io.mopesbox.Utils.Byte();

        flags.set_bit(0, client.isSpectating());

        flags.set_bit(1, info != null ? info.getAbility() != null ? info.getAbility().isPossible() : false : false); // abil
                                                                                                                     // possible
        flags.set_bit(2, ani != null ? client.getPlayer().isDivePossible() : false);// abil dive possible
        flags.set_bit(3, ani != null ? client.getPlayer().isDiveMain() : false);// abil dive is main
        flags.set_bit(7, ani != null ? client.getPlayer().getBarType() == 1 : false);// is air bar

        // no handling for abilities but should be done!!

        writer.writeUInt8(flags.get());

        if (!client.isSpectating()) {
            // developer info

            writer.writeUInt8(client.isSuperDev() ? 6 : client.isDeveloper() ? 5 : client.isDirector() ? 4 : client.isAdministrator() ? 3 : client.isModerator() ? 2 : client.isHelper() ? 1 : 0);
            if (client.isDeveloper() || client.isHelper() || client.isModerator() || client.isAdministrator() || client.isDirector()) {
                writer.writeBoolean(true); // zoom enabled
            }

            List<GameClient> dotsCopy = new ArrayList<>(room.developerDots);
            List<GameClient> realOne = new ArrayList<>();

            for (GameClient dev : dotsCopy) {
                if (dev.getPlayer() != null && !(dev.dotColor.equals("white")))
                    realOne.add(dev);
            }

            List<GameClient> dotsCopy1 = new ArrayList<>(realOne);

            writer.writeUInt8(dotsCopy1.size()); // admin dots
            for (GameClient dev : dotsCopy1) {
                writer.writeUInt32(dev.getPlayer().getId());
                writer.writeUInt16((short) dev.getPlayer().getX() * 4);
                writer.writeUInt16((short) dev.getPlayer().getY() * 4);
                writer.writeString(dev.dotColor);
            }

            List<GameObject> pumpDots = new ArrayList<>(room.pumpDots);

            writer.writeUInt8(pumpDots.size()); // pump dots

            for (GameObject pump : pumpDots) {
                writer.writeUInt32(pump.getId());
                writer.writeUInt16((short) pump.getX() * 4);
                writer.writeUInt16((short) pump.getY() * 4);
                writer.writeString("orange");
            }
            if (ani != null) {

                // TO:DO add ability handling
                // normal ability
                if (info.getAbility() != null && info.getAbility().isPossible()) {
                    PlayerAbility ability = info.getAbility();
                    FlagWriter flagabil = new FlagWriter();
                    flagabil.writeBool(!ability.isRecharging() ? ability.isUsable() : false);
                    flagabil.writeBool(ability.isRecharging());
                    flagabil.writeBool(ability.isActive());
                    writer.writeFlags(flagabil);
                    writer.writeUInt8(ability.getType());
                    // writer.writeBoolean(ability.isUsable());
                    // writer.writeBoolean(ability.isRecharging());
                    // writer.writeBoolean(ability.isActive());
                }

                // dive ability

                if (ani.isDivePossible()) {
                    FlagWriter flagdive = new FlagWriter();
                    flagdive.writeBool(!ani.isDiveRecharging() ? ani.isDiveUsable() : false);
                    flagdive.writeBool(ani.isDiveRecharging());
                    flagdive.writeBool(ani.isDiveActive());
                    // writer.writeBoolean(ani.isDiveUsable());
                    // writer.writeBoolean(ani.isDiveRecharging());
                    // writer.writeBoolean(ani.isDiveActive());
                    writer.writeFlags(flagdive);
                }

                FlagWriter flag1v1 = new FlagWriter(); // Реано крутое изобретение

                flag1v1.writeBool(client.canPvP());
                flag1v1.writeBool(client.isPvPActive()); // ability 1v1 active?
                flag1v1.writeBool(client.show1v1()); // show 1v1 button
                flag1v1.writeBool(!client.isPvPEnabled()); // disable 1v1 button
                flag1v1.writeBool(client.rechargePvP() > 0); // recharging 1v1 button?
                flag1v1.writeBool(ani.inArena); // in arena?
                flag1v1.writeBool(ani.isInfected()); // is infected?

                writer.writeFlags(flag1v1);

                if (ani.inArena) {
                    // TO:DO write more data here.
                    writer.writeUInt32(client.arenaEnemy != null
                            ? client.arenaEnemy.getPlayer() != null ? client.arenaEnemy.getPlayer().getId() : 0
                            : 0); // arena enemy
                } else {
                    if (client.rechargePvP() > 0) {
                        writer.writeUInt16(client.rechargePvP());
                    }
                    writer.writeUInt8(client.getPvPWins());
                    writer.writeUInt8(client.getRequests().size());
                    if (client.getRequests().size() > 0) {
                        List<PvPRequest> reqCopy = new ArrayList<>(client.getRequests());
                        for (PvPRequest preq : reqCopy) {
                            writer.writeUInt8(preq.getID());
                            writer.writeString(preq.getEnemy() != null
                                    ? preq.getEnemy().getPlayer() != null ? preq.getEnemy().getPlayer().getPlayerName()
                                            : "mope.io"
                                    : "mope.io");
                            writer.writeUInt8(
                                    preq.getEnemy() != null
                                            ? preq.getEnemy().getPlayer() != null
                                                    ? preq.getEnemy().getPlayer().getInfo().getAnimalType()
                                                    : 1
                                            : 1);
                            writer.writeUInt8(preq.getEnemy() != null
                                    ? preq.getEnemy().getPlayer() != null ? preq.getEnemy().getPvPWins() : 0
                                    : 0);
                            if (Constants.GAMEMODE == 3)
                                writer.writeUInt8(preq.getEnemy() != null ? preq.getEnemy().getTeam() : 0); // teamid
                            else
                                writer.writeUInt8(0);
                            writer.writeUInt16((short) preq.getDuration() * 100);
                        }
                    }
                }

                // water bar percent
                writer.writeUInt8(ani.getWater());
                writer.writeUInt8(ani.getBarType()); // bar type;

                // current experience
                writer.writeUInt32(client.getXP());
                double exp1 = ((((double) (client.getXP())) / ((double) (client.getNextXP()))) * 100.0);
                int expper = (int) Math.round(Math.min(Math.max(exp1, 0), 100));
                writer.writeUInt8((int) expper); // experience percent;

                WorldUpdate.writeTreeInfo(writer, ani);
                // WorldUpdate.writeUnderneathAnimalsInfo(writer, ani);

                if (Constants.GAMEMODE == 6) {
                    // pandemic
                    writer.writeUInt8(client.getPlayer().getInfection());
                }

                writer.writeUInt32(client.getCoins()); // coins

                writer.writeString(client.getCoinStatus()); // coins msg

                writer.writeUInt32(client.getEXP()); // exp

                // writer.writeBoolean(false); // in a hole TO:DO finsih this bruh.

                // if(Constants.GAMEMODE == 2) { // battle royale
                // writer.writeUInt16(0); // rank
                // writer.writeUInt16(0); // total alive
                // writer.writeUInt16(0); // total kills
                // writer.writeUInt16(0); // top rank
                // writer.writeUInt32(0); // max xp
                // writer.writeString("aboba"); // killed by
                // }
                if (Constants.GAMEMODE == 3) { // team mode
                    if (!ani.getClient().isBot()) {
                        writer.writeUInt8(ani.getTeam()); // team
                        List<Integer> teamsCount = client.getRoom().checkTeams();
                        writer.writeUInt16(teamsCount.get(0));
                        writer.writeUInt16(teamsCount.get(1));
                        writer.writeUInt16(teamsCount.get(2));
                    }
                }
            }
        }




        List<GameObject> add = new ArrayList<>(client.getAddList());

        writer.writeUInt16((short) add.size());

        add.stream()
            .filter(Objects::nonNull)
            .peek(o -> {                
                WorldUpdate.writeObject(writer, o);
                o.writeCustomData_onAdd(writer);
            }).count();
           
        
        


List<GameObject> update = new ArrayList<>(client.getUpdateList());

writer.writeUInt16((short) update.size());

update.stream()
    .filter(Objects::nonNull)
    .peek(o -> {
        WorldUpdate.writeUpdateObject(writer, o, client);
        o.writeCustomData_onUpdate(writer);
    }).count();



RemoveList remove = client.getRemoveList();
RemoveList remCopyReal = new RemoveList();

for (List<GameObject> o : remove) {
    if (!o.isEmpty()) {
        GameObject oi = o.get(0);
        GameObject oii = (o.size() > 1) ? o.get(1) : null;

        remCopyReal.add(oi, oii);
    }
}

writer.writeUInt16((short) remCopyReal.size());

for (List<GameObject> o : remCopyReal) {
    if (o != null && o.size() > 0) {
        GameObject oi = o.get(0);
        GameObject oii = (o.size() > 1) ? o.get(1) : null;

        writer.writeUInt32(oi != null ? oi.getId() : 0);
        writer.writeUInt8(oii != null ? 1 : 0);

        if (oii != null) {
            writer.writeUInt32(oii.getId());
        }

    }
}
client.clearAddList();
client.clearUpdateList();
client.clearRemoveList();





        

        if (Constants.GAMEMODE == 2) {
            battleRoyale(writer, room);
        } else if (Constants.GAMEMODE == 5) {
            zombieMode(writer, room);
        } else if (Constants.GAMEMODE == 7) {
            juggernautMode(writer, room);
        }
    
        return writer;
    }

    public static void battleRoyale(MsgWriter writer, Room room) {
        writer.writeUInt8(room.br_gamestate); // gamestate
        if (room.br_gamestate == 2) {
            double x = (room.br_handler.getSafeArea().getX()) / (room.getWidth() / 200);
            double y = (room.br_handler.getSafeArea().getY()) / (room.getHeight() / 200);
            writer.writeUInt32((int) x);
            writer.writeUInt32((int) y);
            writer.writeUInt32(room.br_handler.getSafeArea().getRadius() / 5);
        }
    }

    public static void juggernautMode(MsgWriter writer, Room room) {
        ArrayList<Meteor> meteors = room.meteoriteList;
        writer.writeUInt8(meteors.size());
        for (Meteor m : meteors) {
            writer.writeUInt16((short) (m.getRadius() + m.getZ()));
            writer.writeUInt16((short) m.getX() * 4);
            writer.writeUInt16((short) m.getY() * 4);
        }
    }

    public static void zombieMode(MsgWriter writer, Room room) {
        writer.writeUInt16(1); // no zombie
        writer.writeUInt16(0); // zombie count
    }

    public static void writeObject(MsgWriter writer, GameObject o) {
        writer.writeUInt8(o.getType());
        // if ani write type
        if (o instanceof Animal) {
            writer.writeUInt8(((Animal) o).getInfo().getAnimalType());
        }
        if (o instanceof Ability) {
            writer.writeUInt8(((Ability) o).getSecondaryType());
        }

        writer.writeUInt32(o.getId());
        writer.writeUInt32((int) Math.round(o.getRadius() + o.getZ()));
        writer.writeUInt16((short) (o.getX() * 4));
        writer.writeUInt16((short) (o.getY() * 4));

        writer.writeUInt8(o.getBiome());

        // if (!(o instanceof Animal)) {
        // writer.writeUInt8(o.getSpecies());
        // writer.writeUInt8(o.getSpeciesSub());
        // }

        io.mopesbox.Utils.Byte oFlags = new io.mopesbox.Utils.Byte();

        oFlags.set_bit(0, o.isSpawnedFromID());
        oFlags.set_bit(1, o.isRectangle());
        oFlags.set_bit(2, o.isSendsAngle());

        writer.writeUInt8(oFlags.get());
        if (o.isSpawnedFromID())
            writer.writeUInt32(o.getSpawnID());

        if (o.isRectangle()) {
            Rectangle rect = (Rectangle) o;
            writer.writeUInt16((short) rect.getWidth());
            writer.writeUInt16((short) rect.getHeight());

        }

        if (o.isSendsAngle()) {
            writer.writeUInt16((short) o.getAngle());
        }

    }

    public static void writeTreeInfo(MsgWriter writer, Animal ani) {
        // TO:DO finish this.

        // is player under tree
        List<Tree> info = new ArrayList<>(ani.getUnderTree());
        writer.writeBoolean(info.size() > 0);
        if (info.size() > 0) {
            writer.writeUInt8(info.size());
            for (Tree a : info) {
                writer.writeUInt32(a.getId());
            }
        }
    }

    public static void writeUnderneathAnimalsInfo(MsgWriter writer, Animal ani) {
        // TO:DO finish this.

        // is player underneath animal?
        writer.writeBoolean(false);
    }

    public static void writeUpdateObject(MsgWriter writer, GameObject o, GameClient client) {
        writer.writeUInt32(o.getId());

        writer.writeUInt16((short) (o.getX() * 4));
        writer.writeUInt16((short) (o.getY() * 4));
        writer.writeUInt32((int) Math.round(o.getRadius() + o.getZ()));

        writer.writeUInt8(o.getSpecies());

        if (o.getType() != 2 && o.getType() != 14) {
            writer.writeUInt8(o.getSecondaryType2()); // spectype2
            if ((o instanceof Egg && client.getPlayer() == null)
                    || (client.getPlayer() != null && o instanceof Egg && ((Egg) o).isBrEgg
                            && (((Egg) o).getOwner() == client.getPlayer()
                                    || (client.getAccount() != null && client.getAccount().admin >= 2))))
                writer.writeBoolean(true); // the br egg is flying to be transparent! for only current client.
            else
                writer.writeBoolean(o.getObjectFlying()); // flag_flying
        }

        io.mopesbox.Utils.Byte flags = new io.mopesbox.Utils.Byte();

        flags.set_bit(0, o.showHP); // show hp
        flags.set_bit(1, o.isHurted); // flash
        writer.writeUInt8(flags.get());

        if (o.showHP) {
            writer.writeUInt8((int) ((((double) o.health) / ((double) o.maxHealth)) * 100.0));
        }

        if (o.isSendsAngle()) {
            writer.writeUInt16((short) o.getAngle());
        }
    }
}