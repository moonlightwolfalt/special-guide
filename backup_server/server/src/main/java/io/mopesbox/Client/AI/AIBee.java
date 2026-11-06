package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.BeeHive;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Waypoint;
import io.mopesbox.World.Room;

public class AIBee extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private Timer liveTimer = new Timer(120000);
    private GameObject target;
    private boolean isDefender = false;
    private double fromx;
    private double fromy;
    private BeeHive beehive;
    private Timer returnTimer = null;

    public AIBee(Room room) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = " ";
    }

    public AIBee(Room room, double fromx, double fromy, BeeHive beehive, int time) {
        this(room);
        this.beehive = beehive;
        this.fromx = fromx;
        this.fromy = fromy;
        this.returnTimer = new Timer(time);
    }

    public AIBee(Room room, Animal target, double fromx, double fromy, BeeHive beehive) {
        this(room);
        this.beehive = beehive;
        this.fromx = fromx;
        this.fromy = fromy;
        this.isDefender = true;
        this.target = target;
        this.retargetingTimer = new Timer(15000);
        this.addx = Utils.randomDouble(-target.getRadius(), target.getRadius());
        this.addy = Utils.randomDouble(-target.getRadius(), target.getRadius());
    }

    @Override
    public void onHurt(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() <= 16) {
            this.target = o;
            this.retargetingTimer = new Timer(15000);
            this.addx = Utils.randomDouble(-target.getRadius(), target.getRadius());
            this.addy = Utils.randomDouble(-target.getRadius(), target.getRadius());
        }
    }

    private Timer imitateTimer = new Timer(2000);

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y) {
        AnimalInfo info;
        info = Animals.byID(67);

        if (info == null)
            return;
            
        if (info.getType().hasCustomClass()) {
            try {
                Class<?> rawAnimalClass = info.getType().getAnimalClass();
                                    
                @SuppressWarnings("unchecked")
                Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                        AnimalInfo.class, String.class, GameClient.class);                this.player = (Animal) aa.newInstance(
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
        if (target == null) {
            this.getPlayer().setMouseX(x);
            this.getPlayer().setMouseY(y);
        } else {
            this.getPlayer().setMouseX(target.getX());
            this.getPlayer().setMouseY(target.getY());
        }
    }

    private Timer retargTimer;

    private Waypoint waypoint;

    private void setWaypoint() {
        if(this.getPlayer() != null) {
            double x = Utils.randomDouble(this.getPlayer().getX()-150, this.getPlayer().getX()+150);
            double y = Utils.randomDouble(this.getPlayer().getY()-150, this.getPlayer().getY()+150);
            waypoint = new Waypoint(x, y);
            if (retargTimer != null) retargTimer.reset();
            retargTimer = new Timer(5000);
        }
    }

    private void setReturning() {
        if(this.getPlayer() != null) {
            if (this.beehive != null && !this.beehive.isDead()) waypoint = new Waypoint(this.beehive.getX(), this.beehive.getY());
            else waypoint = new Waypoint(fromx, fromy);
        }
    }

    @Override
    public void send(MsgWriter writer) {
        //
    }

    @Override
    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;
            
            this.setXp(this.getXP()/2);

            if (killer != null && killer instanceof Animal) 
                ((Animal)killer).getClient().addXp(this.getXP());

            this.shouldRemove(true);
        }
    }

    private Timer retargetingTimer = null;

    private double addx;
    private double addy;

    private boolean returning = false;

    public void attack() {
        if(target != null && this.getPlayer() != null) {
            double dy = target.getY() - this.getPlayer().getY();
            double dx = target.getX() - this.getPlayer().getX();
            double theta = Math.atan2(dy, dx);
            theta *= 180 / Math.PI;
            if (theta < 0) {
                theta += 360;
            }
            theta -= 180;
            if(theta < 0) theta += 360;
            if(((Bee) this.getPlayer()).hasSting) {
                this.getPlayer().setAngle(theta);
                this.getPlayer().useAbility();
            }
        }
    }

    @Override
    public void update() {
        if(this.getPlayer() != null) {
            if(target != null) {
                if(((Animal) target).getClient().getPlayer() != ((Animal) target) || target.isDead()) target = null;
                else {
                    imitateTimer.update(Constants.TICKS_PER_SECOND);
                    if(imitateTimer.isDone()) {
                        this.addx = Utils.randomDouble(-target.getRadius(), target.getRadius());
                        this.addy = Utils.randomDouble(-target.getRadius(), target.getRadius());
                        imitateTimer.reset();
                    }
                    if(!this.getPlayer().getInfo().getAbility().isUsable() && !this.getPlayer().isInvincible()) {
                        if(waypoint != null) {
                            this.getPlayer().setMouseX(this.waypoint.getX());
                            this.getPlayer().setMouseY(this.waypoint.getY());
                            if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 1) {
                                setWaypoint();
                            } else {
                                if(retargTimer != null) {
                                    retargTimer.update(Constants.TICKS_PER_SECOND);
                                    if(retargTimer.isDone()) {
                                        setWaypoint();
                                    }
                                } else setWaypoint();
                            }
                        } else setWaypoint();
                    } else {
                        this.getPlayer().setMouseX(target.getX()+addx);
                        this.getPlayer().setMouseY(target.getY()+addy);
                        if (retargetingTimer != null) {
                            retargetingTimer.update(Constants.TICKS_PER_SECOND);
                            if (retargetingTimer.isDone()) {
                                target = null;
                                retargetingTimer.reset();
                                retargetingTimer = null;
                                setWaypoint();
                            }
                        }
                    }
                    if(target != null) {
                        if (!((Animal) target).isInHole() && ((Bee) this.getPlayer()).canAbility() && Utils.distance(this.getPlayer().getX(), target.getX(), this.getPlayer().getY(), target.getY()) < target.getRadius()*2) {
                            attack();
                        }
                    }
                }
            } else if (!this.isDefender) {
                if (waypoint != null) {
                    this.getPlayer().setMouseX(this.waypoint.getX());
                    this.getPlayer().setMouseY(this.waypoint.getY());
                    if (this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 1) {
                        setWaypoint();
                    } else {
                        retargTimer.update(Constants.TICKS_PER_SECOND);
                        if(retargTimer.isDone()) {
                            setWaypoint();
                        }
                    }
                } else setWaypoint();
            } else if (this.isDefender) {
                setReturning();
                this.getPlayer().setMouseX(this.waypoint.getX());
                this.getPlayer().setMouseY(this.waypoint.getY());
                if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 10) {
                    this.killPlayer(0, beehive);
                    return;
                }
            }
            if (returning) {
                setReturning();
                this.getPlayer().setMouseX(this.waypoint.getX());
                this.getPlayer().setMouseY(this.waypoint.getY());
                if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 10) {
                    this.killPlayer(0, beehive);
                    return;
                }
            }
            if (this.returnTimer != null) {
                this.returnTimer.update(Constants.TICKS_PER_SECOND);
                if(this.returnTimer.isDone()) {
                    this.returning = true;
                    this.returnTimer.reset();
                    this.returnTimer = null;
                }
            }
            this.getPlayer().setWater(this.getPlayer().maxwater);
            if (this.getRequests().size() > 0) {
                this.clearRequests();
            }
            updateRequests();
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if (liveTimer.isDone()) {
                this.killPlayer(1, null);
                liveTimer.reset();
            }
        }
    }
}