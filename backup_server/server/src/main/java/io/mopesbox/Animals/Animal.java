package io.mopesbox.Animals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.mopesbox.Animals.Tier8.Walrus;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier10.Pelican;
import io.mopesbox.Animals.Tier10.Tiger;
import io.mopesbox.Animals.Tier11.Falcon;
import io.mopesbox.Animals.Tier3.Penguin;
import io.mopesbox.Animals.Tier11.Octopus;
import io.mopesbox.Animals.Tier12.Eagle;
import io.mopesbox.Animals.Tier12.Markhor;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier14.BlackwidowSpider;
import io.mopesbox.Animals.Tier14.Cassowary;
import io.mopesbox.Animals.Tier14.GiantSpider;
import io.mopesbox.Animals.Tier15.Bigfoot;
import io.mopesbox.Animals.Tier15.Pterodactyl;
import io.mopesbox.Animals.Tier16.LandMonster;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier17.KingDragon;
import io.mopesbox.Animals.Tier3.Chicken;
import io.mopesbox.Animals.Tier3.Crab;
import io.mopesbox.Animals.Tier3.Duckling;
import io.mopesbox.Animals.Tier3.Mole;
import io.mopesbox.Animals.Tier2.Pigeon;
import io.mopesbox.Animals.Tier4.Armadillo;
import io.mopesbox.Animals.Tier4.Seal;
import io.mopesbox.Animals.Tier4.Woodpecker;
import io.mopesbox.Animals.Tier5.BulletAnt;
import io.mopesbox.Animals.Tier5.Deer;
import io.mopesbox.Animals.Tier5.Flamingo;
import io.mopesbox.Animals.Tier5.Gazelle;
import io.mopesbox.Animals.Tier5.Reindeer;
import io.mopesbox.Animals.Tier7.Macaw;
import io.mopesbox.Animals.Tier7.Warthog;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Animals.Tier6.Duck;
import io.mopesbox.Animals.Tier6.Hedgehog;
import io.mopesbox.Animals.Tier8.Cobra;
import io.mopesbox.Animals.Tier8.Giraffe;
import io.mopesbox.Animals.Tier8.Snail;
import io.mopesbox.Animals.Tier8.Snowyowl;
import io.mopesbox.Animals.Tier6.Peacock;
import io.mopesbox.Animals.Tier3.Meerkat;
import io.mopesbox.Animals.Tier9.Toucan;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Animals.Tier9.PufferFish;
import io.mopesbox.Animals.Tier9.RattleSnake;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Arctic.ArcticIce;
import io.mopesbox.Objects.Dangerous.MopeCoin;
import io.mopesbox.Objects.ETC.AntBite;
import io.mopesbox.Objects.ETC.BigWhirlpool;
import io.mopesbox.Objects.ETC.DirtSpot;
import io.mopesbox.Objects.ETC.PumpkinBall;
import io.mopesbox.Objects.ETC.ScorpionClaw;
import io.mopesbox.Objects.ETC.SinkHole;
import io.mopesbox.Objects.ETC.TrexShake;
import io.mopesbox.Objects.ETC.blackWidowAbility;
import io.mopesbox.Objects.Eatable.Egg;
import io.mopesbox.Objects.Eatable.Water;
import io.mopesbox.Objects.Eatable.gift;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Objects.Static.Berryspot;
import io.mopesbox.Objects.Static.HidingBush;
import io.mopesbox.Objects.Static.ArcticBush;
import io.mopesbox.Objects.Static.Hill;
import io.mopesbox.Objects.Static.LakeIsland;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Objects.Static.Mudspot;
import io.mopesbox.Objects.Static.Oasis;
import io.mopesbox.Objects.Static.PlanktonBush;
import io.mopesbox.Objects.Static.Tree;
import io.mopesbox.Objects.Static.sweatPoison;
import io.mopesbox.Server.FlagWriter;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Animal extends GameObject {

    protected double speed;
    public double mainSpeed = 16;

    public ArrayList<Point> tails = new ArrayList<>();
    public Point mouth;

    private Timer upgradeTimer = new Timer(2000);



    private double smoothVelocityX;
    private double smoothVelocityY;
    public int playerNum;
    public boolean canmove = true;
    public boolean ability_mark = false;

    public boolean inArena;
    public boolean isHiding = false;
    // private boolean shouldShow = false;
    public boolean isGhost = false;
    public boolean inEgg = false;
    public boolean bypass_waterani_slowness = false; // для черепахи, . . .

    public List<PumpkinBall> pumpkinBalls = new ArrayList<>();

    public boolean canMove(){
        return canmove;
    }
    public void setMove(boolean a){
        this.canmove = a;
    }

        public boolean makAbilActive(){
        return ability_mark;
    }
    public void ability_mark_set(boolean a){
        this.ability_mark = a;
    }

    public AnimalInfo getInfo() {
        return info;
    }

    public void resetUpgradeTimer(){
        upgradeTimer.reset();
    }


    public void setSpeed(double sp) {
        this.speed = sp;
    }

    public double getSpeed() {
        return this.speed;
    }

    public boolean isDivePossible() {
        return divePossible;
    }

    public boolean isDiveMain() {
        return diveMain;
    }

    public void setDiveMain(boolean a) {
        diveMain = a;
    }

    public void onKill(Animal victim) {
        // overwrite
    }

    public void setDivePossible(boolean divePossible) {
        this.divePossible = divePossible;
    }

    public boolean isDiveUsable() {
        return diveUsable;
    }

    public void setDiveUsable(boolean diveUsable) {
        this.diveUsable = diveUsable;
    }

    public boolean isDiveRecharging() {
        return diveRecharging;
    }

    public GameObject getHole() {
        return hidingHole;
    }

    public void setDiveRecharging(boolean diveRecharging) {
        this.diveRecharging = diveRecharging;
    }

    public boolean isDiveActive() {
        return diveActive;
    }

    public void setDiveActive(boolean diveActive) {
        this.diveActive = diveActive;
    }

    public boolean divePossible = false;
    public boolean diveMain = false;
    public boolean diveUsable = false;
    public boolean diveRecharging = false;
    public boolean diveActive = false;

    private AnimalInfo info;
    private String playerName;

    private boolean canBeInHole = true;
    private boolean canBeInSmallHole = true;

    public void setCanBeInHole(boolean a) {
        canBeInHole = a;
    }

    public void setCanBeInSmallHole(boolean a) {
        canBeInSmallHole = a;
    }

    public boolean canBeInHole() {
        return canBeInHole;
    }

    public boolean canBeInSmallHole() {
        return canBeInSmallHole;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    private boolean isBoosting = false;

    private GameObject hidingHole = null;

    public boolean isInHole() {
        return hidingHole != null;
    }

    private Timer holeSpawnTimer = new Timer(1000);
    private boolean leavedHole = false;

    public void setHole(GameObject hole) {
        int oldHole = hidingHole != null ? hidingHole.getId() : -1;
        hidingHole = hole;
        if (hidingHole != null) {
            this.hidingHoleVisibilityRad = 15;
            this.lastHidingHoleID = hidingHole.getId();
            this.getClient().getRoom().removeVisObj(this, hidingHole, this.getClient());
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                if(this.getInfo().getType() == AnimalType.FOX ){
                    this.getInfo().getAbility().setUsable(true);
                }
                this.setDiveUsable(false);
                this.setDiveActive(false);
      
            }
        } else {
            this.hidingHoleVisibilityRad = 0;
            this.lastHidingHoleID = 0;
            if (this.getInfo().getAbility().getType() != 0 && !this.isInvincible()) {
                this.getInfo().getAbility().setUsable(true);
                this.setDiveUsable(true);
            }
            this.getClient().getRoom().removeVisObj2(this.getClient());
            if (oldHole != -1) {
                this.setSpawnID(oldHole);
                holeSpawnTimer.reset();
                leavedHole = true;
            }
        }
    }

    public int developerModeNumber;
    public boolean flag_lowWat;
    public boolean flag_inHomeBiome;
    public boolean flag_underWater;
    public boolean flag_eff_invincible;
    public boolean flag_usingAbility;
    public boolean flag_tailBitten;
    public boolean flag_eff_stunned;
    public boolean flag_iceSliding;
    public boolean flag_eff_frozen;
    public boolean flag_eff_onFire;
    public boolean flag_eff_healing;
    public boolean flag_eff_poison;
    public boolean flag_constricted;
    public boolean flag_webStuck;
    public boolean flag_dirtStuck;
    public boolean flag_stealth;
    public boolean flag_eff_bleeding;
    public boolean flag_flying;
    public boolean flag_isGrabbed;
    public boolean flag_eff_aniInClaws;
    public boolean flag_eff_stunk;
    public boolean flag_cold;
    public boolean flag_inWater;
    public boolean flag_inLava;
    public boolean flag_canClimbHill;
    public boolean flag_isClimbingHill;
    public boolean flag_isDevMode;
    public boolean flag_eff_slimed;
    public boolean flag_eff_wobbling;
    public boolean flag_eff_hot;
    public boolean flag_eff_sweatPoisoned;
    public boolean flag_eff_shivering;
    public boolean flag_inHidingHole;
    public boolean flag_eff_grabbedByFlytrap;
    public boolean flag_eff_aloeveraHealing;
    public boolean flag_eff_tossedInAir;
    public boolean flag_eff_isOnSpiderWeb;
    public boolean flag_fliesLikeDragon;
    public boolean flag_eff_isInMud;
    public boolean flag_eff_statue;
    public boolean flag_eff_isOnTree;
    public boolean flag_eff_isUnderTree;
    public boolean flag_speared;
    public boolean flag_eff_dirty;
    public boolean flag_eff_virusInfection;
    public boolean flag_eff_wearingMask;
    public boolean flag_eff_sanitized;
    public boolean flag_viewing1v1Invite;
    public boolean flag_ytmode;
    public boolean flag_zombieInfection;
    public boolean effecT_isZombie;
    // public  maxAnimalHealth;

    public boolean flag_hideDevPrint;

    public int clientID;

    public int arenaWins;
    private int lastHidingHoleID;
    private int hidingHoleVisibilityRad;
    // private int ArenaVisibilityRad;

    public int getWater() {
        return (int) ((((double) this.water) / ((double) this.maxwater)) * 100.0);
    }

    public void setWater(double water) {
        if (water > this.maxwater)
            water = this.maxwater;
        this.water = water;
    }

    public int getInfection() {
        return (int) ((((double) this.pd_infection) / ((double) 100)) * 100.0);
    }

    private boolean isInfected = true;

    public boolean isInfected() {
        return isInfected;
    }

    public void setInfected(boolean infected) {
        isInfected = infected;
    }

    public void setInfection(double infect) {
        if (infect > 100)
            infect = 100;
        this.pd_infection = infect;
    }
    public void addWater(double water) {
        if (water + this.water > this.maxwater)
            water = this.maxwater - this.water;
        this.water += water;
    }

    public void takeWater(double water) {
        if (!this.getClient().isGodmodeEnabled() || this.inArena) {
            if (this.water - water < 0)
                water = this.water;
            this.water -= water;
        }
    }

    private boolean canBeUnderTree = true;

    public void setCanBeUnderTree(boolean a) {
        this.canBeUnderTree = a;
    }

    public boolean getCanBeUnderTree() {
        return this.canBeUnderTree;
    }

    private boolean isFlyingOver = false;

    public void setFlyingOver(boolean a) {
        this.isFlyingOver = a;
        this.flag_fliesLikeDragon = true;
    }

    public boolean getFlyingOver() {
        return this.isFlyingOver;
    }

    private boolean canClimbHills = false;

    public void setCanClimbHills(boolean a) {
        this.canClimbHills = a;
    }

    public boolean getCanClimbHills() {
        return this.canClimbHills;
    }

    private boolean canClimbRocks = false;

    public void setCanClimbRocks(boolean a) {
        this.canClimbRocks = a;
    }

    public boolean getCanClimbRocks() {
        return this.canClimbRocks;
    }

    public void setMaxWater(int water) {
        this.maxwater = water;
    }

    public double water = 100;
    public double maxwater = 100;
    public double pd_infection = 100;
    private double customSpeed = -1;

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    double mouseX = 0;
    double mouseY = 0;

    public int baseRadius;

    public int getBaseRadius() {
        return baseRadius;
    }

    public GameClient client;

    private int immunitySeconds;

    public boolean isInvincible() {
        return immunitySeconds > 0;
    }

    public boolean isStunned() {
        return stunnedSeconds > 0;
    }

    public boolean isStatue() {
        return statueSeconds > 0;
    }

    public int fireSeconds;

    public int poisonSeconds;

    private int frozenSeconds;

    private int spearedSeconds;

    public GameClient getClient() {
        return this.client;
    }
    public boolean getBiteable(GameObject o){
                if (o instanceof Animal) {
                if(((Animal)o).getTier() == this.getTier() && this.getTier() > 14 && ((Animal)o).getTier() > 14)
                return false;
            if (this.flag_isGrabbed)
                return false;
                if(this.isDiveActive() || ((Animal)o).isDiveActive())
                return false;
                if(this.isInHole() || ((Animal)o).isInHole())
                return false;
            if(this.flag_flying && ((Animal)o).flag_flying && this.getTier() > 14 && ((Animal)o).getTier() > 14) return true;
        }
        return false;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof blackWidowAbility){
            if(this.getTier() < 17) return true;
        }
               if (o instanceof Animal) {
                if(this.biteCooldown > 0 || this.collisionBite > 0) return true;
                if(((Animal)o).biteCooldown > 0 || ((Animal)o).collisionBite > 0) return true;
                if(((Animal)o).getTier() == this.getTier() && this.getTier() > 14 && ((Animal)o).getTier() > 14)
                return false;
            if (this.flag_fliesLikeDragon)
                return false;
            if (this.flag_isGrabbed)
                return false;
            if(this.flag_flying && ((Animal)o).flag_flying) return true;
        }
        if (this.flag_flying)
            return false;
        if (this.isDiveActive())
            return false;
        if (this.isInHole())
            return false;
        return true;
    }

    public void setFire(int sec, Animal owner, double dam) {
        this.fireSeconds = sec;
        this.lastKiller = owner;
        if (dam > 0)
            this.hurt(dam, 13, owner);
    }

    private Animal poisonKiller = null;
    private double poisonHurt = 5;

    public void setPoisoned(int sec, Animal owner, double dam) {
        this.poisonSeconds = sec;
        this.lastKiller = owner;
        this.poisonHurt = dam;
        this.poisonKiller = owner;
    }

    public void setStun(int sec) {
        // if (!this.getClient().instantRecharge)
            this.stunnedSeconds = sec;
    }

    public void setWaterAniSlower(int sec) {
        this.waterAniSlowingSeconds = sec;
    }

    private Animal statueKiller;

    public void setStatue(int sec, int type, Animal killer) {
        this.statueSeconds = sec;
        this.statueType = type;
        this.statueKiller = killer;
    }

    public void setShivering(int sec, Animal killer) {
        this.shiveringSeconds = sec;
        this.shiveringBiter = killer;
    }

    public void setFrozen(int sec, Animal owner, int dam) {
        this.frozenSeconds = sec;
        this.lastKiller = owner;
        if (dam > 0)
            this.hurt(dam, 14, owner);
    }

    private double bleedDamage = 0;

    public void setSpeared(int sec, Animal owner, int dam) {
        this.spearedSeconds = sec;
        this.lastKiller = owner;
        this.setBleeding(sec, owner, dam);
        this.hurt(30, 14, owner);
    }

    public void setImmunity(int sec) {
        this.immunitySeconds = sec;
        this.immunityTimer.reset();
        if (this.immunitySeconds == 0) {
            if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                this.getInfo().getAbility().setUsable(true);
                this.setDiveUsable(true);
            }
        }
    }

    public GameObject arenaObject = null;

    private Timer tTimer = new Timer(1250);

    protected boolean isAbilityDone = true; // можно ли юзать абилку ВНЕ сандбокса?

    // private Timer watershootTimer = new Timer(250);

    public Animal(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info.getType() == AnimalType.DUCK ? 22 : info.getType() == AnimalType.DUCKLING ? 10 : Tier.byOrdinal(info.getTier()) != null ? Tier.byOrdinal(info.getTier()).getBaseRadius() : 25, 2);

     


        
        this.icoPlayer = client.icoPlayer;
        this.abilSpeed.isRunning = false;
        this.setCanBeInSmallHole(info.getTier() <= 6);
        if (Constants.GAMEMODE == 3)
            this.setTeam(client.getTeam());
        if (Tier.byOrdinal(info.getTier()).getWidth() != -1) {
            client.visibleW = Tier.byOrdinal(info.getTier()).getWidth();
        }
        if (Tier.byOrdinal(info.getTier()).getHeight() != -1) {
            client.visibleH = Tier.byOrdinal(info.getTier()).getHeight();
        }
        client.setCamzoom((int) (Tier.byOrdinal(info.getTier()).getBaseZoom() * 1000));
        client.baseCamzoom = (int) (Tier.byOrdinal(info.getTier()).getBaseZoom() * 1000);
        client.tier = info.getTier();

        // this.getClient().getRoom().chat(this, "anglenewspeed:")
        this.setImmunity(10);
        this.setMovable(true);
        this.baseRadius = this instanceof Duck ? 22 : this instanceof Duckling ? 10 : Tier.byOrdinal(info.getTier()).getBaseRadius() ;

        if (client.developerModeNumber != 0)
            this.developerModeNumber = client.developerModeNumber;

        else {

            if (client.isDeveloper() && !client.devHidden) {
                this.developerModeNumber = 14;

                if (client.isSuperDev()) {
                    this.developerModeNumber = 3;
                }
            } else
                this.developerModeNumber = 0;
            if (client.isArtist()) {
                this.developerModeNumber = 2;
                this.icoPlayer = 5;
            }

            if (client.isHelper()) {
                this.developerModeNumber = 2;
                this.icoPlayer = 8;
            }

            if (client.isModerator()) {
                this.developerModeNumber = 3;
                this.icoPlayer = 9;
            }

            if (client.isAdministrator()) {
                this.developerModeNumber = 13;
                this.icoPlayer = 10;
            }

            if (client.isDirector()) {
                this.developerModeNumber = 13;
                this.icoPlayer = 11;
            }



            if (!client.devHidden) {
                if (client.isSuperDev())
                    this.developerModeNumber = 2;
                else
                    this.developerModeNumber = 1;
            } else
                this.developerModeNumber = 0;
        }
       
        info.getAbility().setRecharging(false);
        this.info = info;
        this.playerName = playerName;

        this.clientID = 0;


        this.flag_lowWat = false;
        this.flag_inHomeBiome = false;
        this.flag_underWater = false;
        this.flag_eff_invincible = false;
        this.flag_usingAbility = false;
        this.flag_tailBitten = false;
        this.flag_eff_stunned = false;
        this.flag_iceSliding = false;
        this.flag_eff_frozen = false;
        this.flag_eff_onFire = false;
        this.flag_eff_healing = false;
        this.flag_eff_poison = false;
        this.flag_constricted = false;
        this.flag_webStuck = false;
        this.flag_stealth = false;
        this.flag_eff_bleeding = false;
        this.flag_flying = false;
        this.flag_isGrabbed = false;
        this.flag_eff_aniInClaws = false;
        this.flag_eff_stunk = false;
        this.flag_cold = false;
        this.flag_inWater = false;
        this.flag_inLava = false;
        this.flag_canClimbHill = false;
        this.flag_isClimbingHill = false;
        this.flag_isDevMode = client.isDeveloper() || client.isArtist() || client.isHelper() || client.isModerator() || client.isAdministrator() || client.isDirector();
        this.flag_eff_slimed = false;
        this.flag_eff_wobbling = false;
        this.flag_eff_hot = false;
        this.flag_eff_sweatPoisoned = false;
        this.flag_eff_shivering = false;
        this.flag_inHidingHole = false;
        this.flag_eff_grabbedByFlytrap = false;
        this.flag_eff_aloeveraHealing = false;
        this.flag_eff_tossedInAir = false;
        this.flag_eff_isOnSpiderWeb = false;
        this.flag_fliesLikeDragon = false;
        this.flag_eff_isInMud = false;
        this.flag_eff_statue = false;
        this.flag_eff_isOnTree = false;
        this.flag_ytmode = client.isYTMode();
        this.flag_eff_isUnderTree = false;
        this.flag_speared = false;
        this.flag_eff_dirty = false;

        this.flag_hideDevPrint = false;

        // developer stuff

        this.inArena = false;

        this.playerNum = 0;
        this.arenaWins = 0;

        this.lastHidingHoleID = 0;
        this.hidingHoleVisibilityRad = 0;

        // this.ArenaVisibilityRad = 0;

        this.smoothVelocityX = 0;
        this.smoothVelocityY = 0;

        this.speed = 12;
        this.customSpeed = -1;

        this.client = client;

        this.sendsAngle = true;

        if (this.getClient().getRoom().getGameMode() == 2 && this.getClient().getRoom().br_gamestate < 2) {
            this.inEgg = true;
            this.egg = new Egg(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                    (int) Math.round(this.getRadius() * 2),
                    this);
            this.egg.setAngle(Math.random() * 2);
            this.getClient().getRoom().removeVisObjEggs(this, this.getClient());
            this.getClient().getRoom().addObj(this.egg);
        }
        if(info != null && (info.getBiome() == 1 || !(this instanceof Pterodactyl) && !(this instanceof Eagle) && !(this instanceof BlackDragon) && !(this instanceof KingDragon)) && !this.getClient().isBot()) this.setDiveUsable(true);
        if(info != null && (info.getBiome() == 1 || !(this instanceof Pterodactyl) && !(this instanceof Eagle) && !(this instanceof BlackDragon) && !(this instanceof KingDragon)) && !this.getClient().isBot()) this.setDivePossible(true);
    }

    public Egg egg;

    private int diveTim = 0;

            private Timer diveTimer = new Timer(640);

    private Timer immunityTimer = new Timer(300);
    private Timer fireTimer = new Timer(750);
    private Timer paralizeTimer = new Timer(1000);
    private Timer poisonTimer = new Timer(1000);
    private Timer spearedTimer = new Timer(1000);
    private Timer frozenTimer = new Timer(1000);
    private Timer stunTimer = new Timer(1000);
    private Timer waterAniTimer = new Timer(1000);
    private Timer bleedingTimer = new Timer(1000);
    private Timer statueTimer = new Timer(1000);
    private Timer shiveringTimer = new Timer(1000);
    private Timer hpTimer = new Timer(2500);
    private Timer hurtTimer = new Timer(150);
    public Timer biteTimer = new Timer(1000);
    public Timer collisionTimer = new Timer(1000);

    public ArrayList<AnimalType> dangerTypes = new ArrayList<>();
    public ArrayList<AnimalType> edibleTypes = new ArrayList<>();
    public boolean isIslandCollided = false;

    public int lastReason;

    public double biteDamage = 60;

    public int biteCooldown;
    public int collisionBite;

    public GameObject lastKiller = null;

    private List<Tree> underTrees = new ArrayList<>();

    public List<Tree> getUnderTree() {
        return underTrees;
    }

    private List<Tree> overTrees = new ArrayList<>();

    public List<Tree> getOverTree() {
        return overTrees;
    }

    public void checkDiving() {
        if (this.isDiveActive()) {
            if (this.getBiome() != 1 && this.getInfo().getBiome() != BiomeType.VOLCANO.ordinal()) {
                this.disableDiving();
            }
            if(this.isDiveActive() && !this.flag_inLavaLake && this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal()){
                this.disableDiving();
            }
        } else {
            if ((this.getBiome() == 1 && !this.isInHole()) || (this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal() && !this.isInHole() && this.flag_inLavaLake)) {
                this.setDiveUsable(true);
            } else {
                this.setDiveUsable(false);
            }
        }
    }



    public void forcedBoost(double x, double y, double speed) {
        double dx = x - this.getX();
        double dy = y - this.getY();

        double theta = Math.atan2(dy, dx);

        if (theta < 0)
            theta += Math.PI * 2;

        double velocityX = Math.cos(theta);
        double velocityY = Math.sin(theta);

        this.smoothVelocityX = velocityX * speed;
        this.smoothVelocityY = velocityY * speed;

        this.addVelocityX(Utils.lerp(0, this.smoothVelocityX, 0.2));
        this.addVelocityY(Utils.lerp(0, this.smoothVelocityY, 0.2));
    }

    private boolean isWaterfowl = false;

    public boolean isWaterfowl() {
        return this.isWaterfowl;
    }

    public void setWaterfowl(boolean a) {
        this.isWaterfowl = a;
    }

    public void makeBoost(double power) {
        if(this.getClient() != null && this.getClient().freezed)
        return;
        double dx = this.mouseX - this.getX();
        double dy = this.mouseY - this.getY();

        double theta = Math.atan2(dy, dx);
        double dtheta = 0+theta;

        dtheta *= 180 / Math.PI; // rads to degs, range (-180, 180]
        if (dtheta < 0) {
            dtheta += 360;
        }

        this.iceangle = dtheta;

        if (theta < 0)
            theta += Math.PI * 2;
        

        double sspeed = (this.customSpeed == -1 ? this.speed : this.customSpeed);
        sspeed = this.speedManipulation(sspeed);
        if (isGhost)
            sspeed = 20;
        double speed = sspeed * 10;

        double velocityX = Math.cos(theta);
        double velocityY = Math.sin(theta);

        this.takeWater(2);

        if (power != 0) {
            velocityX *= power;
            velocityY *= power;
        }

        this.smoothVelocityX = velocityX * speed;
        this.smoothVelocityY = velocityY * speed;

        this.addVelocityX(Utils.lerp(0, this.smoothVelocityX, 0.2));
        this.addVelocityY(Utils.lerp(0, this.smoothVelocityY, 0.2));
    }

    private Timer rammedTimer = new Timer(1000);

    private int rammed = 0;

    private int rammedH;

    private boolean rammedUp;

    private boolean rammedDone = false;
    private int statueSeconds;
    private int stunnedSeconds;

    private int waterAniSlowingSeconds;

    private int shiveringSeconds;

    public boolean isUnderTree() {
        return this.underTrees.size() > 0;
    }

    public boolean isOverTree() {
        return this.overTrees.size() > 0;
    }

    @Override
    public void onCollisionEnter(GameObject o) {
        if (o instanceof Tree && !this.flag_flying && !this.isDiveActive()) {
            if (this.getCanBeUnderTree() && this.underTrees.size() > 0) {
                if (!this.underTrees.contains((Tree) o))
                    this.underTrees.add((Tree) o);
            } else {
                if (!this.overTrees.contains((Tree) o))
                    this.overTrees.add((Tree) o);
            }
        }
    }
        public double getHurtHP(Animal prey) {
        return prey.maxHealth / 6;
    }
        public boolean canSetBiteCooldown(Animal ani, boolean tailbite) {
        return true;
    }
        


    @Override
    public void onCollision(GameObject o) {   
        if (o instanceof Tree && !this.flag_flying && !this.isDiveActive()) {
            if (this.getCanBeUnderTree()) {
                if (!this.underTrees.contains((Tree) o))
                    this.underTrees.add((Tree) o);
            } else {
                if (!this.overTrees.contains((Tree) o))
                    this.overTrees.add((Tree) o);
            }
        } else if (o instanceof Tree && (this.flag_flying || this.isDiveActive())) {
            if (this.getCanBeUnderTree()) {
                if (this.underTrees.contains((Tree) o))
                    this.underTrees.remove((Tree) o);
            } else {
                if (this.overTrees.contains((Tree) o))
                    this.overTrees.remove((Tree) o);
            }
        }
    }


    @Override
    public void onCollisionExit(GameObject o) {
        if (o instanceof Tree) {
            if (this.getCanBeUnderTree()) {
                if (this.underTrees.contains((Tree) o) && this.underTrees.indexOf(((Tree)o)) != -1)
                    this.underTrees.remove((Tree) o);
            } else {
                if (this.overTrees.contains((Tree) o))
                    this.overTrees.remove((Tree) o);
            }
        }
    }

    public void setMaxHealth(double max) {
        this.maxHealth = max;
        this.health = this.maxHealth;
    }
    

    private boolean canRegenerateHP = true;
    private int hpPerSecond = 5;
    private Timer regenHP = new Timer(1000);

    public void setCanRegenerateHP(boolean toggle) {
        canRegenerateHP = toggle;
    }

    public boolean canRegenerateHP() {
        return canRegenerateHP;
    }

    public void setHPPerSecond(int hp) {
        hpPerSecond = hp;
    }

    protected boolean isInBigWhirlpool = false;

    private int eff_dirtType = 0;
    private int oldWaterValue;

    public void disableDiving() {
        this.setDiveActive(false);
        if (!this.getClient().instantRecharge) {
            setDiveRecharging(true);
            diveTim = 2;
            this.getClient().send(Networking.rechargingAbility(true, diveTim, this.getClient().getRoom()));
            this.setWater(oldWaterValue);
        }
    }

    public void diveRecharge(){
        if (diveTim > 0) {
            diveTimer.update(Constants.TICKS_PER_SECOND);
            if (diveTimer.isDone()) {
                diveTim--;
                if (diveTim < 1) {
                    this.setDiveRecharging(false);
                }
                diveTimer.reset();
            }
        }
    }

    public void updateCollided() {
        if(!this.collidedList.values().stream().anyMatch(LavaLake.class::isInstance)){
            flag_inLavaLake = false;
        }
        if (!this.inArena && !this.isRammed && !this.flag_flying) {
            int collidedHills = 0;
            int collidedSinkHoles = 0;
            int collidedBigWhirlpools = 0;
            int collidedScorpionClaws = 0;
            int collidedIslands = 0;
            int collidedArcticIce = 0;
            int collidedTrexClaws = 0;
            int collidedAntBites = 0;
            int collidedMudspot = 0;
            int collidedHidingBushs = 0;
            int collidedArcticBushs = 0;
            int collidedBerryspot = 0;
            int collidedPlanktonspot = 0;
            int collidedOasis = 0;

            for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
                GameObject a = entry.getValue();
                if (a instanceof Hill && !this.isInHole() && !this.isDiveActive())
                    collidedHills++;
                if (a instanceof SinkHole && ((SinkHole) a).owner != this && !this.isInvincible() && !this.isInHole()
                        && !this.isDiveActive())
                    collidedSinkHoles++;
                if (a instanceof BigWhirlpool && ((BigWhirlpool) a).owner != this && !this.isInvincible()
                        && !this.isInHole() && !this.isDiveActive())
                    collidedBigWhirlpools++;
                if (a instanceof ScorpionClaw && ((ScorpionClaw) a).owner != this && !this.isInvincible()
                        && ((ScorpionClaw) a).gotcha == this && !this.isInHole() && !this.isDiveActive())
                    collidedScorpionClaws++;
                if (a instanceof TrexShake && ((TrexShake) a).owner != this && !this.isInvincible()
                        && ((TrexShake) a).gotcha == this && !this.isInHole() && !this.isDiveActive())
                    collidedTrexClaws++;
                if (a instanceof AntBite && ((AntBite) a).owner != this && !this.isInvincible()
                        && ((AntBite) a).gotcha == this && !this.isInHole() && !this.isDiveActive())
                    collidedAntBites++;
                if (a instanceof ArcticIce && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedArcticIce++;
                if (a instanceof Mudspot && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedMudspot++;
                    if (a instanceof DirtSpot && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedMudspot++;
                if (a instanceof Oasis && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedOasis++;
                if (a instanceof HidingBush && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedHidingBushs++;
                if (a instanceof ArcticBush && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedArcticBushs++;
                if (a instanceof Berryspot && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedBerryspot++;
                if (a instanceof PlanktonBush && !this.flag_fliesLikeDragon && !this.isInHole() && !this.isDiveActive())
                    collidedPlanktonspot++;
                if (a instanceof LakeIsland && !this.isInHole() && !this.isDiveActive())
                    collidedIslands++;
    }
            int finalRadius = this.baseRadius;
            if (this.getCanClimbHills() && !this.getFlyingOver() && collidedHills > 0 && !this.isDiveActive()
                    && !this.isInHole()) {
                finalRadius += this.baseRadius/4;
                this.flag_isClimbingHill = true;
            } else {
                this.flag_isClimbingHill = false;
            }
            if (collidedSinkHoles > 0 || collidedBigWhirlpools > 0) {
                finalRadius -= this.baseRadius / 1.4;
            }
            // if(this.player.getXP = true) {
            //     this.getPlayer().baseRadius = this.getPlayer().baseRadius + 10
            // }
            if (collidedBigWhirlpools > 0) {
                this.isInBigWhirlpool = true;
            } else {
                this.isInBigWhirlpool = false;
            }
            if (collidedArcticIce > 0 && collidedHills <= 0 && this.isSlidingOnIce()) {
                this.isOnIce = true;
            } else {
                this.isOnIce = false;
            }
            if (collidedScorpionClaws > 0 || collidedTrexClaws > 0 || collidedAntBites > 0) {
                this.flag_isGrabbed = true;
            } else {
                this.flag_isGrabbed = false;
            }
            if (collidedMudspot > 0 || collidedOasis > 0 && !this.flag_fliesLikeDragon) {
                this.flag_eff_isInMud = true;
                if(collidedMudspot > 0) {
                    this.flag_eff_dirty = true;
                    this.eff_dirtType = 1;
                } else {
                    this.flag_eff_dirty = false;
                    this.eff_dirtType = 0;
                }
            } else {
                this.flag_eff_isInMud = false;
                this.flag_eff_dirty = false;
                this.eff_dirtType = 0;
            }
            if (this.inArena || collidedHidingBushs > 0  || collidedArcticBushs > 0 || ((collidedBerryspot > 0 && collidedPlanktonspot > 0) 
            && this.getInfo().getTier() >= 6 && this.getInfo().getTier() < 15)) { 
                this.flag_stealth = true; //idiotfuck
                // this.ArenaVisibilityRad = 100;
            } else {
               if(!(this instanceof BlackwidowSpider && this.getSpecies() == 1))this.flag_stealth = false;
            }
            if (collidedIslands > 0) {
                this.isIslandCollided = true;
            } else {
                this.isIslandCollided = false;
            }
            if (finalRadius < 7)
                finalRadius = 7;
            this.setRadius(finalRadius);
        } else {
            this.isInBigWhirlpool = false;
            this.flag_isGrabbed = false;
            this.flag_stealth = this.inArena;
            this.isIslandCollided = false;
            this.flag_eff_isInMud = true;
            this.isInBigWhirlpool = false;
            this.flag_isClimbingHill = false;
            this.flag_eff_dirty = false;
            this.isOnIce = false;
            this.eff_dirtType = 0;
        }
    }

    private boolean canShootFire = false;

    public boolean canShootFire() {
        return this.canShootFire;
    }

    public void setCanShootFire(boolean a) {
        this.canShootFire = a;
    }

    protected int fireShootSpecType = 0;

    public void waterShoot() {
        if (this.isGhost || this.inEgg)
            return;
        if (this.pumpkinBalls.size() > 0) {
            this.pumpkinBalls.get(0).Throw();
        } else if (!this.isRammed && !this.inArena && !this.isInHole() && !this.flag_eff_invincible) {
            if (this.getClient().getShootType() != 0) {
                if (this.getClient().getShootType() == 1) {
                    if (this.getWater() > this.maxwater / 4) {
                        double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                        FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX() + x,
                                this.getY() + y, 10, fireShootSpecType, this.getAngle(), 12, this.getId(),
                                this.getClient().getRoom(), this.getClient(), 1);
                        this.getClient().getRoom().addObj(newFire);
                        this.takeWater(5);
                    }
                }
                if (this.getClient().getShootType() == 2) {
                    if (this.getWater() > this.maxwater / 4) {
                        double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));

                        FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX() - x,
                                this.getY() - y, 10, fireShootSpecType, -this.getAngle(), 12, this.getId(),
                                this.getClient().getRoom(), this.getClient(), 1);
                        this.getClient().getRoom().addObj(newFire);
                        FireBall newFire1 = new FireBall(this.getClient().getRoom().getID(), this.getX() + x,
                                this.getY() + y, 10, fireShootSpecType, this.getAngle(), 12, this.getId(),
                                this.getClient().getRoom(), this.getClient(), 1);
                        this.getClient().getRoom().addObj(newFire1);
                        this.takeWater(5);
                    }
                }
                if (this.getClient().getShootType() == 3) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));

                    MopeCoin mopeCoin = new MopeCoin(this.getClient().getRoom().getID(), this.getX() + x,
                            this.getY() + y, 18, this.getAngle(), this.getClient().getRoom());
                    this.getClient().getRoom().addObj(mopeCoin);
                }
                         if (this.getClient().getShootType() == 4) {
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));

                    gift mopeCoin = new gift(getClient().getRoom().getID(), this.getX()+x, this.getY()+y, this.getBiome(), getClient().getRoom());
                    mopeCoin.setAngle(this.getAngle());
                    this.getClient().getRoom().addObj(mopeCoin);
                    
                            
                }
            }
            if (this.getClient().getShootType() == 0) {
                if (this.getBarType() == 0 && this.getBiome() != 1) {
                    if (this.getWater() > this.maxwater / 4) {
                        
                        Water newWater = new Water(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                                this.getBiome(), 25, this.getAngle(), this, this.getClient().getRoom());
                        this.getClient().getRoom().addObj(newWater);
                        this.takeWater(5);
                      
                    }
                }
                if ((this.getBarType() == 2 && this.canShootFire())) {
                    if (this.getWater() > this.maxwater / 4) {
                        double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 1.2)));
                        FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX() + x,
                                this.getY() + y, 10, fireShootSpecType, this.getAngle(), 12, this.getId(),
                                this.getClient().getRoom(), this.getClient(), 1);
                        this.getClient().getRoom().addObj(newFire);
                        this.takeWater(5);
                    }
                }
            }
        }
    }

    private Timer thirstTimer = new Timer(1000);
    private Animal shiveringBiter = null;
    public boolean ignoreBiomes = false;
    private int ignoringBiomes = 0;

    public void ignoreBiomes() {
        ignoreBiomes = true;
        ignoringBiomes = 2;
    }

    @Override
    public void resetCollidedList() {
        super.resetCollidedList();
        if (ignoringBiomes > 0)
            ignoringBiomes--;
        if (ignoringBiomes <= 0) {
            ignoreBiomes = false;
            ignoringBiomes = 0;
        }
    }

    private Timer healingTimer = new Timer(1000);
    private Timer aloeVeraTimer = new Timer(250);
    private Timer aloeVeraLeafTimer = new Timer(1000);
    private Timer aloeVeraLeafDefTimer = new Timer(10000);
    private Timer aloeVeraDefTimer = new Timer(30000);
    private Timer stoneHealingTimer = new Timer(600);
    private boolean regenHealingHP = false;
    private int regenerhp = 0;
    public boolean flag_inLavaLake = false;
    private boolean aloeVeraLeafHealing = false;
    private boolean aloeVeraHealing = false;
    public void setAloeVeraLeafHealing(boolean a) {
        aloeVeraLeafHealing = a;
        this.getClient().send(Networking.personalGameEvent(255, "Aloe Vera Leaf regenerates your health!"));
    }

    public void setAloeVeraHealing(boolean a) {
        aloeVeraHealing = a;
        this.getClient()
                .send(Networking.personalGameEvent(255, "Aloe Vera Plant make your invincible for 30 seconds!"));
    }

    public void regenerateHP(int hp) {
        regenerhp = hp;
        regenHealingHP = true;
        this.healingTimer.reset();
    }
    public Room getRoom(){
        return this.getClient().getRoom();
    }
    public int getTier(){
        return getInfo().getTier();
    }
    public void beforeUpgrade(Animal animal){
                if(this instanceof Pelican && ((Pelican)this).inClaws != null){
                    ((Pelican)this).disableAbility();
        }
    }


    protected boolean canCalculateAngle = true;
    protected boolean canCalculateMovement = true;

    public double birdFallX = 0;
    public Timer stopBiteTimer = new Timer(10000);
        public Timer lossWata = new Timer(900);
        public Timer damageVenom = new Timer(2000);
                public Timer efectVenom = new Timer(9000);


        public Timer damageForWataLoss = new Timer(3500);
        private Timer webStuckTimer = new Timer(4000);
        public int webStuckType = 0;

    public double birdFallY = 0;

    @Override
    public void update() {
        super.update();

        checkDiving();
        diveRecharge();

        if(flag_webStuck){
            webStuckTimer.update(Constants.TICKS_PER_SECOND);
            if(webStuckTimer.isDone()){
                flag_webStuck = false;
                this.setSpeed(8);
                webStuckTimer.reset();
                this.webStuckType = 0;
                
            }
        }

        if(this.flag_eff_sweatPoisoned){
            lossWata.update(Constants.TICKS_PER_SECOND);
            if(lossWata.isDone()){
                                    if (this.getWater() > this.maxwater / 4) {

            sweatPoison shoot = new sweatPoison(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getBiome(), this.getClient().getRoom(), this);
            this.getClient().getRoom().addObj(shoot);
            lossWata.reset();
            this.takeWater(3);
            
                                    }else{
                                        damageForWataLoss.update(Constants.TICKS_PER_SECOND);
                                        if(damageForWataLoss.isDone()){
                                        this.hurt(1, 0, null);
                                        this.damageForWataLoss.reset();
                                        }
                                    }
            }
            stopBiteTimer.update(Constants.TICKS_PER_SECOND);
            if(stopBiteTimer.isDone()){
                this.flag_eff_sweatPoisoned  = false;
                this.setSpeed(8);
                stopBiteTimer.reset();
            }
        }


        if ((this.pickedByBird || this.pickedByPelican) && (this.pickedBy == null || this.pickedBy.isDead())) {
            this.pickedBy = null;
            // dereference so garbage cleaner cleans it
            this.makeFall(birdFallX, birdFallY, null);
        }
     
        if (this.client.isGodmodeEnabled() && !this.inArena) {
            this.health = this.maxHealth;
            this.water = this.maxwater;
        }
        if ((this instanceof Bee || this instanceof BulletAnt) && this.isInHole()) {
            double addhp = 0.03 * this.maxHealth;
            
            if (this.water + 1 < this.maxwater)
                this.water += 1;
            if (this.health + addhp < this.maxHealth)
                this.health += addhp;
        }
        if (this.inArena) {
            this.water = this.maxwater;
        }
        if (this.health < 1 && !isGhost) {
            if (this.client.getRoom().getGameMode() != 2) {
                      if(lastKiller != null && lastKiller instanceof Pterodactyl){
                        ((Pterodactyl)lastKiller).addWater(25);
                    }
                    if(lastKiller != null && lastKiller instanceof KingDragon || lastKiller instanceof BlackDragon || lastKiller instanceof LandMonster && lastKiller instanceof Animal){
                        ((Animal)lastKiller).addWater(10);
                    }

                client.killPlayer(lastReason, lastKiller);
            } else {
                if (lastKiller != null && lastKiller instanceof Animal){
                
                    ((Animal) lastKiller).getClient().addXp((this).getClient().getStartXP() * (int) 1.8);

                    if ((lastKiller instanceof KingDragon || lastKiller instanceof BlackDragon) && (this instanceof KingDragon || this instanceof BlackDragon)) {
                     ((Animal) lastKiller).health += ((Animal) lastKiller).maxHealth/2;
                    }
                }

                isGhost = true;
                this.getClient().getRoom().removeVisObjGhosts(this, lastKiller, this.getClient());
                this.getClient().getRoom().br_handler.notifyDeath(this.getClient());
                this.onDeath();
            }

            return;

        }
        if (this.canRegenerateHP() && this.health < this.maxHealth) {
            this.regenHP.update(Constants.TICKS_PER_SECOND);
            if (this.regenHP.isDone()) {
                this.health += hpPerSecond;
                this.showHP = true;
                this.hpTimer.reset();
                this.regenHP.reset();
            }
        }
        this.setPumpkins();
        if (this.regenHealingHP) {
            this.showHP = true;
            this.hpTimer.reset();
            this.flag_eff_healing = true;
            stoneHealingTimer.update(Constants.TICKS_PER_SECOND);
            if (stoneHealingTimer.isDone()) {
                if (this.health < this.maxHealth)
                    this.health += Math.min(regenerhp, this.maxHealth - this.health);
                this.addWater(regenerhp / 1.8);
                stoneHealingTimer.reset();
            }
            healingTimer.update(Constants.TICKS_PER_SECOND);
            if (healingTimer.isDone()) {
                this.regenHealingHP = false;
                healingTimer.reset();
            }
        } else {
            this.flag_eff_healing = false;
        }
        if (this.aloeVeraLeafHealing || this.aloeVeraHealing) {
            this.flag_eff_aloeveraHealing = true;
            if (this.aloeVeraLeafHealing) {
                aloeVeraLeafTimer.update(Constants.TICKS_PER_SECOND);
                if (aloeVeraLeafTimer.isDone()) {
                    if (this.health < this.maxHealth)
                        this.health += Math.min(5, this.maxHealth - this.health);
                    aloeVeraLeafTimer.reset();
                }
                aloeVeraLeafDefTimer.update(Constants.TICKS_PER_SECOND);
                if (aloeVeraLeafDefTimer.isDone()) {
                    this.aloeVeraLeafHealing = false;
                    aloeVeraLeafDefTimer.reset();
                }
            }
            if (this.aloeVeraHealing) {
                aloeVeraTimer.update(Constants.TICKS_PER_SECOND);
                if (aloeVeraTimer.isDone()) {
                    if (this.health < this.maxHealth)
                        this.health += Math.min(10, this.maxHealth - this.health);
                    aloeVeraTimer.reset();
                }
                aloeVeraDefTimer.update(Constants.TICKS_PER_SECOND);
                if (aloeVeraDefTimer.isDone()) {
                    this.aloeVeraHealing = false;
                    aloeVeraDefTimer.reset();
                }
            }
        } else {
            this.flag_eff_aloeveraHealing = false;
        }
        if (leavedHole) {
            holeSpawnTimer.update(Constants.TICKS_PER_SECOND);
            if (holeSpawnTimer.isDone()) {
                this.setSpawnID(-1);
                holeSpawnTimer.reset();
                leavedHole = false;
            }
        }
        this.flag_underWater = isDiveActive() || this.isInBigWhirlpool;
        this.flag_inWater = this.getBiome() == 1;
        if (this.getInfo().getAbility().getType() != 0) {
            if (this.isDivePossible()) {
                if (this.getInfo().getAbility().isRecharging() || !this.getInfo().getAbility().isUsable()
                        || !this.getInfo().getAbility().isPossible()) {
                    this.setDiveMain(true);
                } else {
                    this.setDiveMain(false);
                }
            } else {
                this.setDiveMain(false);
            }
        } else {
            if (this.isDivePossible()) {
                this.setDiveMain(true);
            } else {
                this.setDiveMain(false);
            }
        }
        if (this.water <= this.maxwater / 4) {
            this.flag_lowWat = true;
        } else {
            this.flag_lowWat = false;
        }
        if (this.water <= 0) {
            thirstTimer.update(Constants.TICKS_PER_SECOND);
            if (thirstTimer.isDone()) {
                this.hurt(this.maxHealth / 8, 4, null);
                thirstTimer.reset();
            }
        }
        updateCollided();
        if (this.biteCooldown > 0) {
            biteTimer.update(Constants.TICKS_PER_SECOND);
            if (biteTimer.isDone()) {
                this.biteCooldown--;
                biteTimer.reset();
            }
        }
               if (this.collisionBite > 0) {
            collisionTimer.update(Constants.TICKS_PER_SECOND);
            if (collisionTimer.isDone()) {
                this.collisionBite--;
                collisionTimer.reset();
            }
        }
        if (this.isHurted) {
            if (lastReason == 2)
                this.flag_tailBitten = true;
            hurtTimer.update(Constants.TICKS_PER_SECOND);
            if (hurtTimer.isDone()) {
                this.isHurted = false;
                this.flag_tailBitten = false;
                hurtTimer.reset();
            }
        }
        if (this.showHP) {
            hpTimer.update(Constants.TICKS_PER_SECOND);
            if (hpTimer.isDone()) {
                this.showHP = false;
                hpTimer.reset();
            }
        }
        if (isUsingAbility()) {
            if (this.pumpkinBalls.size() > 0)
                this.pumpkinBalls.get(0).Kick();
            else if (this.isAbilityDone || Constants.GAMEMODE == 1)
                useAbility();
        }
        if (!this.isStatue() && !this.inEgg && this.canCalculateMovement)
            calculateMovement();
        if (!this.isRammed && this.canCalculateAngle)
            calculateAngle();

        if (this.pickedByBird || this.pickedByPelican) {
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
        }
        if (this.immunitySeconds > 0) {
            this.flag_eff_invincible = true;
            this.health = this.maxHealth;
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
            immunityTimer.update(Constants.TICKS_PER_SECOND);
            if (immunityTimer.isDone()) {
                this.immunitySeconds--;
                if (this.immunitySeconds <= 0) {
                    if (this.getInfo().getAbility().getType() != 0) {
                        
                        if(this.isInHole() && this.getType() == 4){
                        this.getInfo().getAbility().setUsable(true);
                        }
                        if(!this.isInHole()){
                        this.getInfo().getAbility().setUsable(true);

                        }
                    }

                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                }
                immunityTimer.reset();
            }
        } else {
            this.flag_eff_invincible = false;
        }

        if (this.shiveringSeconds > 0) {
            this.flag_eff_shivering = true;
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
            shiveringTimer.update(Constants.TICKS_PER_SECOND);
            if (shiveringTimer.isDone()) {
                this.shiveringSeconds--;
                this.hurt(20, 14, shiveringBiter);
                if (this.shiveringSeconds <= 0) {
                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                    }
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                }
                shiveringTimer.reset();
            }
        } else {
            this.flag_eff_shivering = false;
        }

        if (this.statueSeconds > 0) {
            this.flag_eff_statue = true;
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
            statueTimer.update(Constants.TICKS_PER_SECOND);
            if (statueTimer.isDone()) {
                this.statueSeconds--;
                this.hurt(5, 14, statueKiller);
                if (this.statueSeconds <= 0) {
                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                    }
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                }
                statueTimer.reset();
            }
        } else {
            this.flag_eff_statue = false;
        }

        if (this.waterAniSlowingSeconds > 0) {
            waterAniTimer.update(Constants.TICKS_PER_SECOND);
            if (waterAniTimer.isDone()) {
                this.waterAniSlowingSeconds--;
                waterAniTimer.reset();
            }
        }

        if (this.stunnedSeconds > 0) {
            this.flag_eff_stunned = true;
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
            stunTimer.update(Constants.TICKS_PER_SECOND);
            if (stunTimer.isDone()) {
                this.stunnedSeconds--;
                if (this.stunnedSeconds <= 0) {
                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                    }
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                }
                stunTimer.reset();
            }
        } else {
            this.flag_eff_stunned = false;
        }

        if (this.bleedingSeconds > 0) {
            this.flag_eff_bleeding = true;
            if (this.getInfo().getAbility().getType() != 0) {
                this.getInfo().getAbility().setActive(false);
                this.getInfo().getAbility().setUsable(false);
                this.setDiveUsable(false);
                this.setDiveActive(false);
            }
            bleedingTimer.update(Constants.TICKS_PER_SECOND);
            if (bleedingTimer.isDone()) {
                this.bleedingSeconds--;
                this.hurt(bleedDamage, 10, bleeder);
                if (this.bleedingSeconds <= 0) {
                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                    }
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                }
                bleedingTimer.reset();
            }
        } else {
            this.flag_eff_bleeding = false;
        }

        if (this.poisonSeconds > 0) {
            if (this.isInvincible()) {
                this.poisonSeconds = 0;
            } else {
                this.flag_eff_poison = true;
                poisonTimer.update(Constants.TICKS_PER_SECOND);
                if (poisonTimer.isDone()) {
                    this.poisonSeconds--;
                    this.hurt(this.poisonHurt, 15, poisonKiller);
                    poisonTimer.reset();
                }
            }
        } else {
            this.flag_eff_poison = false;
        }

        if (this.paralizedSeconds > 0) {
            if (this.isInvincible()) {
                this.paralizedSeconds = 0;
            } else {
                if (this.getInfo().getAbility().getType() != 0) {
                    this.getInfo().getAbility().setActive(false);
                    this.getInfo().getAbility().setUsable(false);
                    this.setDiveUsable(false);
                    this.setDiveActive(false);
                }
                paralizeTimer.update(Constants.TICKS_PER_SECOND);
                if (paralizeTimer.isDone()) {
                    this.paralizedSeconds--;
                    if (this.paralizedSeconds <= 0) {
                        if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                            this.getInfo().getAbility().setUsable(true);
                        }
                        if (this.isDivePossible() && !this.isInHole()) {
                            this.setDiveUsable(true);
                        }
                    }
                    paralizeTimer.reset();
                }
            }
        }

        if (this.fireSeconds > 0) {
            if (this.isInvincible()) {
                this.fireSeconds = 0;
            } else {
                this.flag_eff_onFire = true;
                fireTimer.update(Constants.TICKS_PER_SECOND);
                if (fireTimer.isDone()) {
                    this.fireSeconds-=2;
                    this.hurt(15, 13, lastKiller);
                    fireTimer.reset();
                }
            }
        } else {
            this.flag_eff_onFire = false;
        }

        if (this.spearedSeconds > 0) {
            if (this.isInvincible()) {
                this.spearedSeconds = 0;
            } else {
                this.flag_speared = true;
                spearedTimer.update(Constants.TICKS_PER_SECOND);
                if (spearedTimer.isDone()) {
                    this.spearedSeconds--;
                    this.hurt(5, 14, lastKiller);
                    spearedTimer.reset();
                }
            }
        } else {
            this.flag_speared = false;
        }

        if (this.frozenSeconds > 0) {
            if (this.isInvincible()) {
                this.frozenSeconds = 0;
            } else {
                this.flag_eff_frozen = true;
                if (this.getInfo().getAbility().getType() != 0) {
                    this.getInfo().getAbility().setActive(false);
                    this.getInfo().getAbility().setUsable(false);
                    this.setDiveUsable(false);
                    this.setDiveActive(false);
                }
                frozenTimer.update(Constants.TICKS_PER_SECOND);
                if (frozenTimer.isDone()) {
                    this.frozenSeconds--;
                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                    }
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }
                    frozenTimer.reset();
                }
            }
        } else {
            this.flag_eff_frozen = false;
        }

        tTimer.update(Constants.TICKS_PER_SECOND);

        if (this.getBoost() && !this.isRammed && !this.flag_flying && !this.isGhost && !this.flag_usingAbility && !this.getInfo().getAbility().isActive()) {
            if (tTimer.isDone()) {
                this.makeBoost(0);
                tTimer.reset();
            }
        }
        // watershootTimer.update(Constants.TICKS_PER_SECOND);
        // if(this.getWatershoot() && !this.isRammed && !this.inArena) {
        // if(watershootTimer.isDone()) {
        // this.waterShoot();
        // watershootTimer.reset();
        // }
        // }

        if (this.falling) {
            this.addVelocityX(this.fallX);
            this.addVelocityY(this.fallY);
            this.flag_flying = true;
            // this.getClient().getRoom().chat(this.getClient(), "Fall step: " + fallStep +
            // " Z: " + this.getZ());
            if (this.getZ() > 0)
                this.setZ(this.getZ() - fallStep);
            else
                this.setZ(0);

            if (this.getZ() == 0)
                fallTimer.isRunning = false;
            fallTimer.update(Constants.TICKS_PER_SECOND);
            if (fallTimer.isDone()) {
                this.falling = false;
                this.flag_flying = false;
                this.pickedByBird = false;
                this.pickedByPelican = false;
                this.hurt(1, 0, ((GameObject) rammer));
                this.setStun(2);
                this.setZ(0);
                if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                    this.getInfo().getAbility().setUsable(true);
                    if (this.isDivePossible() && !this.isInHole()) {
                        this.setDiveUsable(true);
                    }

                }
            }
        }

        if (this.isRammed) {
            this.setMouseX(this.rammedX);
            this.setMouseY(this.rammedY);
            if (this.angle < 360)
                this.angle += 5;
            else
                this.angle = 0;
            if (this.rammedUp) {
                this.rammedH++;
                if (this.rammed <= 3)
                    this.rammedUp = false;
            } else if (this.rammedH > 0)
                this.rammedH--;
            else {
                this.rammed = 1;
                this.rammedDone = true;

            }
            this.setRadius(this.baseRadius + this.rammedH);
            rammedTimer.update(Constants.TICKS_PER_SECOND);
            if (rammedTimer.isDone() || rammedDone) {
                this.rammed--;
                if (this.rammed <= 0) {
                    this.isRammed = false;
                    this.flag_flying = false;
                    this.rammedH = 0;
                    this.rammedDone = false;
                    this.setRadius(this.baseRadius);
                    this.hurt(ramDamage, 0, ((GameObject) rammer));
                    this.setStun(2);

                    if (this.getInfo().getAbility().getType() != 0 && !this.isInHole()) {
                        this.getInfo().getAbility().setUsable(true);
                        if (this.isDivePossible() && !this.isInHole()) {
                            this.setDiveUsable(true);
                        }
                    }
                }
                rammedTimer.reset();
            }
        }

        this.abilSpeed.update(Constants.TICKS_PER_SECOND);

        super.update();

        if(this.isDiveActive()){
            if(this.getBarType() != 1 && !(this instanceof Meerkat && this.getInfo().getAbility().isActive())){
                this.setBarType(1);
                // this.setWater(80);
            }
        }
        if(!this.isDiveActive()){
            if(this.getInfo().getType() != AnimalType.SEAMONSTER && this.getBarType() == 1){
                this.setBarType(this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal() ? 2 : 0);
            }
        }


        if (this.isGhost) {
            this.flag_stealth = true;
            this.flag_isGrabbed = true;
            this.getInfo().getAbility().setActive(false);
            this.getInfo().getAbility().setUsable(false);
            this.setDiveUsable(false);
            this.setDiveActive(false);
            this.setSpeed(25);
            this.health = this.maxHealth;
            this.water = 100;
            if (this.pumpkinBalls.size() > 0) {
                for (PumpkinBall ball : this.pumpkinBalls) {
                    ball.Throw();
                }
            }
        } else if (this.inEgg) {
            this.getInfo().getAbility().setActive(false);
            this.getInfo().getAbility().setUsable(false);
            this.setDiveUsable(false);
            this.setDiveActive(false);
            this.health = this.maxHealth;
            this.water = 100;

            if (this.egg != null) {
                this.setX(this.egg.getX());
                this.setY(this.egg.getY());
            } else {
                this.egg = null;
                this.inEgg = false;
            }
        } else {
            this.onWaterLose();
        }
    }
    public boolean pickedByBird = false;
    public boolean pickedByPelican = false;

    public Animal pickedBy;

    private boolean isDesertAnimal = false;

    public void setDesertAnimal(boolean a) {
        this.isDesertAnimal = a;
    }

    public boolean isDesertAnimal() {
        return this.isDesertAnimal;
    }

    private boolean isArcticAnimal = false;

    public void setArcticAnimal(boolean a) {
        this.isArcticAnimal = a;
    }

    public boolean isArcticAnimal() {
        return this.isArcticAnimal;
    }

    private boolean isLavaAnimal = false;
    public boolean bypassAbilSpeed = false;

    public void setLavaAnimal(boolean a) {
        this.isLavaAnimal = a;
    }

    public boolean isLavaAnimal() {
        return this.isLavaAnimal;
    }

    public void setCamzoom(int tcamzoom) {
        if (tcamzoom < 100)
            tcamzoom = 100;
        getClient().camzoom = tcamzoom;
    }

    private Timer exhaustTimerFire = new Timer(6000);
    public Timer waterTimer = new Timer(1000);
    public Timer abilSpeed = new Timer(2500);

    public void onWaterLose() {
        if (this.getBarType() == 0) {

            if (this.flag_eff_isInMud) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone()) {
                    this.addWater(0.5);
                    waterTimer.reset();
                }
            }
             if(this.getBiome() == 0 && this.getInfo().getBiome() == BiomeType.ARCTIC.ordinal()){
                       exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
                if (exhaustTimerFire.isDone()) {
                    this.setFire(2, null, 4);
                    this.getClient().send(Networking.personalGameEvent(255, "Ouch!"));
                    exhaustTimerFire.reset();
                }
            }
     

            else if (this.getBiome() == 1) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone() && !this.isDiveActive() && !this.isInHole()) {
                    this.addWater(Utils.randomInt(4, 6));
                    if (this.fireSeconds > 0)
                        this.fireSeconds--;
                    waterTimer.reset();
                }
            } else if (this.getBiome() != 1 && this.getInfo().getBiome() == 1 && !(this instanceof Crab && this instanceof Snail && this instanceof Pelican)) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone()) {
                    this.takeWater(Utils.randomInt(4, 6));
                    waterTimer.reset();
                    this.setWaterAniSlower(1);
                }
            } else if (this.getBiome() == 4 && (!this.isDesertAnimal() || !this.isLavaAnimal())) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone()) {
                    this.takeWater(1);
                    waterTimer.reset();
                }
                exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
                if (exhaustTimerFire.isDone()) {
                    if (this.getInfo().getBiome() != 4) {
                        this.setFire(2, null, 3);
                        this.getClient().send(Networking.personalGameEvent(255, "Ouch! It's hot in desert!"));
                        exhaustTimerFire.reset();
                    }
                }
            } else if (this.getBiome() == 2 && !this.isArcticAnimal()) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone()) {
                    this.takeWater(1);
                    waterTimer.reset();
                }
                exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
                if (exhaustTimerFire.isDone()) {
                    if (this.getInfo().getBiome() != 2) {
                        this.setFrozen(2, null, 2);
                        this.getClient().send(Networking.personalGameEvent(255, "Brrrr! It's cold in arctic!"));
                        exhaustTimerFire.reset();
                    }
                }
            } else if (this.getBiome() == 3 && this.isArcticAnimal()) {
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if (waterTimer.isDone()) {
                    this.takeWater(2);
                    waterTimer.reset();
                }
                exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
                if (exhaustTimerFire.isDone()) {
                    this.setFire(2, null, 4);
                    this.getClient().send(Networking.personalGameEvent(255, "Ouch! It's hot near volcano!"));
                    exhaustTimerFire.reset();
                }
            } else {
                if (this.isWaterfowl() && this.getBiome() != 1) {
                    waterTimer.update(Constants.TICKS_PER_SECOND);
                    if (waterTimer.isDone()) {
                        this.takeWater(3);
                        waterTimer.reset();
                    }
                    exhaustTimerFire.update(Constants.TICKS_PER_SECOND);
                    if (exhaustTimerFire.isDone()) {
                        this.setFire(2, null, 4);
                        this.getClient().send(Networking.personalGameEvent(255, "Ouch! It's hot outside water!"));
                        exhaustTimerFire.reset();
                    }
                }else if(this.getBiome() != BiomeType.VOLCANO.ordinal() && this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal()){
               waterTimer.update(Constants.TICKS_PER_SECOND);
                    if (waterTimer.isDone()) {
                        this.takeWater(0.5);
                        waterTimer.reset();
                    }
                } else {
                    waterTimer.update(Constants.TICKS_PER_SECOND);
                    if (waterTimer.isDone()) {
                        this.takeWater(0.5);
                        waterTimer.reset();
                    }
                }
            }
            if(this.isInHole() && this.getInfo().getBiome() == BiomeType.OCEAN.ordinal()){
            this.takeWater(1);
            }
        }
        if(this.getBarType() == 2){
                   if(this.getBiome() != BiomeType.VOLCANO.ordinal()){
                    waterTimer.update(Constants.TICKS_PER_SECOND);
                    if(waterTimer.isDone()){
                        this.takeWater(0.5);
                        waterTimer.reset();
                    
                }
            }
            if(this.getBiome() == BiomeType.VOLCANO.ordinal() && this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal() || (this.isLavaAnimal())){
                waterTimer.update(Constants.TICKS_PER_SECOND);
                if(waterTimer.isDone()){
                    waterTimer.reset();
                    this.addWater(0.5);
                }
            }
        }
        if(this.getInfo().getType() != AnimalType.SEAMONSTER && this.isDiveActive()){
        if (this.water <= 0) {
            this.disableDiving();
        }

        if(UseDiveTimer != null){

        UseDiveTimer.update(Constants.TICKS_PER_SECOND);
        if(UseDiveTimer.isDone()){
            this.takeWater(1);
            UseDiveTimer.reset();
        }
        }else {
            UseDiveTimer = new Timer(20 * 8);

        }
        }
    }

    public double speedManipulation(double speed) {
        if (this.stunnedSeconds > 0)
            speed /= 2.5;//was 1.5
        if (this.waterAniSlowingSeconds > 0 && !this.bypass_waterani_slowness)
            speed /= 2;
        if (this.frozenSeconds > 0)
            speed /= 2.5;//was 2
        if (this.paralizedSeconds > 0)
            speed /= 4;
        if (this.isWaterfowl() && this.getBiome() != 1)
            speed /= 2;
        if (this.isDiveActive())
            speed /= 1.75;// was 1.3
        if (!this.abilSpeed.isDone() && !this.bypassAbilSpeed)
            speed /= 2;
        if (this.flag_eff_isInMud)
            speed /= 2;
            if(this.biteCooldown > 0)
            speed /= 1.4;
            if(!this.bypass_waterani_slowness && !this.flag_flying && !this.flag_fliesLikeDragon && this.getInfo().getBiome() != 1)
            speed /= 2;
        return speed;
    }

    public boolean isOnIce = false;

    private double icenextangle = 0;

    private double icenewangle = 0;

    private double iceanglenewspeed = Constants.DEFAULTANGLESPEED;

    private double iceoldangle = 0;

    private double icetrueangle = 0;

    private double iceangle = 0;

    private boolean isSlidingOnIce = true;

    private int statueType = 0;

    public void setSlidingOnIce(boolean a) {
        isSlidingOnIce = a;
    }

    public boolean isSlidingOnIce() {
        return isSlidingOnIce;
    }

    public void calculateIceAngle() {
        double dy = this.mouseY - this.getY();
        double dx = this.mouseX - this.getX();

        double theta = Math.atan2(dy, dx); // range (-PI, PI]

        theta *= 180 / Math.PI; // rads to degs, range (-180, 180]
        if (theta < 0) {
            theta += 360;
        }
        this.icenextangle = theta;
        this.icenewangle = this.iceoldangle - this.icenextangle;

        double tspeed = this.icenextangle - this.iceangle;

        tspeed = this.icenextangle - this.iceangle;
        while (tspeed < -180)
            tspeed += 360;
        while (tspeed > 180)
            tspeed -= 360;

        if (tspeed < 0) {
            tspeed = -tspeed;
        }

        double maxanglespeed = this.iceanglenewspeed;

        maxanglespeed /= 1.5;

        if (this.getBoost() && tspeed < 20)
            maxanglespeed /= 5;
        maxanglespeed /= 3.5;
        if (this.icenewangle > 180) {
            this.icenewangle -= 360;

        }
        if (this.icenewangle < -180) {
            this.icenewangle += 360;
        }

        if (this.icenewangle > maxanglespeed) {
            this.icenewangle = maxanglespeed;
        }
        if (this.icenewangle < -maxanglespeed) {
            this.icenewangle = -maxanglespeed;
        }

        this.icetrueangle = this.iceangle - this.icenewangle;

        if (this.icetrueangle <= 0) {
            this.icetrueangle += 360;
        }
        if (this.icetrueangle >= 360) {
            this.icetrueangle -= 360;
        }
        this.iceoldangle = this.icetrueangle;
        this.iceangle = this.icetrueangle;
    }

    public boolean canHandleMorePumpkins() {

            if (this.getInfo().getTier() < 15 && this.pumpkinBalls.size() <= 0 || (this.getClient().getAccount() != null
                && (this.getClient().getAccount().admin >= 2)))
            return true;
        if (this.getInfo().getTier() >= 15 && this.pumpkinBalls.size() <= 1 || (this.getClient().getAccount() != null
                && (this.getClient().getAccount().admin >= 2)))
            return true;
        if (this.getInfo().getTier() >= 16 && this.pumpkinBalls.size() <= 1 || (this.getClient().getAccount() != null
                && (this.getClient().getAccount().admin >= 2)))
            return true;
        if (this.getInfo().getTier() >= 17 && this.pumpkinBalls.size() <= 1 || (this.getClient().getAccount() != null
                && (this.getClient().getAccount().admin >= 2)))
            return true;
        
        return false;
 
    }

    public void setPumpkins() {
        if (this.pumpkinBalls.size() > 0) {
            List<PumpkinBall> todelpump = new ArrayList<>();
            int addang = 0;
            int naddang = 0;
            int i = 0;
            for (PumpkinBall pump : this.pumpkinBalls) {
                if (pump.owner != this)
                    todelpump.add(pump);
                else {
                    i++;
                    int nang = 0;
                    if (i % 2 == 0)
                        nang = addang;
                    else
                        nang = naddang;
                    pump.takeBonus();
                    double x = ((Math.cos(Math.toRadians(this.getAngle() + nang)) * (this.getRadius() + 30)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle() + nang)) * (this.getRadius() + 30)));
                    pump.setX(this.getX() + x);
                    pump.setY(this.getY() + y);
                    pump.setAngle(this.getAngle() + nang);
                    if (i == 1) {
                        addang = 15;
                        naddang = -15;
                    } else if (i % 2 == 0)
                        addang += 15;
                    else
                        naddang -= 15;
                }
            }
            if (todelpump.size() > 0)
                this.pumpkinBalls.removeAll(todelpump);
        }
    }

    public void calculateMovement() {
        double radius = this.getRadius();
        double speedCoefficient = Utils
                .normalize(Utils.constrain(Utils.distance(this.getX(), this.getMouseX(), this.getY(), this.getMouseY()),
                        0.00001, radius), 0.00001, radius);
        if (speedCoefficient < 0)
            speedCoefficient = 0;
        double icex = 0;
        double icey = 0;
        calculateIceAngle();
        if (isOnIce) {
            double spp = this.speed * speedCoefficient;
            icex = ((Math.cos(Math.toRadians(this.iceangle)) * (spp)));
            icey = ((Math.sin(Math.toRadians(this.iceangle)) * (spp)));
        }
        double dx = (isOnIce ? this.getX() + icex : this.mouseX) - this.getX();
        double dy = (isOnIce ? this.getY() + icey : this.mouseY) - this.getY();

        double theta = Math.atan2(dy, dx);

        if (theta < 0)
            theta += Math.PI * 2;

        double sspeed = (this.customSpeed == -1 ? this.speed : this.customSpeed);
        sspeed = this.speedManipulation(sspeed);
        if (isGhost)
            sspeed = 20;
        double speed = sspeed * speedCoefficient;

        if (speedCoefficient < 0.2)
            speed = 0;

        double velocityX = Math.cos(theta);
        double velocityY = Math.sin(theta);

        this.smoothVelocityX = velocityX * speed;
        this.smoothVelocityY = velocityY * speed;

        this.setVelocityX(Utils.lerp(this.getVelocityX(), this.smoothVelocityX, 0.2));
        this.setVelocityY(Utils.lerp(this.getVelocityY(), this.smoothVelocityY, 0.2));
    }

    /*
     * this.socket.SocketHandler.camera.x = this.x;
     * this.socket.SocketHandler.camera.y = this.y;
     * 
     * 
     * var AngleNew = util.anglebetween2point(this.x, this.y, this.mouseX,
     * this.mouseY);
     * var new_Angle = util.disatanceBetAngle(this.angle, AngleNew);
     * this.newDirection = new_Angle > 0 ? 1 : -1
     * 
     * var sSpeed = 10
     * var acceleration = 8
     * this.currentAcceleration = this.newDirection == 1 ? Math.min(Math.max(sSpeed,
     * this.currentAcceleration * acceleration), new_Angle / 3) :
     * Math.max(Math.min(-sSpeed, this.currentAcceleration * acceleration),
     * new_Angle / 3)
     * 
     * 
     * if (new_Angle >= 0) new_Angle = Math.min(new_Angle, this.currentAcceleration)
     * else new_Angle = Math.max(new_Angle, this.currentAcceleration)
     * this.lastDirection = this.newDirection
     * 
     * this.angle -= new_Angle;
     * if (this.angle < 0) this.angle += 360;
     * if (this.angle > 360) this.angle -= 360;
     */
    /*
     * util.angle_1to360 = function (angle) {
     * var angle = (Math.trunc(angle) % 360) + (angle - Math.trunc(angle));
     * //converts angle to range -360 + 360
     * if (angle > 0.0)
     * return angle;
     * else
     * return angle + 360.0;
     * };
     * util.anglebetween2point = function (originX, originY, targetX, targetY) {
     * var dx = originX - targetX;
     * var dy = originY - targetY;
     * 
     * // var theta = Math.atan2(dy, dx); // [0, Ⲡ] then [-Ⲡ, 0]; clockwise; 0° =
     * west
     * // theta *= 180 / Math.PI; // [0, 180] then [-180, 0]; clockwise; 0° = west
     * // if (theta < 0) theta += 360; // [0, 360]; clockwise; 0° = west
     * 
     * // var theta = Math.atan2(-dy, dx); // [0, Ⲡ] then [-Ⲡ, 0]; anticlockwise; 0°
     * = west
     * // theta *= 180 / Math.PI; // [0, 180] then [-180, 0]; anticlockwise; 0° =
     * west
     * // if (theta < 0) theta += 360; // [0, 360]; anticlockwise; 0° = west
     * 
     * // var theta = Math.atan2(dy, -dx); // [0, Ⲡ] then [-Ⲡ, 0]; anticlockwise; 0°
     * = east
     * // theta *= 180 / Math.PI; // [0, 180] then [-180, 0]; anticlockwise; 0° =
     * east
     * // if (theta < 0) theta += 360; // [0, 360]; anticlockwise; 0° = east
     * 
     * var theta = Math.atan2(-dy, -dx); // [0, Ⲡ] then [-Ⲡ, 0]; clockwise; 0° =
     * east
     * theta *= 180 / Math.PI; // [0, 180] then [-180, 0]; clockwise; 0° = east
     * if (theta < 0) theta += 360; // [0, 360]; clockwise; 0° = east
     * 
     * return theta;
     * };
     * util.distanceBetweenAngle = function (fromAngle, toAngle) {
     * a = fromAngle - toAngle
     * if (a > 180) {
     * a -= 360
     * 
     * }
     * if (a < -180) {
     * a += 360
     * 
     * 
     * }
     * return a
     * }
     */

    double turnSpeed = 10;
    public double acceleration = 8;
    double currentAcceleration = 0;
    public boolean newDirection = false;

    // public void calculateAngle() {
    //     double maxSpeed = 8;
    //     double mouseX = this.mouseX; // mouse x position
    //     double mouseY = this.mouseY; // mouse y position
    //     double playerX = this.getX(); // player x position
    //     double playerY = this.getY(); // player y position
    //     double distance = Math.sqrt((mouseX - playerX) * (mouseX - playerX) + (mouseY - playerY) * (mouseY - playerY));
    //     // double speed = distance / Constants.MAX_DISTANCE;
    //     double speed = Math.min(distance / Constants.MAX_DISTANCE, maxSpeed);
    //     double targetAngle = (double) Math.toDegrees(Math.atan2(mouseY - playerY, mouseX - playerX));
    //     double deltaAngle = targetAngle - this.angle;
    //     if (deltaAngle > 180) {
    //         deltaAngle -= 360;
    //     } else if (deltaAngle < -180) {
    //         deltaAngle += 360;
    //     }
    //     // double rotationSpeed = Constants.DEFAULTANGLESPEED * (1 + speed);
    //     double rotationSpeed = Constants.DEFAULTANGLESPEED + speed * Constants.MAXROTSPEED;
    //     // double maxRotationSpeed = Constants.MAXROTSPEED * rotationSpeed;
    //     double maxRotationSpeed = Constants.MAXROTSPEED;
    //     double deltaAngleRight = deltaAngle; 
    //     double deltaAngleLeft = deltaAngle;
    //     if (deltaAngleRight < 0) { 
    //         deltaAngleRight += 360; 
    //     } 
    //     if (deltaAngleLeft > 0) { 
    //         deltaAngleLeft -= 360; 
    //     } if (Math.abs(deltaAngleRight) < Math.abs(deltaAngleLeft)) { 
    //         deltaAngle = deltaAngleRight; 
    //     } else { 
    //         deltaAngle = deltaAngleLeft; 
    //     }
    //     if(Math.abs(deltaAngle) > maxRotationSpeed) {
    //         deltaAngle = maxRotationSpeed * Math.signum(deltaAngle);
    //     }
    //     this.angle = (this.angle + deltaAngle) % 360.0;
    //     if (this.angle < 0) {
    //         this.angle += 360.0;
    //     }
    //     // if (Math.abs(deltaAngle) < 1.0) { 
    //     //     this.angle = targetAngle; 
    //     // }
    //     // } else {
    //     //     this.angle = (this.angle + deltaAngle) % 360.0;
    //     // } 
    // }

    // public void calculateAngle() {
    //     double mouseX = this.mouseX;
    //     double mouseY = this.mouseY;
    //     double playerX = this.getX();
    //     double playerY = this.getY();
    
    //     double distance = Math.sqrt((mouseX - playerX) * (mouseX - playerX) + (mouseY - playerY) * (mouseY - playerY));
    
    //     // Calculate the distance factor (closer to the center, faster speed)
    //     double distanceFactor = 1.0 - Math.min(distance / Constants.MAX_DISTANCE, 1.0);
    
    //     // Adjusted speed based on distance from the center
    //     double speed = distanceFactor * Constants.MAX_SPEED;
    
    //     double targetAngle = Math.toDegrees(Math.atan2(mouseY - playerY, mouseX - playerX));
    //     double deltaAngle = targetAngle - this.angle;
    
    //     if (deltaAngle > 180) {
    //         deltaAngle -= 360;
    //     } else if (deltaAngle < -180) {
    //         deltaAngle += 360;
    //     }
    
    //     double rotationSpeed = Constants.DEFAULT_ANGLE_SPEED * (2.1 + speed); // Adjusted speed factor
    //     double maxRotationSpeed = Constants.MAX_ROTATION_SPEED * rotationSpeed * 0.5; // Adjusted max rotation speed factor

    //     if (Math.abs(deltaAngle) > maxRotationSpeed) {
    //         deltaAngle = maxRotationSpeed * Math.signum(deltaAngle);
    //     }
    
    //     this.angle = (this.angle + deltaAngle) % 360.0;
    
    //     if (this.angle < 0) {
    //         this.angle += 360.0;
    //     }
    
    //     // Use this.angle in your movement calculations
    //     // ...
    // }
    
    public void calculateAngle() { 
        double mouseX = this.mouseX;  
        double mouseY = this.mouseY;  
        double playerX = this.getX();  
        double playerY = this.getY();  
    
        double targetAngle = Math.toDegrees(Math.atan2(mouseY - playerY, mouseX - playerX)); 
        double deltaAngle = targetAngle - this.angle; 
    
        if (deltaAngle > 180) {
            deltaAngle -= 360; 
        } else if (deltaAngle < -180) { 
            deltaAngle += 360; 
        } 
    
        int tier = this.getInfo().getTier();
    
        
        double rotationSpeed;
        if (tier == 1) {
            rotationSpeed = Constants.DEFAULTANGLESPEED * (5.0 / tier);  
        } else {
            double tierMultiplier = 0.5;
            if(this.inArena) tierMultiplier = 0.5;
            rotationSpeed = Constants.DEFAULTANGLESPEED / (tier * tierMultiplier);
                    }
        double maxRotationSpeed = Constants.MAXROTSPEED * rotationSpeed; 
        double minRotationSpeed = Constants.MINROTSPEED * rotationSpeed;
    
        
        rotationSpeed = Math.max(minRotationSpeed, Math.min(maxRotationSpeed, rotationSpeed));
    
        if(Math.abs(deltaAngle) > rotationSpeed) {
            deltaAngle = rotationSpeed * Math.signum(deltaAngle); 
        } 
    
        this.angle = (this.angle + deltaAngle) % 360.0; 
    
        if (this.angle < 0) { 
            this.angle += 360.0; 
        } 
    }
    
    
    
    

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {

        writer.writeString(this.playerName);
        writer.writeUInt8(this.info.getAnimalSpecies());
        writer.writeUInt8(this.getSpecies());
        writer.writeUInt8(this.getSpeciesSub());
        if (Constants.GAMEMODE == 3)
            writer.writeUInt8(this.getTeam()); // team
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        
      
        writer.writeUInt8(this.getSpecies());
        writer.writeUInt8(this.getSpeciesSub());
        // angle in degrees!
        writer.writeUInt16((short) this.angle);
        writer.writeUInt8(this.getBiome());
        // flags
        FlagWriter flagWriter = new FlagWriter();

        flagWriter.writeBoolean(this.getClient() != null ? this.getClient().muted : false); // muted
        flagWriter.writeBoolean(false); // invisible
        flagWriter.writeBoolean(this.flag_lowWat);
        flagWriter.writeBoolean(this.flag_inHomeBiome);
        flagWriter.writeBoolean(this.flag_underWater);
        flagWriter.writeBoolean(this.flag_eff_invincible);
        flagWriter.writeBoolean(this.getInfo().getAbility() != null ? this.getInfo().getAbility().isActive() : false);
        flagWriter.writeBoolean(this.flag_tailBitten);
        flagWriter.writeBoolean(this.flag_eff_stunned);
        flagWriter.writeBoolean(this.flag_iceSliding);
        flagWriter.writeBoolean(this.flag_eff_frozen);
        flagWriter.writeBoolean(this.flag_eff_onFire);
        flagWriter.writeBoolean(this.flag_eff_healing);
        flagWriter.writeBoolean(this.flag_eff_poison);
        flagWriter.writeBoolean(this.flag_constricted);
        flagWriter.writeBoolean(this.flag_webStuck);
        flagWriter.writeBoolean(this.flag_stealth);
        flagWriter.writeBoolean(this.flag_eff_bleeding);
        flagWriter.writeBoolean(this.flag_flying);
        flagWriter.writeBoolean(this.flag_isGrabbed);
        flagWriter.writeBoolean(this.flag_eff_aniInClaws);
        flagWriter.writeBoolean(this.flag_eff_stunk);
        flagWriter.writeBoolean(this.flag_cold);
        flagWriter.writeBoolean(this.flag_inWater);
        flagWriter.writeBoolean(this.flag_inLava);
        flagWriter.writeBoolean(this.canClimbHills);
        flagWriter.writeBoolean(this.flag_isDevMode);
        flagWriter.writeBoolean(this.flag_eff_slimed);
        flagWriter.writeBoolean(this.flag_eff_wobbling);
        flagWriter.writeBoolean(this.flag_eff_hot);
        flagWriter.writeBoolean(this.flag_eff_sweatPoisoned);
        flagWriter.writeBoolean(this.flag_eff_shivering);
        flagWriter.writeBoolean(this.isInHole());
        flagWriter.writeBoolean(this.flag_eff_grabbedByFlytrap);
        flagWriter.writeBoolean(this.flag_eff_aloeveraHealing);
        flagWriter.writeBoolean(this.flag_eff_tossedInAir);
        flagWriter.writeBoolean(this.flag_eff_isOnSpiderWeb);
        flagWriter.writeBoolean(this.flag_fliesLikeDragon);
        flagWriter.writeBoolean(this.flag_eff_isInMud);
        flagWriter.writeBoolean(this.flag_eff_statue);
        flagWriter.writeBoolean(this.flag_eff_isOnTree);
        flagWriter.writeBoolean(this.flag_eff_isUnderTree);
        writer.writeFlags(flagWriter);
        writer.writeBoolean(this.flag_ytmode);
        writer.writeUInt8(this.icoPlayer);
        writer.writeBoolean(this.flag_speared);
        writer.writeBoolean(this.flag_eff_dirty);
        writer.writeBoolean(this.client.isPvPEnabled());
        writer.writeBoolean(this.inArena);

        writer.writeUInt8(this.client.color);
        writer.writeUInt8(this.arenaWins);
        writer.writeUInt8(this.playerNum);

        if (this.flag_isDevMode)
            writer.writeUInt8(this.developerModeNumber);
            

        if (this.flag_eff_statue)
            writer.writeUInt8(this.statueType); // statue type

        if (this.flag_constricted)
            writer.writeUInt8(0); // eff_constrictedSpecies

        if (this.flag_webStuck)
            writer.writeUInt8(this.webStuckType); // eff_webStuckType

        if (this.flag_eff_dirty)
            writer.writeUInt8(this.eff_dirtType); // eff_dirtType

        writer.writeUInt32(lastHidingHoleID);
        writer.writeUInt16(hidingHoleVisibilityRad);
        // writer.writeUInt16(ArenaVisibilityRad);
    }

    public void changeBoost(boolean boost) {
        this.isBoosting = boost;
    }

    private boolean isShooting = false;

    public void changeWatershoot(boolean a) {
        this.isShooting = a;
    }

    public boolean getWatershoot() {
        return this.isShooting;
    }

    public boolean getBoost() {
        return this.isBoosting;
    }

    public void onHurt() {
        // overwrite!
    }

    public void hurt(double damage, int reason, GameObject biter) {
        if (!this.flag_eff_invincible && !isGhost) {
            
            damage = this.isHiding ? (int) Math.round(damage * 0.75) : damage;

            if (this.health > 0 && (this.inArena || !this.client.isGodmodeEnabled())) 
                this.health -= this.health - damage > 0 ? damage : this.health;
                

            
            this.lastKiller = biter;
            this.lastReason = reason;
            this.showHP = true;
            this.isHurted = true;
            if (this.pumpkinBalls.size() > 0) {
                this.pumpkinBalls.get(0).Throw();
                this.setStun(1);
            }
            this.hpTimer.reset();
            this.hurtTimer.reset();
            onHurt();
            if(this.getClient() != null)
            this.client.onHurt(biter);
        if(this.health <= 0){
                this.setDead(true);
            }
            
        }
    }

    public void setBiteCooldown(int i) {
        this.biteCooldown = i;
    }

     private Timer UseDiveTimer;

    private boolean isUsingAbility = false;

    public boolean isRammed = false;

    public double rammedX;

    public double rammedY;

    public int icoPlayer = 0;

    private int modifier = 0;
    private Animal rammer;

    public boolean isUsingAbility() {
        return isUsingAbility;
    }

    public void useDive() {
    
        this.setDiveActive(true);
        this.oldWaterValue = this.getWater();
        this.setWater(100);
}
public void setDiveDuration(int a){
    this.modifier = a;

     UseDiveTimer = new Timer(600 * modifier == 0 ? 8 : modifier);
}


public void useAbility(){
    //Handle in custom classes!
}

    public void onAbilityDisable() {
        if(this instanceof Pterodactyl){
            getClient().resetCamzoom();
        }
           if (isDiveActive()) {
            this.disableDiving();
        }

        //
    }

    int ez = 0;

    public void changeAbility(boolean es) {
        if(this.getClient() != null && this.getClient().freezed)
        return;
        if(this.isDiveActive() && !es){
            this.useAbility();
            this.disableDiving();
            return;
        }        
                            if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MEERKAT && es){
                                      ((Meerkat)this).useAbility();
                                    return;
                        }
        if(!this.isDiveActive() && es && isDiveMain()){
              PlayerAbility abil = this.getInfo().getAbility();
        
        if (isDivePossible() && !isDiveRecharging() && isDiveUsable() && !isDiveActive()  && (this.getBiome() == 1 && this.getInfo().getBiome() != BiomeType.VOLCANO.ordinal() || (this.flag_inLavaLake && this.getInfo().getBiome() == BiomeType.VOLCANO.ordinal()))
        && !abil.isActive() && !this.isStatue()) {
            this.useDive();
            return;
        }
    }
        

        if(this.getInfo().getAbility().isActive() && this instanceof Falcon && ((Falcon)this).flying && !((Falcon)this).attacking && !((Falcon)this).getClient().falconTarget.getCollidedList().contains(this) && !es){
            ((Falcon)this).handleWwhenFlying();
            return;
        }
        if(this instanceof Markhor && !es){
            ((Markhor)this).useAbility();
        }
             if(this instanceof Falcon && !es){
            ((Falcon)this).useAbil();
            // if(this.getClient() != null) this.getClient().send(Networking.personalGameEvent(255, "FLYING "+"a" + " landing "+"landing" + " running " + "running"));

            return;
        }

           if(this.getInfo().getAbility().isActive() && this instanceof Snowyowl && ((Snowyowl)this).flying && !((Snowyowl)this).attacking && !((Snowyowl)this).getClient().snowyowlTarget.getCollidedList().contains(this) && !es){
            ((Snowyowl)this).handleWwhenFlying();
            return;
        }
             if(this instanceof Snowyowl && !es){
            ((Snowyowl)this).useAbil();
            // if(this.getClient() != null) this.getClient().send(Networking.personalGameEvent(255, "FLYING "+"a" + " landing "+"landing" + " running " + "running"));

            return;
        }

                    if(this instanceof Bigfoot && !es){
                ((Bigfoot)this).useAbil();
                ((Bigfoot)this).cancelFire();

                return;
                }

                    if(this instanceof Tiger && !es){
                ((Tiger)this).useAbility();
                ((Tiger)this).cancelAmbush();

                return;
                }
                                    if(this instanceof RattleSnake && !es){
                ((RattleSnake)this).disableAbility();
                // ((RattleSnake)this).cancelAmbush();

                return;
                }

                              if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CASSOWARY && !es){

                                            ((Cassowary)this).push();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }  

                              if(this.getInfo().getType() == AnimalType.BLACKWIDOW && !es){

                                            ((BlackwidowSpider)this).useAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }   
                                              if(this.getInfo().getType() == AnimalType.OSTRICH && !es){

                                            ((Ostrich)this).useAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }   

                // if(this.getInfo())


        if(this.getInfo().getAbility().isToHold() && !es){
                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GAZELLE){
                                            ((Gazelle)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }

                                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CRAB){
                                            ((Crab)this).stopAbil();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }


                                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.SEAL){
                                            ((Seal)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }


                                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MACAW){
                                            ((Macaw)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                
                                                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.OCTOPUS){
                                            ((Octopus)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                
                


                                     if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.ARMADILLO){
                                            ((Armadillo)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                                     if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GIRAFFE){
                                            ((Giraffe)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                                     if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GIANTSPIDER && !es){
                                            ((GiantSpider)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                



                
                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PIGEON){
                                            ((Pigeon)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                       if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WARTHOG){
                                            ((Warthog)this).doAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                    if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PUFFERFISH){
                                            ((PufferFish)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                           if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.HEDGEHOG){
                                            ((Hedgehog)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                          if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WOODPECKER){
                                            ((Woodpecker)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                   if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PEACOCK){
                                            ((Peacock)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                      if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.FLAMINGO){
                                            ((Flamingo)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                 if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.TOUCAN){
                                            ((Toucan)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                      if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CHICKEN){
                                            ((Chicken)this).stopAbil();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                 if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.COBRA){
                                            ((Cobra)this).stopAbil();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }

                   if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.DEER){
                                            ((Deer)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }
                                   if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.REINDEER){
                                            ((Reindeer)this).disableAbility();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
                }

            if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MOLE){
                                            ((Mole)this).stopAbil();
                                            onAbilityDisable();
                                            isUsingAbility = false;

                return;
            }
            if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MEERKAT){
                                            ((Meerkat)this).stopAbil();
                                            onAbilityDisable();
                                            isUsingAbility = false; 
                return;
                }
                if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WALRUS){
                    ((Walrus)this).stopAbil();
                    onAbilityDisable();
                    isUsingAbility = false;

                return;
            }
                if(this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PENGUIN){
                    ((Penguin)this).disableAbility();
                    onAbilityDisable();
                    isUsingAbility = false;

                return;
            }
        }
            
            if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MOLE && es){
                              ((Mole)this).useAbility();
                            return;
                }
                       if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GIRAFFE && es){
                              ((Giraffe)this).useAbility();
                            return;
                }
  
                              if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.SEAL && es){
                              ((Seal)this).useAbility();
                            return;
                }
                                      if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GIANTSPIDER && es){
                              ((GiantSpider)this).useAbility();
                            return;
                }
                                                if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.MACAW && es){
                              ((Macaw)this).useAbility();
                            return;
                }
                                if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.OCTOPUS && es){
                              ((Octopus)this).useAbility();
                            return;
                }
                         if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.ARMADILLO && es){
                              ((Armadillo)this).useAbility();
                            return;
                }
                        if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PENGUIN && es){
                              ((Penguin)this).useAbility();
                            return;
                }

                if(this.getInfo().getType() == AnimalType.OSTRICH && es){
                  return;
      }
      if(this.getInfo().getType() == AnimalType.BLACKWIDOW && es){
        return;
}
                    if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CASSOWARY && es){
                              ((Cassowary)this).useAbility();
                            return;
                }
                         if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.REINDEER && es){
                              ((Reindeer)this).useAbility();
                            return;
                }
                                    if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CRAB && es){
                              ((Crab)this).useAbility();
                            return;
                }
                                        if(!this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.BIGFOOT && es){
                                 ((Bigfoot)this).holdingW = true;
                              
                            return;
                }
                                                     if(!this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.RATTLESNAKE && es){
                                 ((RattleSnake)this).holdingW = true;
                              
                            return;
                }
                                    if(!this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.TIGER && es){
                                 ((Tiger)this).holdingW = true;

                                 return;
                                    }
                              
                       
                    if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.FLAMINGO && es){
                              ((Flamingo)this).useAbility();
                            return;
                }
                            if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WARTHOG && es){
                              ((Warthog)this).useAbility();
                            return;
                }
                   if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.GAZELLE && es){
                              ((Gazelle)this).useAbility();
                            return;
                }
                          if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PUFFERFISH && es){
                              ((PufferFish)this).useAbility();
                            return;
                }

                                       if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.HEDGEHOG && es){
                              ((Hedgehog)this).useAbility();
                            return;
                }
                            if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PIGEON && es){
                              ((Pigeon)this).useAbility();
                            return;
                }
                 if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.CHICKEN && es){
                              ((Chicken)this).useAbility();
                            return;
                }
                if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WOODPECKER && es){
                              ((Woodpecker)this).useAbility();
                            return;
                }
                  if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.COBRA && es){
                              ((Cobra)this).useAbility();
                            return;
                }
                       if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.PEACOCK && es){
                              ((Peacock)this).useAbility();
                            return;
                }
                        if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.TOUCAN && es){
                              ((Toucan)this).useAbility();
                            return;
                }
                             if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.DEER && es){
                              ((Deer)this).useAbility();
                            return;
                             }
                            if(this.getInfo().getAbility().isToHold() && !this.getInfo().getAbility().isActive() && this.getInfo().getType() == AnimalType.WALRUS && es){
                              ((Walrus)this).useAbility();
                            return;
                }
            
            
        boolean ae = false;
        if (isUsingAbility)
            ae = true;
        else
            ae = false;
        isUsingAbility = es;
        if (ae && !es) {
            onAbilityDisable();
        
    }
    }

    public int ramDamage = 0;

    public void makeRam(double x, double y, Animal rammer, int damage) {
        this.hurt(damage, 0, ((GameObject) rammer));
        this.ramDamage = damage;
        this.rammer = rammer;
        this.isRammed = true;
        this.rammedX = x;
        this.rammedY = y;
        this.rammed = 5;
        this.flag_flying = true;
        this.rammedUp = true;
        if (this.getInfo().getAbility().getType() != 0) {
            this.getInfo().getAbility().setActive(false);
            this.getInfo().getAbility().setUsable(false);
            this.setDiveUsable(false);
            this.setDiveActive(false);
            this.flag_usingAbility = false;
            
        }
    }

    public boolean falling = false;

    public double fallX = 0;
    public double fallY = 0;

    public double fallStep = 0;

    public int fallTime = 1000;

    public Timer fallTimer = new Timer(fallTime);

    public void makeFall(double x, double y, Animal flying) {
        this.flag_flying = true;
        this.flag_isGrabbed = false;
        falling = true;
        fallX = x;
        fallY = y;

        fallTime = (int) Math.round(1400 + (this.getZ() * 20));
        fallStep = (fallTime / Constants.TICKS_PER_SECOND) / this.getZ();
        if (fallStep < 1)
            fallStep = 1;

        fallTimer.setMaximumTime(fallTime);
        fallTimer.reset();
        if (this.getInfo().getAbility().getType() != 0) {
            this.getInfo().getAbility().setActive(false);
            this.getInfo().getAbility().setUsable(false);
            this.setDiveUsable(false);
            this.setDiveActive(false);
        }
    }

    private int barType = 0;

    public void setBarType(int a) {
        barType = a;
    }

    public int getBarType() {
        return this.barType;
    }

    public void onDeath() {
        this.getClient().canUpgradeOrDowngrade = true;

    }

    private int teamID = -1;

    public int getTeam() {
        return teamID;
    }

    public void setTeam(int a) {
        teamID = a;
    }

    private Animal bleeder;

    public int bleedingSeconds = 0;

    public int paralizedSeconds = 0;

    public void setBleeding(int i, Animal trex, double dam) {
        this.bleedingSeconds = i;
        this.bleeder = trex;
        this.bleedDamage = dam;
    }

    public void setParalize(int i, Animal paralizer) {
        this.paralizedSeconds = i;
        this.lastKiller = paralizer;
        this.setStun(i);
    }

}
