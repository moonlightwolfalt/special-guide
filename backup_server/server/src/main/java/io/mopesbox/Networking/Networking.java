package io.mopesbox.Networking;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier15.Santa;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.World.Room;

import static io.mopesbox.Constants.MAX_PLAYERS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Networking {
    public static MsgWriter sendJoinPacket(Room room, GameClient player, boolean isSpectator, boolean reconnect) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.REQUESTJOIN);
        if (room.getPlayers() > MAX_PLAYERS) {
            writer.writeUInt8(0);
            writer.writeString("Server is full!");
            writer.writeUInt8(isSpectator ? 1 : 0);
            // writer.writeUInt8(reconnect ? 1 : 0);
            player.sendDisconnect(false, "Server is full!", false);
        } else {
            writer.writeUInt8(1);
            writer.writeString("Success");
            writer.writeUInt8(isSpectator ? 1 : 0);
            // writer.writeUInt8(reconnect ? 1 : 0);

        }
        return writer;
    }

    public static MsgWriter br_playerCount(Room room, int players, boolean isAlive) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(82); // type
        writer.writeUInt16(1); // players
        writer.writeString(isAlive ? "alive" : "joined");
        return writer;
    }

    public static MsgWriter captcha() {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.RECAPTCHA);
        return writer;
    }

    public static MsgWriter popup(String msg, String type, int time) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.POPUPMSG);
        writer.writeString(msg);
        writer.writeString(type);
        writer.writeUInt8(time);
        return writer;
    }

    public static MsgWriter br_gameState(Room room, int players) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMEMODEHANDLE);
        writer.writeUInt8(41); // type
        writer.writeUInt8(room.br_gamestate); // state
        writer.writeUInt16(players); // players joined
        return writer;
    }

    public static MsgWriter leaderBoardPacket(Room room, GameClient player) {
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
            if (pl == player)
                a = b;
        }
        int ab = Math.min(plrs.size(), 10);
        plrs = plrs.subList(0, ab);
        if (a > ab && a != -1) {
            plrs.remove(9);
            plrs.add(player);
        }
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMESCOREUPDATE);
        writer.writeUInt16(a); // own rank
        int ownrank = a + 1;
        ownrank--;
        writer.writeUInt8(plrs.size()); // players in lb
        a = 0;

        if (player.stat != null) {
            if (player.stat.rank > player.stat.topRank)
                player.stat.topRank = (short) ownrank;
            player.stat.rank = (short) ownrank;

            if (player.stat.maxXP < player.getXP())
                player.stat.maxXP = player.getXP();

        }
        for (GameClient pl : plrs) {
            a++;
            writer.writeUInt16(pl == player ? ownrank : a);
            String name = "mope.io";
            if (pl.getPlayer() != null)
                name = pl.getPlayer().getPlayerName();
            writer.writeString(name);
            writer.writeUInt32(pl.getXP());
            writer.writeString("white");
        }
        room.topplayer = plrs.size() > 0 ? plrs.get(0) : null;
        return writer;
    }

    public static MsgWriter personalGameEvent(int type, String msg) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.PERSONALGAMEEVENT);
        writer.writeUInt8(type);
        if (type == 255) {
            writer.writeString(msg != null ? msg : "message");
        }
        return writer;
    }

    public static MsgWriter promptReason(String msg) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.PROMPT);
        writer.writeString(msg);
        return writer;
    }

    public static MsgWriter customInterface(Room room, BlackDragon ani, int type) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.CUSTOMINTERFACE);
        writer.writeUInt8(type);
        if (type == 1) {
            writer.writeUInt8(15);
            writer.writeBoolean(ani.checkApex(46)); // bd
            writer.writeBoolean(ani.checkApex(71)); // land
            writer.writeBoolean(ani.checkApex(73)); // dino
            writer.writeBoolean(ani.checkApex(70)); // sea
            writer.writeBoolean(ani.checkApex(72)); // ice
            writer.writeBoolean(ani.checkApex(95)); // scorp
            writer.writeBoolean(ani.checkApex(68)); // phoenix
            writer.writeBoolean(ani.checkApex(14)); // dragon
            writer.writeBoolean(ani.checkApex(53)); // trex
            writer.writeBoolean(ani.checkApex(24)); // kraken
            writer.writeBoolean(ani.checkApex(61)); // king crab
            writer.writeBoolean(ani.checkApex(32)); // yeti
            writer.writeBoolean(ani.checkApex(96)); // pterodactyl
        }
        return writer;
    }

    public static MsgWriter customInterface(Room room, Santa ani, int type) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.CUSTOMINTERFACE);
        writer.writeUInt8(type);
        if (type == 2) {
            writer.writeUInt8(15);
            writer.writeUInt8(0);
            writer.writeUInt8(1);
            writer.writeUInt8(2);

        }
        return writer;
    }

    public static MsgWriter playerCountPacket(Room room) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.SERVERSTATSUPDATE);
        writer.writeUInt16((short) room.getPlayers());
        return writer;
    }

    public static MsgWriter changeAnimal(Animal animal, int type) {

        if (animal.getClient().falconTarget != null) {
            animal.getRoom().removeObj(animal.getClient().falconTarget, null);
            animal.getClient().falconTarget = null;
        }
        if (animal.getClient().ostrichTarget != null) {
            animal.getRoom().removeObj(animal.getClient().ostrichTarget, null);
            animal.getClient().ostrichTarget = null;

        }
        if (animal.getClient().markhorTarget != null) {
            animal.getRoom().removeObj(animal.getClient().markhorTarget, null);
            animal.getClient().markhorTarget = null;

        }
        if (animal.getClient().snowyowlTarget != null) {
            animal.getRoom().removeObj(animal.getClient().snowyowlTarget, null);
            animal.getClient().snowyowlTarget = null;

        }
        // if(animal instanceof Pelican && ((Pelican)animal).inClaws != null){
        //     double x = ((Math.cos(Math.toRadians(((Pelican)animal).getAngle())) * (32 /4)));
        //     double y = ((Math.sin(Math.toRadians(((Pelican)animal).getAngle())) * (32 / 4)));
        //     ((Pelican)animal).inClaws.makeFall(x, y, null);
        // }

        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.ANIMALCHANGE);
        writer.writeUInt8(animal.getInfo().getAnimalType());
        writer.writeUInt8(animal.getInfo().getAnimalSpecies());
        writer.writeUInt8(type);

        // writer.writeUInt8(0); // tier
        writer.writeUInt32(animal.getId());
        writer.writeUInt32(animal.getClient().getNextXP()); // max xp

        // eddible types
        ArrayList<AnimalInfo> dangerTypes = new ArrayList<>();
        ArrayList<AnimalInfo> edibleTypes = new ArrayList<>();
        ArrayList<AnimalInfo> tailbiteTypes = new ArrayList<>();
        int currentTier = animal.getInfo().getTier();

        // int maxTierDanger = (currentTier + currentTier * 2 + 3);
        // int minTierEdible = (currentTier - (currentTier / 2 - 3));

        animal.dangerTypes.clear();
        animal.edibleTypes.clear();
        // switch(currentTier){
        // case 1:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 5 && t.getTier() > currentTier){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // }
        // break;
        // case 2:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 7 && t.getTier() > currentTier && t.getTier() >= 3){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 2){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 3:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 9 && t.getTier() > currentTier && t.getTier() > 3){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 3){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;

        // case 4:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 11 && t.getTier() > currentTier){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 4){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 5:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 13 && t.getTier() > currentTier && t.getTier() >= 4){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 5){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 6:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 15 && t.getTier() > currentTier && t.getTier() >= 7){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 6 && t.getTier() > 1){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 7:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 8){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 7 && t.getTier() > 1){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 8:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 9){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 6 && t.getTier() > 2){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 9:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 10){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 9 && t.getTier() > 2){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 10:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 11){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 9 && t.getTier() > 3){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 11:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 12){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 10 && t.getTier() > 3){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 12:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 13){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 12 && t.getTier() > 4){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 13:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 14){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 13 && t.getTier() > 4){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 14:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 15){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 14 && t.getTier() > 5){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 15:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 16){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 15 && t.getTier() > 5){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 16:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 17){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 16 && t.getTier() > 6){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // case 17:
        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() <= 17 && t.getTier() > currentTier && t.getTier() >= 17){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // if(t.getTier() < currentTier && t.getTier() < 16 && t.getTier() > 6){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }
        // }
        // break;
        // }

        for (AnimalInfo t : Animals.getAllAnimals()) {
            if (t.isDanger(animal.getInfo())) {
                dangerTypes.add(t);
            }
        }

        for (AnimalInfo t : Animals.getAllAnimals()) {
            if (t.isEdible(animal))
                edibleTypes.add(t);
        }
        for (AnimalInfo info : dangerTypes) {
            if (info.isBiteable(animal))
                tailbiteTypes.add(info);
        }

        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() > currentTier && t.getTier() <= maxTierDanger){
        // dangerTypes.add(t);
        // animal.dangerTypes.add(t.getType());

        // }
        // }

        // for (AnimalInfo t : Animals.getAllAnimals()) {
        // if (t.getTier() < currentTier && t.getTier() >= minTierEdible){
        // edibleTypes.add(t);
        // animal.edibleTypes.add(t.getType());
        // }

        // }

        for (AnimalInfo info : dangerTypes) {
            if (info.getTier() <= 2)
                continue;
            if (info.getAnimalType() == 17)
                continue;
            tailbiteTypes.add(info);
        }

        for (Tier t : Tier.values()) {
            if (t.getValue() == currentTier)
                tailbiteTypes.addAll(t.getAnimalInfo());
        }
        writer.writeUInt8(dangerTypes.size()); // dangerAniTypes
        for (AnimalInfo info : dangerTypes) {
            writer.writeUInt8(info.getAnimalType());
        }
        writer.writeUInt8(edibleTypes.size()); // edible
        for (AnimalInfo info : edibleTypes) {
            writer.writeUInt8(info.getAnimalType());
        }

        writer.writeUInt8(tailbiteTypes.size()); // tailbite
        for (AnimalInfo info : tailbiteTypes) {
            writer.writeUInt8(info.getAnimalType());
        }

        int edibleObjTypes = 0;
        if (animal.getInfo().getTier() > 0) {
            edibleObjTypes += 2;
        }
        if (animal.getInfo().getTier() > 0 && !(animal instanceof Bee)) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 3 && !(animal instanceof Ostrich)) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 3) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 6) {
            edibleObjTypes += 6;
        }
        if (animal.getInfo().getTier() >= 4) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 9) {
            edibleObjTypes += 3;
        }
        if (animal.getInfo().getTier() >= 8) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 2) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() <= 8) {
            edibleObjTypes += 2;
        }
        if (animal.getInfo().getTier() >= 11) {
            edibleObjTypes += 4;
        }
        if (animal.getInfo().getTier() >= 12) {
            edibleObjTypes += 1;
        }
        if (animal.getBarType() == 0) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 3 && animal.getInfo().getTier() <= 9) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 1 && animal.getInfo().getTier() <= 9) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 7) {
            edibleObjTypes += 3;
        }
        if (animal.getInfo().getTier() >= 5) {
            edibleObjTypes += 2;
        }
        if (animal.getInfo().getTier() >= 3) {
            edibleObjTypes += 1;
        }
        if (animal.getInfo().getTier() >= 10) {
            edibleObjTypes += 1;
        }
        writer.writeUInt8(edibleObjTypes); // edibleObjTypes
        if (edibleObjTypes > 0) {
            if (animal.getInfo().getTier() > 0) {
                writer.writeUInt8(52);
                writer.writeUInt8(97);
            }
            if (animal.getInfo().getTier() > 0 && !(animal instanceof Bee)) {
                writer.writeUInt8(69);
            }
            if (animal.getInfo().getTier() >= 6) {
                writer.writeUInt8(53);
                writer.writeUInt8(51);
                writer.writeUInt8(24);
                writer.writeUInt8(37);
                writer.writeUInt8(36);
                writer.writeUInt8(35);
            }
            if (animal.getInfo().getTier() >= 3 && !(animal instanceof Ostrich)) {
                writer.writeUInt8(66);
            }
            if (animal.getInfo().getTier() >= 3) {
                writer.writeUInt8(63);
            }
            if (animal.getInfo().getTier() >= 5) {
                writer.writeUInt8(23);
                writer.writeUInt8(60);
            }
            if (animal.getInfo().getTier() >= 4) {
                writer.writeUInt8(22);
            }
            if (animal.getInfo().getTier() >= 9) {
                writer.writeUInt8(29);
                writer.writeUInt8(30);
                writer.writeUInt8(39);
            }
            if (animal.getInfo().getTier() >= 8) {
                writer.writeUInt8(95);
            }
            if (animal.getInfo().getTier() >= 2) {
                writer.writeUInt8(49);
            }
            if (animal.getInfo().getTier() >= 3) {
                writer.writeUInt8(31);
            }
            if (animal.getInfo().getTier() >= 7) {
                writer.writeUInt8(25);
                writer.writeUInt8(32);
                writer.writeUInt8(48);
            }
            if (animal.getInfo().getTier() >= 12) {
                writer.writeUInt8(82);
            }
            if (animal.getInfo().getTier() >= 10) {
                writer.writeUInt8(68);
            }
            if (animal.getInfo().getTier() >= 1 && animal.getInfo().getTier() <= 9) {
                writer.writeUInt8(90);
            }
            if (animal.getInfo().getTier() >= 3 && animal.getInfo().getTier() <= 9) {
                writer.writeUInt8(93);
            }
            if (animal.getInfo().getTier() <= 8) {
                writer.writeUInt8(20);
                writer.writeUInt8(26);
            }
            if (animal.getInfo().getTier() >= 11) {
                writer.writeUInt8(54);
                writer.writeUInt8(50);
                writer.writeUInt8(96);
                writer.writeUInt8(38);
            }
            if (animal.getBarType() == 0) {
                writer.writeUInt8(21);
            }
        }
        return writer;
    }

    public static MsgWriter rechargingAbility(boolean isDive, int time, Room room) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.PLAYERABILITY);
        writer.writeUInt8(isDive ? 1 : 0);
        writer.writeUInt16((int) time * (10));
        return writer;
    }

    public static MsgWriter deathPacket(int reason, String coins, double newxp) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.YOUDIED);
        writer.writeUInt8(reason);
        writer.writeUInt32((int) newxp);
        writer.writeString(coins);
        return writer;
    }

}
