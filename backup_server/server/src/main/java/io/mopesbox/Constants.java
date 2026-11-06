package io.mopesbox;

import io.mopesbox.Utils.Utils;

public class Constants {
        private static Config conf = new Config();
        public static final int[] SECRETS = new int[] {
                        Utils.toInt(conf.GetProp("secret_one", "754353")),
                        Utils.toInt(conf.GetProp("secret_two", "412365")),
                        Utils.toInt(conf.GetProp("secret_three", "75534")),
                        Utils.toInt(conf.GetProp("secret_four", "1328"))
        };
        public static final int WIDTH1 = 2125; //8500
        public static final int HEIGHT1 = 1875;

        public static final int WIDTH = 8500; //8500
        public static final int HEIGHT = 7500; //7500
        public static final int LANDW = 5400;//5400
        public static final int LANDH = 4800;//4800
        public static final int ARCTICW = 8500;//8500
        public static final int ARCTICH = 1350; //1350
        public static final int DESERTW = 8500;//8500
        public static final int DESERTH = 1350; //1350
        public static final int OCEANW = 1550;//1550
        public static final int OCEANH = 4800;//4800
        public static final int COINSBEGININXSECAFTERSPAWN = Utils
                        .toInt(conf.GetProp("beginCoinsAfterSpawnInSeconds", "30"));
        public static final int GAMEPORT = Utils.toInt(conf.GetProp("gamePort", "80"));
        public static final boolean ENABLECOINEARNING = Utils.toBool(conf.GetProp("enableCoinEarning", "true"));
        public static final boolean ENABLEIDLECOINS = Utils.toBool(conf.GetProp("enableIdleCoins", "true"));
        public static final boolean GUIENABLED = Utils.toBool(conf.GetProp("guiEnabled", "true"));
        public static final int COINSEVERY = Utils.toInt(conf.GetProp("idleCoinsEveryNSeconds", "20"));
        public static final int IDLECOINS = Utils.toInt(conf.GetProp("idleCoins", "5"));
        public static final int IDLEEXP = Utils.toInt(conf.GetProp("idleExp", "0"));
        public static final int GAMEMODE = Utils.toInt(conf.GetProp("gamemode", "1"));
        public static final boolean ADMINPERMS = Utils.toBool(conf.GetProp("adminPerms", "true"));
        public static final boolean EVENTRIGHTNOW = Utils.toBool(conf.GetProp("eventRightNow", "false"));
        public static final boolean ADMINFOREVERYONE = Utils.toBool(conf.GetProp("adminForEveryone", "false"));
        public static final boolean EVERYONECANUPGRADE = Utils.toBool(conf.GetProp("everyoneCanUpgrade", "true"));
        public static final boolean CHRISTMAS = Utils.toBool(conf.GetProp("isXMas", "false"));
        public static final boolean MOREDEBUGINFO = Utils.toBool(conf.GetProp("moreDebugInfo", "false"));
        public static final double DEFAULTANGLESPEED1 = Utils.toDouble(conf.GetProp("defAngleSpeed", "0.21"));
        public static final double MAXROTSPEED1 = Utils.toDouble(conf.GetProp("maxRotSpeed", "59"));
        public static final double MAX_DISTANCE = Utils.toDouble(conf.GetProp("minMouseDistance", "0"));
        public static final double MIN_DISTANCE = Utils.toDouble(conf.GetProp("minMouseDistance", "0"));
        public static final double SMOOTHING_FACTOR = Utils.toDouble(conf.GetProp("smoothingFactor", "0.5"));
        public static final double MINROTSPEED1 = Utils.toDouble(conf.GetProp("minRotSpeed", "0"));
        public static final String SSLPATH = conf.GetProp("sslPath", "null");
        public static final String MASTERKEY = conf.GetProp("masterKey", "as20y");
        public static final String ACCOUNTSERVERURL = conf.GetProp("accountServerURL", "https://accountserver.mrmodpack.ml");
        public static final String MASTERSERVERURL = conf.GetProp("masterServerURL", "https://masterserver.mrmodpack.ml");
        public static final int BR_COINSAFTERWIN = Utils.toInt(conf.GetProp("br_coinsAfterWin", "1000"));
        public static final double NEWANGLESPEED = Utils.toDouble(conf.GetProp("newAngleSpeed", "0.2"));
        public static final boolean USINGSSL = Utils.toBool(conf.GetProp("usingSSL", "false"));
        public static final boolean ENABLEDCAPTCHA = Utils.toBool(conf.GetProp("enabledCaptcha", "false"));
        public static final boolean ENABLEPACKETLOGGING = Utils.toBool(conf.GetProp("enabledPacketLogging", "false"));
        // 0 - FFA
        // 3 - Team
        public static final int MAX_PLAYERS = Utils.toInt(conf.GetProp("max_players", "500"));
        public static final int MAP = Utils.toInt(conf.GetProp("map", "0"));

        public static final int TICKS_PER_SECOND = 20;
        public static final int VERSION = 165;
        public static final int BETA_VERSION = Utils.toInt(conf.GetProp("beta_version", "1"));
        public static final boolean isExperimental = Utils.toBool(conf.GetProp("isExperimental", "true"));
        public static final boolean isBeta = Utils.toBool(conf.GetProp("isBeta", "false"));
        public static final boolean SHUFFLEMODE = Utils.toBool(conf.GetProp("shuffleMode", "false"));
        public static final boolean isProduction = Utils.toBool(conf.GetProp("isProduction", "false"));
        public static final boolean CLOSEDFORDEV = Utils.toBool(conf.GetProp("closedForDev", "false"));
        public static final int STARTING_TIER = Utils.toInt(conf.GetProp("starting_tier", "1"));
        public static final boolean canSpawnPump = Utils.toBool(conf.GetProp("pumpEnabled", "false"));
        public static final boolean STRESSTEST = Utils.toBool(conf.GetProp("stressTest", "false"));
        public static final boolean FOODSPAWN = Utils.toBool(conf.GetProp("foodSpawn", "true"));

        public static final int MIN_START_PLAYER = 4;
        public static final double DEFAULTANGLESPEED = 17.0;
        public static final double MAXROTSPEED = 17.0;
        public static final double MINROTSPEED = 14.0; 
}
; 