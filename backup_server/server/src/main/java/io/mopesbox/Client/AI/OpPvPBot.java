package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.PvPRequest;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class OpPvPBot extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private boolean notWelcome;

    public OpPvPBot(Room room, String nick) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = nick;
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y, int type, int skin) {
        AnimalInfo info;
        info = Animals.byID(type);

        if (info == null)
            return;

        info.setSkin(skin);

        if (info.getType().hasCustomClass()) {
            try {
                Class<?> rawAnimalClass = info.getType().getAnimalClass();
                                    
                @SuppressWarnings("unchecked")
                Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                        AnimalInfo.class, String.class, GameClient.class);
                this.player = (Animal) aa.newInstance(
                        this.room.getID(), x, y, info, this.playerName, this);
            } catch (InstantiationException e) {
                e.printStackTrace();
                return;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
        } else {
            this.player = new Animal(this.room.getID(), x, y, info, this.playerName, this);
        }
        this.setXp(Tier.byOrdinal(info.getTier()).getStartXP());
        this.room.addObj(this.player);
        this.getPlayer().setMouseX(x);
        this.getPlayer().setMouseY(y);
    }

    private void pendingRequest(PvPRequest a) {
        if (this.getPlayer() != null && !this.getPlayer().flag_flying && this.getPlayer().inArena) {
            PvPArena newarena = new PvPArena(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(),
                    a.getEnemy(), this, this.room.getFightNumber(), this.room);
            this.room.addObj(newarena);
            this.room.sendChat(this, "ok now get ready to get destroyed by a bot lmao");
        }
    }

    @Override
    public void send(MsgWriter writer) {
        //
    }

    private Timer welcomeTimer = new Timer(500);

    @Override
    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;
            this.setXp(this.getXP() / 2);
            if (killer != null && killer instanceof Animal)
                ((Animal) killer).getClient().addXp(this.getXP());
            this.shouldRemove(true);
        }
    }

    // private Timer moveTimer = new Timer(300);
    private Timer moveTimer = new Timer(1);

    @Override
    public void update() {
        if (this.getPlayer() != null) {
            if (!notWelcome) {
                welcomeTimer.update(Constants.TICKS_PER_SECOND);
                if (welcomeTimer.isDone()) {
                    this.room.sendChat(this, "Hello!");
                    this.room.sendChat(this, "I am op pvp bot!");
                    this.room.sendChat(this, "I am basically unbeatable pvp bot!");
                    this.room.sendChat(this, "Use your PVP button and hover it to me to 1v1 and lose to me!");
                    notWelcome = true;
                    welcomeTimer.reset();
                }
            }
            if (this.getPlayer().inArena) {
                this.moveTimer.update(Constants.TICKS_PER_SECOND);
                this.getPlayer().setSpeed(35);
                if (moveTimer.isDone()) {
                    Animal enemy = ((PvPArena) this.getPlayer().arenaObject).getPlayer1();
                    if (enemy != null) {
                        double enemyX = enemy.getX();
                        double enemyY = enemy.getY();
                        double enemyTailX = enemyX
                                - ((Math.cos(Math.toRadians(enemy.getAngle())) * (enemy.getRadius())));
                        double enemyTailY = enemyY
                                - ((Math.sin(Math.toRadians(enemy.getAngle())) * (enemy.getRadius())));
                        double enemyMouthX = enemyX
                                + ((Math.cos(Math.toRadians(enemy.getAngle())) * (enemy.getRadius())));
                        double enemyMouthY = enemyY
                                + ((Math.sin(Math.toRadians(enemy.getAngle())) * (enemy.getRadius())));
                        if (Utils.distance(this.getPlayer().getX(), enemyTailX, this.getPlayer().getY(),
                                enemyTailY) > this.getPlayer().getRadius() / 2) {
                            // this.getPlayer().changeBoost(true);
                            this.getPlayer().setMouseX(enemyTailX);
                            this.getPlayer().setMouseY(enemyTailY);
                        } else {
                            // this.getPlayer().changeBoost(false);
                            this.getPlayer().setMouseX(enemyMouthX);
                            this.getPlayer().setMouseY(enemyMouthY);
                        }
                    }
                    moveTimer.reset();
                }
            } else {
                // this.getPlayer().changeBoost(false);
                double ang = this.getPlayer().getAngle() + 90;
                if (ang > 360)
                    ang -= 360;
                double x = ((Math.cos(Math.toRadians(ang)) * (this.getPlayer().getRadius())));
                double y = ((Math.sin(Math.toRadians(ang)) * (this.getPlayer().getRadius())));
                this.getPlayer().setMouseX(this.getPlayer().getX() + x);
                this.getPlayer().setMouseY(this.getPlayer().getY() + y);
            }
            if (this.getRequests().size() > 0 && !this.getPlayer().inArena) {
                for (PvPRequest a : this.getRequests()) {
                    this.pendingRequest(a);
                }
                this.clearRequests();
            }
            updateRequests();
        }
    }
}