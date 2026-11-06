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
import io.mopesbox.Objects.Biome.Biome;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Waypoint;
import io.mopesbox.World.Room;

public class AISnail extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private Timer liveTimer = new Timer(120000);
    private Biome spawnedFrom;

    public AISnail(Room room, Biome spawnedFrom) {
        super(room, null);
        this.makeBot(true);
        this.spawnedFrom = spawnedFrom;
        this.room = room;
        this.playerName = " ";
    }

    @Override
    public void onHurt(GameObject o) {
        if(o instanceof Animal) {
            if(this.getPlayer() != null) this.getPlayer().useAbility();
        }
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y) {
        AnimalInfo info;
        info = Animals.byID(45);

        if (info == null)
            return;
            
        if (info.getType().hasCustomClass()) {
            try {
                Class<?> rawAnimalClass = info.getType().getAnimalClass();
                                    
                @SuppressWarnings("unchecked")
                Class<Animal> animalClass = (Class<Animal>) rawAnimalClass;
                Constructor<Animal> aa = animalClass.getConstructor(int.class, double.class, double.class,
                        AnimalInfo.class, String.class, GameClient.class);              this.player = (Animal) aa.newInstance(
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

    private Timer retargTimer;

    private Waypoint waypoint;

    private void setWaypoint() {
        if (this.getPlayer() != null) {
            double x = Utils.randomDouble(this.getPlayer().getX()-60, this.getPlayer().getX()+60);
            double y = Utils.randomDouble(this.getPlayer().getY()-60, this.getPlayer().getY()+60);
            waypoint = new Waypoint(x, y);
            if (retargTimer != null) retargTimer.reset();
            retargTimer = new Timer(5000);
        }
    }

    @Override
    public void send(MsgWriter writer) {
        //
    }

    @Override
    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            if(this.spawnedFrom != null) spawnedFrom.snails--;
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;
            this.setXp(this.getXP()/2);
            if(killer != null && killer instanceof Animal) ((Animal)killer).getClient().addXp(this.getXP());
            this.shouldRemove(true);
        }
    }

    @Override
    public void update() {
        if(this.getPlayer() != null) {
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
            if (this.getRequests().size() > 0) {
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