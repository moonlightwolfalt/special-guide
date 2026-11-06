package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier4.OstrishBaby;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Egg;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Waypoint;
import io.mopesbox.World.Room;

public class AIOstrishBaby extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    public Ostrich mom;

    public AIOstrishBaby(Room room) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = " ";
    }

    public AIOstrishBaby(Room room, Ostrich mom, double x, double y) {
        this(room);
        this.mom = mom;
        spawnAnimal(x, y);
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public Egg egg = null;

    public void spawnAnimal(double x, double y) {
        AnimalInfo info;
        info = Animals.byID(69);

        if (info == null)
            return;

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
        if(this.player instanceof OstrishBaby){
            ((OstrishBaby)this.player).setMom(mom);
        }
        this.getPlayer().setPlayerName(mom.getPlayerName());
        this.setXp(Tier.byOrdinal(info.getTier()).getStartXP());
        this.getPlayer().setMouseX(x);
        this.getPlayer().setMouseY(y);
        this.egg = new Egg(this.getRoom().getID(), x, y, this.mom, getRoom(), ((OstrishBaby)this.getPlayer()));
        this.room.addObj(this.egg);
        /*
         *         public Egg(int id, double x, double y, Animal owner, Room room, OstrishBaby ostrishBaby) {

         */
    }
    private Waypoint waypoint = null;
            double lastX, lastY;


    private void setWaypoint() {
        double x, y;
        if (this.getPlayer() != null) {
            if(this.mom != null){
             x = Utils.randomDouble(this.mom.getX() - this.mom.getRadius(),
                    this.mom.getX() + this.mom.getRadius());
             y = Utils.randomDouble(this.mom.getY() - this.mom.getRadius(),
                    this.mom.getY() + this.mom.getRadius());
            lastX = x;
            lastY = y;
            }else{
                 x = lastX + Utils.randomInt(0, 3000);
                 y = lastY + Utils.randomInt(0, 3000);
            }
        waypoint = new Waypoint(x, y);

    }
}

    @Override
    public void send(MsgWriter writer) {
        //
    }

    public Timer hatchTimer = new Timer(10000);
    public Timer evolutionTimer = new Timer(20000);

    public void killEgg() {
        this.room.removeObj(egg, null);
        this.egg = null;
        this.player = null;
        this.shouldRemove(true);
    }

    @Override
    public void killPlayer(int reason, GameObject killer) {
        if (this.getPlayer() != null) {
            this.room.removeObj(this.getPlayer(), killer);
            this.player = null;

            this.setXp(this.getXP() / 2);

            if (killer != null && killer instanceof Animal)
                ((Animal) killer).getClient().addXp(this.getXP());

            if (this.mom != null)
                this.mom.babyOrEgg--;

            this.shouldRemove(true);
        }
    }

    public Timer wayTimer = new Timer(2000);
    public Timer retraceTimer = new Timer(8000);

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
        if (this.waypoint != null) {
            if(this.mom != null && this.mom.getClient().ostrichTarget != null && this.mom.getClient().ostrichTarget.attachedTo != null){
            this.getPlayer().setMouseX(this.mom.getClient().ostrichTarget.attachedTo.getX());
            this.getPlayer().setMouseY(this.mom.getClient().ostrichTarget.attachedTo.getY() + 1);
            }else{
                this.getPlayer().setMouseX(this.waypoint.getX());
            this.getPlayer().setMouseY(this.waypoint.getY());
            }
            retraceTimer.update(Constants.TICKS_PER_SECOND);
            if (retraceTimer.isDone()) {
                this.setWaypoint();
                retraceTimer.reset();
            }
        }
    }

    @Override
    public void update() {
        if (this.egg != null) {
            this.hatchTimer.update(Constants.TICKS_PER_SECOND);
            if (this.hatchTimer.isDone()) {
                this.room.removeObj(egg, this.player);
                this.room.addObj(this.player);
                this.player.setSpawnID(egg.getId());
                this.egg = null;
                this.hatchTimer.reset();
            }
        } else {
            if (this.getPlayer() != null) {
                if (this.getRequests().size() > 0) {
                    this.clearRequests();
                }
                this.way();
                updateRequests();
            }
        }
    }
}