package io.mopesbox.Objects.Static;
import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.Watermelon;
import io.mopesbox.Objects.Eatable.WatermelonSlice;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class LavaLake extends GameObject {
    public int watermelons = 0;
    public int watermelonSlices = 0;
    private Room room;
    private boolean setLiveTimer = false;
    private Timer liveT = new Timer(30000);

    public LavaLake(int id, double x, double y, int rad, Room room) {
        super(id, x, y, rad, 44);
        this.room = room;
        this.setBiome(3);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    public void setLiveTimer(){
        this.setLiveTimer = true;
    }

    @Override
    public void update(){
        super.update();
        if(this.setLiveTimer){
            liveT.update(Constants.TICKS_PER_SECOND);
            if(liveT.isDone()){
                this.room.removeObj(this,  null);
                liveT.reset();
            }
        }
    }



    public void spawnWatermelonSlices(int amount) {
        double minX = (this.getX() - (this.getRadius() / 2));
        double minY = (this.getY() - (this.getRadius() / 2));

        double maxX = (minX + this.getRadius());
        double maxY = (minY + this.getRadius());

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 24, maxX - 24);
            double y = Utils.randomDouble(minY + 24, maxY - 24);
            WatermelonSlice watermelonSlice = new WatermelonSlice(this.room.getID(), x, y, 0, this.room, this);
            this.room.addObj(watermelonSlice);
        }
        watermelonSlices += amount;
    }

    public void spawnWatermelons(int amount) {
        double minX = (this.getX() - (this.getRadius() / 2));
        double minY = (this.getY() - (this.getRadius() / 2));

        double maxX = (minX + this.getRadius());
        double maxY = (minY + this.getRadius());

        for (int i = 0; i < amount; i++) {
            double x = Utils.randomDouble(minX + 24, maxX - 24);
            double y = Utils.randomDouble(minY + 24, maxY - 24);
            Watermelon watermelon = new Watermelon(this.room.getID(), x, y, this.room, this);
            this.room.addObj(watermelon);
        }
        watermelons += amount;
    }

    @Override
    public void onCollision(GameObject o) {
        if (o instanceof Animal && o.getCollideable(this) && !((Animal) o).isLavaAnimal()
                && !((Animal) o).flag_isClimbingHill && !((Animal) o).isInvincible() && !((Animal) o).getFlyingOver()) {
            if(((Animal)o).fireSeconds <= 0) ((Animal) o).setFire(6, null, ((Animal)o).maxHealth / 12);
            else
            ((Animal)o).hurt(1, 13, null);
        }
    
        if(o instanceof Animal){
            ((Animal)o).flag_inLavaLake = true;
        }
    }

    // @Override
    // public void onCollisionEnter(GameObject o) {
    //     if (o instanceof Animal && o.getCollideable(this) && !((Animal) o).isLavaAnimal()
    //             && !((Animal) o).flag_isClimbingHill && !((Animal) o).isInvincible() && !((Animal) o).getFlyingOver() && ((Animal)o).fireSeconds <= 0) {
    //         ((Animal) o).setFire(1, null, 3);
    //     }
    // }

    public boolean collideableIs(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying) {
                return false;
            }
            return true;
        }
        return false;
    }
}
