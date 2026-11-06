package io.mopesbox.Networking;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.AnimalSkins;
import io.mopesbox.Animals.Info.Rares;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Selection {
    static void shuffleArray(AnimalInfo[] ar) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            AnimalInfo a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static MsgWriter createSelection(GameClient client, int msgKind, int timeout, int tier, List<AnimalInfo> animalInfoList){
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.GAMESELECTANIMAL);

        writer.writeUInt8(msgKind);
        // msg kind 5 is close
        if(msgKind != 5) {
            client.setInUpgrade(true);
            client.setTier(tier);
            client.setUpgradeTimer(timeout);

            // writer.writeUInt8(1); // net
            writer.writeUInt8(timeout);
            // writer.writeUInt8(tier); // net
            List<AnimalInfo> rares = Rares.byTier(tier, client.rareHackEnabled());
            if(rares.size() > 0) {
                List<AnimalInfo> todel = new ArrayList<>();
                for(AnimalInfo inf : animalInfoList) {
                    for(AnimalInfo inf2 : rares) {
                        if(inf.getAnimalType() == inf2.getAnimalType()) todel.add(inf);
                    }
                }
                animalInfoList.removeAll(todel);
                animalInfoList.addAll(rares);
            }
            if(client.getAccount() != null) {
                List<String> skins = client.getAccount().getSkins();
                if(skins != null && skins.size() > 0) {
                    List<AnimalInfo> animalInfoList1 = new ArrayList<>();
                    for(String skin : skins) {
                        for(AnimalInfo info : animalInfoList){
                            if(AnimalSkins.byName(skin) != null && info.getType() == AnimalSkins.byName(skin).getType() && !animalInfoList1.contains(AnimalSkins.byName(skin))) animalInfoList1.add(AnimalSkins.byName(skin));
                        }
                        if(AnimalSkins.byName(skin) != null && AnimalSkins.byName(skin).isAni && AnimalSkins.byName(skin).getTier() == tier && !animalInfoList1.contains(AnimalSkins.byName(skin))) animalInfoList1.add(AnimalSkins.byName(skin));
                    }
                    animalInfoList.addAll(animalInfoList1);
                }
            }
            AnimalInfo[] animalInfoListFinish = animalInfoList.toArray(new AnimalInfo[animalInfoList.size()]);
            Arrays.sort(animalInfoListFinish, new Comparator<AnimalInfo>() {
                public int compare(AnimalInfo o1, AnimalInfo o2) {
                    return Integer.compare(client.getPlayer() != null ? client.getPlayer().getBiome() : o1.getBiome(), o2.getBiome());
                }
            });
            client.setSelectionList(Arrays.asList(animalInfoListFinish));
            writer.writeUInt8(animalInfoListFinish.length);

            if(Constants.SHUFFLEMODE) Selection.shuffleArray(animalInfoListFinish);

            for(AnimalInfo info : animalInfoListFinish){
                writer.writeUInt8(Constants.SHUFFLEMODE ? 0 : info.getAnimalType());
                writer.writeUInt8(Constants.SHUFFLEMODE ? 0 : info.getBiome());
                writer.writeUInt8(Constants.SHUFFLEMODE ? 0 : info.getAnimalSpecies());
                writer.writeUInt8(Constants.SHUFFLEMODE ? 0 : info.getAnimalSubSpecies()); //subspecies
                writer.writeUInt8(info.getAnimalSpecies() > 0 && !info.isRare() ? 1 : 0);
            }
        } else {
            client.setInUpgrade(false);
        }
       return writer;
    }
}
