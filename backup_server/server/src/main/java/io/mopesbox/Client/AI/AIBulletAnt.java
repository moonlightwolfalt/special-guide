package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier5.BulletAnt;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.AntHill;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Waypoint;
import io.mopesbox.World.Room;

public class AIBulletAnt extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private Timer liveTimer = new Timer(120000);
    private GameObject target;
    private boolean isDefender = false;
    private double fromx;
    private double fromy;
    private AntHill antHill;
    private Timer returnTimer = null;

    public AIBulletAnt(Room room) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = " ";
    }

    public AIBulletAnt(Room room, double fromx, double fromy, AntHill antHill, int time) {
        this(room);
        this.antHill = antHill;
        this.fromx = fromx;
        this.fromy = fromy;
        this.returnTimer = new Timer(time);
    }

    public AIBulletAnt(Room room, Animal target, double fromx, double fromy, AntHill antHill) {
        this(room);
        this.antHill = antHill;
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
        if(o != null && o instanceof Animal && this.getPlayer() != null){
        if(o instanceof Animal && ((this.getPlayer().getInfo().getSkin() == 0 && ((Animal)o).getInfo().getTier() <= 9) || (this.getPlayer() != null &&this.getPlayer().getInfo().getSkin() == 1 && ((Animal)o).getInfo().getTier() <= 12))) {
            this.target = o;
            this.retargetingTimer = new Timer(15000);
        }
    }
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y, int skin) {
        AnimalInfo info;
        info = Animals.byID(99);

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
        if(target == null) {
            this.getPlayer().setMouseX(x);
            this.getPlayer().setMouseY(y);
        } else {
            this.getPlayer().setMouseX(target.getX());
            this.getPlayer().setMouseY(target.getY());
        }
        if(this.isDefender) {
            this.getPlayer().setImmunity(1);
        }
    }

    private Timer retargTimer;

    private Waypoint waypoint;

    private void setWaypoint() {
        if(this.getPlayer() != null) {
            double x = Utils.randomDouble(this.getPlayer().getX()-150, this.getPlayer().getX()+150);
            double y = Utils.randomDouble(this.getPlayer().getY()-150, this.getPlayer().getY()+150);
            waypoint = new Waypoint(x, y);
            if(retargTimer != null) retargTimer.reset();
            retargTimer = new Timer(5000);
        }
    }

    private void setReturning() {
        if(this.getPlayer() != null) {
            if(this.antHill != null && !this.antHill.isDead()) waypoint = new Waypoint(this.antHill.getX(), this.antHill.getY());
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
            if(((BulletAnt)this.getPlayer()).bite != null) ((BulletAnt)this.getPlayer()).bite.delete();
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;
            this.setXp(this.getXP()/2);
            if(killer != null && killer instanceof Animal) ((Animal)killer).getClient().addXp(this.getXP());
            this.shouldRemove(true);
        }
    }

    private Timer imitateTimer = new Timer(1000);

    private Timer retargetingTimer = null;

    private double addx;
    private double addy;

    private boolean returning = false;

    public void attack() {
        if(target != null && this.getPlayer() != null) {
            this.getPlayer().useAbility();
            this.retargetingTimer = new Timer(15000);
        }
    }

    @Override
    public void update() {
        if(this.getPlayer() != null) {
            if(target != null) {
                if(((Animal) target).getClient().getPlayer() != ((Animal) target) || target.isDead() || ((Animal) target).isInHole()) target = null;
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
                        if(((BulletAnt) this.getPlayer()).bite != null && ((BulletAnt) this.getPlayer()).bite.gotcha != null) {
                            this.returning = true;
                        } else {
                            this.getPlayer().setMouseX(target.getX()+addx);
                            this.getPlayer().setMouseY(target.getY()+addy);
                        }
                        if(retargetingTimer != null && !returning) {
                            retargetingTimer.update(Constants.TICKS_PER_SECOND);
                            if(retargetingTimer.isDone()) {
                                target = null;
                                retargetingTimer.reset();
                                retargetingTimer = null;
                                setWaypoint();
                            }
                        }
                    }
                    if(target != null) {
                        if(!((Animal) target).isInHole() && ((BulletAnt) this.getPlayer()).canAbility() && Utils.distance(this.getPlayer().getX(), target.getX(), this.getPlayer().getY(), target.getY()) < target.getRadius()+this.getPlayer().getRadius() && ((Animal)target).getInfo().getTier() <= 15 && !((Animal) target).inArena && !((Animal) target).isInvincible() && !((Animal) target).flag_flying && !((Animal) target).isInHole() && ((Animal) target).bleedingSeconds <= 0 && !((Animal) target).isStunned()) {
                            attack();
                        }
                    }
                }
            } else if(!this.isDefender) {
                if(waypoint != null) {
                    this.getPlayer().setMouseX(this.waypoint.getX());
                    this.getPlayer().setMouseY(this.waypoint.getY());
                    if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 1) {
                        setWaypoint();
                    } else {
                        retargTimer.update(Constants.TICKS_PER_SECOND);
                        if(retargTimer.isDone()) {
                            setWaypoint();
                        }
                    }
                } else setWaypoint();
            } else if(this.isDefender) {
                setReturning();
                this.getPlayer().setMouseX(this.waypoint.getX());
                this.getPlayer().setMouseY(this.waypoint.getY());
                if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 10) {
                    this.killPlayer(0, antHill);
                    return;
                }
            }
            if(returning) {
                setReturning();
                this.getPlayer().setMouseX(this.waypoint.getX());
                this.getPlayer().setMouseY(this.waypoint.getY());
                if(this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) <= 10) {
                    if(((BulletAnt) this.getPlayer()).bite != null && ((BulletAnt) this.getPlayer()).bite.gotcha != null) {
                        this.antHill.addPreyToHole(((BulletAnt) this.getPlayer()).bite.gotcha);
                    }
                    this.killPlayer(0, antHill);
                    return;
                }
            }
            if(this.returnTimer != null) {
                this.returnTimer.update(Constants.TICKS_PER_SECOND);
                if(this.returnTimer.isDone()) {
                    this.returning = true;
                    this.returnTimer.reset();
                    this.returnTimer = null;
                }
            }
            this.getPlayer().setWater(this.getPlayer().maxwater);
            if(this.getRequests().size() > 0) {
                this.clearRequests();
            }
            updateRequests();
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if(liveTimer.isDone()) {
                this.killPlayer(1, null);
                liveTimer.reset();
            }
        }
    }
}