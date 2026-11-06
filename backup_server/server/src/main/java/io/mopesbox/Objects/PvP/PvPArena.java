package io.mopesbox.Objects.PvP;

// import java.lang.constant.Constable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import io.mopesbox.Objects.ETC.Button;
import io.mopesbox.Networking.Networking;

public class PvPArena extends Ability {
    private GameClient p1;
    private GameClient p2;
    public int arenaState = 0;
    private int fightNumber;
    private double arenaRadius;
    private int fightType = 0;
    private int round = 0;
    private int p1Bites = 0;
    private int p2Bites = 0;
    private int p1Wonrounds = 0;
    private int p2Wonrounds = 0;
    private double arenaRadiusMulti;
    private int timerSpeed = 1;
    private int arenaInnerStage = 0;
    private boolean resetArena = false;
    private int timer = 23;
    private int winner = 0;
    private int timeLeft = 1000;
    private int winnerBonus = 100000;
    private String winnermsg = "GOOD JOB!";
    private double nextArenaRadius;
    private Room room;
    private boolean isShrinking = false;
    private boolean isGrowing = false;
    private Timer ButtonTimer = new Timer(10000);



    public PvPArena(int id, double x, double y, GameClient p1, GameClient p2, int fightNumber, Room room) {
        super(id, x, y, 68, Math.max(Math.max(p1.getPlayer().getRadius(), p2.getPlayer().getRadius()), 300), 0); // Math.max(Math.max(p1.getPlayer().getRadius(),
                                                                                                                 // p2.getPlayer().getRadius()),
                                                                                                                 // 300)
        this.p1 = p1;
        this.p2 = p2;
        this.room = room;
        this.arenaRadiusMulti = 87;
        this.setRadiusArena();
        this.timer = 15; // 23
        this.fightNumber = fightNumber;
        this.setCollideable(false);
        this.setCollideType(1);
        this.setCollideCallbacking(true);

        // setup
        // int rad = Math.round(this.getRadius()/4);

        // if (p1.getPlayer().getInfo().getTier() < 17 &&
        // p2.getPlayer().getInfo().getTier() < 17) {
        // int rad = Math.round(p1.getPlayer().getBaseRadius());
        // }
        p1.getPlayer().setRadius(90);
        p2.getPlayer().setRadius(90);

        p1.getPlayer().health = p1.getPlayer().maxHealth;
        p2.getPlayer().health = p2.getPlayer().maxHealth;
        p1.getPlayer().setSpeed(10);
        p2.getPlayer().setSpeed(10);
        p1.getPlayer().setDivePossible(false);
        p2.getPlayer().setDivePossible(false);
        p1.getPlayer().setWater(p1.getPlayer().maxwater);
        p2.getPlayer().setWater(p2.getPlayer().maxwater);
        p1.getPlayer().playerNum = 1;
        p2.getPlayer().playerNum = 2;
        p1.getPlayer().inArena = true;
        p2.getPlayer().inArena = true;
        p1.getPlayer().setImmunity(10);
        p2.getPlayer().setImmunity(10);
        p1.getPlayer().arenaObject = this;
        p2.getPlayer().arenaObject = this;
        p1.setCamzoom(1200);
        p2.setCamzoom(1200);
        p1.getPlayer().getInfo().getAbility().setPossible(false);
        p2.getPlayer().getInfo().getAbility().setPossible(false);
        p1.getPlayer().setSpeed(12);
        p2.getPlayer().setSpeed(12);
        p1.arenaEnemy = p2;
        p2.arenaEnemy = p1;


                  
        p1.FixedCamara = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), -80,
                        60, 150, 40, 0, p1.getRoom(), p1,
                        "Fixed Camera ", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "1");
                this.room.addObj(p1.FixedCamara);
                
       
                p1.not_FixedCamara = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), 180,
                        60, 150, 40, 0, p1.getRoom(), p1,
                        "Player Camera", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "2");
                this.room.addObj(p1.not_FixedCamara);

                p2.FixedCamara1 = new Button(p2.getRoom().getID(), p2.getPlayer().getX(), p2.getPlayer().getY(), -80,
                        60, 150, 40, 0, p2.getRoom(), p2,
                        "Fixed Camera ", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "1");
                this.room.addObj(p2.FixedCamara1);
       
                        p2.not_FixedCamara1 = new Button(p2.getRoom().getID(), p2.getPlayer().getX(), p2.getPlayer().getY(), 180,
                        60, 150, 40, 0, p2.getRoom(), p2,
                        "Player Camera", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "2");
                this.room.addObj(p2.not_FixedCamara1);
    
                p1.LightMode = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), 180,
                        60, 150, 40, 0, p1.getRoom(), p1,
                        "Light Mode", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "1");
       
                p2.LightMode1 = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), 180,
                        60, 150, 40, 0, p2.getRoom(), p2,
                        "Light Mode", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "2");

                p1.DarkMode = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), 180,
                        60, 150, 40, 0, p1.getRoom(), p1,
                        "Dark Mode", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "1");
       
                p2.DarkMode1 = new Button(p1.getRoom().getID(), p1.getPlayer().getX(), p1.getPlayer().getY(), 180,
                        60, 150, 40, 0, p2.getRoom(), p2,
                        "Dark Mode", 1, "#00ff30",
                        255, "#00fbff", "green", true,
                        "2");
    }

    public void addBite(Animal b) {
        if (p1.getPlayer() == b) {
            p1Bites++;
        } else if (p2.getPlayer() == b) {
            p2Bites++;
        }
    }

    private Timer timerArena = new Timer(1000);
    private boolean arenaEnded = false;
    private boolean isWallHurts = false;
    private Timer timerWall = new Timer(5000);

    public void remPlayer(GameClient a) {
        if (a == p1) {
            p1.getPlayer().inArena = false;
            p1.getPlayer().setRadius(120);
            p1.getPlayer().playerNum = 0;
            p1.getPlayer().setImmunity(15);
            p1.arenaEnemy = null;
            p1.setCamzoom((int) Tier.byOrdinal(p1.getPlayer().getInfo().getTier()).getBaseZoom() * 1000 - 100);
            p1.getPlayer().arenaObject = null;
            p1 = null;
        } else if (a == p2) {
            p2.getPlayer().inArena = false;
            p2.getPlayer().setRadius(120);
            p2.getPlayer().playerNum = 0;
            p2.getPlayer().setImmunity(15);
            p2.arenaEnemy = null;
            p2.setCamzoom((int) Tier.byOrdinal(p2.getPlayer().getInfo().getTier()).getBaseZoom() * 1000 - 100);
            p2.getPlayer().arenaObject = null;
            p2 = null;
        }
    }

    @Override
    public boolean getCollideable() {
        return false;
    }

    @Override
    public void update() {
        super.update();
    
        if ((p1 == null || p1.getPlayer() == null || p2 == null || p2.getPlayer() == null || p1.arenaEnemy == null
                || p2.arenaEnemy == null || p1.getPlayer().inArena == false || p2.getPlayer().inArena == false)
                && !arenaEnded) {
            arenaEnded = true;
            arenaState = 2;
            if (p1 == null || p1.getPlayer() == null || p1.arenaEnemy == null || p1.getPlayer().inArena == false)
                winner = 1;
            else
                winner = 0;
            if (p2 != null && p2.getPlayer() != null) {
                winnerBonus = 100000 - p2.getPlayer().getClient().getXP() / 10000;

            }
            if (p1 != null && p1.getPlayer() != null) {
                winnerBonus = 100000 - p1.getPlayer().getClient().getXP() / 10000;
            }
            if (winnerBonus > 0) {
                arenaState = 3;
                if (p2 != null && p2.getPlayer() != null) {
                    p2.addXp(winnerBonus);
                }
                if (p1 != null && p1.getPlayer() != null) {
                    p1.addXp(winnerBonus);
                }
            }
            timer = 10;

        } else {


            if(p1 != null && p1.getPlayer() != null){
                if(p1.getPlayer().getTier() <= 14){
                    p1.getPlayer().hurt(p1.getPlayer().maxHealth, 0, p2.getPlayer());
                    p1.send(Networking.popup("That animal is not allowed in PvP Arena.", "error", 5));
                }
            }
                 if(p2 != null&&p2.getPlayer() != null){
                if(p2.getPlayer().getTier() <= 14){
                    p2.getPlayer().hurt(p2.getPlayer().maxHealth, 0, p1.getPlayer());
                    p2.send(Networking.popup("That animal is not allowed in PvP Arena.", "error", 5));

                }
            }
            

            if (arenaState == 0) {
                p1.getPlayer().setSpeed(12);
                p2.getPlayer().setSpeed(12);
                p1.getPlayer().setX(this.getX() - (this.getRadius() - p1.getPlayer().getRadius()));
                p1.getPlayer().setY(this.getY());
                p2.getPlayer().setX(this.getX() + (this.getRadius() - p2.getPlayer().getRadius()));
                p2.getPlayer().setY(this.getY());

                ButtonTimer.update(Constants.TICKS_PER_SECOND);
                // if(ButtonTimer.isDone() && p1.FixedCamara != null && p1.not_FixedCamara != null && p2.FixedCamara1 != null && p2.not_FixedCamara1 != null ){
                //         this.room.removeObj(p1.FixedCamara, p1.getPlayer());
                //         this.room.removeObj(p1.FixedCamara1, p1.getPlayer());

                //         this.room.removeObj(p2.FixedCamara1, p2.getPlayer());
                //         this.room.removeObj(p2.not_FixedCamara1, p2.getPlayer());
                



                //     p1.FixedCamara = null;

                //       p1.hideInterFace = null;

                //          p2.FixedCamara1 = null;

                //       p2.hideInterFace1 = null;
                // }
                if(ButtonTimer.isDone() && p1.FixedCamara != null){
                    this.room.removeObj(p1.FixedCamara, p1.getPlayer());
                    p1.FixedCamara = null;

                }
                    if(ButtonTimer.isDone() && p1.not_FixedCamara != null){
                    this.room.removeObj(p1.not_FixedCamara, p1.getPlayer());
                    p1.not_FixedCamara = null;
                }
                           if(ButtonTimer.isDone() && p2.FixedCamara1 != null){
                    this.room.removeObj(p2.FixedCamara1, p2.getPlayer());
                    p2.FixedCamara1 = null;
                }

                           if(ButtonTimer.isDone() && p2.not_FixedCamara1 != null){
                    this.room.removeObj(p2.not_FixedCamara1, p2.getPlayer());
                    p2.not_FixedCamara1 = null;
                }

                  if(ButtonTimer.isDone() && p1.hideInterFace != null){
                    this.room.removeObj(p1.hideInterFace, p1.getPlayer());
                    p1.hideInterFace = null;

                }
                    if(ButtonTimer.isDone() && p1.not_hideInterFace != null){
                    this.room.removeObj(p1.not_hideInterFace, p1.getPlayer());
                    p1.not_hideInterFace = null;
                }
                           if(ButtonTimer.isDone() && p2.not_hideInterFace1 != null){
                    this.room.removeObj(p2.not_hideInterFace1, p2.getPlayer());
                    p2.not_hideInterFace1 = null;
                }

                           if(ButtonTimer.isDone() && p2.hideInterFace1 != null){
                    this.room.removeObj(p2.hideInterFace1, p2.getPlayer());
                    p2.not_hideInterFace1 = null;
                }
                
                timerArena.update(Constants.TICKS_PER_SECOND);
                if (timerArena.isDone()) {
                    timer--;
                    if (timer < 0) {
                        arenaState = 1;
                        timer = 1000;
                        timeLeft = 1000;
                        timerSpeed = 10;
                        arenaInnerStage = 0;
                    }
                    timerArena.reset();
                }
            } else if (arenaState == 1) {
                if (!isWallHurts) {
                    timerWall.update(Constants.TICKS_PER_SECOND);
                    if (timerWall.isDone()) {
                        isWallHurts = true;
                        timerWall.reset();
                    }
                }
                if (isWallHurts) {
                    double distance = Utils.distance(this.getX(), p1.getPlayer().getX(), this.getY(),
                            p1.getPlayer().getY());
                    if (distance > (this.arenaRadius / 100) - p1.getPlayer().getRadius()) {
                        p1.getPlayer().setFire(1, p2.getPlayer(), 0);
                    }
                    distance = Utils.distance(this.getX(), p2.getPlayer().getX(), this.getY(), p2.getPlayer().getY());
                    if (distance > (this.arenaRadius / 100) - p2.getPlayer().getRadius()) {
                        p2.getPlayer().setFire(1, p1.getPlayer(), 0);
                    }
                }
                if (isShrinking) {
                    if (!isGrowing) {
                        arenaRadius -= 100;
                        if (arenaRadius <= nextArenaRadius) {
                            arenaRadius = nextArenaRadius;
                            isShrinking = false;
                        }
                    } else if (isGrowing) {
                        arenaRadius += 100;
                        if (arenaRadius >= nextArenaRadius) {
                            arenaRadius = nextArenaRadius;
                            isShrinking = false;
                        }
                    }
                } else {
                    timerArena.update(Constants.TICKS_PER_SECOND);
                    if (timerArena.isDone()) {
                        timeLeft -= timerSpeed;
                        if (timeLeft < 1) {
                            arenaInnerStage++;
                            if (arenaInnerStage == 1) {
                                isShrinking = true;
                                isGrowing = false;
                                timeLeft = timer;
                                nextArenaRadius = arenaRadius / 1.5;
                            } else if (arenaInnerStage == 2) {
                                isShrinking = true;
                                isGrowing = false;
                                timeLeft = timer;
                                nextArenaRadius = arenaRadius / 1.5;
                            } else if (arenaInnerStage == 3) {
                                isShrinking = true;
                                isGrowing = false;
                                timeLeft = timer;
                                nextArenaRadius = 1000;
                            } else if (arenaInnerStage == 4) {
                                arenaState = 2;
                                timer = 10;
                                arenaEnded = true;
                                if (p1.getPlayer().health > p2.getPlayer().health)
                                    winner = 0;
                                else
                                    winner = 1;
                            }
                        }
                        timerArena.reset();
                    }
                }
            } else if (arenaState == 2 || arenaState == 3) {
                if (winner == 0) {
                    if (p2 != null && p2.getPlayer() != null) {
                        p2.killPlayer(14, p1 != null ? p1.getPlayer() != null ? p1.getPlayer() : null : null);
                        p2 = null;
                    } else if (p2 != null) {
                        p2 = null;
                    }
                    if (p1 != null && p1.getPlayer() != null) {
                        p1.getPlayer().health = p1.getPlayer().maxHealth;
                    }
                } else if (winner == 1) {
                    if (p1 != null && p1.getPlayer() != null) {
                        p1.killPlayer(14, p2 != null ? p2.getPlayer() != null ? p2.getPlayer() : null : null);
                        p1 = null;
                    } else if (p1 != null) {
                        p1 = null;
                    }
                    if (p2 != null && p2.getPlayer() != null) {
                        p2.getPlayer().health = p2.getPlayer().maxHealth;
                    }
                }
                timerArena.update(Constants.TICKS_PER_SECOND);
                if (timerArena.isDone()) {
                    timer--;
                    if (timer < 1) {
                        this.room.removeObj(this, null);
                        if (p1 != null && p1.getPlayer() != null) {
                            p1.addCoins(100);
                            p1.addEXP(50);
                            p1.getPlayer().setSpeed(6);
                            p1.getPlayer().inArena = false;
                            p1.getPlayer().setRadius(p1.getPlayer().getBaseRadius());
                            p1.getPlayer().playerNum = 0;
                            p1.getPlayer().setImmunity(15);
                            p1.getPlayer().health = p1.getPlayer().maxHealth;
                            p1.arenaEnemy = null;
                            p1.getPlayer().getInfo().getAbility().setPossible(true);
                            p1.setCamzoom(p1.baseCamzoom);
                            p1.addPvPWin();
                            p1.getPlayer().arenaObject = null;
                            if (p1.getPlayer().getTier() != 17) {
                                p1.getPlayer().setDivePossible(true);
                            }
                            p1.flag_hideInterface = false;
                            p1.flag_fixedcamara = false;
                            
                        }
                        if (p2 != null && p2.getPlayer() != null) {
                            p2.addCoins(100);
                            p2.addEXP(50);
                            p2.getPlayer().setSpeed(6);
                            p2.getPlayer().inArena = false;
                            p2.getPlayer().setRadius(p2.getPlayer().getBaseRadius());
                            p2.getPlayer().playerNum = 0;
                            p2.getPlayer().setImmunity(15);
                            p2.getPlayer().health = p2.getPlayer().maxHealth;
                            p2.arenaEnemy = null;
                            p2.getPlayer().getInfo().getAbility().setPossible(true);
                            p2.setCamzoom(p2.baseCamzoom);
                            p2.addPvPWin();
                            p2.getPlayer().arenaObject = null;
                            if (p2.getPlayer().getTier() != 17) {
                                p2.getPlayer().setDivePossible(true);
                            }
                            p2.flag_hideInterface = false;
                            p2.flag_fixedcamara = false;
                        }
                    }
                    timerArena.reset();
                }
            }
        }
    }

    public Animal getPlayer1() {
        return p1 != null && p1.getPlayer() != null ? p1.getPlayer() : null;
    }

    public Animal getPlayer2() {
        return p2 != null && p2.getPlayer() != null ? p2.getPlayer() : null;
    }

    private void setRadiusArena() {
        this.arenaRadius = this.getRadius() * arenaRadiusMulti; // this.getRadius()*arenaRadiusMulti
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt32(p1 != null ? p1.getPlayer() != null ? p1.getPlayer().getId() : 0 : 0); // p1 id
        writer.writeUInt32(p2 != null ? p2.getPlayer() != null ? p2.getPlayer().getId() : 0 : 0); // p2 id
        writer.writeString(
                p1 != null ? p1.getPlayer() != null ? p1.getPlayer().getPlayerName() : "mope.io" : "mope.io"); // p1
                                                                                                               // nick
        writer.writeString(
                p2 != null ? p2.getPlayer() != null ? p2.getPlayer().getPlayerName() : "mope.io" : "mope.io"); // p2
                                                                                                               // nick
        writer.writeUInt16(p1 != null ? p1.getPvPWins() : 0); // p1 wins
        writer.writeUInt16(p2 != null ? p2.getPvPWins() : 0); // p2 wins
        writer.writeUInt16(fightNumber); // fight number
        writer.writeUInt8(arenaState); // arena state
        if (arenaRadius < 100)
            arenaRadius = 100;
        writer.writeUInt16((short) arenaRadius); // arena rad
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(arenaState); // arena state
        writer.writeUInt16(fightNumber); // fight number
        writer.writeUInt8(fightType); // fight type
        writer.writeUInt8(round); // rounds
        if (arenaRadius < 100)
            arenaRadius = 100;
        writer.writeUInt16((short) arenaRadius); // arena rad
        writer.writeUInt16(p1Bites); // p1 bites
        writer.writeUInt16(p2Bites); // p2 bites
        writer.writeUInt8(p1Wonrounds); // p1 won rounds
        writer.writeUInt8(p2Wonrounds); // p2 won rounds
        writer.writeBoolean(resetArena); // reset arena
        if (arenaState == 0) {
            writer.writeUInt16(timer * 100); // timer
        } else if (arenaState == 1) {
            writer.writeUInt16(timer * 100); // timer
            writer.writeUInt16(timeLeft); // time left
        } else if (arenaState == 2) {
            writer.writeUInt8(winner == 1 ? 0 : 1); // winner 1
            writer.writeString(winnermsg); // winner msg
        } else if (arenaState == 3) {
            writer.writeUInt8(winner == 1 ? 0 : 1); // winner
            writer.writeUInt32(winnerBonus); // winner bonus
            writer.writeString(winnermsg); // winner msg
        }
    }
}
