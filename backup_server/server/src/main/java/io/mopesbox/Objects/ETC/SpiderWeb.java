package io.mopesbox.Objects.ETC;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier14.GiantSpider;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.AnimalType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class SpiderWeb extends Ability {
    private Timer liveTimer = new Timer(240000);
    private Room room;
    public boolean isBuilding = true;
    private Animal owner;
    private List<Animal> damaged = new ArrayList<>();
    private int damage = 20;
    private Timer sizeTimer = new Timer(300);
    public SpiderWeb(int id, double x, double y, int radius, Room room, double angle, Animal owner, int species) {
        super(id, x, y, 35, radius, 0);
        this.room = room;
        this.owner = owner;
        this.setObjAngle(angle);
        this.setSpecies(species);
        this.setSendsAngle(true);
        this.setCollideable(true);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && !((Animal)o).flag_eff_poison && !((Animal)o).flag_fliesLikeDragon) {
            if (((Animal)o).getInfo().getTier() < this.owner.getInfo().getTier()) {
            ((Animal)o).hurt(o instanceof Bee ? o.health : (damage + (((Animal)o).maxHealth / 18)), 1, this.owner);
            ((Animal)o).setStun(2);
            ((Animal)o).setPoisoned(4, owner, damage);
            ((Animal)o).flag_webStuck = true;
            ((Animal)o).webStuckType = this.getSpecies();
            ((Animal)o).setSpeed(3);
            this.room.removeObj(this, o);
            }
        }
        if(o instanceof Animal &&((Animal)o) == this.owner) this.owner.flag_eff_isOnSpiderWeb = true;
    }
    @Override
    public boolean getCollideable(GameObject o){
        if(o instanceof Animal && !damaged.contains((Animal) o) && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole()){
            if(((Animal)o).getInfo().getType() != AnimalType.GIANTSPIDER && ((Animal)o).getInfo().getType() != AnimalType.GIANTSCORPION){
                return true;
            }
        }
        return false;

    }
    @Override
    public void onCollisionExit(GameObject o){
        if(o instanceof Animal){
        if(((Animal)o) == this.owner && this.owner.flag_eff_isOnSpiderWeb) this.owner.flag_eff_isOnSpiderWeb = false;
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.damage > 10) damage--;
        liveTimer.update(Constants.TICKS_PER_SECOND);
        if(liveTimer.isDone()) {
            this.room.removeObj(this, null);
            liveTimer.reset();
            this.sizeTimer.reset();
        }
        if(this.isBuilding) sizeTimer.update(Constants.TICKS_PER_SECOND);
        if(this.isBuilding && sizeTimer.isDone()){
            double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (this.owner.getRadius())));

            this.setX(this.owner.getX()+x);
            this.setY(this.owner.getY()+y);
            this.setRadius((int) Math.round(this.getRadius() + 1.8));
            this.sizeTimer.reset();
            if(this.getRadius() >= 80) {
                this.sizeTimer.reset();
            if(this.owner instanceof GiantSpider)
            ((GiantSpider)owner).disableAbility();
            this.isBuilding = false;

            }

           

            }
        }


}