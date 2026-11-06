package io.mopesbox.Client.AI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier15.Rudolph;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Fun.Sleigh;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

public class AIDeer extends GameClient {
    private Animal player;
    private Room room;
    private String playerName;
    private Sleigh sleig;
    public Animal xPoint;
    public Animal yPoint;

    public boolean lastest;

    public AIDeer(Room room) {
        super(room, null);
        this.makeBot(true);
        this.room = room;
        this.playerName = " ";
    }

    public void setXandY(Animal a){
        this.xPoint = a;
        this.yPoint = a;
    }
        public void setLastest(boolean a){
        this.lastest = a;
    }

    public AIDeer(Room room, Sleigh sleig, Animal xPoint, Animal yPoint) {
        this(room);
        this.sleig = sleig;
        this.xPoint = xPoint;
        this.yPoint = yPoint;
        spawnAnimal(xPoint.getX(), xPoint.getY());
    }
       public AIDeer(Room room, Sleigh sleig) {
        this(room);
        this.sleig = sleig;
        spawnAnimal(this.sleig.getX(), this.sleig.getY());

    }

    // @Override
    // public void onHurt(GameObject o) {
    //     if(o instanceof Animal && ((Animal)o).getInfo().getTier() <= 16) {
    //         this.target = o;
    //         this.retargetingTimer = new Timer(15000);
    //         this.addx = Utils.randomDouble(-target.getRadius(), target.getRadius());
    //         this.addy = Utils.randomDouble(-target.getRadius(), target.getRadius());
    //     }
    // }


    @Override
    public Animal getPlayer() {
        return this.player;
    }

    public void spawnAnimal(double x, double y) {
        AnimalInfo info;
        info = Animals.byID(103);

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
        ((Rudolph)this.getPlayer()).setOwner(this.sleig.owner.getPlayer());

        this.setXp(Tier.byOrdinal(info.getTier()).getStartXP());
        this.room.addObj(this.player);
        if(this.xPoint != null){
            this.getPlayer().setMouseX(xPoint.getX());
            this.getPlayer().setMouseY(yPoint.getY());
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



    @Override
    public void update() {
        if(this.xPoint != null && this.yPoint != null && !lastest){
        this.getPlayer().setMouseX(this.xPoint.getX());
        this.getPlayer().setMouseY(this.yPoint.getY());
        }
        if(lastest){
        this.getPlayer().setMouseX(this.sleig.owner.getPlayer().getMouseX());
        this.getPlayer().setMouseX(this.sleig.owner.getPlayer().getMouseY());
        }

    }
}