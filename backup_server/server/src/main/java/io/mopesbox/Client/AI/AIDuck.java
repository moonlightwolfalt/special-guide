package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier6.Duck;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.LakeIsland;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Waypoint;
import io.mopesbox.World.Room;

public class AIDuck extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private LakeIsland island;
    private Timer liveTimer = new Timer(150000);
    private boolean isEvoluted = false;

    public AIDuck(Room room) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = " ";
    }

    public AIDuck(Room room, LakeIsland island) {
        this(room);
        this.island = island;
        this.spawnAnimal(island.getX(), island.getY());
    }

    public AIDuck(Room room, LakeIsland island, double x, double y) {
        this(room);
        this.island = island;
        this.isEvoluted = true;
        this.spawnAnimal(x, y);
    }

    @Override
    public void onHurt(GameObject o) {
        if (o instanceof Animal && ((Animal) o).getInfo().getTier() <= 16) {
            //
        }
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y) {
        AnimalInfo info;
        info = Animals.byID(58);

        if (info == null)
            return;

        info.setSkin(Utils.randomBoolean() ? 1 : 0);

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

    private Waypoint waypoint = null;

    private void setWaypoint() {
        if (this.getPlayer() != null) {
            double x = Utils.randomDouble(this.island.lake.getX() - this.island.lake.getRadius(),
                    this.island.lake.getX() + this.island.lake.getRadius());
            double y = Utils.randomDouble(this.island.lake.getY() - this.island.lake.getRadius(),
                    this.island.lake.getY() + this.island.lake.getRadius());
            waypoint = new Waypoint(x, y);
        }
    }

    @Override
    public void send(MsgWriter writer) {
        //
    }

    public boolean respawning = false;
    public Timer respawnTimer = new Timer(5000);

    @Override
    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;

            this.setXp(this.getXP() / 2);

            if (killer != null && killer instanceof Animal)
                ((Animal) killer).getClient().addXp(this.getXP() * (int) 2.3);

                

            if (!this.isEvoluted) {
                respawning = true;
                respawnTimer.reset();
            } else {
                this.shouldRemove(true);
            }
        }
    }

    public Timer wayTimer = new Timer(2000);
    public Timer retraceTimer = new Timer(8000);
    public Timer hatchRetraceTimer = new Timer(5000);

    public void way() {
        if (this.waypoint == null) {
            this.setWaypoint();
            retraceTimer.reset();
        } else if (this.waypoint.getDistance(this.getPlayer().getX(), this.getPlayer().getY()) < 50) {
            this.wayTimer.update(Constants.TICKS_PER_SECOND);
            if (this.wayTimer.isDone()) {
                this.waypoint = null;
                this.wayTimer.reset();
            }
        }
        if (goingHatch) {
            this.getPlayer().setCanClimbHills(true);
            this.getPlayer().setCanClimbRocks(true);
            this.getPlayer().setMouseX(this.island.getX());
            this.getPlayer().setMouseY(this.island.getY());
            hatchRetraceTimer.update(Constants.TICKS_PER_SECOND);
            if (Utils.distance(this.getPlayer().getX(), this.island.getX(), this.getPlayer().getY(),
                    this.island.getY()) < 50 || hatchRetraceTimer.isDone()) {
                this.goingHatch = false;
                this.getPlayer().setCanClimbHills(false);
                this.getPlayer().setCanClimbRocks(false);
                hatchRetraceTimer.reset();
                this.hatch();
            }
        } else {
            if (this.waypoint != null && !((Duck) this.getPlayer()).isRunning) {
                retraceTimer.update(Constants.TICKS_PER_SECOND);
                if (retraceTimer.isDone()) {
                    this.setWaypoint();
                    retraceTimer.reset();
                }
                this.getPlayer().setMouseX(this.waypoint.getX());
                this.getPlayer().setMouseY(this.waypoint.getY());
            } else if (((Duck) this.getPlayer()).isRunning) {
                double x = ((Math.cos(Math.toRadians(this.getPlayer().getAngle())) * (50)));
                double y = ((Math.sin(Math.toRadians(this.getPlayer().getAngle())) * (50)));
                this.getPlayer().setMouseX(this.getPlayer().getX() + x);
                this.getPlayer().setMouseY(this.getPlayer().getY() + y);
            }
        }
    }

    public int ducklings = 0;
    public boolean goingHatch = false;
    public Timer untilHatch = new Timer(30000);

    public void hatch() {
        if (this.getPlayer() != null) {
            if (ducklings < 2 && this.island.duckCount < 3) {
                AIDuckling duckling = new AIDuckling(this.room, this.island, this, this.getPlayer().getX(),
                        this.getPlayer().getY());
                this.room.addAI(duckling);
                ducklings++;
            }
        }
    }

    @Override
    public void update() {
        if (this.getPlayer() != null) {
            if (this.getRequests().size() > 0) {
                this.clearRequests();
            }
            if (ducklings < 3 && !goingHatch && !this.isEvoluted) {
                untilHatch.update(Constants.TICKS_PER_SECOND);
                if (untilHatch.isDone()) {
                    this.goingHatch = true;
                    untilHatch.reset();
                }
            }
            this.way();
            if (this.getRequests().size() > 0) updateRequests();
            liveTimer.update(Constants.TICKS_PER_SECOND);
            if (liveTimer.isDone()) {
                this.killPlayer(1, null);
                liveTimer.reset();
            }
            
        } else {
            if (this.respawning) {
                this.respawnTimer.update(Constants.TICKS_PER_SECOND);
                if (this.respawnTimer.isDone()) {
                    this.respawning = false;
                    this.spawnAnimal(this.island.getX(), this.island.getY());
                    this.respawnTimer.reset();
                }
            }
        }
    }
}