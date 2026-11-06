package io.mopesbox.Client;

import io.mopesbox.Client.GameClient;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier11.Falcon;
import io.mopesbox.Animals.Tier12.Markhor;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier17.BlackDragon;
import io.mopesbox.Animals.Tier8.Snowyowl;
import io.mopesbox.Client.AI.AIDummy;
// import org.javacord.api.entity.channel.*;
// import org.javacord.api.entity.message.embed.EmbedBuilder;
import io.mopesbox.Client.AI.PvPBot;
import io.mopesbox.Constants;
import io.mopesbox.Main;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Networking.Selection;
import io.mopesbox.Objects.*;
import io.mopesbox.Objects.BattleRoyale.*;
import io.mopesbox.Objects.Biome.*;
import io.mopesbox.Objects.Dangerous.*;
import io.mopesbox.Objects.Eatable.*;
import io.mopesbox.Objects.ETC.*;
import io.mopesbox.Objects.Fun.*;
import io.mopesbox.Objects.Juggernaut.Meteor;
import io.mopesbox.Objects.PvP.*;
import io.mopesbox.Objects.Static.*;

/* 
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Objects.BattleRoyale.SafeArea;
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Objects.Dangerous.MopeCoinBox;
import io.mopesbox.Objects.ETC.Button;
import io.mopesbox.Objects.ETC.PumpkinBall;
import io.mopesbox.Objects.ETC.ScorpionClaw;
import io.mopesbox.Objects.ETC.TrexShake;
import io.mopesbox.Objects.Fun.Amogus;
import io.mopesbox.Objects.Fun.FireForCircle;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Objects.PvP.Target;
import io.mopesbox.Objects.Static.Lake;
import io.mopesbox.Objects.Static.SandboxArena;
import io.mopesbox.Objects.Eatable.Egg;
import io.mopesbox.Objects.Eatable.Healstone;
*/
import io.mopesbox.Server.MsgReader;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.PacketException;
import io.mopesbox.Utils.PvPRequest;
import io.mopesbox.Utils.RectangleUtils;
import io.mopesbox.Utils.RemoveList;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import io.mopesbox.World.PlayerStat;
import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
public class GameClient {

    public boolean isShouldRemove() {
        return shouldRemove;
    }

    public void shouldRemove(boolean t) {
        shouldRemove = t;
    }

    private boolean tBot = false;

    public boolean isBot() {
        return tBot;
    }

    public void onHurt(GameObject o) {
        //
        if (o instanceof Animal) {
            canUpgradeOrDowngrade = false;
            canUpgradeOrDowngradeTimer.reset();
        }
    }

    public void makeBot(boolean bot) {
        this.tBot = bot;
    }

    public String dotColor = "cyan";
    public int color = 0;
    public int adminType;

    private boolean isVerifiedByToken = false;
    public boolean canUpgradeOrDowngrade = true;
    public Timer canUpgradeOrDowngradeTimer = new Timer(3000);
    // public Timer keepDataTimer = new Timer(90000);

    private int coins = 0;
    private int exp = 0;
    private boolean shouldRemove = false;
    // private boolean lockedCamzoom = false;
    private boolean show1v1 = false;
    private boolean is1v1Enabled = false;
    private boolean can1v1 = false;
    private boolean rareHack = false;
    private int pvpRecharge = 0;
    private int coinBegin = Constants.COINSBEGININXSECAFTERSPAWN;
    private int pvpWins = 0;
    private List<PvPRequest> pvpRequests = new ArrayList<>();
    private Target pvpTarget;
    private List<GameClient> ignoringPvP = new ArrayList<>();
    private int pvpId = 0;
    public int packagesPer5s = 0;
    public int upgradesPer5s = 0;
    public Timer removeSession = new Timer(10000);
    public Timer packagesTimer = new Timer(5000);
    private int shootType = 0;
    private boolean hidePerms = false;
    private boolean noXP = false;

    public void setNoXP(boolean aboba) {
        noXP = aboba;
    }

    public boolean isNoXPEnabled() {
        return noXP;
    }

    public void setHidePerms(boolean aboba) {
        hidePerms = aboba;
    }

    public boolean compareNicks(String nick1, String nick2) {
        return (nick1.equalsIgnoreCase(nick2.toLowerCase())
                || nick1.toLowerCase().contains(nick2.toLowerCase())
                || nick2.toLowerCase().contains(nick1.toLowerCase())
                || Objects.equals(nick1.toLowerCase(), nick2.toLowerCase()));
    }

    public boolean isHidingPerms() {
        return hidePerms;
    }

    public void setShootType(int type) {
        shootType = type;
    }

    public int getShootType() {
        return shootType;
    }

    public void resetIgnoringPvP() {
        ignoringPvP.clear();
    }

    public boolean rareHackEnabled() {
        return rareHack;
    }

    public void setRareHack(boolean aboba) {
        rareHack = aboba;
    }

    public void setPvPTarget() {
        if (this.getPlayer() != null && this.pvpTarget == null && !this.getPlayer().isInvincible()) {
            this.pvpTarget = new Target(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), this, 1,
                    0);
            this.room.addObj(this.pvpTarget);
        }
    }

    public Target markhorTarget;

    public Target falconTarget;
    public Target ostrichTarget;
    public Target snowyowlTarget;


    private Target DTarget;
    private Button pushButton;
    private Button kickButton;
    private Button warnButton;
    private Button ipBanButton;
    private Button banButton;
    private Button statButton;
    public boolean freezed;
    private boolean interacting = false;
    private Animal targeting;
    public Button hideInterFace;
    public Button FixedCamara;
    public Button FixedCamara1;
    public Button LightMode1;
    public Button LightMode;
    public Button DarkMode;
    public Button DarkMode1;
    public String abobusss;

    public boolean muted;
    public boolean canUpgrade = false;

    public Button hideInterFace1;

    public Button not_hideInterFace;
    public Button not_FixedCamara;
    public Button not_FixedCamara1;

    public Button not_hideInterFace1;

    public boolean flag_fixedcamara = false;
    public boolean flag_fixedcamara1 = false;
    public Button MuteButton;

    public boolean flag_hideInterface = false;
    public int value1 = 0;
    public boolean flag_hideInterface1 = false;
    public int value2 = 0;
    public int tries = 0;

    public boolean canexecute = true;
    private Button JailButton;
    private Button freezeButton;
    public boolean saveSession = true;

    public ArrayList<Animal> PlayersToHide = new ArrayList<Animal>();
    public ConcurrentHashMap<String, String> logsArray = new ConcurrentHashMap<String, String>();
    private Timer sendLogsTimer = new Timer(5000);


    public void interactD() {
        if (this.getPlayer() != null && this.DTarget != null) {
            if (this.DTarget.getTarget() != null && this.DTarget.getTarget().getClient() != null) {
                interacting = true;
                targeting = this.DTarget.getTarget();
                boolean canKick = true;
                boolean canBan = true;
                boolean canIPBan = true;
                boolean canMute = true;
                boolean canJail = true;
                boolean canFreeze = true;
                String expandJail = "";
                String expandFreeze ="";

                String expandKick = "";
                String expandBan = "";
                String expandIPBan = "";
                String expandMute = "";
                String expandWarn = "";
                boolean canWarn = false;
                if (this.getAccount() != null) {
                    if (this.account.admin >= 2 && this.account.limited == 0) {
                        canWarn = true;
                        canBan = true;
                    }
                }
                if (this.isLimited()) {
                    canKick = true;
                    canBan = false;
                    canIPBan = false;
                    canWarn = false;
                    canJail = false;
                    canMute = false;
                    canFreeze = false;
                }

                String nick123 = this.DTarget.getTarget().getClient().getAccount() == null
                        || targeting.getClient().isHidingPerms() ? "No Account"
                                : this.DTarget.getTarget().getClient().getAccount().name;
                String statText = "Nick: " + nick123 + "\n";
                if ((this.isDeveloper() && !this.isLimited()) && this.getAccount().adminType == 2)
                    statText += "IP: " + targeting.getClient().ip
                            + "\n";
                statText += "Godmode:" + (targeting.getClient().isGodmodeEnabled() ? "Enabled" : "Disabled") + "\n";
                statText += "Infinite Ability:" + (targeting.getClient().instantRecharge ? "Enabled" : "Disabled")
                        + "\n";
                statText += "ClientID:" + (targeting.getClient().clientID) + "\n";

                if (targeting.getClient().getAccount() == null) {
                    canBan = false;
                    expandBan = "\n(No Account)";
                    statText += "No Game Account\n";
                } else {
                    statText += "Account ID: " + targeting.getClient().account.id + "\n";
                    if (targeting.getClient().account.adminType > 0)
                        statText += "Admin Type "
                                + (targeting.getClient().account.adminType == 1 ? "Paid" : "Official") + "\n";
                    if ((targeting.getClient().account.admin >= this.account.admin
                            && !targeting.getClient().isLimited())
                            || (targeting.getClient().account.admin >= this.account.admin && this.isLimited())) {
                        canBan = false;
                        canKick = false;
                        canIPBan = false;
                        canMute = false;
                        canFreeze = false;
                        canJail = false;
                        expandFreeze = "\n(High Admin Level)";
                        expandJail = "\n(High Admin Level)";
                        expandKick = "\n(High Admin Level)";
                        expandBan = "\n(High Admin Level)";
                        expandIPBan = "\n(High Admin Level)";
                        expandMute = "\n(High Admin Level)";
                        statText += "Admin Level: " + targeting.getClient().account.admin
                                + (targeting.getClient().getAccount().limited == 1
                                        && targeting.getClient().getAccount().admin > 0 ? " (Limited)" : "")
                                + "\n";
                    } else {
                        statText += "Admin Level: " + targeting.getClient().account.admin
                                + (targeting.getClient().getAccount().limited == 1
                                        && targeting.getClient().getAccount().admin > 0 ? " (Limited)" : "")
                                + "\n";
                    }
                    if (targeting.getClient().getAccount().limited == 1
                            && targeting.getClient().getAccount().admin > 0) {
                        statText += "Warns: " + targeting.getClient().getAccount().warns + "\n";
                    }
                    if ((this.getAdminType() == 2 && targeting.getClient().account.adminType < 2) && (this.getAccount().admin == 6 && this.getAccount().adminType == 2 && this.getAccount().limited == 0 && targeting.getClient().account.admin < 6)) {
                        if (!canBan && targeting.getClient().getAccount() != null)
                            canBan = true;
                        if (!canKick)
                            canKick = true;
                        if (!canIPBan && !this.isLimited())
                            canIPBan = true;
                        if (!canMute)
                            canMute = true;
                        if (!canJail && !this.isLimited())
                            canJail = true;
                        if (canJail)
                            expandJail = "";
                        else
                            expandJail = "\n(Limited)";
                            if (canFreeze)
                            expandFreeze = "";
                        else
                            expandFreeze = "\n(Limited)";
                        expandKick = "";
                        expandBan = "";
                        if (canIPBan)
                            expandIPBan = "";
                        else
                            expandIPBan = "\nLimited";
                        expandMute = "";
                    }

                }
                if (canWarn) {
                    if (targeting.getClient().getAccount() == null || targeting.getClient().getAccount().limited == 0
                            || targeting.getClient().getAccount().admin < 2) {
                        canWarn = false;
                        if (targeting.getClient().getAccount() == null) {
                            expandWarn = "\n(No Account)";
                        }
                        if (targeting.getClient().getAccount() != null) {
                            if (targeting.getClient().getAccount().admin < 2) {
                                expandWarn = "\n(Not Admin)";
                            }
                            if (targeting.getClient().getAccount().limited == 0) {
                                expandWarn = "\n(Not Limited)";
                            }
                        }
                    }
                }
                if (targeting.getClient().getAccount() != null) {
                    if (this.getAdminType() == 2 && targeting.getClient().account.adminType < 2) {
                        if (!canWarn && targeting.getClient().getAccount() != null)
                            canWarn = true;

                        expandWarn = "";
                    }
                }
                this.kickButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), -180,
                        60, 80, 40, 0, room, this, "KICK" + expandKick, 1, "#00ff30", 255, "#00fbff", "green", canKick,
                        "1");
                this.room.addObj(this.kickButton);
                this.ipBanButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), -80,
                        60, 80, 40, 0, room, this, "IP BLOCK" + expandIPBan, 1, "#00ff30", 255, "#00fbff", "green",
                        canIPBan,
                        "2");
                this.room.addObj(this.ipBanButton);
                this.banButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), 80,
                        60, 80, 40, 0, room, this, "BAN" + expandBan, 1, "#00ff30", 255, "#00fbff", "green", canBan,
                        "3");
                this.room.addObj(this.banButton);
                this.warnButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), 180,
                        60, 80, 40, 0, room, this, "WARN" + expandWarn, 1, "#00ff30", 255, "#00fbff", "green", canWarn,
                        "4");
                this.room.addObj(this.warnButton);
                this.JailButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), 290,
                        60, 80, 40, 0, room, this, "JAIL" + expandJail, 1, "#00ff30", 255, "#00fbff", "green", canJail,
                        "5");
                this.room.addObj(this.JailButton);
                this.MuteButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), -290,
                        60, 80, 40, 0, room, this, "Mute/Unmute Player" + expandMute, 1, "#00ff30", 255, "#00fbff",
                        "green", canMute,
                        "6");
                this.room.addObj(this.MuteButton);
                this.freezeButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), -290,
                140, 80, 40, 0, room, this, "Freeze/UnFreeze Player" + expandFreeze, 1, "#00ff30", 255, "#00fbff",
                "green", canFreeze,
                "7");
        this.room.addObj(this.freezeButton);
                this.statButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), 0,
                        140, 200, 120, 0, room, this, statText, 1, "#00ff30", 255, "#00fbff", "green", false, "");
                this.room.addObj(this.statButton);
            }
            this.removeDTarget();
        } else
            this.removeDTarget();
    }

    public void setDTarget() {
        if (this.getPlayer() != null && this.DTarget == null) {
            if (this.interacting) {
                this.room.removeObj(this.kickButton, this.getPlayer());
                this.kickButton = null;
                this.room.removeObj(this.warnButton, this.getPlayer());
                this.warnButton = null;
                this.room.removeObj(this.ipBanButton, this.getPlayer());
                this.ipBanButton = null;
                this.room.removeObj(this.banButton, this.getPlayer());
                this.banButton = null;
                this.room.removeObj(this.statButton, this.getPlayer());
                this.statButton = null;
                this.room.removeObj(this.JailButton, this.getPlayer());
                this.JailButton = null;
                this.room.removeObj(this.MuteButton, this.getPlayer());
                this.MuteButton = null;
                this.room.removeObj(this.freezeButton, this.getPlayer());
                this.freezeButton = null;

                this.interacting = false;
            } else if (Constants.GAMEMODE == 0 || this.getAccount().admin >= 1) {
                this.DTarget = new Target(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), this, 2,
                        0);
                this.room.addObj(this.DTarget);
                this.pushButton = new Button(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), 0,
                        60, 150, 40, 0, room, this,
                        "TOGGLE PUSHING: " + (this.DTarget.getCollideable() ? "ENABLED" : "DISABLED"), 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "1");
                this.room.addObj(this.pushButton);
                // this.testbb = new Button(this.room.getID(), this.getPlayer().getX(),
                // this.getPlayer().getY(), -160,
                // 60, 150, 40, 0, room, this,
                // "FUCK YOU: " + (this.DTarget.getCollideable() ? "ENABLED" : "DISABLED"), 1,
                // "#FF0000",
                // 255, "#00fbff", "red", true,
                // "1");
                // this.room.addObj(this.testbb);
            }
        }
    }

    public int getPvPID() {
        pvpId++;
        if (pvpId > 200)
            pvpId = 0;
        return pvpId;
    }

    public void removeDTarget() {
        if (this.DTarget != null) {
            this.room.removeObj(this.DTarget, null);
            this.DTarget = null;
            this.room.removeObj(this.pushButton, this.getPlayer());
            this.pushButton = null;
        }
    }

    public void removePvPTarget() {
        if (this.pvpTarget != null) {
            this.room.removeObj(this.pvpTarget, null);
            this.pvpTarget = null;
        }
    }

    public List<PvPRequest> getRequests() {
        return pvpRequests;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void setEXP(int exp) {
        this.exp = exp;
    }

    public void addEXP(int exp) {
        this.exp += exp;
    }

    public int getCoins() {
        return this.coins;
    }

    public int getEXP() {
        return this.exp;
    }

    public void addRequest(PvPRequest a) {
        if (!ignoringPvP.contains(a.getEnemy()))
            pvpRequests.add(a);
    }

    public void removeRequest(PvPRequest a) {
        pvpRequests.remove(a);
    }

    public void clearRequests() {
        pvpRequests.clear();
    }

    public boolean isDActive() {
        return this.DTarget != null;
    }

    public boolean isPvPActive() {
        return this.pvpTarget != null;
    }

    public void addPvPWin() {
        if (pvpWins <= 254)
            pvpWins++;
    }

    public int getPvPWins() {
        return pvpWins;
    }

    public int rechargePvP() {
        return pvpRecharge;
    }

    public boolean show1v1() {
        return show1v1;
    }

    public boolean isPvPEnabled() {
        return is1v1Enabled;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    private int coinsState = 2;

    public String ip;

    public void setIP(String ip) {

        this.ip = ip;

    }

    public int tier;
    public Room room;
    public String session;
    // private Logger log;
    public String clientID;

    public WebSocket socket;

    private int pingTries = 0;

    public Animal player;

    public int canvasW;
    public int canvasH;

    private Account account = null;

    public Account getAccount() {
        return account;
    }


    public List<GameObject> getAddList() {
        return addList;
    }

    public List<GameObject> getUpdateList() {
        return updateList;
    }

    public RemoveList getRemoveList() {
        return removeList;
    }

    public List<GameObject> getVisibleList() {
        return visibleList;
    }


    public void clearAddList() {
        addList.clear();
    }

    public void clearUpdateList() {
        updateList.clear();
    }

    public void clearRemoveList() {
        removeList.clear();
    }

    public void removeFromVisible(GameObject object, GameObject killer) {
        if(addList.contains(object)) addList.remove(object);
        if(updateList.contains(object))updateList.remove(object);
        if(visibleList.contains(object)) visibleList.remove(object);
        if(!removeList.contains(object))removeList.add(object, killer);
    }

    public void addFromVisible(GameObject object) {
        if(!addList.contains(object)) addList.add(object);
        if(!updateList.contains(object)) updateList.add(object);
        if(!visibleList.contains(object)) visibleList.add(object);
    }

    public void send(MsgWriter writer) {
        if (this.socket.isOpen())
            this.socket.send(writer.getData());
    }

    private List<GameObject> addList = new ArrayList<>();
    private List<GameObject> updateList = new ArrayList<>();
    private RemoveList removeList = new RemoveList();

    private List<GameObject> visibleList = new ArrayList<>();


    public double visibleW = 800;
    public double visibleH = 600;

    public boolean isConnected() {
        return connected;
    }

    public boolean isSpectating() {
        return spectating;
    }

    private boolean connected;
    private boolean spectating;

    private String playerName;

    private double camX;
    private double camY;
    private double camvelocityX = 5;
    private double camvelocityY = 5;

    public double getCamX() {
        return camX;
    }

    public void setCamX(double camx) {
        this.camX = camx;
    }

    public double getCamY() {
        return camY;
    }

    public void setCamY(double camy) {
        this.camY = camy;
    }

    public int camzoom = 1700;

    public int baseCamzoom = 1700;

    public int getCamzoom() {
        return this.camzoom;
    }

    public void setCamzoom(int tcamzoom) {
        if (tcamzoom < 100)
            tcamzoom = 100;
        this.camzoom = tcamzoom;
    }

    public boolean isInUpgrade() {
        return inUpgrade;
    }

    public void setInUpgrade(boolean inUpgrade) {
        this.inUpgrade = inUpgrade;
        this.inUpgradeTimer = 99;
    }

    private boolean inUpgrade;

    public List<AnimalInfo> getSelectionList() {
        return selectionList;
    }

    public void setSelectionList(List<AnimalInfo> selectionList) {
        this.selectionList = selectionList;
    }

    private List<AnimalInfo> selectionList;

    public int getXP() {
        return xp;
    }

    public int getNextXP() {
        if (this != null && this.getPlayer() != null && this.getPlayer().getInfo() != null)
            return Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgradeXP();
        else
            return 0;
    }

    public int getPreviousXP() {
        if (this != null && this.getPlayer() != null && this.getPlayer().getInfo() != null)
            return Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getPreviousXP();
        else
            return 0;
    }

    private int lastXPBeforeDie;

    public int getStartXP() {
        if (this != null && this.getPlayer() != null && this.getPlayer().getInfo() != null) {
            this.lastXPBeforeDie = Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getStartXP();
            return Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getStartXP();
        } else
            return 0;
    }

    public int getLastXP1() {
        return lastXPBeforeDie;
    }

    public void setXp(long xp) {
        this.xp = (int) (Math.max(0, Math.min(2000000000, xp)));
    }

    public void addXp(long xp) {
        if (!this.isNoXPEnabled())
            this.setXp(this.getXP() + Math.max(0, Math.min(2000000000, xp)));
    }

    public void takeXp(long xp) {
        if (!this.isNoXPEnabled())
            this.setXp(this.getXP() - Math.max(0, Math.min(2000000000, xp)));
    }

    private int xp = 0;
    private boolean isDeveloper;
    private boolean isArtist;
    private boolean isHelper;
    private boolean isDirector;
    private boolean isModerator;
    private boolean isAdministrator;
    private boolean isLimited;
    private boolean mahdi = false;

    public boolean isDeveloper() {
        return isDeveloper;
    }

    public boolean isArtist() {
        return isArtist;
    }

    public boolean isHelper() {
        return isHelper;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public boolean isAdministrator() {
        return isAdministrator;
    }

    public boolean isDirector() {
        return isDirector;
    }

    public boolean isLimited() {
        return isLimited;
    }

    public void setLimited(boolean isLimited) {
        this.isLimited = isLimited;
    }

    public void setDeveloper(boolean isDeveloper) {
        this.isDeveloper = isDeveloper;
    }

    public void setArtist(boolean isArtist) {
        this.isArtist = isArtist;
    }

    public void setHelper(boolean isHelper) {
        this.isHelper = isHelper;
    }

    public void setModerator(boolean isModerator) {
        this.isModerator = isModerator;
    }

    public void setAdministrator(boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
    }

    public void setDirector(boolean isDirector) {
        this.isDirector = isDirector;
    }

    private boolean isSuperDev;
    private boolean specmode = false;
    public boolean isDevLimited = false;

    public boolean instantRecharge = false;
    private boolean isYTMode = false;
    public int icoPlayer = 0;
    public int developerModeNumber = 0;

    public boolean isYTMode() {
        return isYTMode;
    }

    public boolean isSuperDev() {
        return isSuperDev;
    }

    public void setSuperDev(boolean isSuperDev) {
        this.isSuperDev = isSuperDev;
    }

    public GameClient(Room room, WebSocket socket) {
        if (Constants.ENABLECOINEARNING)
            this.coinsState = 2;
        else
            this.coinsState = 1;
        this.room = room;

        this.setDeveloper(false);
        this.setSuperDev(false);
        this.setArtist(false);
        this.setHelper(false);
        this.setModerator(false);
        this.setAdministrator(false);
        this.setDirector(false);
        this.setLimited(true);

        this.session = Utils.randomString(10);

        this.clientID = Utils.randomString(5);
        // this.log = Main.log;

        this.pingTries = 0;

        this.socket = socket;

        this.canvasW = 1920;
        this.canvasH = 1080;

        this.connected = false;
        this.spectating = true;

        this.playerName = "";

        this.inUpgrade = false;

        this.tier = Constants.STARTING_TIER;
        this.selectionList = null;

        this.setCamX(Utils.randomDouble(100, Constants.WIDTH - 100));
        this.setCamY(Utils.randomDouble(100, Constants.HEIGHT - 100));

        // this.instantRecharge = true;
        // this.setGodmode(true);
    }

    public boolean reconnect;
    public String ses = "???";
    // public boolean canSaveClient = true;

    public void onOpen() {
        if (Constants.GAMEMODE == 3) {
            this.setTeam(Utils.randomInt(0, 2));
        }
        Main.instance.ipChecker.interactWithIP(this);

    }

    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            if (killer instanceof Animal)
                ((Animal) killer).onKill(this.getPlayer());

            this.room.removeObj(this.getPlayer(), killer);
               if (killer != null && killer instanceof Animal)
                ((Animal) killer).getClient().addXp(this.getXP() / (int) 1.9);
            this.getPlayer().onDeath();
           this.player = null;
            this.spectating = true;
            this.setXp(this.getXP() / 2);
            this.send(Networking.deathPacket(reason, this.coins + "", this.getXP()));
            if (this.account != null && this.exp > 0 && this.coinsState != 1) {
                this.room.accountConnector.addExp(String.valueOf(this.account.id), this.account.password_token,
                        this.exp);
            }
            this.exp = 0;
            if (this.account != null && this.coins > 0 && this.coinsState != 1)
                this.room.accountConnector.addCoins(String.valueOf(this.account.id), this.account.password_token,
                        this.coins);
            this.coins = 0;
            if (this.account != null && this.coinsState != 1 && this.coinsState != 2) {
                this.coinsState = 0;
                this.coinBegin = Constants.COINSBEGININXSECAFTERSPAWN;
            }
            this.setInUpgrade(false);
        }
    }

    public void onClose() {
        Main.instance.ipChecker.removeIP(this.ip);
        this.shouldRemove = true;
    
        if (this.getPlayer() != null) {


            
            if (!this.saveSession) {
                 this.room.removeObj(this.getPlayer(), null);
                this.player = null;

            }

        }

    }

    private int evaluating = 0;

    private boolean passedJoinGame = false;

    private Timer scamTimer = new Timer(5000);
    private Timer checkIfAccBanned = new Timer(5000);
    private Timer heartbeat = new Timer(20000);


    public void onMessage(byte[] bytes) {
        MsgReader reader = new MsgReader(bytes);
        if (packagesPer5s > 1000) {
            if (!this.socket.isClosed()) {

                MessageType messageType;
                try {
                    messageType = MessageType.byOrdinal(reader.readUInt8());
                    logsArray.put("package per 5s > 100 from "+this.ip, "Last packet sent: "+messageType);

                } catch (PacketException e) {
                    e.printStackTrace();
                }


                this.saveSession = false;

                this.sendDisconnect(true,
                        "I don't like that either.",
                        false);
            }

            return;
        }

        try {

            MessageType messageType = MessageType.byOrdinal(reader.readUInt8());

            if (messageType == null) {
                if (!this.socket.isClosed()) {

                    logsArray.put("package with no content from "+this.ip, "...");
                    this.saveSession = false;

                    this.sendDisconnect(true,
                            "i don't like that either",
                            false);

                    // Main.log.info("Kicked player for sending empty package");
                }
            }

            if (messageType != MessageType.MOUSEPOS) {
                packagesPer5s++;
                if (Constants.ENABLEPACKETLOGGING) {
                    if(messageType != MessageType.HEARTBEAT){
                        if(messageType != MessageType.RIGHTCLICK){
                            if(messageType != MessageType.LEFTCLICK){
                                if(messageType != MessageType.RECAPTCHA){
                                    if(messageType != MessageType.CLIENTRESIZE){
                                        if(messageType != MessageType.UPGRADE){
                                            if(messageType != MessageType.DOWNGRADE){
                                                if(messageType != MessageType.WATERSHOOT){
                                                    if(messageType != MessageType.GAMESELECTANIMAL){
                    logsArray.put("package received from "+this.ip, "PacketType: "+messageType);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // this.getRoom().main.guiThread.toLog("[PACKET LOG]" + "NAME:"
                            // + (this.getPlayer().getPlayerName().length() > 0 ? this.getPlayer().getPlayerName()
                                    // : "SPECTATOR")
                            // + "TYPE" + ":" + messageType);
                }

            }

            switch (messageType) {
                case FIRSTCONNECT: {
                    this.handleFirstConnect(reader);
                    break;
                }
                case HEARTBEAT: {
                    this.heartbeat.reset();
                    break;
                }
                case SPECTATEMODE: {
                    if (this.isSpectating()) {
                        specmode = !specmode;
                        MsgWriter writer = new MsgWriter();
                        writer.writeType(MessageType.SPECTATEMODE);
                        writer.writeBoolean(specmode);
                        this.send(writer);
                    }
                    break;
                }
                case CUSTOMINTERFACE: {
                    if (this.getPlayer() != null && this.getPlayer() instanceof BlackDragon) {
                        this.send(Networking.customInterface(room, ((BlackDragon) this.getPlayer()), 1));
                    }
                    break;
                }
                // case CUSTOMINTERFACEBTN: {
                // int type = reader.readUInt8();
                // String buttonID = reader.readString();

                // switch(buttonID){
                // case "Fly High":{
                // if(this.getPlayer() instanceof Santa){
                // ((Santa)this.getPlayer()).mainAbility = 0;
                // }
                // break;
                // }
                // case "Gives Gifts":{
                // if(this.getPlayer() instanceof Santa){
                // this.setShootType(4);
                // }
                // break;
                // }
                // case "Flying Sleigh":{
                // if(this.getPlayer() instanceof Santa){
                // ((Santa)this.getPlayer()).mainAbility = 1;
                // }
                // break;
                // }
                // }
                // break;
                // }
                case REQUESTACTION: {
                    if (Constants.EVENTRIGHTNOW)
                        return;

                    if (this.getRequests().size() > 0 && this.getPlayer() != null && this.can1v1) {
                        int actiontype = reader.readUInt8();
                        int reqid = reader.readUInt8();
                        PvPRequest selec = null;
                        for (PvPRequest pvpr : this.getRequests()) {
                            if (pvpr.getID() == reqid) {
                                selec = pvpr;
                            }
                        }
                        if (selec != null) {
                            if (actiontype == 0) { // reject
                                this.removeRequest(selec);
                            } else if (actiontype == 1) { // accept
                                if ((((PvPArena) this.getPlayer().arenaObject) == null)) {
                                    this.removeRequest(selec);
                                    this.clearRequests();
                                    if (this.getPlayer() != null && !this.getPlayer().flag_flying
                                            && !this.getPlayer().inArena && selec.getEnemy().getPlayer() != null
                                            && !selec.getEnemy().getPlayer().flag_flying
                                            && !selec.getEnemy().getPlayer().inArena) {
                                        PvPArena newarena = new PvPArena(this.room.getID(), this.getPlayer().getX(),
                                                this.getPlayer().getY(), selec.getEnemy(), this,
                                                this.room.getFightNumber(),
                                                this.room);
                                        this.room.addObj(newarena);
                                    }
                                }
                            } else if (actiontype == 2) { // ignore
                                this.removeRequest(selec);
                                ignoringPvP.add(selec.getEnemy());
                                List<PvPRequest> reqdel = new ArrayList<>();
                                for (PvPRequest reqe : pvpRequests) {
                                    if (reqe.getEnemy() == selec.getEnemy()) {
                                        reqdel.add(reqe);
                                    }
                                }
                                pvpRequests.removeAll(reqdel);
                            }
                        }
                    }
                    break;
                }
                case RECAPTCHA: {
                    String token = reader.readString();
                    if (token != null && !token.equals("error")) {
                        String resp = this.room.accountConnector.checkToken(token);
                        // Main.instance.log.info(resp);
                        Gson gson = new Gson();
                        JsonObject ress = gson.fromJson(resp, JsonObject.class).getAsJsonObject("google_response");
                        // Main.instance.log.info(ress);
                        // Main.log.info(ress);
              
                        if (ress.get("success").getAsBoolean()
                                && ress.get("hostname").getAsString().equalsIgnoreCase("mope2020.fun")) {
                            if (ress.get("score").getAsDouble() < 0.5) {
                                logsArray.put("AccountServer: Solved bot attack "+this.ip, "...");
                                this.saveSession = false;

                                this.sendDisconnect(true,
                                        "MOPERR_420",
                                        false);

                            } else {
                                this.isVerifiedByToken = true;
                            }
                        } else {
                            logsArray.put("AccountServer: Failed captcha verify from "+this.ip, "...");
                            this.saveSession = false;

                            this.sendDisconnect(true,
                                    "Something went wrong...",
                                    false);
                        }
                    } else {
                        logsArray.put("AccountServer: Captcha error from "+this.ip, "...");
                        this.saveSession = false;

                        this.sendDisconnect(true,
                                "Captcha error. Please reload the page.",
                                false);
                    }
                    break;
                }
                case INVITE1V1: {
                    if (this.show1v1) {
                        if (this.is1v1Enabled && this.rechargePvP() < 1) {
                            this.setPvPTarget();
                        }
                    }
                    break;
                }
                case RELOADACCOUNTDATA: {
                    if (this.account != null) {
                        this.updateAccount();
                    }
                    break;
                }

                case PROMPT: {
                    if (this.evaluating != 0) {
                        if (this.evaluating == 1) {
                            String reason = reader.readString();
                            GameClient client = this.targeting.getClient();
                            if (client.getAccount() != null) {
                                if ((this.isSuperDev && !this.isDevLimited)
                                        || (client.account.adminType != 2 && !client.isSuperDev())
                                        || (client.account.adminType != 2 && client.isDevLimited)) {
                                    if (this.getAccount().admin >= 2) {

                                        client.saveSession = false;
                                        this.room.accountConnector.sendLog("Kick", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);


                                        client.sendDisconnect(false,
                                                "You were kicked for: " + reason + " By: "
                                                        + this.getPlayer().getPlayerName(),
                                                false);

                                        this.send(Networking.popup("Successfully kicked " + client.playerName,
                                                "success", 5));
                                    } else {
                                        this.send(Networking.popup("You can't kick admin!", "error", 5));
                                    }
                                } else if (client.account.adminType == 2 && client.account.admin > 2) {
                                    if (!client.isHidingPerms())
                                        this.send(Networking.popup("You can't kick developer!", "error", 5));
                                    else {
                                        this.send(Networking.popup("Successfully kicked " + client.playerName,
                                                "success", 5));
                                        client.send(Networking.popup(
                                                this.playerName + " has attempted to Kick you. Safely moved to ghost.",
                                                "error", 5));
                                        client.getPlayer().isGhost = true;
                                    }
                                }
                            } else if (client.getAccount() == null) {
                                if (this.account.admin >= 2) {
                                    this.room.accountConnector.sendLog("Kick", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);


                                    client.sendDisconnect(false,
                                            "You were kicked for: " + reason + " By: "
                                                    + this.getPlayer().getPlayerName(),
                                            false);
                                            

                                    this.send(
                                            Networking.popup("Successfully kicked " + client.playerName, "success", 5));

                                }
                            }
                            this.setDTarget();
                            this.evaluating = 0;

                        } else if (this.evaluating == 2) {
                            this.abobusss = reader.readString();

                            GameClient client = this.targeting.getClient();
                            if (client.getAccount() != null) {
                                if ((this.isSuperDev && !this.isDevLimited)
                                        || (client.account.adminType != 2 && !client.isSuperDev())
                                        || (client.account.adminType != 2 && client.isDevLimited)) {
                                    if (this.getAccount().admin >= 4) {

                                        // this.send(Networking.popup("Successfully banned " + client.playerName
                                        // + " Account ID: " + client.account.id, "success", 5));

                                        this.send(Networking.promptReason(
                                                "Provide a time for ban (in minutes), type 0 to do a permant ban!"));

                                        this.evaluating = 6;
                                    } else {
                                        this.send(Networking.popup("wtf u cant.",
                                                "cantafford", 5));
                                    }

                                } else {
                                    this.send(Networking.popup("You can't ban admin!", "error", 5));
                                }

                            }

                            this.setDTarget();
                            if (this.evaluating != 6)
                                this.evaluating = 0;
                        } else if (this.evaluating == 3) {
                            String reason = reader.readString();
                            if (Utils.isNumeric(reason)) {
                                GameClient client = this.targeting.getClient();

                                if (client.getAccount() != null) {
                                    if (((this.isDeveloper && !this.isDevLimited))
                                            || (client.account.adminType != 2 && !client.isDeveloper())
                                            || (client.account.adminType != 2 && client.isDevLimited)) {
                                        if (this.getAccount().admin >= 5 && this.getAccount().adminType == 2) {

                                this.room.accountConnector.sendLog("IPBAN (Until server restarts)", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);

                                            this.room.ipblock.addIP(this.targeting.getClient().ip,
                                                    Integer.parseInt(reason));


                                            client.sendDisconnect(false,
                                                    "MOPERR_691",
                                                    false);
                                            this.send(Networking.popup("Successfully banned by ip " + client.playerName,
                                                    "success", 5));
                                        } else {
                                            this.send(Networking.popup("wtf u cant!", "error", 5));
                                        }
                                    } else {
                                        this.send(Networking.popup("You can't ban admin!", "error", 5));

                                    }
                                } else {
                                    if (this.getAccount().admin >= 5 && this.getAccount().adminType == 2) {

                                        this.room.ipblock.addIP(this.targeting.getClient().ip,
                                                Integer.parseInt(reason));

                                                this.room.accountConnector.sendLog("IPBAN (Until server restarts)", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);

                                        client.sendDisconnect(false,
                                                "MOPERR_691",
                                                false);
                                        this.send(Networking.popup("Successfully banned by ip " + client.playerName,
                                                "success", 5));
                                    }
                                }

                            } else {
                                this.send(Networking.popup("You should provide time in minutes!", "error", 5));

                            }
                            this.setDTarget();
                            this.evaluating = 0;
                        } else if (this.evaluating == 4) {
                            String reason = reader.readString();
                            GameClient client = this.targeting.getClient();
                            if (client.getAccount() != null) {
                                if (((this.isSuperDev && !this.isDevLimited))
                                        || (client.account.adminType != 2 && !client.isSuperDev())
                                        || (client.account.adminType != 2 && client.isDevLimited)) {
                                    if (this.getAccount().adminType == 2) {
                                    this.room.accountConnector.sendLog("Warn", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);

                                        this.room.accountConnector.warnPlayer(String.valueOf(client.account.id), reason,
                                                String.valueOf(this.account.id));
                                        this.send(Networking.popup("Successfully warned " + client.playerName
                                                + " Account ID: " + client.account.id, "success", 5));
                                        client.send(Networking.popup("You got warned! Reason: " + reason, "error", 15));
                                        client.updateAccount();

                                    } else {
                                        this.send(Networking.popup("wtf u cant",
                                                "cantafford", 5));
                                    }
                                }
                            } else {
                                if (this.getAccount().adminType == 2) {
                                    this.send(Networking.popup(
                                            "Look  like he has no account. Just sending a popup with alert",
                                            "cantafford", 5));
                                    client.send(Networking.popup("You got warned! Reason:" + reason, "error", 15));
                                }
                            }
                            this.setDTarget();
                            this.evaluating = 0;
                        } else if (this.evaluating == 5) {

                            String reason = reader.readString();
                            GameClient client = this.targeting.getClient();

                            if (client.getAccount() != null) {
                                if ((this.isSuperDev && !this.isDevLimited)
                                        || (client.account.adminType != 2 && !client.isSuperDev())
                                        || (client.account.adminType != 2 && client.isDevLimited)) {
                                    if (this.getAccount().admin >= 1) {
                    this.room.accountConnector.sendLog("Mute", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, reason);


                                        // this.room.accountConnector.warnPlayer(String.valueOf(client.account.id),
                                        // reason, String.valueOf(this.account == null ? -1 : this.account.id));
                                        this.send(Networking.popup("Successfully muted " + client.playerName, "success",
                                                5));
                                        client.send(Networking.popup("You have been muted: " + reason, "error", 15));
                                        client.muted = true;

                                    } else {
                                        this.send(Networking.popup("wtf u cant", "error", 5));
                                    }
                                } else {
                                    this.send(Networking.popup("You can't mute admin", "error", 5));

                                }
                            } else {
                                if (this.getAccount().adminType == 2) {
                                    this.send(
                                            Networking.popup("Successfully muted " + client.playerName, "success", 5));
                                    client.send(Networking.popup("You have been muted: " + reason, "error", 15));
                                    client.muted = true;

                                }
                            }

                            this.setDTarget();
                            this.evaluating = 0;

                        } else if (this.evaluating == 6) {
                            String time = reader.readString();

                            GameClient client = this.targeting.getClient();
                            if (client.getAccount() != null) {
                                if ((this.isSuperDev && !this.isDevLimited)
                                        || (client.account.adminType != 2 && !client.isSuperDev())
                                        || (client.account.adminType != 2 && client.isDevLimited)) {
                                    if (this.getAccount().adminType == 2) {

                                        this.room.accountConnector.sendLog("Ban", this.getAccount().id, client.getAccount() != null? client.getAccount().id : 0, this.abobusss + " TIME (In minutes) "+time+"m");

                                        this.room.accountConnector.banPlayer(String.valueOf(client.account.id),
                                                this.abobusss,
                                                String.valueOf(this.account.id), time);
                                        this.send(Networking.popup("Successfully banned " + client.playerName
                                                + " Account ID: " + client.account.id, "success", 5));

                                        client.sendDisconnect(false,
                                                "You were banned for: " + this.abobusss + " By: "
                                                        + this.getPlayer().getPlayerName(),
                                                true);
                                        client.saveSession = false;

                                        this.abobusss = "";

                                    } else {
                                        this.send(Networking.popup("wtf u cant.",
                                                "cantafford", 5));
                                    }

                                } else {
                                    this.send(Networking.popup("You can't ban admin!", "error", 5));
                                }

                            }

                            this.setDTarget();
                            this.evaluating = 0;
                        }
                    }
                    break;
                }
                case BUTTONCLICKEDID: {
                    int id = reader.readUInt32();
                    boolean holding = reader.readUInt8() == 1;

                    if (!holding) {
                        if (this.kickButton != null && id == this.kickButton.getId()) {
                            this.evaluating = 1;
                            this.send(Networking.promptReason("Provide a reason for kick"));
                        } else if (this.banButton != null && id == this.banButton.getId()) {
                            this.evaluating = 2;
                            this.send(Networking.promptReason("Provide a reason for ban"));
                        } else if (this.ipBanButton != null && id == this.ipBanButton.getId()) {
                            this.evaluating = 3;
                            this.send(Networking.promptReason("Provide a time in minutes for block"));
                        } else if (this.warnButton != null && id == this.warnButton.getId()) {
                            this.evaluating = 4;
                            this.send(Networking.promptReason("Reason of warn"));
                        } else if (this.MuteButton != null && id == this.MuteButton.getId()) {

                            if (this.targeting != null) {

                                if (!this.targeting.getClient().muted) {

                                    this.evaluating = 5;
                                    this.send(Networking.promptReason("Reason of mute"));

                                } else if (this.targeting.getClient().muted && this.account.adminType == 2) {
                                    this.targeting.getClient().muted = false;
                                    this.send(Networking.personalGameEvent(255, "Player has been unmuted!"));

                                }

                            }

                        } else if (this.JailButton != null && id == this.JailButton.getId()) {

                            if (this.sandboxArena != null) {
                                this.room.removeObj(this.sandboxArena, this.getPlayer());
                                this.sandboxArena = null;
                                // this.send(Networking.popup("Removed !", "success", 5));
                            }

                            this.sandboxArena = new SandboxArena(this.room.getID(), this.getPlayer().getX(),
                                    this.getPlayer().getY(), 200, this, room);

                            this.room.addObj(this.sandboxArena);
                            if (this.targeting != null) {

                                this.sandboxArena.canWalk.add(this.targeting.getClient());

                                this.sandboxArena.setStatic(true);

                                this.sandboxArena.attaching = true;

                                this.send(Networking.popup("Jail Created!", "success", 5));
                            } else {
                                this.send(Networking.popup("Something went wrong!", "error", 5));

                            }

                        } else if (this.freezeButton != null && id == this.freezeButton.getId()) {
                            if(this.targeting != null){
                                if(!this.targeting.getClient().freezed){
                                this.targeting.getClient().freezed = true;
                                this.send(Networking.popup("Player now can't move!", "success", 5));
                                return;

                            }else {
                                
                                this.targeting.getClient().freezed = false;
                                this.send(Networking.popup("Player now can move!", "success", 5));

                            }

                            } else {
                                this.send(Networking.popup("Something went wrong!", "error", 5));

                            }

                        }
                        else if (this.pushButton != null && id == this.pushButton.getId()) {
                            this.DTarget.setCollideable(!this.DTarget.getCollideable());
                            this.room.removeVisObj3(this.pushButton, null);
                            this.pushButton.setLabel(
                                    "TOGGLE PUSHING: " + (this.DTarget.getCollideable() ? "ENABLED" : "DISABLED"));

                        } else if (this.hideInterFace != null && id == this.hideInterFace.getId()) {

                            if (this.flag_hideInterface == false) {
                                this.flag_hideInterface = true;
                                this.value1 = 1;

                                this.room.removeObj(this.hideInterFace, this.getPlayer());
                                this.hideInterFace = null;
                                this.room.removeObj(this.not_hideInterFace, this.getPlayer());
                                this.not_hideInterFace = null;

                            }
                        }

                        else if (this.hideInterFace1 != null && id == this.hideInterFace1.getId()) {

                            if (this.flag_hideInterface1 == false) {
                                this.flag_hideInterface1 = true;

                                this.room.removeObj(this.hideInterFace1, this.getPlayer());
                                this.hideInterFace = null;
                                this.room.removeObj(this.not_hideInterFace1, this.getPlayer());
                                this.not_hideInterFace1 = null;

                            }
                        } else if (this.FixedCamara != null && id == this.FixedCamara.getId()) {
                            if (this.flag_fixedcamara == false) {
                                this.flag_fixedcamara = true;
                                this.room.removeObj(this.FixedCamara, this.getPlayer());
                                this.room.removeObj(this.not_FixedCamara, this.getPlayer());
                                this.FixedCamara = null;
                                this.not_FixedCamara = null;

                                this.hideInterFace = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                        this.getPlayer().getY(), -80,
                                        60, 150, 40, 0, this.getRoom(), this,
                                        "HIDE PLAYERS ", 1, "#00ff30",
                                        255, "#00fbff", "green", true,
                                        "1");
                                this.room.addObj(this.hideInterFace);

                                this.not_hideInterFace = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                        this.getPlayer().getY(), 180,
                                        60, 150, 40, 0, this.getRoom(), this,
                                        "SHOW PLAYERS ", 1, "#00ff30",
                                        255, "#00fbff", "green", true,
                                        "2");
                                this.room.addObj(this.not_hideInterFace);

                            } else if (this.flag_fixedcamara == true) {
                                this.flag_fixedcamara = false;
                            }

                        } else if (this.FixedCamara1 != null && id == this.FixedCamara1.getId()) {
                            if (this.flag_fixedcamara1 == false) {
                                this.flag_fixedcamara1 = true;
                                this.room.removeObj(this.FixedCamara1, this.getPlayer());
                                this.room.removeObj(this.not_FixedCamara1, this.getPlayer());
                                this.FixedCamara = null;
                                this.not_FixedCamara = null;
                                this.hideInterFace1 = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                        this.getPlayer().getY(), -80,
                                        60, 150, 40, 0, this.getRoom(), this,
                                        "HIDE PLAYERS ", 1, "#00ff30",
                                        255, "#00fbff", "green", true,
                                        "1");
                                this.room.addObj(this.hideInterFace1);

                                this.not_hideInterFace1 = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                        this.getPlayer().getY(), 180,
                                        60, 150, 40, 0, this.getRoom(), this,
                                        "SHOW PLAYERS ", 1, "#00ff30",
                                        255, "#00fbff", "green", true,
                                        "2");
                                this.room.addObj(this.not_hideInterFace1);

                            } else if (this.flag_fixedcamara1 == true) {
                                this.flag_fixedcamara1 = false;
                            }
                        }

                        else if (this.not_FixedCamara != null && id == this.not_FixedCamara.getId()) {

                            this.room.removeObj(this.FixedCamara, this.getPlayer());
                            this.room.removeObj(this.not_FixedCamara, this.getPlayer());
                            this.FixedCamara = null;
                            this.not_FixedCamara = null;

                            this.hideInterFace = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                    this.getPlayer().getY(), -80,
                                    60, 150, 40, 0, this.getRoom(), this,
                                    "HIDE PLAYERS ", 1, "#00ff30",
                                    255, "#00fbff", "green", true,
                                    "1");
                            this.room.addObj(this.hideInterFace);

                            this.not_hideInterFace = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                    this.getPlayer().getY(), 180,
                                    60, 150, 40, 0, this.getRoom(), this,
                                    "SHOW PLAYERS ", 1, "#00ff30",
                                    255, "#00fbff", "green", true,
                                    "2");
                            this.room.addObj(this.not_hideInterFace);

                        } else if (this.not_FixedCamara1 != null && id == this.not_FixedCamara1.getId()) {

                            this.room.removeObj(this.FixedCamara1, this.getPlayer());
                            this.room.removeObj(this.not_FixedCamara1, this.getPlayer());
                            this.FixedCamara = null;
                            this.not_FixedCamara = null;

                            this.hideInterFace1 = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                    this.getPlayer().getY(), -80,
                                    60, 150, 40, 0, this.getRoom(), this,
                                    "HIDE PLAYERS ", 1, "#00ff30",
                                    255, "#00fbff", "green", true,
                                    "1");
                            this.room.addObj(this.hideInterFace1);

                            this.not_hideInterFace1 = new Button(this.getRoom().getID(), this.getPlayer().getX(),
                                    this.getPlayer().getY(), 180,
                                    60, 150, 40, 0, this.getRoom(), this,
                                    "SHOW PLAYERS ", 1, "#00ff30",
                                    255, "#00fbff", "green", true,
                                    "2");
                            this.room.addObj(this.not_hideInterFace1);

                        } else if (this.not_hideInterFace != null && id == this.not_hideInterFace.getId()) {
                            this.room.removeObj(this.not_hideInterFace, this.getPlayer());
                            this.room.removeObj(this.hideInterFace, this.getPlayer());

                            this.hideInterFace = null;

                            this.not_hideInterFace = null;

                        } else if (this.not_hideInterFace1 != null && id == this.not_hideInterFace1.getId()) {
                            this.room.removeObj(this.not_hideInterFace1, this.getPlayer());
                            this.room.removeObj(this.hideInterFace1, this.getPlayer());
                            this.not_hideInterFace1 = null;
                            this.hideInterFace1 = null;

                        }

                        break;
                    }
                }

                case KEYD: {
                    if (this.isHelper() && this.getPlayer() != null && reader.readUInt8() == 1 && this.getAccount().adminType == 2) {
                        if (!this.isDActive())
                            this.setDTarget();
                        else
                            this.interactD();
                    }
                    break;
                }
                case KEYE: {
                    if (this.isHelper() || this.isDeveloper() && this.getPlayer() != null && reader.readUInt8() == 1) {
                        if (this.getPlayer() != null && this.getPlayer().isInvincible()) {
                            this.getPlayer().setImmunity(0);
                        }
                        this.getPlayer().hurt(this.getPlayer().maxHealth / 8, 0, null);
                    }
                    break;
                }
                case RIGHTCLICK: {
                    if (this.getPlayer() != null) {
                        boolean es = reader.readUInt8() == 1;
                        if (this.pvpTarget != null) {
                            if (!es) {
                                Animal sel = this.pvpTarget.getTarget();
                                if (sel != null && sel.getClient().pvpRequests.size() < 1) {
                                    sel.getClient().addRequest(new PvPRequest(this, 20, this.getPvPID()));
                                    if (ignoringPvP.contains(sel.getClient()))
                                        ignoringPvP.remove(sel.getClient());
                                } else if (!this.instantRecharge)
                                    pvpRecharge = 99;
                                this.removePvPTarget();
                            }
                        } else {

                            // }else if(!es){
                            // if(this.getPlayer().getInfo().getAbility().isToHold()){
                            // if(this.getPlayer().getInfo().getAbility().isActive() &&
                            // this.getPlayer().getInfo().getType() == AnimalType.MOLE){
                            // ((Mole)this.getPlayer()).stopAbil();
                            // }
                            // }
                            // }
                            this.getPlayer().changeAbility(es);

                        }
                    }

                    break;
                }
                case WATERSHOOT: {
                    if (this.getPlayer() != null) {
                        this.getPlayer().waterShoot();
                    }
                    break;
                }
                case REQUESTJOIN: {
                    this.handleRequest(reader);
                    break;
                }

                case LEFTCLICK: {
                    if (this.player != null) {
                        int bol = reader.readUInt8();
                        if (this.player.canMove())
                            this.player.changeBoost(bol == 1);
                    }
                    break;
                }

                case UPGRADE: {
                    if (this.isSuperDev()
                            || ((Constants.ADMINFOREVERYONE == true || Constants.EVERYONECANUPGRADE
                                    || this.isDeveloper())
                                    && (Constants.EVENTRIGHTNOW == false && Constants.GAMEMODE != 2))) {
                        if (!this.isSuperDev() && !canUpgradeOrDowngrade) {
                            this.send(Networking.personalGameEvent(255,
                                    "You need to wait " + this.canUpgradeOrDowngradeTimer.tim / 1000
                                            + " seconds to upgrade (You were attacked by another animal recently)."));
                            return;
                        }
                        if (this.getPlayer() != null && !this.getPlayer().inArena) {
                            if (this.player != null && this.canUpgrade) {
                                if (this.getPlayer().inArena) {
                                    ((PvPArena) this.getPlayer().arenaObject).remPlayer(this);
                                }

                                if (this.tier < 17)
                                    this.tier++;

                                else

                                    this.tier = 1;
                                this.setXp(Tier.byOrdinal(this.tier).getStartXP());
                                this.setInUpgrade(true);
                                this.send(Selection.createSelection(this, 0, 15, this.tier,
                                        Tier.byOrdinal(this.tier).getAnimalInfo()));
                                this.canUpgrade = false;

                            }
                        }
                    }

                    break;
                }

                case DOWNGRADE: {
                    if (this.isSuperDev()
                            || ((Constants.ADMINFOREVERYONE == true || Constants.EVERYONECANUPGRADE
                                    || this.isDeveloper())
                                    && (Constants.EVENTRIGHTNOW == false && Constants.GAMEMODE != 2))) {
                        if (!this.isSuperDev() && !canUpgradeOrDowngrade) {
                                 this.send(Networking.personalGameEvent(255,
                                    "You need to wait " + this.canUpgradeOrDowngradeTimer.tim / 1000
                                            + " seconds to upgrade (You were attacked by another animal recently)."));
                            return;
                        }
                        if (this.getPlayer() != null && !this.getPlayer().inArena) {
                            if (this.getPlayer() != null && this.canUpgrade) {
                                if (this.getPlayer().inArena) {
                                    ((PvPArena) this.getPlayer().arenaObject).remPlayer(this);
                                }

                                if (this.tier > 1)
                                    this.tier--;

                                else

                                    this.tier = 17;
                                this.setXp(Tier.byOrdinal(this.tier).getStartXP());
                                this.setInUpgrade(true);
                                this.send(Selection.createSelection(this, 0, 15, this.tier,
                                        Tier.byOrdinal(this.tier).getAnimalInfo()));
                                this.canUpgrade = false;

                            }
                        }
                    }
                    break;
                }

                case ZOOM: {
                    boolean wheelup = reader.readUInt8() == 1;
                    short a = reader.readInt16();
                    if(this.getAccount() != null){
                        if(this.account.admin > 0){
                    if (wheelup) {
                        this.setCamzoom(this.getCamzoom() + a);
                    } else {
                        this.setCamzoom(this.getCamzoom() - a);
                    }
                }
                    break;
                }
                }

                case RECONNECTEDBYCLOUDFLARE: {
                }

                case MOUSEPOS:
                    if (this.getPlayer() != null) {
                        short mouseX = reader.readInt16();
                        short mouseY = reader.readInt16();

                        if (!this.getPlayer().canMove() || this.freezed) {

                            this.player.setMouseX(this.getPlayer().getX());
                            this.player.setMouseY(this.getPlayer().getY() + 1);
                            return;

                        }
                        if (!this.getPlayer().isRammed && this.getPlayer().canMove()) {
                            this.player.setMouseX(mouseX);
                            this.player.setMouseY(mouseY + 1);
                        } else if (this.player.isRammed && this.getPlayer().canMove()) {
                            this.player.setMouseX(this.player.rammedX);
                            this.player.setMouseY(this.player.rammedY + 1);
                        }
                    }
                    break;
                case GAMESELECTANIMAL:
                    this.handleSelection(reader);
                    break;
                case CHAT:
                    if (this.getPlayer() == null)
                        return;
                    String amsg = reader.readString();
                    amsg = amsg.replaceAll("﷽", "");
                    amsg = amsg.replaceAll("ௌ", "");
                    amsg = amsg.replaceAll("Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼", "");
                    amsg = amsg.replaceAll(
                            "̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿|̳̳̳̳̿̿̿̿l̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿|̳̳̳̳̿̿̿̿l̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊",
                            "");
                    amsg = amsg.replaceAll(
                            "Тͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥ",
                            "");
                    amsg = amsg.replaceAll(
                            "Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼̠͕̼̠̦͚̫͔̯̹͉͉̘͎͕̼̣̝͙̱̟̹̩̟̳̦̭͉̮̖̭̣̣̞̙̗̜̺̭̻̥͚͙̝̦̲̱͉͖͉̰̦͎̫̣̼͎͍̠̮͓̹̹͉̤̰̗̙͕͇͔̱͕̭͈̳̗̭͔̘̖̺̮̜̠͖̘͓̳͕̟̠̱̫̤͓͔̘̰̲͙͍͇̙͎̣̼̗̖͙̯͉̠̟͈͍͕̪͓̝̩̦̖̹̼̠̘̮͚̟",
                            "");
                    amsg = amsg.replaceAll(
                            "ส็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็",
                            "");
                    // amsg = amsg.replaceAll(" ", "");
                    amsg = amsg.replaceAll(
                            "ฏ๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎ํํํํํํํํํํํํํํํํํํํํํํํํํํ",
                            "");
                    amsg = amsg.replaceAll("ปี้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้", "");
                    amsg = amsg.replaceAll(
                            "Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼̠͕̼̠̦͚̫͔̯̹͉͉̘͎͕̼̣̝͙̱̟̹̩̟̳̦̭͉̮̖̭̣̣̞̙̗̜̺̭̻̥͚͙̝̦̲̱͉͖͉̰̦͎̫̣̼͎͍̠̮͓̹̹͉̤̰̗̙͕͇͔̱͕̭͈̳̗̭͔̘̖̺̮̜̠͖̘͓̳͕̟̠̱̫̤͓͔̘̰̲͙͍͇̙͎̣̼̗̖͙̯͉̠̟͈͍͕̪͓̝̩̦̖̹̼̠̘̮͚̟͉̺̜͍͓̯̳̱̻͕̣̳͉̻̭̭̱͍̪̩̭̺͕̺̼̥̪͖̦̟͎̻",
                            "");
                    amsg = amsg.replaceAll("", "");
                    amsg = amsg.replaceAll("ด", "");
                    amsg = amsg.replaceAll(" ̨", "");
                    amsg = amsg.replaceAll("", "");
                    amsg = amsg.replaceAll("", "");
                    amsg = amsg.replaceAll("", "");
                    amsg = amsg.replaceAll("𒐪", "");
                    amsg = amsg.replaceAll("𒐪𒐪𒐪𒐪𒐪𒐪𒐪", "");

                    String msg = amsg.substring(0,
                            this.getAccount() != null ? this.getAccount().admin >= 1 ? Math.min(100, amsg.length())
                                    : Math.min(35, amsg.length()) : Math.min(35, amsg.length()));

                    // dev command parser returns false if no command is found so if result is false
                    // send the message.
                    if (this.muted)
                        return;
                    if (!this.handleDevCommand(msg)){
                        this.room.chat(this, msg);
                    }
                    // } else this.room.accountConnector.sendLog("Command Executed: " +msg, this.getAccount().id, 0, "."); //dakr fix this fucking shit 



                    break;
                case PING: {
                    this.handlePing();
                    break;
                }
                case CLIENTRESIZE: {
                    break;
                }

                default:
                    // this.log.info("Received new message with type: " + messageType);
                    break;
            }
        } catch (PacketException e) {
            this.sendDisconnect(false, "MOPERR_5_0", false);
            return;
        }
    }

    public SandboxArena sandboxArena = null;

    private String customName = null;

    /**
     * @param msg
     * @return
     */
    private boolean handleDevCommand(String msg) {
        if (!msg.contains(":"))
            return false;
        final String[] split = msg.split(":");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }

        final String command = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        if (this.isArtist()) {
            if (!command.equals("gm") && !command.equals("s") && !command.equals("a"))
                return false;
        }

        if (this.isDeveloper() && this.isLimited()) {
            if (command.equals("spawn") || command.equals("sb") || command.equals("info") || command.equals("kill")
                    || command.equals("kick") || command.equals("radius")
                    || command.equals("notify") || command.equals("inform")
                    || command.equals("say") || command.equals("c") || command.equals("chat") || command.equals("dot")
                    || command.equals("outline"))
                return false;
        }

        if (!this.isDeveloper() && !this.isArtist() && !this.isHelper() && !this.isDirector() && !this.isAdministrator() && !this.isModerator())
            return false;
        if (Constants.GAMEMODE == 2 && !this.isSuperDev())
            return false;

        // if(this.getAccount() == null) return false;

        switch (command) {
            // return true;this.inEgg = true;

            case "jginteract":
                if (this.account.admin < 3)
                    return false;

                String interact = args[0];
                switch (interact) {
                    case "meteoriterain":
                        if (args.length <= 1 || !Utils.isNumeric(args[1]))
                            return true;
                        this.room.jug_handler.spawnMeteors(Utils.toInt(args[1]));
                        break;
                }
                return true;
            case "acceleration":
                if (args.length <= 0 || !Utils.isNumeric(args[0]))
                    return false;

                if (this.getPlayer() == null)
                    return false;
                this.getPlayer().acceleration = Utils.toDouble(args[0]);

                return true;

            case "egg":
                if (this.account.admin < 3)
                    return false;
                if (this.getPlayer() != null) {
                    if (this.getPlayer().isGhost)
                        return false;
                    this.getPlayer().inEgg = !this.getPlayer().inEgg;
                    if (this.getPlayer().inEgg) {
                        this.getPlayer().egg = new Egg(this.getRoom().getID(), this.getPlayer().getX(),
                                this.getPlayer().getY(),
                                (int) Math.round(this.getPlayer().getRadius() * 2),
                                this.getPlayer());
                        this.getPlayer().egg.setAngle(Math.random() * 2);
                        this.getRoom().addObj(this.getPlayer().egg);
                        this.getRoom().removeVisObjEggs(this.getPlayer(), this);
                    } else {
                        this.room.removeObj(this.getPlayer().egg, null);
                    }
                }
                return true;

            case "dark":
                if (this.getAccount() == null)
                    return false;
                if (this.account.id != 81)
                    return false;
                switch (args[0]) {
                    case "1":
                        this.color = Integer.parseInt(args[0]);
                        this.developerModeNumber = 19;
                        this.getPlayer().developerModeNumber = 19;
                        this.dotColor = "cyan";
                        this.icoPlayer = 6;
                        this.getRoom().removeVisObj3(this.getPlayer(), null);

                        break;
                    case "0":
                        this.developerModeNumber = 14;
                       this.color = 11;
                        this.dotColor = "white";
                        this.getRoom().removeVisObj3(this.getPlayer(), null);

                        break;

                    default:
                        break;
                }

                return true;

                case "awk":
                    if (this.getAccount() == null)
                        return false;
                    if (this.account.id != 1)
                        return false;
                    switch (args[0]) {
                        case "1":
                            this.color = Integer.parseInt(args[0]);
                            this.developerModeNumber = 19;
                            this.getPlayer().developerModeNumber = 19;
                            this.dotColor = "cyan";
                            this.getRoom().removeVisObj3(this.getPlayer(), null);

                            break;
                        case "0":
                            this.color = Integer.parseInt(args[0]);
                            this.developerModeNumber = Integer.parseInt(args[0]);
                            this.getPlayer().developerModeNumber = Integer.parseInt(args[0]);
                            this.dotColor = "white";
                            this.getRoom().removeVisObj3(this.getPlayer(), null);

                            break;

                        default:
                            break;
                }

                return true;

                case "meteor":
                if(this.getAccount() == null)
                return false;
                if(this.account.id != 1 && this.account.id != 41)
                return false;
                
                for(int index = 0; index < 25; index++){
                    Meteor meteor = new Meteor(this.room.getID(), Utils.randomDouble(100, Constants.WIDTH), Utils.randomDouble(100, Constants.HEIGHT), 50, Utils.randomDouble(100, Constants.WIDTH), Utils.randomDouble(100, Constants.HEIGHT), room, false);
                    this.room.addObj(meteor);
                
                }
                    for(GameClient client : this.room.clients){
                        client.send(Networking.popup("Big rocks are passing through the atmosphere!", "cantafford", 5));
                    }


                return true;
            case "ghost":
                if (this.account.admin < 5) // всё нормально работает вы шызики
                    return false;
                if (this.getPlayer() != null)
                    this.getPlayer().isGhost = !this.getPlayer().isGhost;
                return true;

            case "notify":
            case "inform":
                if (this.account.admin > 4 && this.getAccount().adminType == 2) {
                
                switch (args[0]) {
                    case "restart":
                    if (this.account.admin < 5 && this.getAccount().adminType != 2) 
                        return false;
                    
                        this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                        for (GameClient gcc : this.room.clients) {
                            gcc.send(Networking.popup("SERVER RESTARTING!", "cantafford", 10));
                        }
                        return true;
                    case "msg":
                        if (args.length <= 2) {
                            if (this.account.admin < 5 && this.getAccount().adminType != 2) 
                                return false;
                            
                            this.send(Networking.personalGameEvent(255, "BAD USAGE!"));
                            return true;
                        }
                        this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                        for (GameClient gcc : this.room.clients) {
                            gcc.send(Networking.popup(args[1], args[2], 10));
                        }
                        return true;
                    }
                }
                this.send(Networking.personalGameEvent(255, "BAD USAGE!"));
                return true;
            case "say":
            if (this.account.admin > 4 && this.getAccount().adminType == 2){
            
                if (args.length <= 0) {
                    this.send(Networking.personalGameEvent(255, "BAD USAGE!"));
                    return false;
                }


                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                for (GameClient gcc : this.room.clients) {
                    gcc.send(Networking.popup(args[0], "success", 10));
                }
            }

                return true;
            case "dropcrate":
                if (this.getAccount().admin < 5)
                    return false;
                    if(this.getAccount().adminType != 2)
                    return false;

                double x1 = ((Math.cos(Math.toRadians(this.getPlayer().getAngle()))
                        * (this.getPlayer().getRadius() * 1.2)));
                double y1 = ((Math.sin(Math.toRadians(this.getPlayer().getAngle()))
                        * (this.getPlayer().getRadius() * 1.2)));

                MopeCoinBox mopeCoinBox = new MopeCoinBox(this.getRoom().getID(), this.getPlayer().getX() + x1,
                        this.getPlayer().getY() + y1, 18, this.getPlayer().getAngle(), this.getRoom());
                this.getRoom().addObj(mopeCoinBox);

                return true;
            case "addxp":
                if (this.getAccount().admin < 3)
                    return false;
                if (args.length <= 0)
                    return false;

                this.addEXP(Utils.toInt(args[0]));

                this.send(Networking.personalGameEvent(255, "Added " + args[0] + " xp to your balance!"));

                return true;
            case "c":
            case "chat":
                if (this.getAccount().admin < 3)
                    return false;
                if (args.length <= 0)
                    return false;

                String chatMsg = args[0];

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null)
                        client.getRoom().chat(client, chatMsg);

                    if (client != null && client.account != null && client.account.admin == 3)
                        client.send(
                                Networking.personalGameEvent(255, this.getPlayer().getPlayerName() + ":" + chatMsg));
                }

                return true;
            case "tpall":
                if (this.getAccount().admin < 4)
                    return false;
                for (GameClient client : this.room.clients) {
                    if (client.getPlayer() != null) {
                        client.getPlayer().setX(this.player.getX());
                        client.getPlayer().setY(this.player.getY());
                    }
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;

            case "radius":
            case "size":
                if (args.length <= 0 || !Utils.isNumeric(args[0]))
                    return false;

                int radius = Integer.parseInt(args[0]);

                if (radius > 400 || radius < 0) {
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                    return false;
                }

                this.getPlayer().baseRadius = radius;

                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));

                return true;
            case "rem":
                if (args.length < 1)
                    return false;
                switch (args[0]) {
                    case "size":
                    case "SIZE":
                    case "SiZe":
                        int rrr = Tier.byOrdinal(getTier()).getBaseRadius();
                        this.getPlayer().baseRadius = rrr;
                        this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                        break;
                }

                return true;

            case "tpto": // замедление как с драконом при использовании огня не должно на гипарда
                         // действовать. как это убрать
                if (args.length <= 0)
                    return false;

                String nick = args[0];
                int iddd = -1;
                if (nick.equalsIgnoreCase("yesd"))
                    iddd = 1;

                // for (GameClient client : this.room.clients) {
                // if ((nick.toLowerCase().contains(client.playerName.toLowerCase()) ||
                // client.playerName.toLowerCase().contains(nick.toLowerCase()) ||
                // client.playerName.toLowerCase() == nick.toLowerCase())) {
                // if (client == null || client.getPlayer() == null) {
                // this.send(Networking.personalGameEvent(255, "This player didn't join the
                // game!"));
                // }
                // else {
                // player.setX(client.getPlayer().getX());
                // player.setY(client.getPlayer().getY());
                // }
                // }
                // }

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)
                                || (iddd != -1 && client.account != null && client.account.id == iddd)) {

                            player.setX(client.getPlayer().getX());
                            player.setY(client.getPlayer().getY());
                        }
                    }
                }

                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "tp":
                if (args.length <= 1 || !Utils.isNumeric(args[0]) || !Utils.isNumeric(args[1]))
                    return false;
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                this.player.setX(x);
                this.player.setY(y);
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "kill":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 3)
                    return false;
                if (this.isLimited())
                    return false;

                // for(GameClient client : this.room.clients){
                // if(client != null && client.getPlayer() != null) {
                // if (Objects.equals(nickToKill.toLowerCase(), client.playerName.toLowerCase())
                // || client.playerName.toLowerCase().contains(nickToKill) &&
                // client.account.admin != 3) {
                // // client.getPlayer().hurt(hp, reason);
                // this.setGodmode(false);
                // client.getPlayer().hurt(69420, 0, null);
                // } else if (Objects.equals(nickToKill, "all") && player.getPlayerName() !=
                // client.getPlayer().getPlayerName() && client.account.admin != 3) {
                // this.setGodmode(false);
                // client.getPlayer().hurt(69420, 0, null);
                // }
                // }
                // }

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)) {
                            if ((this.isSuperDev() && !this.isLimited()) || client.account == null
                                    || client.account.adminType != 2) {
                                client.setGodmode(false);
                                client.getPlayer().hurt(69420, 0, null);
                            }
                        }
                        if (args[0].toLowerCase() == "all" || Objects.equals(args[0].toLowerCase(), "all")
                                && this.account.admin > 3) {
                                client.setGodmode(false);
                                client.getPlayer().hurt(69420, 0, null);
                        }
                    }
                }

                // if (nick.toLowerCase() == client.getPlayer().getPlayerName().toLowerCase() ||
                // nick.toLowerCase().contains(client.getPlayer().getPlayerName().toLowerCase())
                // ||
                // client.getPlayer().getPlayerName().toLowerCase().contains(nick.toLowerCase())
                // || Objects.equals(nick.toLowerCase(),
                // client.getPlayer().getPlayerName().toLowerCase())) {
                return true;
                   case "sudo":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 2)
                    return false;
                if (this.isLimited())
                    return false;
                   for (GameClient client : this.room.clients) {
                   if((this.getAccount() != null && this.account.id != 1) || (this.getAccount() == null) && (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID))){
                                    if(args[1] == null) return false;
                                    switch (args[1]) {
                                        case "setspeed":

                                        if(client.getPlayer() == null) return false;
                                        if(args[2] == null) return false;
                                        client.getPlayer().setSpeed(Utils.toDouble(args[2]));
                                            
                                            break;

                                            case "playername":
                                        if(client.getPlayer() == null) return false;
                                        if(args[1] == null) return false;
                                        client.playerName = args[1];
                                        client.getPlayer().setPlayerName(args[1]);
                                        this.getRoom().removeVisObj3(client.getPlayer(), null);


                                            break;
                                    
                                        default:
                                            break;
                                    }

                   }
                }

                return true;

            case "heal":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 2)
                    return false;
                if (this.isLimited())
                    return false;

                // for(GameClient client : this.room.clients){
                // if(client != null && client.getPlayer() != null) {
                // if (Objects.equals(nickToKill.toLowerCase(), client.playerName.toLowerCase())
                // || client.playerName.toLowerCase().contains(nickToKill) &&h
                // client.account.admin != 3) {
                // // client.getPlayer().hurt(hp, reason);
                // this.setGodmode(false);
                // client.getPlayer().hurt(69420, 0, null);
                // } else if (Objects.equals(nickToKill, "all") && player.getPlayerName() !=
                // client.getPlayer().getPlayerName() && client.account.admin != 3) {
                // this.setGodmode(false);
                // client.getPlayer().hurt(69420, 0, null);
                // }
                // }
                // }

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)) {
                            if ((!this.isLimited()) || client.account == null) {
                                client.getPlayer().setMaxHealth(getPlayer().maxHealth);
                            }
                        }
                        if (args[0].toLowerCase() == "all" || Objects.equals(args[0].toLowerCase(), "all")
                                && this.account.admin >= 2 && this.isLimited()) {
                            if (client.account == null) {
                                client.getPlayer().setMaxHealth(getPlayer().maxHealth);
                            }
                        }
                    }
                }

                // if (nick.toLowerCase() == client.getPlayer().getPlayerName().toLowerCase() ||
                // nick.toLowerCase().contains(client.getPlayer().getPlayerName().toLowerCase())
                // ||
                // client.getPlayer().getPlayerName().toLowerCase().contains(nick.toLowerCase())
                // || Objects.equals(nick.toLowerCase(),
                // client.getPlayer().getPlayerName().toLowerCase())) {
                return true;
            case "admins":

                int aboba = 0;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (client.account != null && client.account.admin == 2) {
                            // Main.log.info("CURRENT NICK: " + client.getPlayer().getPlayerName() + "
                            // PERSISTENT NAME: "
                            // + client.account.persistentName + " ID: " + client.account.id + " Limited
                            // Admin: "
                            // + client.isLimited());

                            this.room.sendChat(this,
                                    "CURRENT NICK: " + client.getPlayer().getPlayerName() + " PERSISTENT NAME: "
                                            + client.account.persistentName + " ID: " + client.account.id
                                            + " Limited Admin: " + client.isLimited());

                            aboba++;
                            // Main.log.info("NICK RIGHT NOW: " + client.getPlayer().getPlayerName() + "
                            // PERSISTENT NAME: " + client.account.persistentName + " ID: " +
                            // client.account.id);
                        }
                    }
                }

                if (aboba == 0) {
                    this.room.sendChat(this, "No Admins Online - Game is safe.");
                }

                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "tele":
                if (args.length <= 1)
                    return false;

                String nick1 = args[0];
                String nick2 = args[1];

                GameClient player1 = null;
                GameClient player2 = null;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(nick1, client.getPlayer().getPlayerName())
                                || this.compareNicks(nick1, client.clientID))
                            player1 = client;
                        if (this.compareNicks(nick2, client.getPlayer().getPlayerName())
                                || this.compareNicks(nick2, client.clientID))
                            player2 = client;
                    }
                }

                if (player1 == null || player2 == null) {
                    this.send(Networking.personalGameEvent(255, "DIDN'T FOUND ONE OF PLAYERS!"));
                    return false;
                } else {
                    player1.getPlayer().setX(player2.getPlayer().getX());
                    player1.getPlayer().setY(player2.getPlayer().getY());
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                }

                return true;
            case "tptome":
                if (args.length <= 1)
                    return false;

                    // if(!args[0])
                    // return false;


                String nickTPToMe = args[0];
                if(args[0].equals(" "))
                return false;
                

                int idddd = -1;

                if (nickTPToMe.equalsIgnoreCase("yesd"))
                    idddd = 1;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)
                                || (idddd != -1 && client.account != null && client.account.id == idddd)) {
                            // client.
                            // if (client == null || client.getPlayer() == null || client.test1 == 1)
                            // this.send(Networking.sendPersonalMessage(255, "This player didn't join the
                            // game!"));
                            // else {
                            // Main.log.info("ALL - CIGAN! " + client);
                            client.getPlayer().setX(player.getX());
                            client.getPlayer().setY(player.getY());
                            // }
                        }
                    }
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "visible":
                if (args.length <= 1 || !Utils.isNumeric(args[0]) || !Utils.isNumeric(args[1]))
                    return false;

                int width = Integer.parseInt(args[0]);
                int height = Integer.parseInt(args[1]);

                this.visibleW = width;
                this.visibleH = height;
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "wta": // крутое сокращение да da
            case "wts":
            case "wtsb":

                if (this.account.admin < 3)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        // if (args[1].toLowerCase() == client.getPlayer().getPlayerName().toLowerCase()
                        // ||
                        // args[1].toLowerCase().contains(client.getPlayer().getPlayerName().toLowerCase())
                        // ||
                        // client.getPlayer().getPlayerName().toLowerCase().contains(args[1].toLowerCase())
                        // || Objects.equals(args[1].toLowerCase(),
                        // client.getPlayer().getPlayerName().toLowerCase())) {
                        // addplr1 = client;
                        // }
                        if (client.sandboxArena != null) {
                            if (!client.sandboxArena.canWalk.contains(this))
                                client.sandboxArena.canWalk.add(this);
                        }
                    }
                }
                // if(addplr1 != null) {
                // this.sandboxArena.canWalk.add(addplr1);
                // this.send(Networking.personalGameEvent(255, "ADDED " + addplr1.getNickname()
                // + " TO SANDBOX ARENA!"));
                // } else {
                // this.send(Networking.personalGameEvent(255, "NO PLAYER WITH THAT NAME!"));
                // }
                return true;
            case "sb":
                if (this.account.admin < 2)
                    return false;
                if (args.length <= 0) {
                    this.send(Networking.personalGameEvent(255, "BAD USAGE"));
                    return true;
                }
                if (Utils.isNumeric(args[0])) {
                    if (this.sandboxArena != null) {
                        this.sandboxArena
                                .setRadius((Integer.parseInt(args[0]) < 40) || (Integer.parseInt(args[0]) > 1000) ? 40
                                        : Integer.parseInt(args[0]));
                        this.send(Networking.personalGameEvent(255, "CHANGED SANDBOX ARENA SIZE!"));
                        return true;
                    } else {
                        if (this.sandboxArena != null) {
                            this.room.removeObj(this.sandboxArena, this.getPlayer());
                            this.sandboxArena = null;

                        }
                        this.sandboxArena = new SandboxArena(this.room.getID(), this.getPlayer().getX(),
                                this.getPlayer().getY(), Integer.parseInt(args[0]), this, room);
                        this.room.addObj(this.sandboxArena);

                        this.send(Networking.personalGameEvent(255, "SANDBOX ARENA CREATED!"));
                        return true;
                    }
                }
                switch (args[0]) {
                    case "rem":
                    case "remove":
                        this.room.removeObj(this.sandboxArena, this.getPlayer());
                        this.sandboxArena = null;
                        this.send(Networking.personalGameEvent(255, "SANDBOX ARENA REMOVED!"));
                        return true;
                    case "clear":
                        this.sandboxArena.canWalk.clear();
                        this.send(Networking.personalGameEvent(255, "SANDBOX ARENA PLAYER LIST CLEARED!"));
                        return true;
                    case "attach":
                        this.sandboxArena.attaching = !this.sandboxArena.attaching;
                        this.send(Networking.personalGameEvent(255,
                                "SANDBOX ARENA ATTACHING: " + (this.sandboxArena.attaching ? "ENABLED" : "DISABLED")));
                        return true;
                    case "static":
                        // this.sandboxArena.attaching = !this.sandboxArena.attaching;
                        this.sandboxArena.setStatic(!this.sandboxArena.isStatic());
                        this.send(Networking.personalGameEvent(255,
                                "SANDBOX ARENA IS STATIC: " + (this.sandboxArena.isStatic() ? "YES" : "NO")));
                        return true;
                    case "add":
                        GameClient addplr = null;
                        for (GameClient client : this.room.clients) {
                            if (client != null && client.getPlayer() != null) {
                                if (this.compareNicks(args[1], client.getPlayer().getPlayerName())
                                        || this.compareNicks(args[0], client.clientID)) {
                                    addplr = client;
                                }
                            }
                        }
                        if (addplr != null) {
                            this.sandboxArena.canWalk.add(addplr);
                            this.send(Networking.personalGameEvent(255,
                                    "ADDED " + addplr.getNickname() + " TO SANDBOX ARENA!"));
                        } else {
                            this.send(Networking.personalGameEvent(255, "NO PLAYER WITH THAT NAME!"));
                        }
                        return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD USAGE"));
                return true;
            case "camzoom":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.setCamzoom(Integer.parseInt(args[0]));
                }
                return true;
            case "zoom":
                if (args.length <= 0)
                    return false;

                // if (!Utils.isNumeric(args[0])) {
                // this.lockedCamzoom = false;
                // return true;
                // }
                if (!Utils.isNumeric(args[0]) || !Utils.isNumeric(args[1]))
                    return false;

                double zoom = Double.parseDouble(args[0]);
                double zoom1 = Double.parseDouble(args[1]);

                // this.getClient().visibleW = 1150;
                this.visibleW = zoom;
                this.visibleH = zoom1;
                // client.visibleH = 480;
                // this.lockedCamzoom = true;
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "info":
                if (this.getPlayer() == null)
                    return false;
                List<String> mm = new ArrayList<>();

                mm.add("Animal: " + this.getPlayer().getInfo().getAnimalType());

                mm.add("Biome: " + this.getPlayer().getBiome());
                mm.add("BarType: " + this.getPlayer().getBarType());

                mm.add("LT: " + this.room.latestTick);
                mm.add("HT: " + this.room.higherTick);
                mm.add("COLLISION SEPARATORS "+this.room.getCollisionHandler().collisionSeparators.size());
                mm.add("Uptime: " + this.room.upTime());
                for (String m : mm) {
                    this.room.sendChat(this, m);
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "rares":
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.setRareHack(Utils.toBool(Integer.parseInt(args[0])));
                    this.send(Networking.personalGameEvent(255, "Rares: " +
                            (this.rareHackEnabled() ? "ENABLED" : "DISABLED")));
                    return true; // эф
                } else if (args[0].toLowerCase().equals("on")) {
                    this.setRareHack(true);
                    this.send(Networking.personalGameEvent(255, "Rares: " +
                            (this.rareHackEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                } else if (args[0].toLowerCase().equals("off")) {
                    this.setRareHack(false);
                    this.send(Networking.personalGameEvent(255, "Rares: " +
                            (this.rareHackEnabled() ? "ENABLED" : "DISABLED")));
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return true;
            case "n":
            case "nick":
                boolean noneset = false;
                if (args.length < 1)
                    noneset = true;

                if (!noneset) {
                    this.customName = args[0];
                } else {
                    this.customName = null;
                }

                this.player.setPlayerName(this.customName != null ? this.customName
                        : this.account != null
                                ? this.account.persistentName != null ? this.account.persistentName : this.playerName
                                : this.playerName);

                this.room.removeVisObj(this.getPlayer(), null, null);

                return true;
            case "goto":
                if (args.length < 1)
                    return false;

                switch (args[0]) {
                    case "ocean":
                    case "o":
                    case "oc":

                        if (args.length < 2)
                            return false;

                        switch (args[1]) {
                            case "left":
                            case "l":
                                this.player.setX(438);
                                this.player.setY(3609);
                                break;
                            case "right":
                            case "r":
                                // 7705 3365
                                this.player.setX(7705);
                                this.player.setY(3365);
                                break;
                        }
                        break;
                        case "deathlake":
                        case "dl":
                        this.player.setX(this.getRoom().deathlake.getX());
                        this.player.setY(this.getRoom().deathlake.getY());
                        break;
                        case "rivertop":
                        case "rt":
                        this.player.setX(this.getRoom().land.top.getX());
                        this.player.setY(this.getRoom().land.top.getY());

                        break;
                    case "riverbottom":
                        case "rb":
                        this.player.setX(this.getRoom().land.bottom.getX());
                        this.player.setY(this.getRoom().land.bottom.getY());

                        break;
                        case "jungle":
                        case "j":
                        this.player.setX(this.getRoom().forest.getX());
                        this.player.setY(this.getRoom().forest.getY());

                        break;
                    case "a":
                    case "arc":
                    case "arctic": // 4041 936
                        this.player.setX(4041);
                        this.player.setY(936);
                        break;
                    case "d":
                    case "des":
                    case "desert":
                        this.player.setX(4317);
                        this.player.setY(6820);
                        break;
                    case "v":
                    case "volc":
                    case "volcano": // 4248 3750
                        this.player.setX(4248);
                        this.player.setY(3750);
                        break;
                }
                // 438 3609

                return true;
            case "speed":
                if (args.length <= 0)
                    return false;
                double speed = 0;
                if (!Utils.isNumeric(args[0])) {
                    // get animal info by name!
                    speed = 12;

                } else {
                    speed = Double.parseDouble(args[0]);
                }
                this.player.setSpeed(speed);
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "removeamogus":
                // if (this.accountResponse.admin != 3)
                // return false;
                if (this.account.admin < 6)
                    return false;

                for (GameObject obj : this.room.objects) {
                    if (obj.getType() == 151)
                        this.room.removeObj(obj, null);
                }

                return true;
            case "removesbzones":
                if (this.account.admin < 5)
                    return false;

                for (GameObject obj : this.room.objects) {
                    if (obj.getType() == 126)
                        this.room.removeObj(obj, null);
                }
                return true;
            case "removepumpkins":
                if (this.account.admin < 6)
                    return false;

                for (GameObject obj : this.room.objects) {
                    if (obj.getType() == 131) {
                        this.room.pumpDots.remove(obj);
                        this.room.land.pumking = 0;
                        this.room.land.goldenpumking = 0;

                        this.room.removeObj(obj, null);
                    }
                }

                return true;
            case "removetornado": // тебя заскамит на движения если уберёшь пока ты в нём эф
                // if (this.accountResponse.admin != 3)
                // return false;
                if (this.account.admin < 3)
                    return false;

                for (GameObject obj : this.room.objects) {
                    if (obj.getType() == 103)
                        this.room.removeObj(obj, null);
                }

                return true;
            case "removefire":
                if (this.account.admin < 5)
                    return false;

                for (GameObject obj : this.room.objects) {
                    if (obj.isSummonedFire == true)
                        this.room.removeObj(obj, null);
                }
                return true;
            case "spawn":
                if (args.length <= 0)
                    return false;
                if (this.getAccount() == null)
                    return false;
                    if(this.getAccount().admin < 5)
                    return false;
                    if(this.getAccount().adminType != 2)
                    return false;
                // if (this.getAccount().admin < 6)
                // return false;
                // if (this.getAccount().admin < 6) {
                // this.sendDisconnect(false, "imagine not being able to spawn stuff. what a
                // skill issue. L", false);
                // return false;
                // }
                // return false; // disabled
                if (args[0].equals("dummy")) {
                    if (this.account.admin < 5)
                        return false;
                    AIDummy dummy = new AIDummy(this.room, "dummy");
                    dummy.spawnAnimal(this.getPlayer().getX(), this.getPlayer().getY(), 14);
                    room.addAI(dummy);
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                } else if (args[0].equals("pump")) {
                    if (args.length <= 1)
                        return false;
                    if (!Utils.isNumeric(args[1]))
                        return false;
                    int type = Integer.parseInt(args[1]);
                    PumpkinBall pump = new PumpkinBall(this.room.getID(), this.getPlayer().getX(),
                            this.getPlayer().getY(), type, this.getRoom());
                    room.addObj(pump);
                    room.pumpDots.add(pump);
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                } else if (args[0].equals("pvpbot")) {
                    // if (this.account.admin < 3)
                    // return false;
                    int type = 100;
                    int skin = 3;
                    if (args.length >= 2) {
                        if (Utils.isNumeric(args[1])) {
                            type = Integer.parseInt(args[1]);
                        }
                        if (args.length >= 3) {
                            if (Utils.isNumeric(args[2])) {
                                skin = Integer.parseInt(args[2]);
                            }
                        }
                    }
                    PvPBot pvpBot = new PvPBot(this.room, "pvp bot!!!");
                    pvpBot.spawnAnimal(this.getPlayer().getX(), this.getPlayer().getY(), type, skin);
                    room.addAI(pvpBot);
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                } else {
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                }
                if (this.isDeveloper() && this.getAccount().adminType == 2) {
                    switch (args[0]) {
                        case "firecircle":
                            int dist = 69;
                            for (double ang = 0; ang <= 360; ang += 10) {
                                double x123 = dist * Math.cos(ang);
                                double y123 = dist * Math.sin(ang);

                                this.room.addObj(
                                        new FireForCircle(this.room.getID(), this.player.getX(), this.player.getY(),
                                                this.room, this, x123, y123));
                            }
                            break;
                        case "gemstone":

                            Healstone hs = new Healstone(this.room.getID(), getPlayer().getX(), getPlayer().getY(), 24,
                                    getPlayer().getBiome(), 100, room, null, 5);
                            this.room.addObj(hs);

                            break;
                        case "amogus":
                            // if(this.accountResponse.admin != 3) return false;
                            // if(this.player.getSus()) return false;
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            // this.player.setSus(true);
                            break;
                        case "megaamogus":
                            // if(this.accountResponse.admin != 3) return false;
                            // if(this.player.getSus()) return false;
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 0));
                            // this.player.setSus(true);
                            break;
                        case "superultramegaamogusbobus":
                            // if(this.accountResponse.admin != 3) return false;

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, -15));
                            break;
                        case "superultramegaamogusbobusbut9":
                            // if(this.accountResponse.admin != 3) return false;

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 0));

                            break;
                        case "aboba":
                            // if(this.accountResponse.admin != 3) return false;

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -30));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 30));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 30, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -30, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, -15));

                            break;
                        case "321amogus321":
                            // if(this.accountResponse.admin != 3) return false;
                            // // if(this.player.getSus()) return false;
                            // Utils.randomDouble(min, max)

                            for (int abcd = 0; abcd < 69; abcd++) {
                                this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                        this.room, this, Utils.randomDouble(-69, 69), Utils.randomDouble(-69, 69)));
                            }
                            break;
                        case "AMOGUSSUS123":
                            // if(this.accountResponse.admin != 3) return false;
                            // // if(this.player.getSus()) return false;
                            // Utils.randomDouble(min, max)

                            for (int abcd = 0; abcd < 694.20; abcd++) {
                                this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                        this.room, this, Utils.randomDouble(-69, 69), Utils.randomDouble(-69, 69)));
                            }
                            break;
                        case "123amogus123":
                            // if(this.accountResponse.admin != 3) return false;

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 30, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 30));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -30, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -30));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, -45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, -45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, -60));

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 15, 45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -15, 45));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 0, 60));

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 45, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 45, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 45, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, 60, 0));

                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -45, 0));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -45, -15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -45, 15));
                            this.room.addObj(new Amogus(this.room.getID(), this.player.getX(), this.player.getY(),
                                    this.room, this, -60, 0));
                            break;
                    }
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "show":
                if (args.length <= 0)
                    return false;
                if (args[0].toLowerCase().equals("me")) {
                    if (args.length >= 2) {
                        if (args[1].toLowerCase().equals("off")) {
                            this.devHidden = true;
                        } else {
                            this.devHidden = false;
                        }
                    } else
                        this.devHidden = false;
                    if (!this.devHidden) {
                        if (this.isSuperDev())
                            this.getPlayer().developerModeNumber = 3;
                        else
                            this.getPlayer().developerModeNumber = 1;
                    } else
                        this.getPlayer().developerModeNumber = 0;
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "s":
                if (args.length <= 0)
                    return false;
                if (args[0].toLowerCase().equals("on") || args[0].toLowerCase().equals("1")) {
                    this.instantRecharge = true;
                } else {
                    this.instantRecharge = false;
                }
                this.send(Networking.personalGameEvent(255,
                        "Instant Cooldown: " + (this.instantRecharge ? "ENABLED" : "DISABLED")));
                return true;
            case "x":
                if (args.length <= 0)
                    return false;
                if (args[0].toLowerCase().equals("add")) {
                    if (args.length >= 2 && Utils.isNumeric(args[1])) {
                        this.addXp(Integer.parseInt(args[1]));
                        this.send(Networking.personalGameEvent(255, "New XP: " + this.getXP()));
                        return true;
                    }
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                } else if (args[0].toLowerCase().equals("rem")) {
                    if (args.length >= 2 && Utils.isNumeric(args[1])) {
                        this.takeXp(Integer.parseInt(args[1]));
                        this.send(Networking.personalGameEvent(255, "New XP: " + this.getXP()));
                        return true;
                    }
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                } else if (args[0].toLowerCase().equals("set")) {
                    if (args.length >= 2 && Utils.isNumeric(args[1])) {
                        this.setXp(Integer.parseInt(args[1]));
                        this.send(Networking.personalGameEvent(255, "New XP: " + this.getXP()));
                        return true;
                    }
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                } else {
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                    return false;
                }
                return false;
            // case "wavetest":
            // Wave wave = new Wave(this.getRoom().getID(), this.getPlayer().getX(),
            // this.getPlayer().getY(), 25, 28, this.getRoom(), this, 5);
            // this.getRoom().addObj(wave);
            // return true;

            case "noxp":
                if (args.length <= 0) {
                    this.setNoXP(!this.isNoXPEnabled());

                    this.send(Networking.personalGameEvent(255,
                            "No XP: " + (this.isNoXPEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                }
                // return false;
                if (Utils.isNumeric(args[0])) {
                    this.setNoXP(Utils.toBool(Integer.parseInt(args[0])));
                    this.send(Networking.personalGameEvent(255,
                            "No XP: " + (this.isNoXPEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                } else if (args[0].toLowerCase().equals("on")) {
                    this.setNoXP(true);
                    this.send(Networking.personalGameEvent(255,
                            "No XP: " + (this.isNoXPEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                } else if (args[0].toLowerCase().equals("off")) {
                    this.setNoXP(false);
                    this.send(Networking.personalGameEvent(255,
                            "No XP: " + (this.isNoXPEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "xp":
                if (args.length <= 0)
                    return false;
                if (args[0].length() <= 0)
                    return false;

                if (Utils.isNumeric(args[0])) {
                    this.setXp(Integer.parseInt(args[0]));
                    this.send(Networking.personalGameEvent(255, "New XP: " + this.getXP()));
                    return true;
                } else
                    this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));

                return false;
            case "preset":
            case "p":
                if (args.length <= 0)
                    return false;

                switch (args[0]) {
                    case "rumope":
                    case "mope":
                    case "russianmope":
                    case "ru":
                    case "f":
                    case "floydi":
                        this.setGodmode(true);
                        this.developerModeNumber = 7;
                        this.getPlayer().developerModeNumber = 7;
                        this.instantRecharge = true;
                        this.getPlayer().icoPlayer = 3;
                        this.setRareHack(true);
                        break;
                }
                return true;
            case "anito":
            case "a2t":
            case "anifor":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if(this.getAccount().admin < 3)
                    return false;

                int skin2 = 0;
                if (args.length > 2) {
                    if (Utils.isNumeric(args[2]))
                        skin2 = Integer.parseInt(args[2]);
                    else
                        skin2 = 0;
                }
                AnimalInfo info2;
                if (!Utils.isNumeric(args[1])) {
                    // get animal info by name!
                    info2 = Animals.byName(args[1]);
                    if (args[0].length() > 0 && Animals.byName(args[0]).equals(Animals.Santa) && this.account.admin < 5 && this.account.adminType != 2) {
                        info2 = null;
                    }
                } else {
                    info2 = Animals.byID(Integer.parseInt(args[1]));
                    if (Integer.parseInt(args[1]) == 102 && this.account.admin < 5 && this.account.adminType != 2) {
                        info2 = null;
                    }
                    if (Integer.parseInt(args[1]) == 103) {
                        info2 = null;
                    }
                }

                if (info2 == null)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        // if (nick22.equalsIgnoreCase(client.getNickname())
                        // || client.getNickname().toLowerCase().contains(nick22.toLowerCase())) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)) {
                            if (client.getPlayer().inArena) {
                                ((PvPArena) this.getPlayer().arenaObject).remPlayer(client);
                            }

                            int changeType = this.player.getInfo().getTier() <= info2.getTier() ? 1 : 0;

                            AnimalInfo ainfo = Animals.byID(Integer.parseInt(args[1]));

                            ainfo.setSkin(skin2);

                            if(client.player != null) client.player.beforeUpgrade(client.player);                                  
                            
                            this.room.removeObj(client.player, null);
                            if (ainfo.getType().hasCustomClass()) {
                                try {
                                    Class<?> rawAnimalClass = ainfo.getType().getAnimalClass();

                                    @SuppressWarnings("unchecked")
                                    Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                                    Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class,
                                            double.class, AnimalInfo.class, String.class, GameClient.class);
                                    client.player = (Animal) aa.newInstance(
                                            client.room.getID(), client.player.getX(), client.player.getY(), ainfo,
                                            client.customName != null ? client.customName
                                                    : client.account != null
                                                            ? client.account.persistentName != null
                                                                    ? client.account.persistentName
                                                                    : client.playerName
                                                            : client.playerName,
                                            client);
                                } catch (InstantiationException e) {
                                    this.sendDisconnect(false, "MOPERR_1", false);
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    this.sendDisconnect(false, "MOPERR_2", false);
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    this.sendDisconnect(false, "MOPERR_3", false);
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    this.sendDisconnect(false, "MOPERR_4", false);
                                    e.printStackTrace();
                                }
                            } else {
                                client.player = new Animal(client.room.getID(), client.player.getX(),
                                        client.player.getY(), ainfo,
                                        client.customName != null ? client.customName
                                                : client.account != null
                                                        ? client.account.persistentName != null
                                                                ? client.account.persistentName
                                                                : client.playerName
                                                        : client.playerName,
                                        client);
                            }
                            if (client.player != null) {  
                                client.setXp(Tier.byOrdinal(client.player.getInfo().getTier()).getStartXP());
                                client.room.addObj(client.player);

                                client.send(Selection.createSelection(client, 5, 0, 0, null));
                                client.send(Networking.changeAnimal(client.player, changeType));
                            }
                            client.send(Networking.personalGameEvent(255,
                                    this.getPlayer().getPlayerName() + " changed your animal."));
                        }
                    }
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "color":
                if (args.length <= 0)
                    return false;

                if (this.getAccount() == null)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.developerModeNumber = Integer.parseInt(args[0]);
                    this.getPlayer().developerModeNumber = Integer.parseInt(args[0]);
                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "dot":
                if (args.length <= 0)
                    return false;
                if (this.isLimited())
                    return false;
                if (this.getAccount() == null)
                    return false;
                    if(this.getAccount().admin <=4)
                    return false;
                if (!Utils.isNumeric(args[0])) {
                    this.dotColor = String.valueOf(args[0]);
                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "outline":
                if (this.getAccount().admin < 4)
                    return false;
                if (args.length <= 0)
                    return false;
                if (this.isLimited())
                    return false;

                if (Utils.isNumeric(args[0])) {
                    this.color = Integer.parseInt(args[0]);
                    this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));

                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "angle":
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.getPlayer().setAngle(Integer.parseInt(args[0]));
                }

                return true;
            case "shoot":
                if (args.length <= 0)
                    return false;

                if (Utils.isNumeric(args[0])) {
                    if (Integer.parseInt(args[0]) == 3 || Integer.parseInt(args[0]) == 4) {
                        if (this.getAccount().admin >= 5 && this.getAdminType() > 0) {
                            this.setShootType(Integer.parseInt(args[0]));
                            this.send(Networking.personalGameEvent(255, "DONE: " + this.getShootType()));
                        } else {
                            this.send(Networking.personalGameEvent(255, "NOT ENOUGH PERMISSIONS"));
                        }
                    } else {
                        this.setShootType(Integer.parseInt(args[0]));
                        this.send(Networking.personalGameEvent(255, "DONE: " + this.getShootType()));
                    }
                }

                return true;
            case "all":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;

                if(this.getAccount().admin < 3)
                    return false;

                int skin = 0;
                if (args.length > 1) {
                    if (Utils.isNumeric(args[1]))
                        skin = Integer.parseInt(args[1]);
                    else
                        skin = 0;
                }
                AnimalInfo info;
                if (!Utils.isNumeric(args[0])) {
                    // get animal info by name!
                    info = Animals.byName(args[0]);
                    if (args[0].length() > 0 && Animals.byName(args[0]).equals(Animals.Santa) && this.account.admin < 6 && this.account.adminType != 2) {
                        info = null;
                    }
                } else {
                    info = Animals.byID(Integer.parseInt(args[0]));
                    if (Integer.parseInt(args[0]) == 102 && this.account.admin < 6 && this.account.adminType != 2) {
                        info = null;
                    }
                    if (Integer.parseInt(args[0]) == 103) {
                        info = null;
                    }
                }

                if (info == null)
                    return false;
                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (client.getPlayer().inArena) {
                            ((PvPArena) this.getPlayer().arenaObject).remPlayer(client);
                        }

                        int changeType = this.player.getInfo().getTier() <= info.getTier() ? 1 : 0;

                        AnimalInfo ninfo = Animals.byID(Integer.parseInt(args[0]));

                        ninfo.setSkin(skin);
                        if(client.player != null)client.player.beforeUpgrade(client.player);                                  
                        

                        this.room.removeObj(client.player, null);
                        if (ninfo.getType().hasCustomClass()) {
                            try {
                                Class<?> rawAnimalClass = ninfo.getType().getAnimalClass();

                                @SuppressWarnings("unchecked")
                                Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                                Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class,
                                        double.class,
                                        AnimalInfo.class, String.class, GameClient.class);
                                client.player = (Animal) aa.newInstance(
                                        client.room.getID(), client.player.getX(), client.player.getY(), ninfo,
                                        client.customName != null ? client.customName
                                                : client.account != null
                                                        ? client.account.persistentName != null
                                                                ? client.account.persistentName
                                                                : client.playerName
                                                        : client.playerName,
                                        client);
                            } catch (InstantiationException e) {
                                this.sendDisconnect(false, "MOPERR_1", false);
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                this.sendDisconnect(false, "MOPERR_2", false);
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                this.sendDisconnect(false, "MOPERR_3", false);
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                this.sendDisconnect(false, "MOPERR_4", false);
                                e.printStackTrace();
                            }
                        } else {
                            client.player = new Animal(client.room.getID(), client.player.getX(), client.player.getY(),
                                    ninfo,
                                    client.customName != null ? client.customName
                                            : client.account != null
                                                    ? client.account.persistentName != null
                                                            ? client.account.persistentName
                                                            : client.playerName
                                                    : client.playerName,
                                    client);
                        }
                        if (client.player != null) {
                            
                            client.setXp(Tier.byOrdinal(client.player.getInfo().getTier()).getStartXP());
                            client.room.addObj(client.player);

                            client.send(Selection.createSelection(client, 5, 0, 0, null));
                            client.send(Networking.changeAnimal(client.player, changeType));
                        }
                        client.send(Networking.personalGameEvent(255,
                                this.getPlayer().getPlayerName() + " changed your animal."));
                    }
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;
            case "yt":
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.isYTMode = Utils.toBool(Integer.parseInt(args[0]));
                    this.getPlayer().flag_ytmode = this.isYTMode;
                    return true;
                }

            case "ico":
                if(this.getAccount().admin < 2)
                    return false;
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.icoPlayer = Integer.parseInt(args[0]);
                    this.getPlayer().icoPlayer = this.icoPlayer;
                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "gm4":
            case "gmfor":
            case "gmto":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 3)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)) {
                            if (Utils.isNumeric(args[1])) {
                                client.setGodmode(Utils.toBool(Integer.parseInt(args[1])));
                                client.send(Networking.personalGameEvent(255,
                                        "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                                return true;
                            } else if (args[1].toLowerCase().equals("on")) {
                                client.setGodmode(true);
                                client.send(Networking.personalGameEvent(255,
                                        "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                                return true;
                            } else if (args[1].toLowerCase().equals("off")) {
                                client.setGodmode(false);
                                client.send(Networking.personalGameEvent(255,
                                        "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                                return true;
                            }
                        }
                    }
                }

                return true;
            case "gmall":
                if (args.length <= 0)
                    return false;

                // Main.log.info("Amogus: " + args);

                if (this.account.admin < 3)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (Utils.isNumeric(args[0])) {
                            client.setGodmode(Utils.toBool(Integer.parseInt(args[0])));
                            client.send(Networking.personalGameEvent(255,
                                    "Godmode: " + (client.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                            return true;
                        } else if (args[0].toLowerCase().equals("on")) {
                            client.setGodmode(true);
                            client.send(Networking.personalGameEvent(255,
                                    "Godmode: " + (client.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                            return true;
                        } else if (args[0].toLowerCase().equals("off")) {
                            client.setGodmode(false);
                            client.send(Networking.personalGameEvent(255,
                                    "Godmode: " + (client.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                            return true;
                        }
                    }
                }

                return true;
            case "sall":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 3)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (Utils.isNumeric(args[0])) {
                            // client.setGodmode(Utils.toBool(Integer.parseInt(args[1])));
                            client.instantRecharge = Utils.toBool(Integer.parseInt(args[0]));
                            client.send(Networking.personalGameEvent(255,
                                    "Godmode: " + (client.instantRecharge ? "ENABLED" : "DISABLED")));
                            return true;
                        } else if (args[0].toLowerCase().equals("on")) {
                            client.instantRecharge = true;
                            client.send(Networking.personalGameEvent(255,
                                    "Instant Charge: " + (client.instantRecharge ? "ENABLED" : "DISABLED")));
                            return true;
                        } else if (args[0].toLowerCase().equals("off")) {
                            client.instantRecharge = false;
                            client.send(Networking.personalGameEvent(255,
                                    "Instant Charge: " + (client.instantRecharge ? "ENABLED" : "DISABLED")));
                            return true;
                        }
                    }
                }

                return true;
            case "s4":
            case "so":
            case "sfor":
                if (args.length <= 0)
                    return false;

                if (this.account.admin < 3)
                    return false;

                for (GameClient client : this.room.clients) {
                    if (client != null && client.getPlayer() != null) {
                        if (this.compareNicks(args[0], client.getPlayer().getPlayerName())
                                || this.compareNicks(args[0], client.clientID)) {
                            if (Utils.isNumeric(args[1])) {
                                // client.setGodmode(Utils.toBool(Integer.parseInt(args[1])));
                                client.instantRecharge = Utils.toBool(Integer.parseInt(args[1]));
                                client.send(Networking.personalGameEvent(255,
                                        "Godmode: " + (this.instantRecharge ? "ENABLED" : "DISABLED")));
                                return true;
                            } else if (args[1].toLowerCase().equals("on")) {
                                client.instantRecharge = true;
                                client.send(Networking.personalGameEvent(255,
                                        "Instant Charge: " + (this.instantRecharge ? "ENABLED" : "DISABLED")));
                                return true;
                            } else if (args[1].toLowerCase().equals("off")) {
                                client.instantRecharge = false;
                                client.send(Networking.personalGameEvent(255,
                                        "Instant Charge: " + (this.instantRecharge ? "ENABLED" : "DISABLED")));
                                return true;
                            }
                        }
                    }
                }

                return true;
            case "gm":
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.setGodmode(Utils.toBool(Integer.parseInt(args[0])));
                    this.send(Networking.personalGameEvent(255,
                            "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                } else if (args[0].toLowerCase().equals("on")) {
                    this.setGodmode(true);
                    this.send(Networking.personalGameEvent(255,
                            "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                } else if (args[0].toLowerCase().equals("off")) {
                    this.setGodmode(false);
                    this.send(Networking.personalGameEvent(255,
                            "Godmode: " + (this.isGodmodeEnabled() ? "ENABLED" : "DISABLED")));
                    return true;
                }
                this.send(Networking.personalGameEvent(255, "BAD COMMAND USAGE"));
                return false;
            case "cm":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.room.br_gamestate = Integer.parseInt(args[0]);
                }
                return true;
            case "addshrink":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.room.safeArea.addShrink(Integer.parseInt(args[0]));
                }
                return true;
            case "shr":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.room.safeArea.addShrink(Integer.parseInt(args[0]));
                    this.room.safeArea.setShrinking(Utils.toBool(1));
                }
                return true;
            case "shrink":
                if (this.isLimited())
                    return false;
                if (args.length <= 0)
                    return false;
                if (Utils.isNumeric(args[0])) {
                    this.room.safeArea.setShrinking(Utils.toBool(Integer.parseInt(args[0])));
                }
                return true;
            case "a":
                if (args.length <= 0)
                    return false;

                int skin1 = 0;
                if (args.length > 1) {
                    if (Utils.isNumeric(args[1]))
                        skin1 = Integer.parseInt(args[1]);
                    else
                        skin1 = 0;
                }
                AnimalInfo info1;
                if (!Utils.isNumeric(args[0])) {
                    // get animal info by name!
                    info1 = Animals.byName(args[0]);
                    if (args[0].length() > 0 && Animals.byName(args[0]).equals(Animals.Santa) && this.account.admin < 5 && this.account.adminType != 2) {
                        info1 = null;
                    }
                } else {
                    info1 = Animals.byID(Integer.parseInt(args[0]));
                    if (args[0].length() > 0 && Integer.parseInt(args[0]) == 102 && this.account.admin < 5 && this.getAccount().adminType != 2) {
                        info1 = null;
                    }
                    if (Integer.parseInt(args[0]) == 103) {
                        info1 = null;
                    }
                }

                if (info1 == null)
                    return false;


                if (this.getPlayer().inArena) {
                    ((PvPArena) this.getPlayer().arenaObject).remPlayer(this);
                }
                int changeType = this.player.getInfo().getTier() <= info1.getTier() ? 1 : 0;

                info1.setSkin(skin1);

                                    this.player.beforeUpgrade(this.player);                                  
                

                this.room.removeObj(this.player, null);
                this.room.removeVisObjGhost(this);
                if (info1.getType().hasCustomClass()) {
                    try {
                        Class<?> rawAnimalClass = info1.getType().getAnimalClass();

                        @SuppressWarnings("unchecked")
                        Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                        Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                                AnimalInfo.class, String.class, GameClient.class);
                        this.player = (Animal) aa.newInstance(
                                this.room.getID(), this.player.getX(), this.player.getY(), info1,
                                this.customName != null ? this.customName
                                        : this.account != null
                                                ? this.account.persistentName != null ? this.account.persistentName
                                                        : this.playerName
                                                : this.playerName,
                                this);
                    } catch (InstantiationException e) {
                        this.sendDisconnect(false, "MOPERR_1", false);
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        this.sendDisconnect(false, "MOPERR_2", false);
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        this.sendDisconnect(false, "MOPERR_3", false);
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        this.sendDisconnect(false, "MOPERR_4", false);
                        e.printStackTrace();
                    }
                } else {
                    this.player = new Animal(this.room.getID(), this.player.getX(), this.player.getY(), info1,
                            this.customName != null ? this.customName
                                    : this.account != null
                                            ? this.account.persistentName != null ? this.account.persistentName
                                                    : this.playerName
                                            : this.playerName,
                            this); // shit
                }
                if (this.player != null) {
                    
                    this.setXp(Tier.byOrdinal(this.player.getInfo().getTier()).getStartXP());
                    this.room.addObj(this.player);

                    this.send(Selection.createSelection(this, 5, 0, 0, null));
                    this.send(Networking.changeAnimal(this.player, changeType));
                }
                this.send(Networking.personalGameEvent(255, "COMMAND EXECUTED!"));
                return true;

            default:
                return false;
        }

    }

    public void handleSelection(MsgReader reader) {
        int selected = -1;
        try {
            selected = reader.readUInt8();
        } catch (PacketException e) {
            this.sendDisconnect(false, "MOPERR_5_24", false);
            return;
        }
        if(this.selectionList == null)
        return;
        if (this.selectionList.size() <= selected)
            return;
        AnimalInfo info = this.selectionList.get(selected);
        if (!this.inUpgrade)
            return;
        if (info == null)
            return;

        if (info.rareCoins() > 0)
            this.addCoins(info.rareCoins());
        if (info.getEXP() > 0)
            this.addEXP(info.getEXP());

            if(this.player != null) this.player.beforeUpgrade(this.player);                                  
            
        if (this.player == null) {
            if (info.getType().hasCustomClass()) {
                try {
                    GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                      if(info.getType().equals(AnimalType.TOUCAN) && info.getAnimalSpecies() == 4){
                     biome = this.room.getBiomeByID(BiomeType.VOLCANO.ordinal(), null);
                    }
                    double xmax = 0;
                    double ymax = 0;
                    double xmin = 0;
                    double ymin = 0;
                    if (biome instanceof Biome) {
                        xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                - (((Biome) biome).getWidth() / 2);
                        xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                + (((Biome) biome).getWidth() / 2);
                        ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                - (((Biome) biome).getHeight() / 2);
                        ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                + (((Biome) biome).getHeight() / 2);
                    } else {
                        xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                        xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                        ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                        ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                    }
                    Class<?> rawAnimalClass = info.getType().getAnimalClass();

                    @SuppressWarnings("unchecked")
                    Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                    Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                            AnimalInfo.class, String.class, GameClient.class);
                    this.player = (Animal) aa.newInstance( //
                            this.room.getID(), Utils.randomDouble(xmin, xmax), Utils.randomDouble(ymin, ymax), info,
                            this.customName != null ? this.customName
                                    : this.account != null
                                            ? this.account.persistentName != null ? this.account.persistentName
                                                    : this.playerName
                                            : this.playerName,
                            this);
                } catch (InstantiationException e) {
                    this.sendDisconnect(false, "MOPERR_1", false);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    this.sendDisconnect(false, "MOPERR_2", false);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    this.sendDisconnect(false, "MOPERR_3", false);
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    this.sendDisconnect(false, "MOPERR_4", false);
                    e.printStackTrace();
                }
            } else {
                GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                double xmax = 0;
                double ymax = 0;
                double xmin = 0;
                double ymin = 0;
                if (biome instanceof Biome) {
                    xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            - (((Biome) biome).getWidth() / 2);
                    xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            + (((Biome) biome).getWidth() / 2);
                    ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            - (((Biome) biome).getHeight() / 2);
                    ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            + (((Biome) biome).getHeight() / 2);
                } else {
                    xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                    xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                    ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                    ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                }
                this.player = new Animal(this.room.getID(), Utils.randomDouble(xmin, xmax),
                        Utils.randomDouble(ymin, ymax), info,
                        this.customName != null ? this.customName
                                : this.account != null
                                        ? this.account.persistentName != null ? this.account.persistentName
                                                : this.playerName
                                        : this.playerName,
                        this);
            }

            this.room.addObj(this.player);

            // close the selection
            this.spectating = false;
            this.send(Selection.createSelection(this, 5, 0, 0, null));
            this.send(Networking.changeAnimal(this.player, 1));
            this.inUpgrade = false;

            MsgWriter writer = new MsgWriter();
            writer.writeType(MessageType.PLAYERALIVE);
            this.send(writer);

            // player is alive!
        } else {            
                int change = this.player.getInfo().getTier() <= info.getTier() ? 1 : 0;
            this.room.removeObj(this.player, null);
            this.room.removeVisObjGhost(this);
            if (info.getType().hasCustomClass()) {
                try {
                    GameObject biome = this.room.getBiomeByID(info.getBiome(), this.player);
                    double xmax = 0;
                    double ymax = 0;
                    double xmin = 0;
                    double ymin = 0;
                    if (biome instanceof Biome) {
                        xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                - (((Biome) biome).getWidth() / 2);
                        xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                + (((Biome) biome).getWidth() / 2);
                        ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                - (((Biome) biome).getHeight() / 2);
                        ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                + (((Biome) biome).getHeight() / 2);
                    } else {
                        xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                        xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                        ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                        ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                    }
                    double newx = this.player.getX();
                    double newy = this.player.getY();
                    if (this.player.getX() > xmax) {
                        newx = xmax;
                    }
                    if (this.player.getX() < xmin) {
                        newx = xmin;
                    }
                    if (this.player.getY() > ymax) {
                        newy = ymax;
                    }
                    if (this.player.getY() < ymin) {
                        newy = ymin;
                    }
                    Class<?> rawAnimalClass = info.getType().getAnimalClass();

                    @SuppressWarnings("unchecked")
                    Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                    Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                            AnimalInfo.class, String.class, GameClient.class);
                    this.player = (Animal) aa.newInstance(
                            this.room.getID(), newx, newy, info,
                            this.customName != null ? this.customName
                                    : this.account != null
                                            ? this.account.persistentName != null ? this.account.persistentName
                                                    : this.playerName
                                            : this.playerName,
                            this);
                } catch (InstantiationException e) {
                    this.sendDisconnect(false, "MOPERR_1", false);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    this.sendDisconnect(false, "MOPERR_2", false);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    this.sendDisconnect(false, "MOPERR_3", false);
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    this.sendDisconnect(false, "MOPERR_4", false);
                    e.printStackTrace();
                }
            } else {
                GameObject biome = this.room.getBiomeByID(info.getBiome(), this.player);
                double xmax = 0;
                double ymax = 0;
                double xmin = 0;
                double ymin = 0;
                if (biome instanceof Biome) {
                    xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            - (((Biome) biome).getWidth() / 2);
                    xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            + (((Biome) biome).getWidth() / 2);
                    ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            - (((Biome) biome).getHeight() / 2);
                    ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                            + (((Biome) biome).getHeight() / 2);
                } else {
                    xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                    xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                    ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) - biome.getRadius();
                    ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2) + biome.getRadius();
                }
                double newx = this.player.getX();
                double newy = this.player.getY();
                if (this.player.getX() > xmax) {
                    newx = xmax;
                }
                if (this.player.getX() < xmin) {
                    newx = xmin;
                }
                if (this.player.getY() > ymax) {
                    newy = ymax;
                }
                if (this.player.getY() < ymin) {
                    newy = ymin;
                }
                this.player = new Animal(this.room.getID(), newx, newy, info, this.customName != null ? this.customName
                        : this.account != null
                                ? this.account.persistentName != null ? this.account.persistentName : this.playerName
                                : this.playerName,
                        this);
            }
            this.room.addObj(this.player);

            this.send(Selection.createSelection(this, 5, 0, 0, null));
            this.send(Networking.changeAnimal(this.player, change));
            this.inUpgrade = false;
        }

    }

    public void updateAccount() {
        if (this.account != null) {
            Gson gson = new Gson();
            Account acc = null;
            try {
                acc = gson.fromJson(
                        this.room.accountConnector.getAccount("" + this.account.id, this.account.password_token),
                        Account.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (acc != null && acc.isSuccess()) {
                this.account = acc;

                this.icoPlayer = 0;
                this.setArtist(false);
                this.setHelper(false);
                this.setModerator(false);
                this.setAdministrator(false);
                this.setDirector(false);
                this.setDeveloper(false);
                this.setLimited(true);
                this.setSuperDev(false);

                if (this.account.admin <= 0)
                    this.account.persistentName = null;
                    if(this.account.admin == 1 && this.account.limited == 1)
                    this.setArtist(true);
                if (this.account.admin >= 1) {
                    this.setHelper(true);
                    this.icoPlayer = 8;
                    this.developerModeNumber = 10;
                }
                if (this.account.admin >= 2) {
                    this.setModerator(true);
                    this.icoPlayer = 9;
                    this.developerModeNumber = 3;
                }
                if (this.account.admin >= 3) {
                    this.setAdministrator(true);
                    this.icoPlayer = 10;
                    this.developerModeNumber = 13;
                }
                if (this.account.admin >= 4) {
                    this.setDirector(true);
                    this.icoPlayer = 11;
                    this.developerModeNumber = 10;
                }
                if (this.account.admin >= 5) {
                    this.setDeveloper(true);
                    this.icoPlayer = 12;
                    this.developerModeNumber = 14;
                }
                if (this.account.limited == 1) {
                    this.setLimited(true);
                } else
                    this.setLimited(false);
                if (this.account.admin >= 6) {
                    this.setSuperDev(true);
                    this.icoPlayer = 13;
                }
                if (this.account.youtube == 1)
                    this.isYTMode = true;

                if (this.account.artist == 1)
                    this.setArtist(true);

                if (Constants.ADMINFOREVERYONE) {

                    this.setDeveloper(true);
                    this.setLimited(false);
                }
                if (this.account.admin == 3 && this.isLimited()) {

                    this.isDevLimited = true;
                }
                this.setAdminType(this.account.adminType);
            } else {
                this.sendDisconnect(false, "Unable to log in you!", true);
            }
        }
    }

    public void updateCoins(int coins) {
        if (this.account != null) {
            Gson gson = new Gson();
            Account acc = null;
            try {
                acc = gson.fromJson(
                        this.room.accountConnector.getAccount("" + this.account.id, this.account.password_token),
                        Account.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (acc != null && acc.isSuccess()) {
                this.account = acc;

                this.icoPlayer = 0;
                this.setArtist(false);
                this.setHelper(false);
                this.setModerator(false);
                this.setAdministrator(false);
                this.setDirector(false);
                this.setDeveloper(false);
                this.setLimited(true);
                this.setSuperDev(false);

                if (this.account.admin <= 0)
                    this.account.persistentName = null;
                if (this.account.admin >= 1) {
                    this.setHelper(true);
                    this.icoPlayer = 8;
                    this.developerModeNumber = 10;
                }
                if (this.account.admin >= 2) {
                    this.setModerator(true);
                    this.icoPlayer = 9;
                    this.developerModeNumber = 3;
                }
                if (this.account.admin >= 3) {
                    this.setAdministrator(true);
                    this.icoPlayer = 10;
                    this.developerModeNumber = 13;
                }
                if (this.account.admin >= 4) {
                    this.setDirector(true);
                    this.icoPlayer = 11;
                    this.developerModeNumber = 10;
                }
                if (this.account.admin >= 5) {
                    this.setDeveloper(true);
                    this.icoPlayer = 12;
                    this.developerModeNumber = 14;
                }
                if (this.account.limited == 1) {
                    this.setLimited(true);
                } else
                    this.setLimited(false);
                if (this.account.admin >= 6) {
                    this.setSuperDev(true);
                    this.icoPlayer = 13;
                }
                if (this.account.youtube == 1)
                    this.isYTMode = true;

                if (this.account.artist == 1)
                    this.setArtist(true);

                if (Constants.ADMINFOREVERYONE) {
                    this.setDeveloper(true);
                    this.setLimited(false);
                }
                this.room.accountConnector.addCoins(String.valueOf(this.account.id),
                        String.valueOf(this.account.password_token), coins);
                this.setAdminType(this.account.adminType);

            } else {
                this.room.accountConnector.addCoins(String.valueOf(this.account.id),
                        String.valueOf(this.account.password_token), coins);
                this.sendDisconnect(false, "Unable to log in you!", true);
            }
        }
    }

    public void handleFirstConnect(MsgReader reader) {
        try {
            int[] list = { reader.readUInt32(), reader.readUInt32(), reader.readUInt32(), reader.readUInt32() };
            if (Arrays.equals(list, Constants.SECRETS)) {
                reconnect = reader.readUInt8() == 1 ? true : false;
                if (reconnect) {
                    ses = reader.readString();

                }
                boolean inAccount = reader.readUInt8() == 1 ? true : false;

                if (inAccount) {
                    String login = reader.readString();
                    String passwordToken = reader.readString();
                    Gson gson = new Gson();
                    Account acc = null;
                    try {
                        acc = gson.fromJson(this.room.accountConnector.getAccount(login, passwordToken), Account.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (acc != null && acc.isSuccess()) {
                        if (acc.banned == 1) {
                            this.sendDisconnect(false, "You are BANNED!", true);
                        } else {
                            this.account = acc;

                            if (Constants.CLOSEDFORDEV) {
                                // Main.log.info("a: " + this.account.admin + " name " +
                                // this.account.persistentName);
                                if (this.account.admin > 1) {

                                    this.send(
                                            Networking.popup(
                                                    "Welcome "
                                                            + (this.account.persistentName == null ? acc.name
                                                                    : this.account.persistentName)
                                                            + " the server is on testing! ",
                                                    "success", 10));
                                } else {
                                    this.sendDisconnect(true,
                                            "Sorry the server is currently closed for developer testing!",
                                            false);
                                    return;
                                }

                            }

                            if (this.account.admin <= 0)
                                this.account.persistentName = null;
                                if(this.account.admin == 1 && this.account.limited == 1)
                                this.setArtist(true);
                            if (this.account.admin >= 1) {
                                this.setHelper(true);
                                this.icoPlayer = 8;
                                this.developerModeNumber = 10;
                            }
                            if (this.account.admin >= 2) {
                                this.setModerator(true);
                                this.icoPlayer = 9;
                                this.developerModeNumber = 3;
                            }
                            if (this.account.admin >= 3) {
                                this.setAdministrator(true);
                                this.icoPlayer = 10;
                                this.developerModeNumber = 13;
                            }
                            if (this.account.admin >= 4) {
                                this.setDirector(true);
                                this.icoPlayer = 11;
                                this.developerModeNumber = 10;
                            }
                            if (this.account.limited == 1) {
                                this.setLimited(true);
                            } else
                                this.setLimited(false);
                            if (this.account.admin >= 5) {
                                this.setDeveloper(true);
                                this.icoPlayer = 12;
                                this.developerModeNumber = 14;
                            }
                            if (this.account.admin >= 6) {
                                this.developerModeNumber = 7;
                                this.setSuperDev(true);
                                this.icoPlayer = 13;
                            }
                            if (this.account.youtube == 1)
                                this.isYTMode = true;

                            if (this.account.artist == 1)
                                this.setArtist(true);

                            if (Constants.ADMINFOREVERYONE) {
                                this.setDeveloper(true);
                                this.setLimited(false);
                            }
                            if (this.account.id == 5) {
                                this.dotColor = "yellow";
                        this.developerModeNumber = 14;
                        this.icoPlayer = 12;
                       this.color = 11;
                       
                            }
                            if (this.account.id == 25) {
                       this.color = 13;
                       this.icoPlayer = 3;
                       
                            }
                            if(this.account.id == 33 || this.account.id == 13){
                                this.color = 1;
                                this.icoPlayer = 10;
                            }


                            this.setAdminType(this.account.adminType);

                            if (Constants.ENABLECOINEARNING)
                                this.coinsState = 0;

                        }
                    } else {
                        this.sendDisconnect(false, "something goes wrong", true);
                    }
                } else {
                    if (Constants.ADMINFOREVERYONE) {
                        this.setDeveloper(true);
                        this.setLimited(false);
                    }
                    if (Constants.CLOSEDFORDEV && !reconnect) {

                        // если не т аккаунта то баним по дефолту эф
                        this.sendDisconnect(true,
                                "Sorry the server is currently closed for developer testing!",
                                false);
                        return;

                    }

                }
                if (reconnect) {

                    // this.getRoom().saved_clients.
                    for (Map.Entry<String, GameClient> entry : Main.instance.sessionManager.getSessions()
                            .entrySet()) {
                        String a = entry.getKey();
                        GameClient c = entry.getValue();

                        if (a.equals(ses)) {
                            if(c.getPlayer() != null){
                            if (inAccount) {// Making sure to get the same account values!
                                c.account.id = this.account.id;
                                c.account.password_token = this.account.password_token;
                                c.account.admin = this.account.admin;
                                c.updateAccount();// do a request to accountserver and update
                                                  // the rest of data
                            }
                            // System.out.print(c.player + "\n");
                            Main.instance.server.clients.put(this.socket, c);
                            c.room = this.room;
                            c.shouldRemove = false;
                            c.socket = this.socket;
                            c.spectating = false;
                            c.getPlayer().getOldCollidedList().clear();
                            c.getPlayer().getCollidedList().clear();
                            c.mahdi = true;
                            c.passedJoinGame = true;
                            this.saveSession = false;
                            // System.out.print(c.player +"\n");

                            // c.setIP(cp);
                            c.ip = this.ip;
                            this.shouldRemove = true;
                            c.removeSession.reset();
                            Main.instance.sessionManager.removeSession(c, false);

                                                        // System.out.print(c.player +"\n");


                            // c.playerName = this.playerName;

                            if (c.getAccount() != null) {
                                if (Constants.CLOSEDFORDEV && c.account.admin < 2) {

                                    c.sendDisconnect(true,
                                            "Sorry the server is currently closed for developer testing!",
                                            false);
                                    return;

                                }

                            }
                            MsgWriter writer = new MsgWriter();
                            writer.writeType(MessageType.FIRSTCONNECT);

                            writer.writeString(ses);

                            writer.writeUInt8(c.room.getGameMode());
                            // writer.writeUInt8(this.room.getGameState());
                            writer.writeUInt16((short) c.room.getPlayers());
                            writer.writeUInt16((short) c.room.getServerVer());
                            if (Constants.isBeta)
                                writer.writeUInt16(Constants.BETA_VERSION);
                            // writer.writeUInt8(this.room.getSeason());
                            // writer.writeUInt8(this.room.isAllowedToChat() ? 1 : 0);

                            if (c.socket.isOpen())
                                c.socket.send(writer.getData());
                            if (Constants.ENABLEDCAPTCHA)
                                c.send(Networking.captcha());
                                        // System.out.print(c.player +"\n");


                                // AnimalInfo info = Animals.byID(c.getPlayer().getInfo().getAnimalType());
                                // if (info.getType().hasCustomClass()) {
    
                                //     try {
    
                                //         Class<?> rawAnimalClass = info.getType().getAnimalClass();
    
                                //         @SuppressWarnings("unchecked")
                                //         Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                                //         Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class,
                                //                 double.class,
                                //                 AnimalInfo.class, String.class, GameClient.class);
                                //         c.player = (Animal) aa.newInstance(
                                //                 c.room.getID(), c.player.getX(), c.player.getY(), info,
                                //                 c.customName != null ? c.customName
                                //                         : c.account != null
                                //                                 ? c.account.persistentName != null
                                //                                         ? c.account.persistentName
                                //                                         : c.playerName
                                //                                 : c.playerName,
                                //                 c);
                                //     } catch (InstantiationException e) {
                                //         c.sendDisconnect(false, "MOPERR_1", false);
                                //         e.printStackTrace();
                                //     } catch (IllegalAccessException e) {
                                //         c.sendDisconnect(false, "MOPERR_2", false);
                                //         e.printStackTrace();
                                //     } catch (InvocationTargetException e) {
                                //         c.sendDisconnect(false, "MOPERR_3", false);
                                //         e.printStackTrace();
                                //     } catch (NoSuchMethodException e) {
                                //         c.sendDisconnect(false, "MOPERR_4", false);
                                //         e.printStackTrace();
                                //     }
                                // } else {
                                //     c.player = new Animal(c.room.getID(), c.player.getX(), c.player.getY(),
                                //             info,
                                //             c.customName != null ? c.customName
                                //                     : c.account != null
                                //                             ? c.account.persistentName != null
                                //                                     ? c.account.persistentName
                                //                                     : c.playerName
                                //                             : c.playerName,
                                //             c); // shit
                                // }
                            // System.out.print(c.camzoom+"\n");

                            // c.spectating = false;

                            c.setXp(c.getXP());
                            c.setCamzoom(c.getCamzoom());
                            c.inUpgrade = false;
                            c.spectating = false;
                                                        // System.out.print(c.player +"\n");

                            // c.update()

                            return;
                            // c.saveSession = false;
                        }

                        }
                    }
                }

                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.FIRSTCONNECT);

                writer.writeString(this.session);

                writer.writeUInt8(this.room.getGameMode());
                // writer.writeUInt8(this.room.getGameState());
                writer.writeUInt16((short) this.room.getPlayers());
                writer.writeUInt16((short) this.room.getServerVer());
                if (Constants.isBeta)
                    writer.writeUInt16(Constants.BETA_VERSION);
                // writer.writeUInt8(this.room.getSeason());
                // writer.writeUInt8(this.room.isAllowedToChat() ? 1 : 0);

                if (this.socket.isOpen())
                    this.socket.send(writer.getData());
                if (Constants.ENABLEDCAPTCHA)
                    this.send(Networking.captcha());

            } else {
                this.sendDisconnect(false, "Handshake failure.", false);
            }
        } catch (PacketException e) {
            this.sendDisconnect(false, "MOPERR_5_1", false);
        }
    }

    private Timer reqTimer = new Timer(1000);
    private Timer rechargePvp = new Timer(200);

    public void updateRequests() {
        if (this.pvpRequests.size() > 0) {
            reqTimer.update(Constants.TICKS_PER_SECOND);
            if (reqTimer.isDone()) {
                List<PvPRequest> fordelete = new ArrayList<>();
                for (PvPRequest req : this.pvpRequests) {
                    req.updateDuration();
                    if (req.getDuration() < 1) {
                        fordelete.add(req);
                    }
                }
                for (PvPRequest reqq : fordelete) {
                    this.removeRequest(reqq);
                }
                reqTimer.reset();
            }
        }
    }

    private Timer coinsStateTimer = new Timer(1000);
    private Timer coinsGetTimer = new Timer(Constants.COINSEVERY * 1000);

    public void updateCoins() {
        if (coinsState == 0) {
            if (this.getPlayer() != null && !this.getPlayer().isGhost && !this.getPlayer().inEgg) {
                coinsStateTimer.update(Constants.TICKS_PER_SECOND);
                if (coinsStateTimer.isDone()) {
                    if (coinBegin < 1)
                        coinsState = 99;
                    coinBegin--;
                    coinsStateTimer.reset();
                }
            }
        } else if (coinsState > 5) {
            if (this.getPlayer() != null && !this.getPlayer().isGhost && !this.getPlayer().inEgg) {
                if (Constants.ENABLEIDLECOINS) {
                    coinsGetTimer.update(Constants.TICKS_PER_SECOND);
                    if (coinsGetTimer.isDone()) {
                        this.addCoins(Constants.IDLECOINS);
                        this.addEXP(Constants.IDLEEXP);
                        coinsGetTimer.reset();
                    }
                }
            }
        }
    }

    private Timer pvpTargetTimer = new Timer(1000);
    public GameClient arenaEnemy;
    public boolean devHidden = false;
    private int inUpgradeTimer = 0;
    private Timer upgradeTimer = new Timer(1000);
    private Timer upgradeCooldownTimer = new Timer(500);

    private boolean godmodeEnabled = false;

    public boolean isGodmodeEnabled() {
        return godmodeEnabled;
    }

    public void setAdminType(int a) {
        this.adminType = a;
    }

    public int getAdminType() {
        return this.adminType;
    }

    public void setGodmode(boolean a) {
        godmodeEnabled = a;
    }

    private Timer verifTimer = new Timer(60 * 1000);

    private boolean d = false;

    public int timesThatCanReconnect = 0;

    public void update() {



        heartbeat.update(Constants.TICKS_PER_SECOND);
        if(heartbeat.isDone()){
            if(this.socket.isOpen()){
                this.onClose();//Check if player still alive and if it is, remove it and also remove client
                this.socket.close();//Closing ws from server side
            }
            heartbeat.reset();

        }
        if(this.socket.isOpen()){
            if(checkIfAccBanned.isDone()){

            if(this.getAccount() != null){
                checkIfAccBanned.update(Constants.TICKS_PER_SECOND);
                    Gson gson = new Gson();
                    Account account_1 = gson.fromJson(this.room.accountConnector.getAccount(String.valueOf(this.account.id), this.account.password_token), Account.class);
                    if(account_1.banned == 1){
                        this.saveSession = false;
                    this.sendDisconnect(false, "You're banned", true);
                    }

                }
                if(this.room.ipblock.checkIP(this.ip)){
                    this.saveSession = false;
                    this.sendDisconnect(false, "MOPERR_691", true);

                }
                checkIfAccBanned.reset();

            }
        }

        if (this.getPlayer() != null) {
            upgradeCooldownTimer.update(Constants.TICKS_PER_SECOND);
            if (upgradeCooldownTimer.isDone()) {
                this.canUpgrade = true;
            }
            if (!canUpgradeOrDowngrade) {
                canUpgradeOrDowngradeTimer.update(Constants.TICKS_PER_SECOND);
                if (canUpgradeOrDowngradeTimer.isDone()) {
                    canUpgradeOrDowngrade = true;

                }
            }
        }

        if (this.getPlayer() != null) {
            if(this.xp < 10){
                this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius();
            } 
            if(Tier.byOrdinal(getTier()).getBaseRadius() < this.getPlayer().baseRadius && this.getPlayer().getInfo().getAnimalType() != 100 && !(this.getXP() >= 500000)) {
                this.getPlayer().baseRadius = this.getPlayer().baseRadius - 1;
            }
            if(Tier.byOrdinal(getTier()).getBaseRadius() > this.getPlayer().baseRadius && !(this.getXP() >= 500000)) {
                this.getPlayer().baseRadius = this.getPlayer().baseRadius + 1;
            }
            if(this.getXP() < this.getStartXP() / 2){
                double rr = Math.ceil(this.getXP());
                String rr2 = Integer.toString((int) rr);
                // this.room.chat(this, rr2);
                int rttr = this.getPlayer().getInfo().getTier() - 1;
                if(rttr <= 0) rttr = 1;
                this.getPlayer().baseRadius = Tier.byOrdinal(rttr).getBaseRadius();
            }
            if(this.getXP() < this.getStartXP() / 4){
                double rr = Math.ceil(this.getXP());
                String rr2 = Integer.toString((int) rr);
                // this.room.chat(this, rr2);
                int rttr = this.getPlayer().getInfo().getTier() - 4;
                if(rttr <= 0) rttr = 1;

                this.getPlayer().baseRadius = Tier.byOrdinal(rttr).getBaseRadius();
            }
            if(this.getPlayer().getInfo().getTier() == 17 && this.getStartXP() < this.getXP() && this.getPlayer().getInfo().getAnimalType() != 100){
                if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() - this.getStartXP() < 4000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() - this.getStartXP() < 6000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 6000000 && this.getXP() - this.getStartXP() < 8000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 8000000 && this.getXP() - this.getStartXP() < 10000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
                if(this.getXP() - this.getStartXP() >= 10000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 17 && this.getStartXP() < this.getXP() && this.getPlayer().getInfo().getAnimalType() == 100){
                if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() - this.getStartXP() < 4000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 10;
                }
                if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() - this.getStartXP() < 6000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 14;
                }
                if(this.getXP() - this.getStartXP() >= 6000000 && this.getXP() - this.getStartXP() < 8000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 16;
                }
                if(this.getXP() - this.getStartXP() >= 8000000 && this.getXP() - this.getStartXP() < 10000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 18;
                }
                if(this.getXP() - this.getStartXP() >= 10000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 20;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 16 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 1000000 && this.getXP() - this.getStartXP() < 2000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() - this.getStartXP() < 3000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 3000000 && this.getXP() - this.getStartXP() < 4000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() - this.getStartXP() < 5000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 15 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 1000000 && this.getXP() - this.getStartXP() < 2000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() - this.getStartXP() < 3000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 3000000 && this.getXP() - this.getStartXP() < 4000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() - this.getStartXP() < 5000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 14 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 100000 && this.getXP() - this.getStartXP() < 200000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 200000 && this.getXP() - this.getStartXP() < 300000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 300000 && this.getXP() - this.getStartXP() < 400000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 400000 && this.getXP() - this.getStartXP() < 4000000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 13 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 50000 && this.getXP() - this.getStartXP() < 100000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 100000 && this.getXP() - this.getStartXP() < 150000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 150000 && this.getXP() - this.getStartXP() < 200000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 200000 && this.getXP() - this.getStartXP() < 400000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 12 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 26000 && this.getXP() - this.getStartXP() < 52000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 52000 && this.getXP() - this.getStartXP() < 78000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 78000 && this.getXP() - this.getStartXP() < 104000){
                    if(Tier.byOrdinal(getTier()) != null) this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
                if(this.getXP() - this.getStartXP() >= 104000 && this.getXP() - this.getStartXP() < 200000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 11 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 14000 && this.getXP() - this.getStartXP() < 28000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 28000 && this.getXP() - this.getStartXP() < 42000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 42000 && this.getXP() - this.getStartXP() < 54000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 54000 && this.getXP() - this.getStartXP() < 104000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 10 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 8000 && this.getXP() - this.getStartXP() < 16000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 16000 && this.getXP() - this.getStartXP() < 24000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 24000 && this.getXP() - this.getStartXP() < 32000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 32000 && this.getXP() - this.getStartXP() < 54000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 9 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() - this.getStartXP() < 8000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 12000 && this.getXP() - this.getStartXP() < 16000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 16000 && this.getXP() - this.getStartXP() < 20000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 20000 && this.getXP() - this.getStartXP() < 32000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 8 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() - this.getStartXP() < 4000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() - this.getStartXP() < 6000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 6000 && this.getXP() - this.getStartXP() < 8000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 8000 && this.getXP() - this.getStartXP() < 20000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 7 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() - this.getStartXP() < 2000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() - this.getStartXP() < 3000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 3000 && this.getXP() - this.getStartXP() < 4000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() - this.getStartXP() < 8000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 6 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 500 && this.getXP() - this.getStartXP() < 1000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() - this.getStartXP() < 1500){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 1500 && this.getXP() - this.getStartXP() < 2000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() - this.getStartXP() < 4000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 5 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 250 && this.getXP() - this.getStartXP() < 500){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 500 && this.getXP() - this.getStartXP() < 750){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 750 && this.getXP() - this.getStartXP() < 1000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() - this.getStartXP() < 2000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 4 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 100 && this.getXP() - this.getStartXP() < 200){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 200 && this.getXP() - this.getStartXP() < 300){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 300 && this.getXP() - this.getStartXP() < 400){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
                if(this.getXP() - this.getStartXP() >= 400 && this.getXP() - this.getStartXP() < 1000){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 3 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 70 && this.getXP() - this.getStartXP() < 140){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 140 && this.getXP() - this.getStartXP() < 210){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 210 && this.getXP() - this.getStartXP() < 280){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 280 && this.getXP() - this.getStartXP() < 350){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 350 && this.getXP() - this.getStartXP() < 400){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 2 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 25 && this.getXP() - this.getStartXP() < 50){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 50 && this.getXP() - this.getStartXP() < 75){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 75 && this.getXP() - this.getStartXP() < 100){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 100 && this.getXP() - this.getStartXP() < 350){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                }
            }
            if(this.getPlayer().getInfo().getTier() == 1 && this.getStartXP() < this.getXP()){
                if(this.getXP() - this.getStartXP() >= 10 && this.getXP() - this.getStartXP() < 20){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
                }
                if(this.getXP() - this.getStartXP() >= 20 && this.getXP() - this.getStartXP() < 30){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
                }
                if(this.getXP() - this.getStartXP() >= 30 && this.getXP() - this.getStartXP() < 40){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
                }
                if(this.getXP() - this.getStartXP() >= 40 && this.getXP() - this.getStartXP() < 100){
                    this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
                
            }
            
        }
            if (!this.getPlayer().inArena) {
                if (this.flag_hideInterface) {
                    this.flag_hideInterface = false;
                }
                if (this.flag_hideInterface1) {
                    this.flag_hideInterface1 = false;
                }
                if (this.flag_fixedcamara) {
                    this.flag_fixedcamara = false;
                }
                if (this.flag_fixedcamara1) {
                    this.flag_fixedcamara1 = false;
                }
                if (this.FixedCamara != null) {
                    this.room.removeObj(this.FixedCamara, this.getPlayer());
                    this.FixedCamara = null;

                }
                if (this.not_FixedCamara != null) {
                    this.room.removeObj(this.not_FixedCamara, this.getPlayer());
                    this.not_FixedCamara = null;
                }
                if (this.FixedCamara1 != null) {
                    this.room.removeObj(this.FixedCamara1, this.getPlayer());
                    this.FixedCamara1 = null;
                }

                if (this.not_FixedCamara1 != null) {
                    this.room.removeObj(this.not_FixedCamara1, this.getPlayer());
                    this.not_FixedCamara1 = null;
                }

                if (this.hideInterFace != null) {
                    this.room.removeObj(this.hideInterFace, this.getPlayer());
                    this.hideInterFace = null;

                }
                if (this.not_hideInterFace != null) {
                    this.room.removeObj(this.not_hideInterFace, this.getPlayer());
                    this.not_hideInterFace = null;
                }
                if (this.hideInterFace1 != null) {
                    this.room.removeObj(this.hideInterFace1, this.getPlayer());
                    this.hideInterFace1 = null;
                }

                if (this.not_hideInterFace1 != null) {
                    this.room.removeObj(this.not_hideInterFace1, this.getPlayer());
                    this.not_hideInterFace1 = null;
                }
            }
            

            // if(this.getInfo)
        

            // Tier currentTier = Tier.byOrdinal(this.getPlayer().getTier());
            // if (currentTier != null) {
            //     Tier nextTier = Tier.byOrdinal(getTier() + 1);

            //     if(nextTier == null) nextTier = Tier.byOrdinal(getTier());
            
            //     if (nextTier != null) {
            //         int startXP = currentTier.getStartXP();
            //         int endXP = nextTier.getStartXP();
            //         int currentXP = this.getXP();
            
            //         double percentage = (double) (currentXP - startXP) / (endXP - startXP);
            //         // percentage = Math.min(1.0, Math.max(0.0, percentage));


            
            //         int baseRadius = currentTier.getBaseRadius();
            //         // int maxRadius = nextTier.getBaseRadius();
            
            //         int radius = (int) (baseRadius + Math.round(percentage) * 3);
            
            //         if(!(this.getPlayer() instanceof PufferFish && this.getPlayer().getInfo().getAbility().isActive()) && !this.getPlayer().isRammed && !this.getPlayer().pickedByBird && !this.getPlayer().pickedByPelican && !this.getPlayer().flag_isClimbingHill && !this.getPlayer().inArena) this.getPlayer().baseRadius = radius;
            //         if(!(this.getPlayer() instanceof PufferFish && this.getPlayer().getInfo().getAbility().isActive()) && !this.getPlayer().isRammed && !this.getPlayer().pickedByBird && !this.getPlayer().pickedByPelican && !this.getPlayer().flag_isClimbingHill && !this.getPlayer().inArena) this.getPlayer().setRadius(radius);
            //     }
            // }
            
            
            
        }
        // if(this.getPlayer() != null){
        // if(this.getAccount() != null){
        // if(this.getPlayer() != null && this.getPlayer().inArena && this.account.id ==
        // 1 || this.flag_fixedcamara || this.flag_fixedcamara1){ //&&
        // this.getPlayer().getPlayerName() == "hacker"
        // Animal enemy = this.arenaEnemy.getPlayer();
        // if(enemy != null) {
        // double enemyX = enemy.getX();
        // double enemyY = enemy.getY();
        // double enemyTailX = enemyX-((Math.cos(Math.toRadians(enemy.getAngle())) *
        // (enemy.getRadius())));
        // double enemyTailY = enemyY-((Math.sin(Math.toRadians(enemy.getAngle())) *
        // (enemy.getRadius())));
        // double enemyMouthX = enemyX+((Math.cos(Math.toRadians(enemy.getAngle())) *
        // (enemy.getRadius())));
        // double enemyMouthY = enemyY+((Math.sin(Math.toRadians(enemy.getAngle())) *
        // (enemy.getRadius())));
        // if(Utils.distance(this.getPlayer().getX(), enemyTailX,
        // this.getPlayer().getY(), enemyTailY) > this.getPlayer().getRadius()/2) {
        // this.getPlayer().changeBoost(true);
        // this.getPlayer().setMouseX(enemyTailX);
        // this.getPlayer().setMouseY(enemyTailY);
        // } else {
        // this.getPlayer().changeBoost(false);
        // this.getPlayer().setMouseX(enemyMouthX);
        // this.getPlayer().setMouseY(enemyMouthY);
        // }
        // }
        // }
        // }
        // }

        // if(this.getPlayer() != null && !(this.getPlayer() instanceof PufferFish) &&
        // !(this.getPlayer() instanceof Duck) && !(this.getPlayer() instanceof
        // Duckling)){

        // if(this.xp < 10){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius();
        // }
        // if(Tier.byOrdinal(getTier()).getBaseRadius() < this.getPlayer().baseRadius &&
        // this.getPlayer().getInfo().getAnimalType() != 100 && !(this.getXP() >=
        // 500000)) {
        // this.getPlayer().baseRadius = this.getPlayer().baseRadius - 1;
        // }
        // if(Tier.byOrdinal(getTier()).getBaseRadius() > this.getPlayer().baseRadius &&
        // !(this.getXP() >= 500000)) {
        // this.getPlayer().baseRadius = this.getPlayer().baseRadius + 1;
        // }
        // if(this.getXP() < this.getStartXP() / 2){
        // double rr = Math.ceil(this.getXP());
        // String rr2 = Integer.toString((int) rr);
        // // this.room.chat(this, rr2);
        // int rttr = this.getPlayer().getInfo().getTier() - 1;
        // if(rttr <= 0) rttr = 1;
        // this.getPlayer().baseRadius = Tier.byOrdinal(rttr).getBaseRadius();
        // }
        // if(this.getXP() < this.getStartXP() / 4){
        // double rr = Math.ceil(this.getXP());
        // String rr2 = Integer.toString((int) rr);
        // // this.room.chat(this, rr2);
        // int rttr = this.getPlayer().getInfo().getTier() - 4;
        // if(rttr <= 0) rttr = 1;

        // this.getPlayer().baseRadius = Tier.byOrdinal(rttr).getBaseRadius();
        // }
        // if(this.getPlayer().getInfo().getTier() == 17 && this.getStartXP() <
        // this.getXP() && this.getPlayer().getInfo().getAnimalType() != 100){
        // if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() -
        // this.getStartXP() < 4000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() -
        // this.getStartXP() < 6000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 6000000 && this.getXP() -
        // this.getStartXP() < 8000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 8000000 && this.getXP() -
        // this.getStartXP() < 10000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // if(this.getXP() - this.getStartXP() >= 10000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 17 && this.getStartXP() <
        // this.getXP() && this.getPlayer().getInfo().getAnimalType() == 100){
        // if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() -
        // this.getStartXP() < 4000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 10;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() -
        // this.getStartXP() < 6000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 14;
        // }
        // if(this.getXP() - this.getStartXP() >= 6000000 && this.getXP() -
        // this.getStartXP() < 8000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 16;
        // }
        // if(this.getXP() - this.getStartXP() >= 8000000 && this.getXP() -
        // this.getStartXP() < 10000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 18;
        // }
        // if(this.getXP() - this.getStartXP() >= 10000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 20;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 16 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 1000000 && this.getXP() -
        // this.getStartXP() < 2000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() -
        // this.getStartXP() < 3000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 3000000 && this.getXP() -
        // this.getStartXP() < 4000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() -
        // this.getStartXP() < 5000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 15 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 1000000 && this.getXP() -
        // this.getStartXP() < 2000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 2000000 && this.getXP() -
        // this.getStartXP() < 3000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 3000000 && this.getXP() -
        // this.getStartXP() < 4000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000000 && this.getXP() -
        // this.getStartXP() < 5000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 14 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 100000 && this.getXP() -
        // this.getStartXP() < 200000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 200000 && this.getXP() -
        // this.getStartXP() < 300000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 300000 && this.getXP() -
        // this.getStartXP() < 400000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 400000 && this.getXP() -
        // this.getStartXP() < 4000000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 13 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 50000 && this.getXP() -
        // this.getStartXP() < 100000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 100000 && this.getXP() -
        // this.getStartXP() < 150000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 150000 && this.getXP() -
        // this.getStartXP() < 200000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 200000 && this.getXP() -
        // this.getStartXP() < 400000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 12 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 26000 && this.getXP() -
        // this.getStartXP() < 52000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 52000 && this.getXP() -
        // this.getStartXP() < 78000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 78000 && this.getXP() -
        // this.getStartXP() < 104000){
        // if(Tier.byOrdinal(getTier()) != null) this.getPlayer().baseRadius =
        // Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // if(this.getXP() - this.getStartXP() >= 104000 && this.getXP() -
        // this.getStartXP() < 200000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 11 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 14000 && this.getXP() -
        // this.getStartXP() < 28000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 28000 && this.getXP() -
        // this.getStartXP() < 42000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 42000 && this.getXP() -
        // this.getStartXP() < 54000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 54000 && this.getXP() -
        // this.getStartXP() < 104000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 10 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 8000 && this.getXP() -
        // this.getStartXP() < 16000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 16000 && this.getXP() -
        // this.getStartXP() < 24000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 24000 && this.getXP() -
        // this.getStartXP() < 32000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 32000 && this.getXP() -
        // this.getStartXP() < 54000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 9 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() -
        // this.getStartXP() < 8000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 12000 && this.getXP() -
        // this.getStartXP() < 16000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 16000 && this.getXP() -
        // this.getStartXP() < 20000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 20000 && this.getXP() -
        // this.getStartXP() < 32000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 8 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() -
        // this.getStartXP() < 4000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() -
        // this.getStartXP() < 6000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 6000 && this.getXP() -
        // this.getStartXP() < 8000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 8000 && this.getXP() -
        // this.getStartXP() < 20000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 7 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() -
        // this.getStartXP() < 2000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() -
        // this.getStartXP() < 3000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 3000 && this.getXP() -
        // this.getStartXP() < 4000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 4000 && this.getXP() -
        // this.getStartXP() < 8000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 6 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 500 && this.getXP() -
        // this.getStartXP() < 1000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() -
        // this.getStartXP() < 1500){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 1500 && this.getXP() -
        // this.getStartXP() < 2000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 2000 && this.getXP() -
        // this.getStartXP() < 4000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 5 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 250 && this.getXP() -
        // this.getStartXP() < 500){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 500 && this.getXP() -
        // this.getStartXP() < 750){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 750 && this.getXP() -
        // this.getStartXP() < 1000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 1000 && this.getXP() -
        // this.getStartXP() < 2000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 4 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 100 && this.getXP() -
        // this.getStartXP() < 200){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 200 && this.getXP() -
        // this.getStartXP() < 300){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 300 && this.getXP() -
        // this.getStartXP() < 400){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // if(this.getXP() - this.getStartXP() >= 400 && this.getXP() -
        // this.getStartXP() < 1000){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 5;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 3 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 70 && this.getXP() - this.getStartXP()
        // < 140){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 140 && this.getXP() -
        // this.getStartXP() < 210){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 210 && this.getXP() -
        // this.getStartXP() < 280){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 280 && this.getXP() -
        // this.getStartXP() < 350){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 350 && this.getXP() -
        // this.getStartXP() < 400){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 2 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 25 && this.getXP() - this.getStartXP()
        // < 50){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 50 && this.getXP() - this.getStartXP()
        // < 75){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 75 && this.getXP() - this.getStartXP()
        // < 100){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 100 && this.getXP() -
        // this.getStartXP() < 350){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;
        // }
        // }
        // if(this.getPlayer().getInfo().getTier() == 1 && this.getStartXP() <
        // this.getXP()){
        // if(this.getXP() - this.getStartXP() >= 10 && this.getXP() - this.getStartXP()
        // < 20){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 1;
        // }
        // if(this.getXP() - this.getStartXP() >= 20 && this.getXP() - this.getStartXP()
        // < 30){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 2;
        // }
        // if(this.getXP() - this.getStartXP() >= 30 && this.getXP() - this.getStartXP()
        // < 40){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 3;
        // }
        // if(this.getXP() - this.getStartXP() >= 40 && this.getXP() - this.getStartXP()
        // < 100){
        // this.getPlayer().baseRadius = Tier.byOrdinal(getTier()).getBaseRadius() + 4;

        // }

        // }

        // }

        scamTimer.update(Constants.TICKS_PER_SECOND);
        if (scamTimer.isDone())
            packagesPer5s = 0;

        if (Constants.ENABLEDCAPTCHA) {
            verifTimer.update(Constants.TICKS_PER_SECOND);
            if (verifTimer.isDone() && !d) {
                d = true;
                if (!this.isVerifiedByToken) {
                    // Main.log.info("Resolved Bot Attack. Coming from IP: "
                    // + this.ip);
                    this.saveSession = false;
                    this.sendDisconnect(true,
                            "MOPERR_420",
                            false);
                }
            }
        }
        if (this.getPlayer() != null) {
            if (this.xp < 9700000 && this.tier == 17) {
                AnimalInfo info8;
                this.room.removeObj(this.player, null);
                this.room.removeVisObjGhost(this);
                info8 = Animals.byID(14);
                int ttccount = 0;
                if (this.getPlayer().getBiome() == 0 && this.getPlayer().getInfo().getAnimalType() != 100)
                    info8 = Animals.byID(73);
                else if (this.getPlayer().getBiome() == 1 && this.getPlayer().getInfo().getAnimalType() != 100)
                    info8 = Animals.byID(70);
                else if (this.getPlayer().getBiome() == 4 && this.getPlayer().getInfo().getAnimalType() != 100)
                    info8 = Animals.byID(95);
                else if (this.getPlayer().getBiome() == 2 && this.getPlayer().getInfo().getAnimalType() != 100)
                    info8 = Animals.byID(72);
                else if (this.getPlayer().getBiome() == 3)
                    info8 = Animals.byID(71);
                else if (this.getPlayer().getInfo().getAnimalType() == 100) {
                    info8 = Animals.byID(1);
                    this.setXp(0);
                    ttccount = 1;
                }
                int changeType2 = this.player.getInfo().getTier() <= info8.getTier() ? 1 : 0;

                // this.send(Selection.createSelection(this, 0, 1, this.tier, infos));
                if (info8.getType().hasCustomClass()) {
                    try {
                        Class<?> rawAnimalClass = info8.getType().getAnimalClass();

                        @SuppressWarnings("unchecked")
                        Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                        Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                                AnimalInfo.class, String.class, GameClient.class);
                        this.player = (Animal) aa.newInstance(
                                this.room.getID(), this.player.getX(), this.player.getY(), info8,
                                this.customName != null ? this.customName
                                        : this.account != null
                                                ? this.account.persistentName != null ? this.account.persistentName
                                                        : this.playerName
                                                : this.playerName,
                                this);
                    } catch (InstantiationException e) {
                        this.sendDisconnect(false, "MOPERR_1", false);
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        this.sendDisconnect(false, "MOPERR_2", false);
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        this.sendDisconnect(false, "MOPERR_3", false);
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        this.sendDisconnect(false, "MOPERR_4", false);
                        e.printStackTrace();
                    }
                } else {
                    this.player = new Animal(this.room.getID(), this.player.getX(), this.player.getY(), info8,
                            this.customName != null ? this.customName
                                    : this.account != null
                                            ? this.account.persistentName != null ? this.account.persistentName
                                                    : this.playerName
                                            : this.playerName,
                            this); // shit
                }
                if (this.player != null) {
                    if (ttccount == 1) {
                        this.setXp(0);
                    } else {
                        this.setXp(9500000);
                    }
                    this.room.addObj(this.player);
                    this.tier = info8.getTier();

                    this.send(Selection.createSelection(this, 5, 0, 0, null));
                    this.send(Networking.changeAnimal(this.player, changeType2));
                }
                // this.player = new Animal(this.room.getID(), this.player.getX(),
                // this.player.getY(), info8,
                // this.customName != null ? this.customName
                // : this.account != null
                // ? this.account.persistentName != null ? this.account.persistentName
                // : this.playerName
                // : this.playerName,
                // this);
                // if (this.player != null) {
                // this.setXp(9500000);
                // this.room.addObj(this.player);
                // this.tier = info8.getTier();

                // this.send(Selection.createSelection(this, 5, 0, 0, null));
                // this.send(Networking.changeAnimal(this.player, changeType2));
                // }
            }

        }

        if (Constants.GAMEMODE != 2 && Constants.GAMEMODE != 7) {
            if (this.isHelper) {
                this.is1v1Enabled = true;
                this.show1v1 = true;
                this.can1v1 = true;
            } else {
                if (this.getTier() >= 15) {
                    this.is1v1Enabled = true;
                    this.show1v1 = true;
                    this.can1v1 = true;
                } else {
                    this.is1v1Enabled = false;
                    this.show1v1 = false;
                    this.can1v1 = false;
                }
            }
        } else {
            this.is1v1Enabled = false;
            this.show1v1 = false;
            this.can1v1 = false;
        }
        if (this.pvpTarget != null) {
            if (this.getPlayer() != null) {
                this.is1v1Enabled = false;
                this.pvpTarget.setX(this.getPlayer().getMouseX());
                this.pvpTarget.setY(this.getPlayer().getMouseY());
                pvpTargetTimer.update(Constants.TICKS_PER_SECOND);
                if (pvpTargetTimer.isDone()) {
                    this.pvpTarget.updateTimer();
                    pvpTargetTimer.reset();
                }
                if (this.pvpTarget.timer < 1) {
                    this.removePvPTarget();
                }
            } else
                this.removePvPTarget();
        }
        if (this.markhorTarget != null || this.falconTarget != null || this.ostrichTarget != null || this.snowyowlTarget != null) {
            if (this.getPlayer() != null) {
                if (this.markhorTarget != null && !this.markhorTarget.isFreezed()) {
                    this.markhorTarget.setX(this.getPlayer().getMouseX());
                    this.markhorTarget.setY(this.getPlayer().getMouseY());

                }
                if (this.falconTarget != null && !this.falconTarget.isFreezed()) {
                    this.falconTarget.setX(this.getPlayer().getMouseX());
                    this.falconTarget.setY(this.getPlayer().getMouseY());
                }
                    if (this.snowyowlTarget != null && !this.snowyowlTarget.isFreezed()) {
                    this.snowyowlTarget.setX(this.getPlayer().getMouseX());
                    this.snowyowlTarget.setY(this.getPlayer().getMouseY());
                }
                if (this.ostrichTarget != null && this.ostrichTarget.attachedTo == null) {
                    this.ostrichTarget.setX(this.getPlayer().getMouseX());
                    this.ostrichTarget.setY(this.getPlayer().getMouseY());
                }
                if (this.snowyowlTarget != null && !(this.getPlayer() instanceof Snowyowl)) {
                    this.room.removeObj(this.snowyowlTarget, null);
                    this.snowyowlTarget = null;
                }
                       if (this.falconTarget != null && !(this.getPlayer() instanceof Falcon)) {
                    this.room.removeObj(this.falconTarget, null);
                    this.falconTarget = null;
                }

                else {
                    if (this.markhorTarget != null && !(this.getPlayer() instanceof Markhor)) {
                        this.room.removeObj(this.markhorTarget, null);
                        this.markhorTarget = null;
                    } else if (this.ostrichTarget != null && !(this.getPlayer() instanceof Ostrich)) {
                        this.room.removeObj(this.ostrichTarget, null);
                        this.ostrichTarget = null;
                    }

                }
            }
        }

        if (this.DTarget != null) {
            if (this.getPlayer() != null) {
                this.DTarget.setX(this.getPlayer().getMouseX());
                this.DTarget.setY(this.getPlayer().getMouseY());
            } else
                this.removeDTarget();
        }
        if (this.getPlayer() != null) {
            if (this.getPlayer().isInvincible()) {
                this.is1v1Enabled = false;
            }
            if (this.getPlayer().inArena) {
                this.is1v1Enabled = false;
            }
            if (show1v1) {
                rechargePvp.update(Constants.TICKS_PER_SECOND);
                if (rechargePvp.isDone()) {
                    if (this.pvpRecharge > 0) {
                        this.pvpRecharge--;
                    }
                    rechargePvp.reset();
                }
            } else {
                is1v1Enabled = false;
            }
            if (this.isInUpgrade()) {
                if (this.inUpgradeTimer > 0) {
                    upgradeTimer.update(Constants.TICKS_PER_SECOND);
                    if (upgradeTimer.isDone()) {
                        this.inUpgradeTimer--;
                        if (this.inUpgradeTimer <= 0) {
                            this.inUpgradeTimer = 0;
                            int selected = 0;
                            AnimalInfo info = this.selectionList.get(selected);
                            if (!this.inUpgrade)
                                return;
                            if (info == null)
                                return;
                                
                            this.room.removeObj(this.player, null);
                            this.room.removeVisObjGhost(this);
                                this.player.beforeUpgrade(this.player);                                  
                            
                            if (info.getType().hasCustomClass()) {
                                try {
                                    GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                                    double xmax = 0;
                                    double ymax = 0;
                                    double xmin = 0;
                                    double ymin = 0;
                                    if (biome instanceof Biome) {
                                        xmin = ((Biome) biome).getX()
                                                + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - (((Biome) biome).getWidth() / 2);
                                        xmax = ((Biome) biome).getX()
                                                - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + (((Biome) biome).getWidth() / 2);
                                        ymin = ((Biome) biome).getY()
                                                + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - (((Biome) biome).getHeight() / 2);
                                        ymax = ((Biome) biome).getY()
                                                - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + (((Biome) biome).getHeight() / 2);
                                    } else {
                                        xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - biome.getRadius();
                                        xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + biome.getRadius();
                                        ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - biome.getRadius();
                                        ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + biome.getRadius();
                                    }
                                    Class<?> rawAnimalClass = info.getType().getAnimalClass();

                                    @SuppressWarnings("unchecked")
                                    Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                                    Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class,
                                            double.class,
                                            AnimalInfo.class, String.class, GameClient.class);
                                    this.player = (Animal) aa.newInstance(
                                            this.room.getID(), Utils.randomDouble(xmin, xmax),
                                            Utils.randomDouble(ymin, ymax), info,
                                            this.customName != null ? this.customName
                                                    : this.account != null
                                                            ? this.account.persistentName != null
                                                                    ? this.account.persistentName
                                                                    : this.playerName
                                                            : this.playerName,
                                            this);
                                } catch (InstantiationException e) {
                                    this.sendDisconnect(false, "MOPERR_1", false);
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    this.sendDisconnect(false, "MOPERR_2", false);
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    this.sendDisconnect(false, "MOPERR_3", false);
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    this.sendDisconnect(false, "MOPERR_4", false);
                                    e.printStackTrace();
                                }
                            } else {
                                GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                                double xmax = 0;
                                double ymax = 0;
                                double xmin = 0;
                                double ymin = 0;
                                if (biome instanceof Biome) {
                                    xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - (((Biome) biome).getWidth() / 2);
                                    xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + (((Biome) biome).getWidth() / 2);
                                    ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - (((Biome) biome).getHeight() / 2);
                                    ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + (((Biome) biome).getHeight() / 2);
                                } else {
                                    xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - biome.getRadius();
                                    xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + biome.getRadius();
                                    ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - biome.getRadius();
                                    ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + biome.getRadius();
                                }
                                this.player = new Animal(this.room.getID(), Utils.randomDouble(xmin, xmax),
                                        Utils.randomDouble(ymin, ymax),
                                        info,
                                        this.customName != null ? this.customName
                                                : this.account != null
                                                        ? this.account.persistentName != null
                                                                ? this.account.persistentName
                                                                : this.playerName
                                                        : this.playerName,
                                        this);
                            }
                            
                            this.room.addObj(this.player);

                            this.send(Selection.createSelection(this, 5, 0, 0, null));
                            this.send(Networking.changeAnimal(this.player, 1));
                            this.setInUpgrade(false);
                        }
                        upgradeTimer.reset();
                    }
                }
            }
            if (Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgradeXP() <= this.getXP() && (Tier
                    .byOrdinal(Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgraded(this.getXP())) != null
                    || Tier.byOrdinal(
                            Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgraded(this.getXP()) - 1) != null)
                    && !this.getPlayer().inArena && this.getPlayer().getInfo().getTier() < 17) {
                if (this.getTier() < (Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgraded(this.getXP()))
                        && !this.isInUpgrade()) {
                    this.setInUpgrade(true); // скам с массой
                    this.send(Selection.createSelection(this, 0, 15,
                            (Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getUpgraded(this.getXP())),
                            Tier.byOrdinal((Tier.byOrdinal(this.getPlayer().getInfo().getTier())
                                    .getUpgraded(this.getXP()))) != null
                                            ? Tier.byOrdinal((Tier.byOrdinal(this.getPlayer().getInfo().getTier())
                                                    .getUpgraded(this.getXP()))).getAnimalInfo()
                                            : Tier.byOrdinal((Tier.byOrdinal(this.getPlayer().getInfo().getTier())
                                                    .getUpgraded(this.getXP()) - 1)).getAnimalInfo()));
                }
            }
        } else {
            if (this.isInUpgrade()) {
                if (this.inUpgradeTimer > 0) {
                    upgradeTimer.update(Constants.TICKS_PER_SECOND);
                    if (upgradeTimer.isDone()) {
                        this.inUpgradeTimer--;
                        if (this.inUpgradeTimer <= 0) {
                            this.inUpgradeTimer = 0;
                            int selected = 0;
                            AnimalInfo info = this.selectionList.get(selected);
                            if (!this.inUpgrade)
                                return;
                            if (info == null)
                                return;
                                if(this.player != null)this.player.beforeUpgrade(this.player);                                  
                                
                            if (info.getType().hasCustomClass()) {
                                try {
                                    GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                                    double xmax = 0;
                                    double ymax = 0;
                                    double xmin = 0;
                                    double ymin = 0;
                                    if (biome instanceof Biome) {
                                        xmin = ((Biome) biome).getX()
                                                + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - (((Biome) biome).getWidth() / 2);
                                        xmax = ((Biome) biome).getX()
                                                - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + (((Biome) biome).getWidth() / 2);
                                        ymin = ((Biome) biome).getY()
                                                + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - (((Biome) biome).getHeight() / 2);
                                        ymax = ((Biome) biome).getY()
                                                - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + (((Biome) biome).getHeight() / 2);
                                    } else {
                                        xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - biome.getRadius();
                                        xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + biome.getRadius();
                                        ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                - biome.getRadius();
                                        ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                                + biome.getRadius();
                                    }
                                    Class<?> rawAnimalClass = info.getType().getAnimalClass();

                                    @SuppressWarnings("unchecked")
                                    Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                                    Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class,
                                            double.class,
                                            AnimalInfo.class, String.class, GameClient.class);
                                    this.player = (Animal) aa.newInstance( //
                                            this.room.getID(), Utils.randomDouble(xmin, xmax),
                                            Utils.randomDouble(ymin, ymax), info,
                                            this.customName != null ? this.customName
                                                    : this.account != null
                                                            ? this.account.persistentName != null
                                                                    ? this.account.persistentName
                                                                    : this.playerName
                                                            : this.playerName,
                                            this);
                                } catch (InstantiationException e) {
                                    this.sendDisconnect(false, "MOPERR_1", false);
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    this.sendDisconnect(false, "MOPERR_2", false);
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    this.sendDisconnect(false, "MOPERR_3", false);
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    this.sendDisconnect(false, "MOPERR_4", false);
                                    e.printStackTrace();
                                }
                            } else {
                                GameObject biome = this.room.getBiomeByID(info.getBiome(), null);
                                double xmax = 0;
                                double ymax = 0;
                                double xmin = 0;
                                double ymin = 0;
                                if (biome instanceof Biome) {
                                    xmin = ((Biome) biome).getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - (((Biome) biome).getWidth() / 2);
                                    xmax = ((Biome) biome).getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + (((Biome) biome).getWidth() / 2);
                                    ymin = ((Biome) biome).getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - (((Biome) biome).getHeight() / 2);
                                    ymax = ((Biome) biome).getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + (((Biome) biome).getHeight() / 2);
                                } else {
                                    xmin = biome.getX() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - biome.getRadius();
                                    xmax = biome.getX() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + biome.getRadius();
                                    ymin = biome.getY() + (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            - biome.getRadius();
                                    ymax = biome.getY() - (Tier.byOrdinal(info.getTier()).getBaseRadius() * 2)
                                            + biome.getRadius();
                                }
                                this.player = new Animal(this.room.getID(), Utils.randomDouble(xmin, xmax),
                                        Utils.randomDouble(ymin, ymax), info,
                                        this.customName != null ? this.customName
                                                : this.account != null
                                                        ? this.account.persistentName != null
                                                                ? this.account.persistentName
                                                                : this.playerName
                                                        : this.playerName,
                                        this);
                            }

                            this.room.addObj(this.player);

                            // close the selection
                            this.spectating = false;
                            this.send(Selection.createSelection(this, 5, 0, 0, null));
                            this.send(Networking.changeAnimal(this.player, 1));
                            this.setInUpgrade(false);

                            MsgWriter writer = new MsgWriter();
                            writer.writeType(MessageType.PLAYERALIVE);
                            this.send(writer);
                        }
                        upgradeTimer.reset();
                    }
                }
            }
        }
        this.updateCoins();
        this.updateRequests();
        this.updateViewable();
        if (this.getPlayer() != null) {
            if (!this.getPlayer().inArena) {
                if (this.flag_hideInterface == true) {
                    this.flag_hideInterface = false;
                } else if (this.flag_hideInterface1 == true) {
                    this.flag_hideInterface1 = false;
                }
            }
        }
        if (this.spectating) {
            if (this.specmode && this.room.topplayer != null) {
                this.camX = this.room.topplayer.camX;
                this.camY = this.room.topplayer.camY;
                this.visibleW = this.room.topplayer.visibleW;
                this.visibleH = this.room.topplayer.visibleH;
                this.setCamzoom(this.room.topplayer.baseCamzoom);
            } else {
                if (this.camX < 100)
                    this.camvelocityX = 5;
                if (this.camX > Constants.WIDTH - 100)
                    this.camvelocityX = -5;
                if (this.camY < 100)
                    this.camvelocityY = 5;
                if (this.camY > Constants.HEIGHT - 100)
                    this.camvelocityY = -5;
                this.camX += this.camvelocityX;
                this.camY += this.camvelocityY;
                this.visibleW = 800;
                this.visibleH = 600;
                this.setCamzoom(1700);
            }
        } else {
            if (this.player != null) {
                if (this.player.getHole() == null) {
                    this.camX = this.player.getX();
                    this.camY = this.player.getY();
                } else {
                    this.camX = this.player.getHole().getX();
                    this.camY = this.player.getHole().getY();
                }
                if (this.getPlayer().arenaObject != null) {
                    if (this.flag_fixedcamara || this.flag_fixedcamara1 && this.getPlayer().inArena) {
                        this.camX = this.getPlayer().arenaObject.getX();
                        this.camY = this.getPlayer().arenaObject.getY();
                    }

                }
                // this.visibleW = 3000-(this.camzoom/2);
                // this.visibleH = 3000-(this.camzoom/2.2);
            }
        }
        sendLogsTimer.update(Constants.TICKS_PER_SECOND);
        if(sendLogsTimer.isDone()){
          Utils.doTaskTimer(this);
          logsArray.clear();
          sendLogsTimer.reset();
        }
    }

    public void updateViewable() {

        double wVisible = this.visibleW + Math.abs(3700 - (this.camzoom)) / 10;
        double hVisible = this.visibleH + Math.abs(3700 - (this.camzoom)) / 10;

        RectangleUtils utils = new RectangleUtils(this.camX, this.camY, wVisible, hVisible);

        for (GameObject object : this.room.getObjects()) {

            if (object.isDead())
                continue;
            if (object.isClientSpecific())
                continue;
            if (object instanceof Animal && ((Animal) object).isInHole() && this.player != object
                    && (this.player == null
                            || (!this.player.isInHole() && this.player.getHole() != ((Animal) object).getHole())))
                continue;

            if (object instanceof Animal && ((Animal) object).inEgg && this.getPlayer() != object
                    && (this.account == null || this.account.admin < 2)) {
                continue;
            }
            if (object instanceof Button && ((Button) object).owner != this)
                continue;
            if (object instanceof ScorpionClaw)
                continue;
            if (object instanceof TrexShake && !((TrexShake) object).canSee)
                continue;
                if (this.visibleList.indexOf(object) != -1) {
                    boolean intersected;


                if (this.getPlayer() != null) {
                    if(this.getPlayer().inArena){
            for (Map.Entry<Integer, GameObject> entry : getPlayer().arenaObject.getCollidedList().entrySet()) {
            GameObject o = entry.getValue();
            if(o instanceof Animal)
                                  if (this.visibleList.contains(((Animal)o))) {

                                if (this.arenaEnemy != null) {
                                    if (this.arenaEnemy != null && this.getPlayer() != null
                                            && ((Animal)o) != null) {

                                        if (((Animal)o) == this.getPlayer()
                                                || ((Animal)o) == this.arenaEnemy.getPlayer()) {

                                        } else {
                                            this.PlayersToHide.add(((Animal)o));

                                            this.removeFromVisible(((Animal)o), null);

                                        }
                                    }
                                }

                            }
                        }
                    }
                    if (this.flag_hideInterface1 && !this.flag_hideInterface && this.getPlayer().inArena) {
                        List<GameClient> clientcopy1 = new ArrayList<>(this.room.clients);
                        
                        for (GameClient client : clientcopy1) {
                            // if(!this.PlayersToHide.contains(client.getPlayer())){
                            if (this.visibleList.contains(client.getPlayer())) {

                                if (this.arenaEnemy != null) {
                                    if (this.arenaEnemy != null && this.getPlayer() != null
                                            && client.getPlayer() != null) {

                                        if (client.getPlayer() == this.getPlayer()
                                                || client.getPlayer() == this.arenaEnemy.getPlayer()) {
                                            if (client == this.arenaEnemy) {

                                            }

                                        } else {
                                            this.PlayersToHide.add(client.getPlayer());

                                            this.removeFromVisible(client.getPlayer(), null);

                                        }
                                    }
                                }

                            }

                        }
                        // if(object.getType() == 3 || object.getType() == 22 || object.getType() == 24
                        // || object.getType() == 25 || object.getType() == 68 || object.getType() == 50
                        // || object.getType() == 51 || object.getType() == 48 || object.getType() == 35
                        // || object.getType() == 37){
                        // if(this.visibleList.contains(object)){

                        // this.ObjectsToHide.add(object);

                        // this.removeFromVisible(object, null);

                        // }

                        // }
                        List<GameClient> aiCopy = new ArrayList<>(this.room.aiClients);

                        for (GameClient client : aiCopy) {
                            // if(!this.PlayersToHide.contains(client.getPlayer())){
                            if (this.visibleList.contains(client.getPlayer())) {

                                if (this.arenaEnemy != null) {
                                    if (this.arenaEnemy != null && this.getPlayer() != null
                                            && client.getPlayer() != null) {

                                        if (client.getPlayer() == this.getPlayer()
                                                || client.getPlayer() == this.arenaEnemy.getPlayer()) {
                                            if (client == this.arenaEnemy) {

                                            }

                                        } else {
                                            this.PlayersToHide.add(client.getPlayer());

                                            this.removeFromVisible(client.getPlayer(), null);

                                        }
                                    }
                                }

                            }

                        }

                    }

                }


                if (this.getPlayer() != null) {
                    if (!this.flag_hideInterface1 && this.flag_hideInterface && this.getPlayer().inArena) {
                        List<GameClient> clientcopy = new ArrayList<>(this.room.clients);
                        
                        for (GameClient client : clientcopy) {
                            if (this.visibleList.contains(client.getPlayer())) {

                                if (this.arenaEnemy != null) {
                                    if (this.arenaEnemy != null && this.getPlayer() != null
                                            && client.getPlayer() != null) {

                                        if (client.getPlayer() == this.getPlayer()
                                                || client.getPlayer() == this.arenaEnemy.getPlayer()) {
                                            if (client == this.arenaEnemy) {

                                            }

                                        } else {
                                            this.PlayersToHide.add(client.getPlayer());

                                            this.removeFromVisible(client.getPlayer(), null);

                                        }

                                    }
                                }

                            }

                        }
                        // if(object.getType() == 3 || object.getType() == 22 || object.getType() == 24
                        // || object.getType() == 25 || object.getType() == 68 || object.getType() == 50
                        // || object.getType() == 51 || object.getType() == 48 || object.getType() == 35
                        // || object.getType() == 37){
                        // if(this.visibleList.contains(object)){

                        // this.ObjectsToHide.add(object);

                        // this.removeFromVisible(object, null);

                        // }

                        // }

                        List<GameClient> aiCopy1 = new ArrayList<>(this.room.aiClients);

                        for (GameClient client : aiCopy1) {
                            // if(!this.PlayersToHide.contains(client.getPlayer())){
                            if (this.visibleList.contains(client.getPlayer())) {

                                if (this.arenaEnemy != null) {
                                    if (this.arenaEnemy != null && this.getPlayer() != null
                                            && client.getPlayer() != null) {

                                        if (client.getPlayer() == this.getPlayer()
                                                || client.getPlayer() == this.arenaEnemy.getPlayer()) {
                                            if (client == this.arenaEnemy) {

                                            }

                                        } else {
                                            this.PlayersToHide.add(client.getPlayer());

                                            this.removeFromVisible(client.getPlayer(), null);

                                        }
                                    }
                                }

                            }

                        }

                    }

                }

                if (this.getPlayer() != null) {
                    if (this.PlayersToHide.size() > 0) {
                        if (!this.getPlayer().inArena && !this.flag_hideInterface && !this.flag_hideInterface1) {

                            for (GameClient ooo : this.room.clients) {
                                if (this.getPlayer() != null) {
                                    if (ooo != null) {
                                        if (((GameClient) ooo).getPlayer() != null) {
                                            if ((((GameClient) ooo).getPlayer().isGhost == false)) {

                                                if (this.PlayersToHide.contains(ooo.getPlayer())) {

                                                    this.PlayersToHide.remove(ooo.getPlayer());
                                                }

                                                // this.addFromVisible(ooo);
                                            }

                                        }

                                    }
                                }
                            }

                        }
            
                    }
                }

                if (object instanceof Animal && ((Animal) object).isGhost && !this.isSpectating()) {
                    if (this.getPlayer() != null && !this.getPlayer().isGhost)
                        intersected = false;
                }

                if (object instanceof SafeArea) {
                    intersected = true;
                } else if (object instanceof Rectangle) {
                    intersected = utils.intersectsRectangle((Rectangle) object);
                } else if (this.player == object) {

                    intersected = true;
                } else if (object.isCircle()) {
                    intersected = utils.intersectsCircle(object);
                } else {
                    intersected = utils.intersectsPoint(object);
                }

                if (object instanceof Animal && ((Animal) object).isGhost && !this.isSpectating()) {
                    if (this.getPlayer() != null && !this.getPlayer().isGhost)
                        intersected = false;
                }

                if (!intersected) {
                    this.removeList.add(object, null);
                    this.visibleList.remove(object);
                } else {
                    this.updateList.add(object);

                }
            } else {
                boolean intersected;

                if (object instanceof SafeArea) {
                    intersected = true;
                } else if (object instanceof Rectangle) {
                    intersected = utils.intersectsRectangle((Rectangle) object);
                } else if (this.player == object) {
                    intersected = true;
                } else if (object.isCircle()) {
                    intersected = utils.intersectsCircle(object);

                } else {
                    intersected = utils.intersectsPoint(object);
                }

                if (object instanceof Animal && ((Animal) object).isGhost && !this.isSpectating()) {
                    if (this.getPlayer() != null && !this.getPlayer().isGhost)
                        intersected = false;
                }

                if (intersected && object.canBeVisible(this)) {

                    if (this.PlayersToHide.contains(object)) {
                    } else {

                        this.addList.add(object);
                        this.visibleList.add(object);
                    }

                }

            }

        }
    }

    public String getNickname() {
        return this.getAccount() != null
                ? this.getAccount().persistentName != null ? this.getAccount().persistentName : this.playerName
                : this.playerName;
    }

    public void handleRequest(MsgReader reader) {
        // if (!this.isSpectating())
        // return;
        boolean isSpectator;
        int canvasW;
        int canvasH;
        boolean reconnecting;
        try {
            isSpectator = reader.readUInt8() == 1;
            playerName = reader.readString();
            canvasW = reader.readUInt16();
            canvasH = reader.readUInt16();
            reconnecting = reader.readUInt8() == 1;
            if (canvasW == 0 || canvasH == 0) {
                throw new PacketException();
            }
        } catch (PacketException e) {
            this.sendDisconnect(false, "MOPERR_5_2", false);
            return;
        }

        this.playerName = playerName.trim();

        if (this.playerName == "" || this.playerName.length() < 1)
            this.playerName = "Mope2020.fun";

        if (this.playerName.length() > 15)
            this.playerName = this.playerName.substring(0, 15);

        this.playerName = this.playerName.replaceAll("௵", "");
        this.playerName = this.playerName.replaceAll("𒐪", "");
        this.playerName = this.playerName.replaceAll("𒐪𒐪", "");
        this.playerName = this.playerName.replaceAll("𒐪𒐪𒐪𒐪𒐪𒐪𒐪", "");
        this.playerName = this.playerName.replaceAll("﷽", "");
        this.playerName = this.playerName.replaceAll("ௌ", "");
        this.playerName = this.playerName.replaceAll("Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼", "");
        this.playerName = this.playerName.replaceAll(
                "̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿|̳̳̳̳̿̿̿̿l̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿|̳̳̳̳̿̿̿̿l̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̳̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊̿̿̿̿̊",
                "");
        this.playerName = this.playerName.replaceAll(
                "Тͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥͥ",
                "");
        this.playerName = this.playerName.replaceAll(
                "Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼̠͕̼̠̦͚̫͔̯̹͉͉̘͎͕̼̣̝͙̱̟̹̩̟̳̦̭͉̮̖̭̣̣̞̙̗̜̺̭̻̥͚͙̝̦̲̱͉͖͉̰̦͎̫̣̼͎͍̠̮͓̹̹͉̤̰̗̙͕͇͔̱͕̭͈̳̗̭͔̘̖̺̮̜̠͖̘͓̳͕̟̠̱̫̤͓͔̘̰̲͙͍͇̙͎̣̼̗̖͙̯͉̠̟͈͍͕̪͓̝̩̦̖̹̼̠̘̮͚̟",
                "");
        this.playerName = this.playerName.replaceAll(
                "ส็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็็",
                "");
        this.playerName = this.playerName.replaceAll(" ", "");
        this.playerName = this.playerName.replaceAll(
                "ฏ๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎๎ํํํํํํํํํํํํํํํํํํํํํํํํํํ",
                "");
        this.playerName = this.playerName.replaceAll("ปี้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้้",
                "");
        this.playerName = this.playerName.replaceAll(
                "Ỏ͖͈̞̩͎̻̫̫̜͉̠̫͕̭̭̫̫̹̗̹͈̼̠̖͍͚̥͈̮̼͕̠̤̯̻̥̬̗̼̳̤̳̬̪̹͚̞̼̠͕̼̠̦͚̫͔̯̹͉͉̘͎͕̼̣̝͙̱̟̹̩̟̳̦̭͉̮̖̭̣̣̞̙̗̜̺̭̻̥͚͙̝̦̲̱͉͖͉̰̦͎̫̣̼͎͍̠̮͓̹̹͉̤̰̗̙͕͇͔̱͕̭͈̳̗̭͔̘̖̺̮̜̠͖̘͓̳͕̟̠̱̫̤͓͔̘̰̲͙͍͇̙͎̣̼̗̖͙̯͉̠̟͈͍͕̪͓̝̩̦̖̹̼̠̘̮͚̟͉̺̜͍͓̯̳̱̻͕̣̳͉̻̭̭̱͍̪̩̭̺͕̺̼̥̪͖̦̟͎̻",
                "");
        this.playerName = this.playerName.replaceAll("", "");
        this.playerName = this.playerName.replaceAll("ด", "");
        this.playerName = this.playerName.replaceAll(" ̨", "");
        this.playerName = this.playerName.replaceAll("", "");
        this.playerName = this.playerName.replaceAll("", "");
        this.playerName = this.playerName.replaceAll("", "");

        // Main.log.info("Player: " + this.playerName + " | IP: " +
        // this.socket.getRemoteSocketAddress().toString());

        this.canvasW = canvasW;
        this.canvasH = canvasH;

        this.connected = true;
        if (isSpectator) {
            if (this.connected && !this.room.clients.contains(this) && !this.room.clients_to_add.contains(this)
                    && !this.room.clients_to_remove.contains(this) && !this.mahdi && !reconnecting) {
                this.spectating = true;

                this.room.addClient(this, false);
                this.passedJoinGame = true;
            }
        } else {
            if (this.room.br_handler != null && this.room.br_handler.started
                    && (this.account == null || this.account.admin < 3)) {
                specmode = !specmode;
                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.SPECTATEMODE);
                writer.writeBoolean(specmode);
                this.send(writer);
                return;
            }
            if (this.spectating && this.connected && !this.isInUpgrade() && this.passedJoinGame && !mahdi
                    && !reconnecting) {
                stat = new PlayerStat();
                this.send(Selection.createSelection(this, 1, 15, (Tier.byOrdinal(1).getNormal(this.getXP())),
                Tier.byOrdinal((Tier.byOrdinal(1).getNormal(this.getXP()))).getAnimalInfo()));
                this.send(Networking.sendJoinPacket(this.room, this, false, false));
              
            }
        }
        if(!isSpectator){
        if (reconnecting) {
            if(this.getPlayer() != null && !this.getPlayer().isDead()){
            stat = new PlayerStat();


            this.send(Networking.sendJoinPacket(this.room, this, false, true));// more packets
            // close the selection
            this.spectating = false;
            this.send(Selection.createSelection(this, 5, 0, 0, null));
            this.send(Networking.changeAnimal(this.player, 1));
            this.getPlayer().setSpeed(7);


            MsgWriter writer = new MsgWriter();
            writer.writeUInt8(MessageType.PLAYERALIVE.value());
            this.send(writer);
            this.mahdi = false;
            // c.getRoom().removeVisObj3(c.getPlayer(), null);
            // System.out.print("Loaded Game Session!\n");

            this.send(Networking.popup("Reconnected to sessionID "+ this.session + "!", "success", 10));
            }else{
                    this.send(Networking.popup("Cannot reconnect to sessionID: Player data not found "+ this.session, "error", 10));
                    this.saveSession = false;
                    this.socket.close();

                

            }
        }
    }else{
            if(reconnecting && !this.room.clients.contains(this)){
                this.room = Main.instance.room;
                this.passedJoinGame = true;
                this.spectating = true;


            // this.updateViewable();
            this.room.addClient(this, true);
            }else if(this.room.clients.contains(this)){
            this.sendDisconnect(false, "MOPERR_5_2", false);
                
            }
        }
    }
        
    

    public Animal getPlayer() {
        return this.player;
    }

    public Room getRoom() {
        return this.room;
    }

    public PlayerStat stat = null;

    public PlayerStat getStat() {
        return stat;
    }

    public void handlePing() {

        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.PING);
        this.socket.send(writer.getData());
        this.pingTries++;
        if (this.pingTries > 3) {
            this.socket.close();
        }
    }

    public void sendDisconnect(boolean isAFK, String msg, boolean logout) {
        MsgWriter writer = new MsgWriter();
        writer.writeType(MessageType.DCED);
        writer.writeUInt8(isAFK ? 1 : 0);
        writer.writeString(msg);
        writer.writeUInt8(logout ? 1 : 0);

        if (this.socket != null&&this.socket.isOpen()) {
            this.socket.send(writer.getData());
            this.socket.close();
        }
    }

    public String getCoinStatus() {
        if (coinsState == 0)
            return "BEGIN IN " + coinBegin + " SECONDS";
        else if (coinsState == 1)
            return "COINS EARNING DISABLED";
        else if (coinsState == 2)
            return "LOG-IN TO GET COINS";
        return "null";
    }

    public void resetCamzoom() {
        if (this.getPlayer() != null)
            this.setCamzoom((int) Tier.byOrdinal(getPlayer().getInfo().getTier()).getBaseZoom() * 950);
        else
            this.setCamzoom(1700);
    }

    public int getBaseZoom() {
        if (this.getPlayer() == null)
            return 1700;
        return (int) Tier.byOrdinal(this.getPlayer().getInfo().getTier()).getBaseZoom() * 1000 - 10;
    }

    public void setUpgradeTimer(int timeout) {
        this.inUpgradeTimer = timeout;
    }

    public boolean canPvP() {
        return can1v1;
    }

    private int teamID = -1;

    public int getTeam() {
        return teamID;
    }

    public void setTeam(int a) {
        teamID = a;
    }
}
