package io.mopesbox.Animals.Tier17;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.IceCrystal;
import io.mopesbox.Objects.ETC.Tailslap;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Objects.Static.SandboxArena;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;

public class KingDragon extends Animal {

    public KingDragon(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        client.setCamzoom((int) ((Tier.byOrdinal(info.getTier()).getBaseZoom() / 1.5) * 1000));
        this.getClient().baseCamzoom = (int) ((Tier.byOrdinal(info.getTier()).getBaseZoom() / 1.5) * 1000);
        this.baseRadius = (int) Math.round(Tier.byOrdinal(info.getTier()).getBaseRadius() * 1.1); //1.5
        this.biteDamage = 80;
        this.setMaxHealth(775); //1400
        this.setRadius(baseRadius);
        this.flag_fliesLikeDragon = true;
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCanBeUnderTree(false);
        this.setCanBeInHole(false);
        this.setFlyingOver(true);
        this.setBarType(2);
        this.setLavaAnimal(true);
        this.setCanRegenerateHP(true);
        this.setSpeed(8);
    }

    private Timer tailSlapTimer = new Timer(15000);

    @Override
    public void waterShoot() {
        if (!this.isRammed && !this.inArena && canTailslap && !this.flag_flying && !this.isInHole() && !this.isInvincible()
                && !this.isGhost) {
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
            Tailslap tailslap = new Tailslap(this.getClient().getRoom().getID(), this.getX() - x, this.getY() - y,
                    this.getRadius(), this.getClient().getRoom(), this.getAngle(), this,
                    this.getInfo().getAnimalSpecies());
            this.getClient().getRoom().addObj(tailslap);
            if (!this.getClient().instantRecharge)
                canTailslap = false;
            tailSlapTimer.reset();
        }
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() <= 16 && ((Animal)o).getInfo().getTier() > 5  && !this.flag_flying) {
            return true;
        }
        if(o instanceof SandboxArena && ((SandboxArena)o).getCollideable(this)){
            return true;
        }
            if(o instanceof IceCrystal) return true;
            if(o instanceof Bee) return false;

        return false;
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(this.getWater()); // lava
        writer.writeBoolean(false); // can use tailslap
        writer.writeUInt16(0); // tail state
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(this.getWater()); // lava
        writer.writeBoolean(false); // can use tailslap
        writer.writeUInt16(0); // tail state
    }

    private int abilT = 0;

    // private Timer abilTimer = new Timer(1000);
    private Timer abilTimer = new Timer(950);

    private int fireStreaming = 20;

    public void abilityRecharge() {
        if (abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if (abilTimer.isDone()) {
                abilT--;
                if (abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                    fireStreaming = 20;
                }
                abilTimer.reset();
            }
        }
    }

    private boolean canTailslap = true;

    private Timer coolDownTimer = new Timer(1000);

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(o instanceof Animal && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying && ((Animal)o).getInfo().getTier() >= 16 && ((Animal)o).getInfo().getTier() < 5) {
        }
    }


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        if (coolDownTimer.isDone()) {
            this.flag_fliesLikeDragon = true;
         } else {
            coolDownTimer.update(Constants.TICKS_PER_SECOND);
            this.flag_fliesLikeDragon = false;
         }// /ban deleted user bc why not
        if (!canTailslap) {
            tailSlapTimer.update(Constants.TICKS_PER_SECOND);
            if (tailSlapTimer.isDone()) {
                canTailslap = true;
                tailSlapTimer.reset();
            }
        }
    }

    @Override
    public void changeAbility(boolean es) {
        super.changeAbility(es);
        if (!es)
            noUsingAbility();
    }

    public void noUsingAbility() {
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            this.fireStreaming = 20;
            if (abilT < 1 && !this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 7;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
        }
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(),
                this.getRadius(), 16, true, this.getClient().getNickname(), this.getClient().getRoom(),
                this.getClient().getXP() / 2);
        this.getClient().getRoom().addObj(carcass);
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            if (this.getWater() > this.maxwater / 4){
            for (int i = 0; i < 4; i++) {
                          int mangle = 20;
                if(this.getClient().getXP() >= 20000000) mangle = 20;
                double ran = Utils.randomDouble(-2, 2);
                double x = ((Math.cos(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
                double y = ((Math.sin(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
                FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies() + 5, this.getAngle(), 25, this.getId(), this.getClient().getRoom(), this.getClient(),5);
                this.getClient().getRoom().addObj(newFire);

                 mangle = 0;
                 if(getClient().getXP() >= 20000000){
                ran = Utils.randomDouble(-2, 2);
                x = ((Math.cos(Math.toRadians(this.getAngle()+mangle+ran)) * (this.getRadius())));
                y = ((Math.sin(Math.toRadians(this.getAngle()+mangle+ran)) * (this.getRadius())));
                newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies() + 5, this.getAngle(), 25, this.getId(), this.getClient().getRoom(), this.getClient(),5); //s=20
                this.getClient().getRoom().addObj(newFire);
                 }

                    ran = Utils.randomDouble(-2, 2);
                    x = ((Math.cos(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                    y = ((Math.sin(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                    newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies()+ 5, this.getAngle(), 25, this.getId(), this.getClient().getRoom(), this.getClient(),5);
                    this.getClient().getRoom().addObj(newFire);
            
                
            }
            this.fireStreaming--;
            this.takeWater(0.5);

            if (this.fireStreaming < 0) {
                if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = 7;
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                this.fireStreaming = 20;
            }
            }
        }
    }
}
