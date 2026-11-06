package io.mopesbox.Objects.ETC;


import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier5.BulletAnt;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Static.AntHill;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;
import java.util.Map;
public class AntBite extends Ability {
    private Timer liveTimer = new Timer(500);
    private Room room;
    public Animal owner;
    public Animal gotcha;
    private int skin;
    public AntBite(int id, double x, double y, int radius, Room room, Animal owner) {
        super(id, x, y, 0, radius, 0);
        this.room = room;
        this.owner = owner;
        this.skin = this.owner.getInfo().getSkin();
        this.setSendsAngle(true);
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && !(o instanceof BulletAnt) && gotcha == null && ((Animal)o).getInfo().getTier() <= 15 && ((Animal) o) != this.owner && !((Animal) o).inArena && !((Animal) o).isInvincible() && !((Animal) o).flag_flying && !((Animal) o).isInHole() && ((Animal) o).bleedingSeconds <= 0 && !((Animal) o).isStunned()) {
            if((this.skin == 0 && ((Animal)o).getInfo().getTier() <= 9) || (this.skin == 1 && ((Animal)o).getInfo().getTier() <= 12)) {
                this.gotcha = (Animal)o;
                this.owner.setSpeed(this.owner.mainSpeed*(skin == 0 ? 2 : 2.5));
                liveTimer = new Timer(skin == 0 ? 3000 : 5000);
            }
            ((Animal)o).hurt(skin == 0 ? 5 : 10, 14, this.owner);
            ((Animal)o).setBleeding(5, this.owner, skin == 0 ? 1 : 2);
            ((Animal)o).setParalize(5, this.owner);
        }
    }
    
    @Override
    public void update() {
        super.update();
        if(this.gotcha != null) {
            double x = ((Math.cos(Math.toRadians(this.owner.getAngle())) * (this.gotcha.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.owner.getAngle())) * (this.gotcha.getRadius())));
            this.gotcha.setX(this.getX()+x);
            this.gotcha.setY(this.getY()+y);
            this.gotcha.setStun(5);
            this.gotcha.setBleeding(5, this.owner, skin == 0 ? 1 : 2);
            this.gotcha.setParalize(5, this.owner);
        }

        liveTimer.update(Constants.TICKS_PER_SECOND);

        if (liveTimer.isDone() || this.owner == null) {
            ((BulletAnt) this.owner).disableAbility(10);
            this.delete();
            liveTimer.reset();
            return;
        }
    }

    private boolean isDeleting = false;

    public void delete() {
        if(!isDeleting) {
            if(this.gotcha != null) {
                this.gotcha.setBleeding(5, this.owner, 5);
                this.gotcha.setParalize(5, this.owner);
                this.gotcha.setStun(5);
                if(this.gotcha.getCollidedList().size() > 0 && !this.owner.getClient().isBot()) {
                    for (Map.Entry<Integer, GameObject> entry : this.gotcha.getCollidedList().entrySet()) {
                        GameObject obj = entry.getValue();
                        if(obj instanceof AntHill && !this.gotcha.isInHole()) {
                            ((AntHill)obj).addPreyToHole(this.gotcha);
                        }
                    }
                }
            }
            isDeleting = true;
            this.room.removeObj(this, null);
        }
    }
}