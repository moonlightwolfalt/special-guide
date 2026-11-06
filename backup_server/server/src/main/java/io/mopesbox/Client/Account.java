package io.mopesbox.Client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Account {
    private boolean success = true;
    public int id;
    public String persistentName;
    public String email;
    public String social;
    public String email_isVerified;
    public String name;
    public String profilePicUrl;
    public String locale;
    public String password_token;
    public String expirydate;
    public int discordAdmin;
    public String discordID;
    public String discordTag;
    public String discordCode;
    public int coins;
    public int warns;
    public int youtube;
    public int artist;
    public int level;
    public int admin;
    public int adminType;
    public int limited;
    public int xpNextLvl;
    public int banned;
    public int gold_blackDragon;
    public int gold_kingDragon;
    public int gold_Dragon;
    public int gold_kraken;
    public int gold_yeti;
    public int gold_trex;
    public int gold_kingCrab;
    public int gold_phoenix;
    public int gold_pterodactyl;
    public int gold_iceMonster;
    public int gold_dinoMonster;
    public int gold_seaMonster;
    public int gold_landMonster;
    public int gold_giantScorion;
    public int gold_bluewhale;
    public int gold_elephant;
    public int gold_cassowary;
    public int gold_giantspider;
    public int gold_blackwidow;
    public int gold_mammoth;
    public int gold_hippo;
    public int gold_ostrich;
    public int gold_boa;
    public int gold_komodo;
    public int gold_killerwhale;
    public int gold_sabertooth;
    public int skin_orangeScorpion;
    public int skin_blueDragon;
    public int skin_kingRipper;
    public int skin_kingStan;
    public int skin_kingShah;
    public int skin_dragon_gray;
    public int skin_kraken_cyan;
    public int skin_yeti_emerald;
    public int skin_snowman;
    public int skin_trex_gray;
    public int skin_kingCrab;
    public int skin_phoenix_blue;
    public int skin_pterodactyl_green;
    public int skin_lavaStrider;
    public int skin_boneKing;
    public int skin_dragon_terminator;
    public int skin_dragon_red;
    public int skin_iceMonster;
    public int skin_dinoMonster;
    public int skin_seaMonster;
    public int skin_landMonster;
    public int skin_bluewhale;
    public int skin_elephant;
    public int skin_cassowary;
    public int skin_giantspider;
    public int skin_blackwidow;
    public int skin_mammoth;
    public int skin_hippo;
    public int skin_ostrich;
    public int skin_boa;
    public int skin_komodo;
    public int skin_killerwhale;
    public int skin_sabertooth;
    public int skin_tigerShark;
    public int skin_hamerHeadShark;
    public int skin_panda;
    public int skin_zonkey;
    public int skin_okapi;
    public int skin_giantCroc;
    public int skin_purpleMouse;
    public int skin_starMouse;
    public int skin_winterMouse;
    public int skin_prismaToucan;
    public int gold_mouse;
    public int skin_queenScarlet;
    public int skin_queenCeleste;
    public int skin_bigFoot;
    public int skin_kingMelon;
    public int skin_woollyRhino;
    public int skin_queenRipper;
    public int skin_kingInferno;
    public int skin_sapphireMouse;
    public int skin_amethystMouse;
    public int skin_emeraldMouse;
    public int skin_diamondMouse;
    public int skin_lordofice;
    public int skin_retrocrocodilealligator;
    public int skin_partycrock;
    public int skin_rubblealligator;
    public int skin_rubyblackdragon;
    public int skin_kingGolder;
    // csdfdf
    public int skin_devilMouse;
    public int skin_angelMouse;
    public int skin_cheeseYsMouse;
    public int skin_rizurrar;
    public int skin_snowyFalcon;
    public int skin_mouseElemental;
    public int skin_drillMole;

    //ghfdjghjdfhskg
    public int skin_banditMouse;
    public int skin_chocolatebirthdaycakeMouse;
    public int skin_strawberrybirthdaycakeMouse;
    public int skin_chocolateDragon;
    public int skin_neonDragon;
    public int skin_galaxyPhoenix;
    public int skin_meltedMonster;
    public int skin_giftboxKraken;


    // case 'skin_retrocrocodilealligator':
    //     return [_0x17b991, 0, `Retro Crocodile\n Alligator`, 'land/croc/4/croc'];
    // case 'skin_partycrock':
    //     return [_0x17b991, 0, `Party\n C'rock`, 'land/croc/5/croc'];
    // case 'skin_rubblealligator':
    //     return [_0x17b991, 0, `Rubble,\n Alligator`, 'land/croc/6/croc'];
    // case 'skin_rubyblackdragon':
    //      return [_0x1da4d1, 0, `The\nRuby Black Dragon`, 'volcano/blackdragon/7/blackdragon'];
    // case 'skin_kingGolder':
    //     return [_0x4e99c5, 0, `King\nGolder`, 'volcano/kingdragon/12/kingdragon'];

    public boolean isSuccess() {
        return success;
    }

    public List<String> getSkins() {
        try {
            Map<String, Integer> values = new HashMap<String, Integer>();
            values.put("gold_blackDragon", this.gold_blackDragon);
            values.put("gold_kingDragon", this.gold_kingDragon);
            values.put("gold_Dragon", this.gold_Dragon);
            values.put("gold_kraken", this.gold_kraken);
            values.put("gold_yeti", this.gold_yeti);
            values.put("gold_trex", this.gold_trex);
            values.put("gold_kingCrab", this.gold_kingCrab);
            values.put("gold_phoenix", this.gold_phoenix);
            values.put("gold_pterodactyl", this.gold_pterodactyl);
            values.put("gold_iceMonster", this.gold_iceMonster);
            values.put("gold_dinoMonster", this.gold_dinoMonster);
            values.put("gold_seaMonster", this.gold_seaMonster);
            values.put("gold_landMonster", this.gold_landMonster);
            values.put("gold_giantScorion", this.gold_giantScorion);
            values.put("gold_bluewhale", this.gold_bluewhale);
            values.put("gold_elephant", this.gold_elephant);
            values.put("gold_cassowary", this.gold_cassowary);
            values.put("gold_giantspider", this.gold_giantspider);
            values.put("gold_blackwidow", this.gold_blackwidow);
            values.put("gold_mammoth", this.gold_mammoth);
            values.put("gold_hippo", this.gold_hippo);
            values.put("gold_ostrich", this.gold_ostrich);
            values.put("gold_boa", this.gold_boa);
            values.put("gold_komodo", this.gold_komodo);
            values.put("gold_killerwhale", this.gold_killerwhale);
            values.put("gold_sabertooth", this.gold_sabertooth);
            values.put("skin_orangeScorpion", this.skin_orangeScorpion);
            values.put("skin_blueDragon", this.skin_blueDragon);
            values.put("skin_kingRipper", this.skin_kingRipper);
            values.put("skin_kingStan", this.skin_kingStan);
            values.put("skin_kingShah", this.skin_kingShah);
            values.put("skin_dragon_gray", this.skin_dragon_gray);
            values.put("skin_kraken_cyan", this.skin_kraken_cyan);
            values.put("skin_yeti_emerald", this.skin_yeti_emerald);
            values.put("skin_snowman", this.skin_snowman);
            values.put("skin_trex_gray", this.skin_trex_gray);
            values.put("skin_kingCrab", this.skin_kingCrab);
            values.put("skin_phoenix_blue", this.skin_phoenix_blue);
            values.put("skin_pterodactyl_green", this.skin_pterodactyl_green);
            values.put("skin_lavaStrider", this.skin_lavaStrider);
            values.put("skin_boneKing", this.skin_boneKing);
            values.put("skin_dragon_terminator", this.skin_dragon_terminator);
            values.put("skin_dragon_red", this.skin_dragon_red);
            values.put("skin_iceMonster", this.skin_iceMonster);
            values.put("skin_dinoMonster", this.skin_dinoMonster);
            values.put("skin_seaMonster", this.skin_seaMonster);
            values.put("skin_landMonster", this.skin_landMonster);
            values.put("skin_bluewhale", this.skin_bluewhale);
            values.put("skin_elephant", this.skin_elephant);
            values.put("skin_cassowary", this.skin_cassowary);
            values.put("skin_giantspider", this.skin_giantspider);
            values.put("skin_blackwidow", this.skin_blackwidow);
            values.put("skin_mammoth", this.skin_mammoth);
            values.put("skin_hippo", this.skin_hippo);
            values.put("skin_ostrich", this.skin_ostrich);
            values.put("skin_boa", this.skin_boa);
            values.put("skin_komodo", this.skin_komodo);
            values.put("skin_killerwhale", this.skin_killerwhale);
            values.put("skin_sabertooth", this.skin_sabertooth);
            values.put("skin_tigerShark", this.skin_tigerShark);
            values.put("skin_hamerHeadShark", this.skin_hamerHeadShark);
            values.put("skin_panda", this.skin_panda);
            values.put("skin_zonkey", this.skin_zonkey);
            values.put("skin_okapi", this.skin_okapi);
            values.put("skin_giantCroc", this.skin_giantCroc);
            values.put("skin_purpleMouse", this.skin_purpleMouse);
            values.put("skin_starMouse", this.skin_starMouse);
            values.put("skin_winterMouse", this.skin_winterMouse);
            values.put("skin_prismaToucan", this.skin_prismaToucan);
            values.put("gold_mouse", this.gold_mouse);
            values.put("skin_queenScarlet", this.skin_queenScarlet);
            values.put("skin_queenCeleste", this.skin_queenCeleste);
            values.put("skin_bigFoot", this.skin_bigFoot);
            values.put("skin_kingMelon", this.skin_kingMelon);
            values.put("skin_woollyRhino", this.skin_woollyRhino);
            values.put("skin_queenRipper", this.skin_queenRipper);
            values.put("skin_kingInferno", this.skin_kingInferno); //skin_sapphireMouse
            values.put("skin_sapphireMouse", this.skin_sapphireMouse);
            values.put("skin_amethystMouse", this.skin_amethystMouse);
            values.put("skin_emeraldMouse", this.skin_emeraldMouse);
            values.put("skin_diamondMouse", this.skin_diamondMouse);
            values.put("skin_lordofice", this.skin_lordofice);
            values.put("skin_retrocrocodilealligator", this.skin_retrocrocodilealligator);
            values.put("skin_partycrock", this.skin_partycrock);
            values.put("skin_rubblealligator", this.skin_rubblealligator);
            values.put("skin_rubyblackdragon", this.skin_rubyblackdragon);
            values.put("skin_kingGolder", this.skin_kingGolder);
            // sdfadsfg
            values.put("skin_devilMouse", this.skin_devilMouse);
            values.put("skin_angelMouse", this.skin_angelMouse);
            values.put("skin_cheeseYsMouse", this.skin_cheeseYsMouse);
            values.put("skin_rizurrar", this.skin_rizurrar);
            values.put("skin_snowyFalcon", this.skin_snowyFalcon);
            values.put("skin_mouseElemental", this.skin_mouseElemental);
            values.put("skin_drillMole", this.skin_drillMole);
            //gfjgfdkgjfdkgkfgfdlsk
            values.put("skin_banditMouse", this.skin_banditMouse);
            values.put("skin_chocolatebirthdaycakeMouse", this.skin_chocolatebirthdaycakeMouse);
            values.put("skin_strawberrybirthdaycakeMouse", this.skin_strawberrybirthdaycakeMouse);
            values.put("skin_chocolateDragon", this.skin_chocolateDragon);
            values.put("skin_neonDragon", this.skin_neonDragon);
            values.put("skin_galaxyPhoenix", this.skin_galaxyPhoenix);
            values.put("skin_meltedMonster", this.skin_meltedMonster);
            values.put("skin_giftboxKraken", this.skin_giftboxKraken);



            // case 'skin_retrocrocodilealligator':
    //     return [_0x17b991, 0, `Retro Crocodile\n Alligator`, 'land/croc/4/croc'];
    // case 'skin_partycrock':
    //     return [_0x17b991, 0, `Party\n C'rock`, 'land/croc/5/croc'];
    // case 'skin_rubblealligator':
    //     return [_0x17b991, 0, `Rubble,\n Alligator`, 'land/croc/6/croc'];
    // case 'skin_rubyblackdragon':
    //      return [_0x1da4d1, 0, `The\nRuby Black Dragon`, 'volcano/blackdragon/7/blackdragon'];
    // case 'skin_kingGolder':
    //     return [_0x4e99c5, 0, `King\nGolder`, 'volcano/kingdragon/12/kingdragon'];
            List<String> strn = new ArrayList<>();
            Field[] fields = Account.class.getFields();
            for (Field f : fields) {
                if (f.getName().contains("skin_") || f.getName().contains("gold_")) {
                    if(values.get(f.getName()) == 1) strn.add(f.getName());
                }
            }
            return strn;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}