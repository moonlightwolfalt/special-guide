package io.mopesbox.Animals.Tier6;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;

public class Duck extends Animal {
    public Duck(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.baseRadius = 22;
        this.setWaterfowl(false);
        this.setRadius(baseRadius);
        this.setMaxHealth(216);
        info.getAbility().setPossible(false);
        info.getAbility().setUsable(false);
    }

    public boolean isRunning = false;
    private boolean isAttacking = false;
    private Timer abilTimer = new Timer(4000);
    private Timer runTimer = new Timer(600);

    public void nRun() {
        this.makeBoost(0);
    }

    @Override
    public void update() {
        super.update();
        if (isRunning) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            runTimer.update(Constants.TICKS_PER_SECOND);
            if (runTimer.isDone()) {
                this.getClient().getRoom().sendChat(this.getClient(), "QUACK!");
                nRun();
                runTimer.reset();
            }
            if (abilTimer.isDone()) {
                isRunning = false;
                this.getInfo().getAbility().setActive(false);
                abilTimer.reset();
            }
        }
    }

    public void nowRun() {
        isRunning = true;
        abilTimer.reset();
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(this.isAttacking);
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(this.isAttacking);
    }

    @Override
    public void hurt(double damage, int reason, GameObject biter) {
        nowRun();
        super.hurt(damage, reason, biter);
    }

    @Override
    public void useAbility() {
        super.useAbility();
    }

}
