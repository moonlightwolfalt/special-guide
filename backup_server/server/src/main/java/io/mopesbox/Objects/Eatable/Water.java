package io.mopesbox.Objects.Eatable;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.ElephantTrunk;
import io.mopesbox.Objects.Fire.Fire;
import io.mopesbox.Objects.Static.LavaLake;
import io.mopesbox.Objects.Static.Tree;
import io.mopesbox.Objects.Static.Waterspot;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class Water extends GameObject{
    private Animal owner = null;
    private int speed = 0;
    private Room room;
    private double aangle;
    private Waterspot origin;
    public Water(int id, double x, double y, int biome, Room room) {
        super(id, x, y, 12, 21);
        this.setBiome(biome);
        this.setMovable(true);
        this.room = room;
    }

    public Water(int id, double x, double y, int biome, Room room, Waterspot origin) {
        super(id, x, y, 12, 21);
        this.setBiome(biome);
        this.origin = origin;
        this.setMovable(true);
        this.room = room;
        this.setSpawnID(this.origin.getId());
    }

    public Water(int id, double x, double y, int biome, int speed, double angle, Animal owner, Room room) {
        super(id, x, y, 12, 21);
        this.setBiome(biome);
        this.setMovable(true);
        this.owner = owner;
        this.speed = speed;
        this.room = room;
        this.aangle = angle;
    }

    private Timer liveTimer = new Timer(3000);

    public boolean collideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
            if(((Animal)o).getBarType() != 0) {
                return false;
            }
            if(((Animal)o) == this.owner && this.speed > 10) {
                return false;
            }
            if(((Animal)o).water == ((Animal)o).maxwater) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal || o instanceof LavaLake || o instanceof Tree || o instanceof Water) {
            return false;
        }
        return true;
    }
    
    private boolean isDone = false;

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (!this.isDone && (o instanceof RedMushroom || o instanceof Watermelon || o instanceof Mushroom || (o instanceof Fire && !((Fire)o).isDamaged) || (collideableIs(o) && o instanceof Animal && ((Animal)o).getBarType() == 0))) {
            this.isDone = true;

            if(this.origin != null) {
                this.origin.waters--;
            }
            if(o instanceof Fire) {
                ((Fire)o).setDamage(20);
            } else if (o instanceof Watermelon) {
                ((Watermelon)o).addWater();
            } else if (o instanceof Mushroom) {
                ((Mushroom)o).addWater();
            } else if (o instanceof RedMushroom) {
                ((RedMushroom)o).addWater();
            } else {
                Animal biter = (Animal) o;
                biter.addWater(3);
                if(biter.fireSeconds > 0) biter.fireSeconds--;

                if (((Animal)o).pumpkinBalls.size() > 0) {
                        ((Animal)o).pumpkinBalls.get(0).Throw();
                        ((Animal)o).setStun(2);
                }
                
            }
            this.room.removeObj(this, o);
        } else if (!this.isDone && o instanceof ElephantTrunk) {
            ((ElephantTrunk)o).getOwner().addWater(3);
            this.isDone = true;
            this.room.removeObj(this, ((ElephantTrunk)o).getOwner());
        }
    }

    @Override
    public void update() {
        super.update();
        if(this.origin == null) {
            if(this.speed > 0) {
                this.setVelocityX(((Math.cos(Math.toRadians(this.aangle)) * (this.speed))));
                this.setVelocityY(((Math.sin(Math.toRadians(this.aangle)) * (this.speed))));
                this.speed--;
            } else {
                liveTimer.update(Constants.TICKS_PER_SECOND);
                if(liveTimer.isDone()) {
                    this.room.removeObj(this, null);
                    liveTimer.reset();
                }
            }
        }
    }

    public Animal getOwner() {
        return owner;
    }
}
