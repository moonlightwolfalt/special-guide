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
import io.mopesbox.World.Room;

public class AIDummy extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private boolean notWelcome;

    public AIDummy(Room room, String nick) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = nick;
    }

    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y, int type) {
        AnimalInfo info;
        info = Animals.byID(type);

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

    private void pendingRequest(PvPRequest a) {
        if(this.getPlayer() != null && !this.getPlayer().flag_flying && this.getPlayer().inArena) {
            PvPArena newarena = new PvPArena(this.room.getID(), this.getPlayer().getX(), this.getPlayer().getY(), a.getEnemy(), this, this.room.getFightNumber(), this.room);
            this.room.addObj(newarena);
            this.room.sendChat(this, "OK go pvp");
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
            this.setXp(this.getXP()/2);
            if(killer != null && killer instanceof Animal) ((Animal)killer).getClient().addXp(this.getXP());
            this.shouldRemove(true);
        }
    }

    @Override
    public void update() {
        if(this.getPlayer() != null) {
            if(!notWelcome) {
                welcomeTimer.update(Constants.TICKS_PER_SECOND);
                if(welcomeTimer.isDone()) {
                    this.room.sendChat(this, "Hello!");
                    notWelcome = true;
                    welcomeTimer.reset();
                }
            }
            if(this.getPlayer().inArena) {
                this.getPlayer().setMouseX(this.getPlayer().arenaObject.getX());
                this.getPlayer().setMouseY(this.getPlayer().arenaObject.getY());
            } else {
                this.getPlayer().setMouseX(this.getPlayer().getX());
                this.getPlayer().setMouseY(this.getPlayer().getY());
            }
            if(this.getRequests().size() > 0 && !this.getPlayer().inArena) {
                for(PvPRequest a : this.getRequests()) {
                    this.pendingRequest(a);
                }
                this.clearRequests();
            }
            updateRequests();
        }
    }
}