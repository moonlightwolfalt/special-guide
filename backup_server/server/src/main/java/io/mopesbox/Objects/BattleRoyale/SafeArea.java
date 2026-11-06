package io.mopesbox.Objects.BattleRoyale;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class SafeArea extends GameObject {
    private Room room;

    public SafeArea(int id, double x, double y, int radius, Room room) {
        super(id, x, y, radius, 76);
        this.setCollideable(false);
        this.setCollideCallbacking(false);

        this.room = room;
        shrinkRad = (radius * 4) - 100;
        shrinkingRad = (radius * 4) - 100 - (radius + 400);
        this.setMovable(false);
    }

    private Timer shrinkAddTimer = new Timer(1);
    private Timer shrinkTimer = new Timer(1);
    private Timer bigDamageTimer = new Timer(3000);

    public Timer changeAngleTimer = new Timer(10000);
    public double targetAngle = Math.random() * 2;
    public int speed = 4;

    @Override
    public void update() {
        changeAngleTimer.update(Constants.TICKS_PER_SECOND);
        if (changeAngleTimer.isDone()) {
            targetAngle += (Math.random() * 2);
            changeAngleTimer.reset();
        }

        /*
         * this.room.br_center_rad = this.getRadius()/4;
         * this.room.br_center_x = ((((int) Math.round(this.getX()))/4)/10)-5;
         * this.room.br_center_y = ((((int) Math.round(this.getY()))/4)/10)+5;
         */
        if (addingShrink > 0) {

            if (shrinkingRad <= 75) {
                addingShrink = 0;
            } else {
                shrinkAddTimer.update(Constants.TICKS_PER_SECOND);
                if (shrinkAddTimer.isDone()) {
                    shrinkingRad -= 75;
                    addingShrink -= 75;
                    shrinkAddTimer.reset();
                }
            }
        }
        if (isShrinking) {
            if (this.getRadius() < 50) {
                isShrinking = false;
            } else {
                shrinkTimer.update(Constants.TICKS_PER_SECOND);
                if (shrinkTimer.isDone()) {
                    int fullred = (shrinkingRad / 4);
                    if (this.getRadius() > fullred) {
                        int reduce = fullred >= 5 ? 5 : fullred;

                        this.setRadius(this.getRadius() - reduce);
                        fullred -= reduce;
                    } else {

                        if (isShrinking) {
                            shrinkRad = this.getRadius() * 4;
                            this.room.br_handler.onShrinkDone();

                            isShrinking = false;
                        }

                    }
                    shrinkTimer.reset();
                } // ЭФФФ - ЛАЙВ ДЕН АМОГУС СКАМ БАН
            }
        }
        bigDamageTimer.update(Constants.TICKS_PER_SECOND);
        for (GameClient gc : this.room.clients) {
            if (gc.getPlayer() != null && !gc.getPlayer().isGhost) {
                Animal plr = gc.getPlayer();
                if (plr.isInvincible())
                    continue;
                double distance = Utils.distance(this.getX(), plr.getX(), this.getY(), plr.getY());
                if (distance > (this.getRadius()) - plr.getRadius()) {

                    if (bigDamageTimer.isDone()) {
                        plr.setFire(2, null, plr.maxHealth / 4);
                    } else {
                        if (!plr.flag_eff_onFire && plr.fireSeconds <= 0)
                            plr.setFire(2, null, 3);
                    }
                    gc.send(Networking.personalGameEvent(255, "Staying in an unsafe area can harm you!"));
                }
            }
        }
        if (bigDamageTimer.isDone()) {
            bigDamageTimer.reset();
        }

        this.setXUnsafe(this.getX() + Math.cos(targetAngle) * speed);
        this.setYUnsafe(this.getY() + Math.sin(targetAngle) * speed);

        if (this.getX() - 1 <= 0) {
            this.setXUnsafe(1);
        }
        if (this.getY() - 1 <= 0) {
            this.setYUnsafe(1);
        }
        if (this.getX() >= Constants.WIDTH) {
            this.setYUnsafe(Constants.WIDTH - 1);
        }
        if (this.getY() >= Constants.HEIGHT) {
            this.setY(Constants.HEIGHT - 1);
        }
    }

    private int addingShrink = 0;
    private boolean isShrinking = false;

    public boolean isShrinking() {
        return isShrinking;
    }

    public void addShrink(int rad) {
        this.addingShrink = rad;
    }

    public void setShrinking(boolean a) {
        this.isShrinking = a;
    }

    private int shrinkRad = 4;
    private int shrinkingRad = 4;

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt32(shrinkRad);
        writer.writeUInt32(shrinkingRad);
    }
}
