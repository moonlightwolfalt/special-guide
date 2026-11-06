package io.mopesbox.World;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Collision.CollisionHandler;
import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Networking.WorldUpdate;
import io.mopesbox.Objects.Biome.*;
import io.mopesbox.Objects.Juggernaut.Meteor;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Objects.Static.Hill;
import io.mopesbox.Objects.Static.Extraocean;
import io.mopesbox.Objects.Static.LakeIsland;
import io.mopesbox.Objects.Static.Oasis;
import io.mopesbox.Objects.Static.Waterspot;
import io.mopesbox.Objects.Static.Berryspot;
import io.mopesbox.Objects.Static.DeathLake;
import io.mopesbox.Objects.Static.PlanktonBush;
import io.mopesbox.Objects.Static.Lake;
import io.mopesbox.Objects.Static.Mudspot;
import io.mopesbox.Objects.Static.IsLand;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Objects.Arctic.ArcticIce;
import io.mopesbox.Objects.BattleRoyale.SafeArea;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.AccountConnector;
import io.mopesbox.Utils.Emoji;
import io.mopesbox.Utils.FoodGenerator;
import io.mopesbox.Utils.IPBlacklist;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.ObjectTypes;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import net.openhft.affinity.Affinity;

// import org.javacord.api.DiscordApi;
// import org.javacord.api.entity.channel.TextChannel;
// import org.javacord.api.entity.message.embed.EmbedBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class Room extends Thread {

    private final CollisionHandler collisionHandler;
    // private final LeaderThread leaderboardHandler;
    private final FoodGenerator foodGenerator;
    public AccountConnector accountConnector;
    public GameList objects;
    public IPBlacklist ipblock = new IPBlacklist(this);
    // public boolean isThreadReady = true;

    public ArrayList<Meteor> meteoriteList = new ArrayList<>();

    public ArrayList<GameClient> clients = new ArrayList<>();
    public ArrayList<GameClient> aiClients = new ArrayList<>();
    public ArrayList<GameClient> aiClientsToRemove = new ArrayList<>();
    public ArrayList<GameClient> clients_to_add = new ArrayList<GameClient>();
    public ArrayList<GameClient> clients_to_remove = new ArrayList<GameClient>();
    public ArrayList<GameObject> rectangles = new ArrayList<GameObject>();
    public ArrayList<GameObject> no_update_obj = new ArrayList<GameObject>();

    

    // public DiscordApi clientDiscord;
    public Main main;
    // public Logger log;
    public long tick;
    public GameClient topplayer;

    private int gameMode;
    private int gameState;
    private int serverVer;
    private int season;
    private boolean allowedToChat;
    private int width;
    private int height;
    public long startTime = System.nanoTime();
    private Timer masterUpdate = new Timer(5000);
    public ArrayList<String> IpsToTroll = new ArrayList<>();
    public ArrayList<String> IpBan = new ArrayList<>();

    public CollisionHandler getCollisionHandler() {
        return this.collisionHandler;
    }

    public String upTime() {
        return Utils.getReadableTime(System.nanoTime() - startTime);
    }

    public io.mopesbox.Collision.Rectangle getBounds() {
        return new io.mopesbox.Collision.Rectangle(0, 0, this.getWidth(), this.getHeight());
    }
    

    public ArrayList<GameClient> developerDots = new ArrayList<GameClient>();
    public ArrayList<GameObject> pumpDots = new ArrayList<GameObject>();

    public int goals;
    public int br_center_x = 100;
    public int br_center_y = 100;
    public int br_center_rad = 500;
    public int br_gamestate = 0;
    public ArrayList<GameClient> whitelist = new ArrayList<GameClient>();

    public ArrayList<GameClient> getList() {
        return whitelist;
    }

    public int getAlivePlayers() {
        int a = 0;
        for (GameClient cl : this.clients) {
            if (cl.getPlayer() != null && !cl.getPlayer().isGhost && !cl.isBot() && !cl.isSpectating())
                a++;
        }
        return a;
    }

    private int object_id = 0;

    private GameObject highlight;
    public Land land;
    public Rainforest forest;
    public Ocean oleft;
    public Ocean oright;
    public Desert desert;
    public Arctic arctic;
    public VolcanoBiome volcanoBiome;
    public VolcanoBiome volcanoBiome2;
    public VolcanoBiome volcanoBiome3;
    public SafeArea safeArea;
    public long latestTick = 0;
    public long higherTick = 0;
    public HashMap<Integer, Integer> lticks = new HashMap<>();
    public HashMap<Integer, Integer> hticks = new HashMap<>();

    public BattleRoyaleHandler br_handler;
    public JuggernautHandler jug_handler;
    // publicString[] ezz=new String[] {"Ani", "Sam", "Joe"};

    public String[] ezz = new String[] { "Wait... This isn't what I typed!", "Anyone else really like Rick Astley?",
            "Hey helper, how play game?", "Sometimes I sing soppy, love songs in the car.",
            "I like long walks on the beach and playing mope.io", "Please go easy on me, this is my first game!",
            "You're a great person! Do you want to play some mope.ios with me?",
            "In my free time I like to watch cat videos on Youtube",
            "When I saw the witch with the potion, I knew there was trouble brewing.",
            "If the Minecraft world is infinite, how is the sun spinning around it?",
            "Hello everyone! I am an innocent player who loves everything mope.io", "Plz give me doggo memes!",
            "I heard you like Minecraft, so I built a computer …inecraft so you can Minecraft while you Minecraft",
            "Why can't the Ender Dragon read a book? Because he always starts at the End.",
            "Maybe we can have a rematch?", "I sometimes try to say bad things then this happens :(",
            "Behold, the great and powerful, my magnificent and almighty nemisis!", "Doin a bamboozle fren.",
            "Your clicks per second are godly. :eek:", "What happens if I add chocolate milk to macaroni and cheese?",
            "Can you paint with all the colors of the wind", "Blue is greener than purple for sure",
            "I had something to say, then I forgot it.", "When nothing is right, go left.",
            "I need help, teach me how to play!", "Your personality shines brighter than the sun.",
            "You are very good at the game friend.", "I like pineapple on my pizza",
            "I like pasta, do you prefer nachos?", "I like Minecraft pvp but you are truly better than me!",
            "I have really enjoyed playing with you! <3", "ILY <3", "Pineapple doesn't go on pizza!",
            "Lets be friends instead of fighting okay?" };

    public GameList getObjects() {
        return objects;
    }

    public int getGameMode() {
        return gameMode;
    }

    public int getGameState() {
        return gameState;
    }

    public int getPlayers() {
        int aboba = 0;
        for (GameClient c : this.clients) {
            if (!c.isSpectating() && c.getPlayer() != null) {
                aboba++;
            }
        }
        return aboba;
    }

    public List<Integer> getStaff() {
        int artists = 0;
        int admins = 0;
        int developers = 0;
        for (GameClient cl : this.clients) {
            if (cl.getAccount() != null && cl.getAccount().admin > 0) {
                if (cl.getAccount().admin == 1)
                    artists++;
                else if (cl.getAccount().admin == 2)
                    admins++;
                else if (cl.getAccount().admin == 3)
                    developers++;
                else
                    admins++;
            }
        }
        List<Integer> list = new ArrayList<>();
        list.add(artists);
        list.add(admins);
        list.add(developers);
        return list;
    }

    public int getServerVer() {
        return serverVer;
    }

    public int getSeason() {
        return season;
    }

    public boolean isAllowedToChat() {
        return allowedToChat;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GameObject getHighlight() {
        return highlight;
    }

    public void resetRoom() {
        this.clients_to_add.clear();
        for (GameClient c : this.clients) {
            Main.instance.ipChecker.ipInteractions.clear();
            c.updateCoins(c.getCoins());
            c.sendDisconnect(false, "Resetting the room.", false);
        }

        this.clients.clear();
        this.clients_to_remove.clear();

        this.object_id = 0;
        this.objects.gameMap.clear();

        this.generate();

        br_gamestate = 0;
        Main.instance.sessionManager.getSessions().clear();

    }

    public Room(final Main main) {
        this.accountConnector = new AccountConnector();
        this.main = main;
        // this.log = Main.log;

        this.objects = new GameList();

        this.tick = 1000 / Constants.TICKS_PER_SECOND;

        this.gameMode = Constants.GAMEMODE;

        this.gameState = 0;

        this.serverVer = Constants.VERSION;

        this.season = 1;

        this.allowedToChat = true;

        this.width = Constants.WIDTH;

        this.height = Constants.HEIGHT;

        this.collisionHandler = new CollisionHandler(this, this.main);

        this.collisionHandler.setDaemon(true);

        this.collisionHandler.setPriority(Thread.MAX_PRIORITY);

        // this.leaderboardHandler = new LeaderThread(this);

        // this.leaderboardHandler.setDaemon(true);

       this.foodGenerator = new FoodGenerator(this);

      this.foodGenerator.setDaemon(true);

        this.collisionHandler.start();

        // this.leaderboardHandler.start();

    if(Constants.FOODSPAWN) this.foodGenerator.start();

    }

    public void addObj(GameObject object) {
        object.setDead(false);
        this.objects.add(object);
        object.onAdd();
    }

    public void addObjects(ArrayList<GameObject> objects) {
        for (GameObject obj : objects) {
            this.addObj(obj);
        }
    }

    public void removeObj(GameObject object, GameObject killer) {
        if (object == null)

            return;
        
        object.setDead(true);
        this.objects.remove(object);

        for (GameClient client : this.clients) {
            if (client.getVisibleList().indexOf(object) != -1) {
                client.removeFromVisible(object, killer);
            }
        }
    }

    public void removeVisObj(GameObject object, GameObject killer, GameClient exclude) {
        for (GameClient client : this.clients) {
            if (client.getVisibleList().indexOf(object) != -1 && client != exclude
                    && (client.getPlayer() == null || !client.getPlayer().isInHole())) {
                client.removeFromVisible(object, killer);
            }
        }
    }

    public void removeVisObjGhosts(GameObject object, GameObject killer, GameClient exclude) {
        for (GameClient client : this.clients) {
            if (client.getVisibleList().indexOf(object) != -1 && client != exclude
                    && (client.getPlayer() == null || !client.getPlayer().isGhost)) {
                client.removeFromVisible(object, killer);
            }
        }
    }

    public void removeVisObjEggs(GameObject object, GameClient exclude) {
        for (GameClient client : this.clients) {
            if (object != null && client.getPlayer() != null && client.getVisibleList().indexOf(object) != -1 && client != exclude
                    && (client.getPlayer() == null || !client.getPlayer().inEgg)) {
                client.removeFromVisible(object, client.getPlayer().egg);
            }
        }
    }

    public void removeVisObj3(GameObject object, GameObject killer) {
        for (GameClient client : this.clients) {
            if (client.getVisibleList().indexOf(object) != -1) {
                client.removeFromVisible(object, killer);
            }
        }
    }

    // public void removeVisObj2(GameClient gc) {
    //     for (GameObject obj : this.objects) {
    //         if (!(obj instanceof Animal))
    //             continue;
    //         if (gc.getVisibleList().indexOf(obj)!= -1 && ((Animal) obj).isInHole()) {
    //             gc.removeFromVisible(obj, ((Animal) obj).getHole());
    //         }
    //     }
    // }


        public void removeVisObj2(GameClient gc) {
            for (GameClient obj : this.clients) {
                if (obj.getPlayer() == null)
                    continue;
                if (gc.getVisibleList().indexOf(obj.getPlayer())!= -1 && obj.getPlayer().isInHole()) {
                    gc.removeFromVisible(obj.getPlayer(), obj.getPlayer().getHole());
                }
            }
        }

    public void removeVisObj4(GameObject object) {
        for (GameClient client : this.clients) {
            if (client.getVisibleList().indexOf(object) != -1) {
                client.removeFromVisible(object, object);
            }
        }
    }

    public void removeVisObjGhost(GameClient gc) {
        for (GameObject obj : this.objects) {
            if (!(obj instanceof Animal))
                continue;
            if (gc.getVisibleList().indexOf(obj) != -1 && ((Animal) obj).isGhost) {
                gc.removeFromVisible(obj, null);
            }
        }
    }

    public int getID() {
        this.object_id++;
        return this.object_id;
    }

    public void generate() {

                Affinity.setAffinity(4);

        try {
            System.out.print("Started generating room.\n");

            if(Constants.MAP == 0){

            this.land = new Land(this.getID(), 4250, 3750, Constants.LANDW, Constants.LANDH, this);
            this.addObj(land);
            this.volcanoBiome = new VolcanoBiome(this.getID(), Constants.WIDTH / 2, Constants.HEIGHT / 2, 420, this,
                    land.getId());
            this.oright = new Ocean(this.getID(), 7725, 3750, Constants.OCEANW, Constants.OCEANH, this);
            this.arctic = new Arctic(this.getID(), 4250, 675, Constants.ARCTICW, Constants.ARCTICH, this);
            // this.arctic.spawnVolcano();

            this.oleft = new Ocean(this.getID(), 775, 3750, Constants.OCEANW, Constants.OCEANH, this);

            this.addObj(volcanoBiome);
            volcanoBiome.spawnVolcano();
            land.spawnLakesForLand(2, 3);
            land.spawnHills(70);
            land.spawnTrees(10);
            land.spawnHoles(20);
            land.spawnWaterspot(10);
            land.spawnBerrybushes(35);
            land.spawnHidingBushs(20, true);

            arctic.spawnLakesForArctic(3);

            this.addObj(arctic);
            arctic.spawnHills(40);
            arctic.spawnTrees(20);
            arctic.spawnHoles(20);
            arctic.spawnWaterspot(6);
            arctic.spawnBerrybushes(10);
            arctic.spawnHidingBushs(15, true);
            arctic.spawnArcticBushs(15, true);

            // arctic.spawnMeat(250);

            // arctic beach left
            this.addObj(new Rectangle(this.getID(), 850, 1295, 1700, 150, ObjectTypes.Beach.getType()));
            // arctic beach right
            this.addObj(new Rectangle(this.getID(), 7650, 1295, 1700, 150, ObjectTypes.Beach.getType()));

            // forest
            this.forest = new Rainforest(this.getID(), 2250, 2050, 1400, 1400, this);
            this.addObj(forest);
            forest.spawnTrees(10);

            // land beach left
            this.addObj(new Rectangle(this.getID(), 1660, 3675, 190, 4950, ObjectTypes.Beach.getType()));

            // land beach right
            this.addObj(new Rectangle(this.getID(), 6840, 3675, 190, 4950, ObjectTypes.Beach.getType()));

            // ocean left
            this.oleft = new Ocean(this.getID(), 775, 3750, Constants.OCEANW, Constants.OCEANH, this);
            this.addObj(oleft);
            oleft.spawnHills(40);
            oleft.spawnHoles(15);
            oleft.spawnIsLand(10);
            oleft.spawnPlanktonBushs(10);
            double Xam = 4;
            double Yam = 14;
            double OceanBallSize = 500;
            double OceanBallSizeW = Constants.OCEANW / Xam;

            double OceanBallSizeH = Constants.OCEANH / Yam;
            for (var x = 1; x < Xam + 1; x++) {
                for (var y = 1; y < Yam; y++) {
                    if (x == Xam && y == 1) {
                        var OBEW = new Extraocean(this.getID(), 0, 0, (int) (Math.floor(OceanBallSize * 0.6)), 0, this);
                        OBEW.setX(Constants.OCEANW - OceanBallSizeW * 0.55);
                        OBEW.setY(Constants.OCEANH + OceanBallSizeH * 0.75);
                        this.addObj(OBEW);
                        continue;
                    }
                    ;
                    if (x != 4 && y != 1)
                        continue;
                    var OBEW = new Extraocean(this.getID(), 0, 0, (int) (Math.floor(OceanBallSize * 0.9)), 0, this);
                    OBEW.setX((OceanBallSizeW * (x)) - OceanBallSizeW * (y <= 1 ? 0.3 : 0.9));
                    OBEW.setY(Constants.ARCTICH
                            + (OceanBallSizeH * (y) - (y > 1 ? OceanBallSizeH * 0.3 : -OceanBallSizeH * 0.1)));
                    this.addObj(OBEW);
                }
                ;
            }
            ;
            // ocean right
            this.oright = new Ocean(this.getID(), 7725, 3750, Constants.OCEANW, Constants.OCEANH, this);
            this.addObj(oright);
            oright.spawnHills(40);
            oright.spawnHoles(15);
            oright.spawnIsLand(10);
            oright.spawnPlanktonBushs(10);
            for (var x = 1; x < Xam + 1; x++) {
                for (var y = 1; y < Yam; y++) {
                    if (x == Xam && y == 1) {
                        var OBEW = new Extraocean(this.getID(), 0, 0, (int) (Math.floor(OceanBallSize * 0.6)), 0, this);
                        OBEW.setX(Constants.WIDTH - (Constants.OCEANW + OceanBallSizeW * 0.55));
                        OBEW.setY(Constants.OCEANH + OceanBallSizeH * 0.75);
                        this.addObj(OBEW);
                        continue;
                    }
                    ;
                    if (x != 4 && y != 1)
                        continue;
                    var OBEW = new Extraocean(this.getID(), 0, 0, (int) (Math.floor(OceanBallSize * 0.9)), 0, this);
                    OBEW.setX(Constants.WIDTH - (OceanBallSizeW * (x)) + OceanBallSizeW * (y <= 1 ? 0.3 : 0.9));
                    OBEW.setY(Constants.ARCTICH
                            + (OceanBallSizeH * (y) - (y > 1 ? OceanBallSizeH * 0.3 : -OceanBallSizeH * 0.1)));
                    this.addObj(OBEW);
                }
                ;
            }
            ;

            // desert
            this.desert = new Desert(this.getID(), 4250, 6825, Constants.DESERTW, Constants.DESERTH, this);
            this.addObj(desert);
            desert.spawnHills(40);
            desert.spawnHoles(10);
            desert.spawnTrees(5);
            desert.spawnWaterspot(7);
            desert.spawnLavaLakes(5);
            desert.spawnAntHills(5);
            desert.spawnFlytraps(15);
            desert.spawnOasis(3);

            // desert beach left
            this.addObj(new Rectangle(this.getID(), 850, 6205, 1700, 150, ObjectTypes.Beach.getType()));
            // desert beach right
            this.addObj(new Rectangle(this.getID(), 7650, 6205, 1700, 150, ObjectTypes.Beach.getType()));
        }else if(Constants.MAP == 1 ){
            this.land = new Land(this.getID(), 6350, 2000, Constants.WIDTH/2, Constants.HEIGHT/2, this);
            this.addObj(land);
            this.volcanoBiome = new VolcanoBiome(this.getID(), Constants.WIDTH / 2, Constants.HEIGHT / 2, 420, this,
                    land.getId());
            this.volcanoBiome2 = new VolcanoBiome(this.getID(), 2125, 1881, 420, this, land.getId());
            this.volcanoBiome3 = new VolcanoBiome(this.getID(), 2125, 5650, 420, this, land.getId());
            this.oright = new Ocean(this.getID(), 2125, 5650, Constants.WIDTH/2, Constants.HEIGHT/2, this);
            this.oleft = new Ocean(this.getID(), 2125, 5650, Constants.WIDTH/2, Constants.HEIGHT/2, this);

            this.arctic = new Arctic(this.getID(), 2125, 1875, Constants.WIDTH/2, Constants.HEIGHT/2, this);
            // this.arctic.spawnVolcano();

            this.addObj(volcanoBiome);
            volcanoBiome.spawnVolcano();
            this.addObj(volcanoBiome2);
            volcanoBiome2.spawnVolcano();
            this.addObj(volcanoBiome3);
            volcanoBiome3.spawnVolcano();
            // land.spawnLakesForLand(2, 3);

            land.spawnHills(70);
            land.spawnTrees(10);
            land.spawnHoles(20);
            land.spawnWaterspot(10);
            land.spawnBerrybushes(35);
            land.spawnHidingBushs(20, true);
            this.addObj(oright); //this.oleft
            this.addObj(oleft); //this.oleft
            oright.spawnHills(40);
            oright.spawnHoles(15);
            oright.spawnIsLand(10);
            oright.spawnPlanktonBushs(10);

            // arctic.spawnLakesForArctic(3);

            this.addObj(arctic);
            arctic.spawnHills(40);
            arctic.spawnTrees(20);
            arctic.spawnHoles(20);
            arctic.spawnWaterspot(6);
            arctic.spawnBerrybushes(10);
            arctic.spawnHidingBushs(15, true);
            arctic.spawnArcticBushs(15, true);

            // arctic.spawnMeat(250);

            // arctic beach left
            // arctic beach right

            // forest
            this.forest = new Rainforest(this.getID(), 7500, 1000, 1400, 1400, this);
            this.addObj(forest);
            forest.spawnTrees(10);

            // land beach left

            // land beach right

            // ocean left
         
            // ocean right
          
        

            // desert
            this.desert = new Desert(this.getID(), 6375, 5650, Constants.WIDTH/2, Constants.HEIGHT/2, this);
            this.addObj(desert);
            desert.spawnHills(40);
            desert.spawnHoles(10);
            desert.spawnTrees(5);
            desert.spawnWaterspot(7);
            desert.spawnLavaLakes(5);
            desert.spawnAntHills(5);
            desert.spawnFlytraps(15);
            desert.spawnOasis(3);

          
        }
            System.out.print("Map Loaded\n");


        } catch (Exception e) {
            e.printStackTrace();

        }

        switch (Constants.GAMEMODE) {
            case 2: {
                br_handler = new BattleRoyaleHandler(this);
                break;
                // battle royale
                // this.safeArea = new SafeArea(this.getID(), Constants.WIDTH / 2,
                // Constants.HEIGHT / 2, 1000, this);
                // this.addObj(this.safeArea);
            }
            case 7: {
                jug_handler = new JuggernautHandler(this);
                break;
            }
        }

        for (GameObject object : objects) {
            if (object instanceof Rectangle) {
                this.rectangles.add(object);
            }
        }
        // public Meteor(int id, double x, double y, int rad, double landX, double
        // landY, Room room) {
        /*
         * for (int i = 0; i < 100; i++) {
         * this.addObj(new Meteor(this.getID(), Utils.randomDouble(0, Constants.WIDTH),
         * Utils.randomDouble(0, Constants.HEIGHT), Utils.randomInt(50, 100),
         * Utils.randomDouble(0, Constants.WIDTH), Utils.randomDouble(0,
         * Constants.HEIGHT),this));
         * }
         */
        // this.log.info("Generation finished.");
        if(Constants.FOODSPAWN)foodGenerator.initialize();

    }

    @Override
    public void run() {
        this.generate();
        while (true) {
            try {
                // if (isThreadReady)
                    this.update();

                Thread.sleep(this.tick);
                // Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    // private Timer meteorTimer = new Timer(1000);

    // private void antiKd() {
    // //antiTeamingDamageTimer.update(Constants.TICKS_PER_SECOND);
    // List<KingDragon> kds = new ArrayList<>();
    // List<BlackDragon> bds = new ArrayList<>();
    // for(GameObject o : this.objects){
    // if(o instanceof KingDragon) {
    // kds.add((KingDragon) o);
    // }
    // }

    // if (kds.size() > 4) {
    // for (KingDragon kd : kds){
    // if (!kd.inArena) {
    // for (KingDragon kd2 : kds){
    // if (!kd2.inArena && kd2 != kd) {
    // PvPArena newarena = new PvPArena(this.getID(), kd.getX(),
    // kd.getY(), kd2.getClient(), kd.getClient(), this.getFightNumber(),
    // this);
    // this.addObj(newarena);
    // return;
    // }
    // }
    // }
    // }
    // }

    // if (kds.size() == 4) {
    // for (KingDragon kd : kds){
    // if (!kd.inArena) {
    // for (KingDragon kd2 : kds){
    // if (!kd2.inArena && kd2 != kd) {
    // PvPArena newarena = new PvPArena(this.getID(), kd.getX(),
    // kd.getY(), kd2.getClient(), kd.getClient(), this.getFightNumber(),
    // this);
    // this.addObj(newarena);
    // return;
    // }
    // }
    // }
    // }
    // }

    // }
    private void antiTeaming() {
        antiTeamingDamageTimer.update(Constants.TICKS_PER_SECOND);
        List<KingDragon> kds = new ArrayList<>();
        List<BlackDragon> bds = new ArrayList<>();
        for (GameObject o : this.objects) {
            if (o instanceof KingDragon) {
                kds.add((KingDragon) o);
            } else if (o instanceof BlackDragon) {
                bds.add((BlackDragon) o);
            }
        }

        if (kds.size() > 2) {
            for (KingDragon kd : kds) {
                if (!kd.inArena) {
                    for (KingDragon kd2 : kds) {
                        if (!kd2.inArena && kd2 != kd) {
                            PvPArena newarena = new PvPArena(this.getID(), kd.getX(),
                                    kd.getY(), kd2.getClient(), kd.getClient(), this.getFightNumber(),
                                    this);
                            this.addObj(newarena);
                            return;
                        }
                    }
                }
            }
        }
        if (bds.size() > 6) {
            for (BlackDragon bd : bds) {
                if (!bd.inArena) {
                    for (BlackDragon bd2 : bds) {
                        if (!bd2.inArena && bd2 != bd) {
                            PvPArena newarena = new PvPArena(this.getID(), bd.getX(),
                                    bd.getY(), bd2.getClient(), bd.getClient(), this.getFightNumber(),
                                    this);
                            this.addObj(newarena);
                            return;
                        }
                    }
                }
            }
        }
        if (antiTeamingDamageTimer.isDone() && bds.size() > 1) {
            for (BlackDragon bd : bds) {
                if (!bd.inArena) {
                    for (BlackDragon bd2 : bds) {
                        if (bd != bd2) {
                            if (!bd2.inArena && Utils.distance(bd.getX(), bd2.getX(), bd.getY(), bd2.getY()) < 200) {
                                bd.hurt(bd.maxHealth / 40, 0, null);
                                bd2.hurt(bd2.maxHealth / 40, 0, null);
                            }
                        }
                    }
                }
            }
            antiTeamingDamageTimer.reset();
        }
    }

    private Timer antiTeamingDamageTimer = new Timer(10000);
    public DeathLake deathlake;


    private void updateClientAdd(){
                for (GameClient client : this.clients_to_add) {
        
        
         
                    
                    this.clients.add(client);
        
                    this.main.guiThread.changePlayers(this.getPlayers(), this.getStaff());
        
                
            }
    }
    private void updateClientRemove(){
                for (GameClient client : this.clients_to_remove) {
                
                this.clients.remove(client);
                
                this.main.guiThread.changePlayers(this.getPlayers(), this.getStaff());
                
                        }
                
    }


    public void update() {
        if (br_handler != null)
            br_handler.update();
        if (jug_handler != null)
            jug_handler.update();
        developerDots.clear();
        this.meteoriteList.clear();

        // update spectators!

        // meteorTimer.update(Constants.TICKS_PER_SECOND);
        // if(meteorTimer.isDone()) {
        // this.addObj(new Meteor(this.getID(), Utils.randomDouble(0, Constants.WIDTH),
        // Utils.randomDouble(0, Constants.HEIGHT), 50, Utils.randomDouble(0,
        // Constants.WIDTH), Utils.randomDouble(0, Constants.HEIGHT), this));
        // meteorTimer.reset();
        // }
        if (Constants.GAMEMODE != 0)
            this.antiTeaming();

        ipblock.update();

        // this.antiKd();

        masterUpdate.update(Constants.TICKS_PER_SECOND);
        if (masterUpdate.isDone()) {
            if (this.accountConnector != null)
                this.accountConnector.updatePlayers(this.getPlayers());
            masterUpdate.reset();
        }

        if(this.clients_to_add.size() > 0) this.updateClientAdd();
        if(this.clients_to_remove.size() > 0) this.updateClientRemove();

        for (GameClient ai : this.aiClientsToRemove) {
            this.aiClients.remove(ai);
        }
                for (Map.Entry<String, GameClient> entry : Main.instance.sessionManager.getSessions().entrySet()) {
                GameClient c = entry.getValue();
                c.removeSession.update(Constants.TICKS_PER_SECOND);
                if(c.removeSession.isDone()){
                    Main.instance.sessionManager.removeSession(c, true);
                }

                }

        this.clients_to_remove.clear();

        this.aiClientsToRemove.clear();

        this.clients_to_add.clear();
        
        for (GameClient client : this.clients) {
            if (!client.devHidden && client.isDeveloper() && client.getPlayer() != null && !client.getPlayer().isGhost)
                developerDots.add(client);
        }
        List<GameClient> clientcopy = new ArrayList<>(this.clients);

        for (GameClient client : clientcopy) {
            if (client.isShouldRemove()) {
                clients_to_remove.add(client);
                continue;
            } else {

                client.update();
                client.send(WorldUpdate.create(client, this));
            }
        }

        List<GameClient> aiCopy = new ArrayList<>(this.aiClients);

        for (GameClient ai : aiCopy) {
            if (ai.isShouldRemove()) {
                aiClientsToRemove.add(ai);
                continue;
            } else {
                ai.update();
            }
        }

        if (Constants.GAMEMODE == 7) {
            for (GameObject o : this.objects) {
                if (o instanceof Meteor)
                    this.meteoriteList.add((Meteor) o);
            }
        }
        // isThreadReady = false;

    }

    public Emoji[] emojis = new Emoji[] {
            new Emoji("skull", "💀"),
            new Emoji("sob", "😭")
    };

    public String emojis(String msg) {
        for (Emoji emoji : emojis) {
            if (msg.contains(":" + emoji.getPlaceholder() + ":")) {
                msg = msg.replaceAll(":" + emoji.getPlaceholder() + ":", emoji.getEmoji());
            }
        }
        return msg;
    }

    public void sendChat(GameClient client, String msg) {
        if (client.getPlayer() == null)
            return;
        GameObject ani = client.getPlayer().inEgg ? client.getPlayer().egg
                : (client.getPlayer().isInHole() ? client.getPlayer().getHole() : client.getPlayer());

        msg = this.emojis(msg);

        for (GameClient c : clients) {
            if (c.getVisibleList().contains(ani)) {

                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.CHAT);
                writer.writeUInt32(ani.getId());
                writer.writeString(msg);

                c.send(writer);

            }
        }
    }

    public void chat(GameClient client, String msg) {
        sendChat(client, msg);
        if(this.main != null && client.getPlayer() != null && Constants.GUIENABLED)this.main.guiThread.toLog(String.format("%s: %s", client.getPlayer().getPlayerName(), msg));
    }

    public void addAI(GameClient client) {
        aiClients.add(client);
    }

    public List<Integer> checkTeams() {
        List<Integer> teamsCount = new ArrayList<>();
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        for (GameClient c : clients) {
            if (c.getPlayer() != null) {
                if (c.getTeam() == 0)
                    t1++;
                if (c.getTeam() == 1)
                    t2++;
                if (c.getTeam() == 2)
                    t3++;
            }
        }
        teamsCount.add(t1);
        teamsCount.add(t2);
        teamsCount.add(t3);
        return teamsCount;
    }

    public void addClient(GameClient client, boolean reconnect) {
        MsgWriter packet = getRoomInfo(client, reconnect);

        if(reconnect){
            client.clearAddList();
            client.clearRemoveList();
            client.clearUpdateList();
            client.getVisibleList().clear();
            client.getRoom().objects = this.objects;
            client.room = this;
            client.updateViewable();
        }

        clients_to_add.add(client);

        // auto send these packets
        client.send(Networking.sendJoinPacket(this, client, true, reconnect));

        MsgWriter writer = new MsgWriter();
        writer.writeUInt8(MessageType.READYTOPLAY.value());
        client.send(writer);

        client.send(packet);

    if(reconnect)this.joinGameAfterReconnect(client, false);


        // Networking.sendMessage(player.client.socket, Networking.sendJoinPacket(this,
        // player, true))
        // Networking.sendMessage(player.client.socket, this.readyToPlay())
        // Networking.sendMessage(player.client.socket, this.sendRoomInfo(player))
    }

    // public void givenList_shouldReturnARandomElement() {
    // List<Integer> givenList = Arrays.asList(1, 2, 3);
    // Random rand = new Random();
    // int randomElement = givenList.get(rand.nextInt(givenList.size()));
    // }
    public String getRandomItem(String[] list) {
        int rnd = new Random().nextInt(list.length);
        return list[rnd];
    }

    public GameObject getBiomeByID(int id, GameObject obj) {
        if (id == 0)
            return land;
        if (id == 5)
            return forest;
        if (id == 1) {
            if (obj == null)
                return Utils.randomBoolean() ? oleft : oright;
            else {
                double distance = Utils.distance(oleft.getX(), obj.getX(), oleft.getY(), obj.getY());
                double distance2 = Utils.distance(oright.getX(), obj.getX(), oright.getY(), obj.getY());
                if (distance > distance2)
                    return oright;
                else
                    return oleft;
            }
        }
        if (id == 2)
            return arctic;
        if (id == 3)
            return volcanoBiome;
        if (id == 4)
            return desert;
        else
            return null;
    }

    public MsgWriter getRoomInfo(GameClient client, boolean reconnect) {
        List<Hill> hills = new ArrayList<>();
        List<Waterspot> waterspot = new ArrayList<>();
        List<LakeIsland> lakeislands = new ArrayList<>();
        List<Oasis> oasisf = new ArrayList<>();
        List<GameObject> berryspot = new ArrayList<>();
        List<River> rivers = new ArrayList<>();
        List<Mudspot> muds = new ArrayList<>();
        List<Lake> lakes = new ArrayList<>();
        List<IsLand> islands = new ArrayList<>();
        for (GameObject gmob : this.objects) {
            if (gmob instanceof River) {
                rivers.add((River) gmob);
            }
            if (gmob instanceof Waterspot) {
                waterspot.add((Waterspot) gmob);
            }
            if (gmob instanceof Berryspot || gmob instanceof PlanktonBush) {
                berryspot.add(gmob);
            }
            if (gmob instanceof LakeIsland) {
                lakeislands.add((LakeIsland) gmob);
            }
            if (gmob instanceof IsLand) {
                islands.add((IsLand) gmob);
            }
            if (gmob instanceof Oasis) {
                oasisf.add((Oasis) gmob);
            }
            if (gmob instanceof Hill)
                hills.add((Hill) gmob);
            if (gmob instanceof Mudspot)
                muds.add((Mudspot) gmob);
            if (gmob instanceof Lake)
                lakes.add((Lake) gmob);
        }
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.NEWGAMEROOM);
        writer.writeUInt8(client.isSpectating() ? 1 : 0);

        writer.writeUInt16((short) this.width);
        writer.writeUInt16((short) this.height);
        writer.writeUInt8(this.gameMode);

        writer.writeUInt16((short) (client.getCamX() * 4));
        writer.writeUInt16((short) (client.getCamY() * 4));
        writer.writeUInt32(client.getCamzoom());
        writer.writeUInt8(reconnect ? 1 : 0);




        writer.writeUInt16((short) Constants.OCEANW);
        writer.writeUInt16((short) Constants.OCEANH);
        writer.writeUInt16((short) Constants.ARCTICW);
        writer.writeUInt16((short) Constants.ARCTICH);
        writer.writeUInt16((short) Constants.DESERTW);
        writer.writeUInt16((short) Constants.DESERTH);
        writer.writeUInt16((short) Constants.LANDW);
        writer.writeUInt16((short) Constants.LANDH);

        writer.writeUInt16((short) rivers.size()); // rivers
        for (River river : rivers) {
            writer.writeUInt16((short) river.getWidth());
            writer.writeUInt16((short) river.getHeight());
            writer.writeUInt16((short) (river.getX() - (river.getWidth() / 2)));
            writer.writeUInt16((short) (river.getY() + (river.getHeight() / 2)));
        }

        if (volcanoBiome != null) {
            writer.writeUInt16((short) 1); // volcano
            writer.writeUInt16(volcanoBiome.getRadius() / 5);
            writer.writeUInt16((short) Math.round(volcanoBiome.getX()));
            writer.writeUInt16((short) Math.round(volcanoBiome.getY()));
        } else {
            writer.writeUInt16((short) 0);
        }

        writer.writeUInt16((short) lakes.size()); // lakes
        for (Lake lake : lakes) {
            writer.writeUInt16((short) Math.round(lake.getX()));
            writer.writeUInt16((short) Math.round(lake.getY()));
            writer.writeUInt16(lake.getRadius() / 5);
        }

        writer.writeUInt16((short) muds.size()); // mud
        for (Mudspot mud : muds) {
            writer.writeUInt16((short) Math.round(mud.getX()));
            writer.writeUInt16((short) Math.round(mud.getY()));
            writer.writeUInt16(mud.getRadius() / 5);
        }

        writer.writeUInt16((short) this.arctic.ices.size()); // arcticIce
        for (ArcticIce ice : this.arctic.ices) {
            writer.writeUInt16((short) Math.round(ice.getX()));
            writer.writeUInt16((short) Math.round(ice.getY()));
            writer.writeUInt16(ice.getRadius() / 5);
        }

        writer.writeUInt16((short) hills.size()); // hills
        for (Hill hill : hills) {
            writer.writeUInt8(hill.getBiome());
            writer.writeUInt16((short) Math.round(hill.getX()));
            writer.writeUInt16((short) Math.round(hill.getY()));
            writer.writeUInt16(hill.getRadius() / 5);
        }

        writer.writeUInt16((short) lakeislands.size()); // lakeisland
        for (LakeIsland lakeisland : lakeislands) {
            writer.writeUInt16((short) Math.round(lakeisland.getX()));
            writer.writeUInt16((short) Math.round(lakeisland.getY()));
            writer.writeUInt16(lakeisland.getRadius() / 5);
        } // lake islands

        // writer.writeUInt16((short) islands.size()); // lakeisland (net)
        // for (IsLand island : islands) {
        // writer.writeUInt16((short) Math.round(island.getX()));
        // writer.writeUInt16((short) Math.round(island.getY()));
        // writer.writeUInt16(island.getRadius() / 5);
        // } // lake islands (net)

        writer.writeUInt16((short) berryspot.size()); // berryspot
        for (GameObject berrysp : berryspot) {
            writer.writeUInt16((short) Math.round(berrysp.getX()));
            writer.writeUInt16((short) Math.round(berrysp.getY()));
        } // berrySpot

        writer.writeUInt16((short) waterspot.size()); // waterSpot
        for (Waterspot watersp : waterspot) {
            writer.writeUInt16((short) Math.round(watersp.getX()));
            writer.writeUInt16((short) Math.round(watersp.getY()));
        } // waterSpot

        writer.writeUInt16((short) 0); // quickSand

        writer.writeUInt16((short) oasisf.size()); // lakes
        for (Oasis oasisa : oasisf) {
            writer.writeUInt16((short) Math.round(oasisa.getX()));
            writer.writeUInt16((short) Math.round(oasisa.getY()));
            writer.writeUInt16(oasisa.getRadius() / 5);
        } // oasis

        writer.writeUInt16((short) 1); // rainforest
        writer.writeUInt16((short) Math.round(forest.getWidth()));
        writer.writeUInt16((short) Math.round(forest.getHeight()));
        writer.writeUInt16((short) Math.round(forest.getX()));
        writer.writeUInt16((short) Math.round(forest.getY()));


        return writer;

    }

    public void joinGameAfterReconnect(GameClient client, boolean isFirstRequest){
        MsgWriter writer1 = new MsgWriter();
        writer1.writeType(MessageType.PLAYGAME);
        writer1.writeUInt8(isFirstRequest ? 1 : 0);
        client.send(writer1);
    }

    public void removeClient(GameClient client) {
        clients_to_remove.add(client);
    }

    private int fightnum = 0;

    public int getFightNumber() {
        int a = fightnum;
        fightnum++;
        return a;
    }
}
