package io.mopesbox.Objects.Static;

import java.util.Map;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Timer;
import io.mopesbox.World.Room;

public class FlyTrapMouth extends GameObject {

    public Room room;
    private FlyTrap body;
    private double startX;
    private double startY;
    private Animal trapped;
    private Timer trappedTimer = new Timer(5000);
    private Timer cooldownTimer = new Timer(5000);
    private Timer hurtTimer = new Timer(1000);
    private boolean onCooldown = false;

    public FlyTrapMouth(int id, double x, double y, Room room, FlyTrap body) {
        super(id, x, y, 25, 88);
        this.setCollideCallbacking(true);
        this.setCollideable(false);
        this.setCustomCollisionRadius(this.getRadius() * 2.5);
        this.setHasCustomCollisionRadius(true);
        this.setSendsAngle(true);
        this.setBiome(BiomeType.DESERT.ordinal());
        this.startX = x;
        this.startY = y;
        this.body = body;
        this.room = room;
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if (o instanceof Animal && ((Animal) o).getInfo().getTier() >= 15) {
            return false;
        }
        if(getCollideableIs(o)) return true;

        return true;
    }

    @Override
    public void update() {
        super.update();
        if (!this.onCooldown) {
            if (this.trapped == null) {
                this.grabbedAni = false;
                this.cooldownTimer.reset();
                this.trappedTimer.reset();
                if (!this.isAttacking){}
                else {
                    if (!this.stage1) {
                        double x = ((Math.cos(Math.toRadians(this.angle)) * (this.getRadius() * 2))) * 1.8;
                        double y = ((Math.sin(Math.toRadians(this.angle)) * (this.getRadius() * 2))) * 1.8;
                        this.setX(this.getX() + x);
                        this.setY(this.getY() + y);
                        this.stage1 = true;
                    } else {
                        stagTimer.update(Constants.TICKS_PER_SECOND);
                        if (stagTimer.isDone()) {
                            if (!this.stage2) {
                                this.stage2 = true;
                                this.hunt();
                            } else {
                                this.setX(startX);
                                this.setY(startY);
                                this.onCooldown = true;
                                this.cooldownTimer.reset();
                            }
                            stagTimer.reset();
                        }
                    }
                }
            } else {
                this.grabbedAni = true;
                this.trapped.setX(this.getX());
                this.trapped.setY(this.getY());
                this.trapped.setVelocityX(0);
                this.trapped.setVelocityY(0);
                this.trapped.flag_eff_grabbedByFlytrap = true;
                this.hurtTimer.update(Constants.TICKS_PER_SECOND);
                if (this.hurtTimer.isDone()) {
                    this.trapped.hurt(this.trapped.maxHealth / 5, 1, body);
                    this.hurtTimer.reset();
                }
                this.trappedTimer.update(Constants.TICKS_PER_SECOND);
                if (this.trappedTimer.isDone()) {
                    this.setX(startX);
                    this.setY(startY);
                    if (this.trapped != null)
                        this.trapped.flag_eff_grabbedByFlytrap = false;
                    this.stage1 = false;
                    this.stage2 = false;
                    this.isAttacking = false;
                    this.isMouthClosed = false;
                    this.trapped = null;
                    this.onCooldown = true;
                    this.cooldownTimer.reset();
                    this.trappedTimer.reset();
                }
            }
        } else {
            this.stage1 = false;
            this.stage2 = false;
            this.isAttacking = false;
            this.grabbedAni = false;
            this.isMouthClosed = true;
            this.trapped = null;
            this.cooldownTimer.update(Constants.TICKS_PER_SECOND);
            if (this.cooldownTimer.isDone()) {
                this.stage1 = false;
                this.stage2 = false;
                this.isAttacking = false;
                this.isMouthClosed = false;
                this.trapped = null;
                this.onCooldown = false;
                this.cooldownTimer.reset();
            }
        }
    }


  
    // public void checkTargets() {
    //     for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
    //         GameObject o = entry.getValue();
      
    //     }
        
    // }

    @Override
    public void onCollision(GameObject o){
        if(!this.isAttacking){
            if (o instanceof Animal && ((Animal) o).getInfo().getTier() < 14) {
                Animal animal = (Animal) o;
                double dy = this.getY() - animal.getY();
                double dx = this.getX() - animal.getX();
                double theta = Math.atan2(dy, dx);
                theta *= 180 / Math.PI;
                if (theta < 0) {
                    theta += 360;
                }
                theta -= 180;
                if (theta < 0)
                    theta += 360;
                double angleDiff = Math.toRadians(Math.abs(theta - this.getAngle()));
                if (angleDiff <= Math.PI / 3) {
                    this.attack();
                }
            }
        }
    }

    public void trap(Animal o) {
        this.trapped = o;
    }

    public void hunt() {
        for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
            GameObject o = entry.getValue();
                   if (o instanceof Animal) {
                Animal animal = (Animal) o;
                if (animal.getInfo().getTier() >= 1 && animal.getInfo().getTier() <= 3
                        && !animal.flag_eff_grabbedByFlytrap) {
                    this.trap(animal);
                    this.setX(startX);
                    this.setY(startY);
                    this.stage1 = false;
                    this.stage2 = false;
                    this.isAttacking = false;
                    this.isMouthClosed = true;
                    break;
                } else if (animal.getInfo().getTier() >= 4 && animal.getInfo().getTier() < 14) {
                    animal.hurt(animal.maxHealth / 4, 1, body);
                    animal.flag_eff_sweatPoisoned = true;
                    this.setX(startX);
                    this.setY(startY);
                    this.stage1 = false;
                    this.stage2 = false;
                    this.isAttacking = false;
                    this.isMouthClosed = false;
                    this.onCooldown = true;
                    this.cooldownTimer.reset();
                    break;
                }
            }
        
        }
    }

    private Timer stagTimer = new Timer(500);
    private boolean stage1 = false;
    private boolean stage2 = false;

    public void attack() {
        this.isAttacking = true;
        this.isMouthClosed = true;
        
        
    }

    public boolean isAttacking = false;
    public boolean isMouthClosed = false;
    public boolean grabbedAni = false;
    public boolean isKillable = false;

    public void writeInfo(MsgWriter writer, boolean newlyVisible) {
        writer.writeBoolean(isAttacking);
        writer.writeBoolean(isMouthClosed);
        writer.writeBoolean(grabbedAni);
        writer.writeBoolean(isKillable);

        if (newlyVisible) {
            double x = ((Math.cos(Math.toRadians(this.angle)) * (this.body.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.angle)) * (this.body.getRadius())));
            writer.writeUInt16((short) ((this.body.getX() + x) * 4));
            writer.writeUInt16((short) ((this.body.getY() + y) * 4));
        }
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writeInfo(writer, true);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writeInfo(writer, false);
    }
}

