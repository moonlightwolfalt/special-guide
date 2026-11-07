package io.mopesbox.Client;

import io.mopesbox.Animals.Info.AnimalSkins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
    // Shop skins imported from mope.trygames.party
    public int astralPhoenix;
    public int bonfire_phoenix;
    public int skin_dragon_3_3;
    public int skin_dragon_3_9;
    public int skin_dragon_202_0;
    public int skin_dragon_224_0;
    public int skin_leatherBackTurtle;
    public int skin_celestialCobra;
    public int skin_brazenBull;
    public int skin_luckyCharmMonster;
    public int skin_iceToucan;
    public int skin_clowoudLion;
    public int skin_sunsetClowoudLion;
    public int skin_stormClowoudLion;
    public int skin_kaijuRex;
    public int skin_warTHog;
    public int skin_velvetChimp;
    public int skin_aquaPhoenix;
    public int skin_icePhoenix;
    public int skin_ashPhoenix;
    public int skin_redGiantPhoenix;
    public int skin_copperPhoenix;
    public int skin_goofyDragon;
    public int skin_desertDragon;
    public int skin_forgeDragon;
    public int skin_mythicalSerpent;
    public int skin_fireyDragon;
    public int skin_technoSaber;
    public int skin_iceFox;
    public int skin_roseDragon;
    public int skin_purpleHaze;
    public int skin_clownfish;
    public int skin_tullyMonster;
    public int skin_vampireSquid;
    public int skin_cuttlefish;
    public int skin_deluxeShrimp;
    public int skin_monochromeRabbit;
    public int skin_nightFox;
    public int skin_plagueRaptor;
    public int skin_titanBison;
    public int skin_dowagerVarande;
    public int skin_emeprorVarande;
    public int skin_rubyDevil;
    public int skin_metalicCrab;
    public int skin_queenFlame;
    public int halloween_wyrmous;
    public int halloween_equusthulhu;
    public int halloween_frostaline;
    public int halloween_pufferMace;
    public int halloween_carnageodactyl;
    public int halloween_mrDollar;
    public int halloween_wendigoDeer;
    public int halloween_cactiRat;
    public int halloween_hiPower;
    public int halloween_puumpkat;
    public int halloween_doomedarius;
    public int halloween_roboMouse;
    public int halloween_uncleSam;
    public int halloween_zombieMole;
    public int halloween_flammyHart;
    public int halloween_gummyViper;
    public int halloween_grimRipper;
    public int halloween_bigCookie;
    public int halloween_gargolgon;
    public int halloween_candyRus;
    public int halloween_mantilian;
    public int halloween_phantomRay;
    public int halloween_suitedToucan;
    public int halloween_vulgeist;
    public int halloween_burlone;
    public int halloween_unicornze;
    public int skin_snowgirl;
    public int skin_pterodactyl_blue;


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

    // Names of the account's int fields that are skins (they match AnimalSkins).
    private static final List<Field> SKIN_FIELDS = new ArrayList<>();
    static {
        for (Field f : Account.class.getFields()) {
            if (f.getType() == int.class && !Modifier.isStatic(f.getModifiers()) && AnimalSkins.byName(f.getName()) != null)
                SKIN_FIELDS.add(f);
        }
    }

    public List<String> getSkins() {
        List<String> owned = new ArrayList<>();
        try {
            for (Field f : SKIN_FIELDS) {
                if (f.getInt(this) == 1) owned.add(f.getName());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return owned;
    }
}