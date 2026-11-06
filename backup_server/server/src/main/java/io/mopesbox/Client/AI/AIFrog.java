package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
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
import io.mopesbox.World.Room;

public class AIFrog extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private Timer liveTimer = new Timer(120000);
    private Biome spawnedFrom;

    public AIFrog(Room room, Biome spawnedFrom) {
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
        info = Animals.byID(62);

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
        this.getPlayer().setMouseX(x);
        this.getPlayer().setMouseY(y);
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
            if(killer != null && killer instanceof Animal) ((Animal)killer).getClient().addXp(this.getXP() * (int) 2.3);
            
            this.shouldRemove(true);
        }
    }

    private Timer jumpTimer = new Timer(6000);

    @Override
    public void update() {
        if (this.getPlayer() != null) {
            jumpTimer.update(Constants.TICKS_PER_SECOND);
            PlayerAbility abil = this.getPlayer().getInfo().getAbility();
            if(jumpTimer.isDone() && abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !abil.isActive()) {
                double ang = Utils.randomDouble(0, 360);
                this.getPlayer().setAngle(ang);
                double x = ((Math.cos(Math.toRadians(ang)) * (500)));
                double y = ((Math.sin(Math.toRadians(ang)) * (500)));
                this.getPlayer().setMouseX(this.getPlayer().getX()+x);
                this.getPlayer().setMouseY(this.getPlayer().getY()+y);
                this.getPlayer().useAbility();
                jumpTimer.reset();
            }
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