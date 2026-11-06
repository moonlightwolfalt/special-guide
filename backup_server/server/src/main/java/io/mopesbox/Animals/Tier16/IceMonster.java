package io.mopesbox.Animals.Tier16;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.ETC.IceCrystal;
import io.mopesbox.Objects.ETC.IceShard;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Client.GameClient;

public class IceMonster extends Animal {
    private List<IceShard> iceShards = new ArrayList<>();
    public IceMonster(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setArcticAnimal(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setMaxHealth(450);
        this.biteDamage = 60;
        this.setSlidingOnIce(false);
        this.addCrystal(0, -50, 0);
        this.addCrystal(50, -80, 30);
        this.addCrystal(-80, -10, 310);
        this.addCrystal(80, -10, 30);
        this.addCrystal(-50, -80, 330);
        this.setSpeed(12);
        getClient().setCamzoom(1300);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(iceShards.size()); // crystal count
        for(IceShard ice : iceShards) {
            writer.writeInt16((short) ice.x);
            writer.writeInt16((short) ice.y);
            writer.writeUInt16((short) ice.radius*100);
            writer.writeUInt16((short) ice.angle*100);
        }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(iceShards.size()); // crystal count
        for(IceShard ice : iceShards) {
            writer.writeInt16((short) ice.x);
            writer.writeInt16((short) ice.y);
            writer.writeUInt16((short) ice.radius*100);
            writer.writeUInt16((short) ice.angle*100);
        }
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if(abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if(abilTimer.isDone()) {
                abilT--;
                if(abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                }
                abilTimer.reset();
            }
        }
    }

    public void addCrystal(double x, double y, double angle) {
        IceShard ice = new IceShard(x, y, 8, angle);
        this.iceShards.add(ice);
    }

    public int hasStableCrystal() {
        for(IceShard ice : iceShards) {
            if(ice.radius >= 8) return iceShards.indexOf(ice);
        }
        return -1;
    }

    public void updateShards() {
        for(IceShard ice : iceShards) {
            if(ice.radius <= 8) ice.radius += 1;
        }
    }

    private Timer crystalUpdateTimer = new Timer(5000);

    @Override
    public void update() {
        super.update();
        abilityRecharge();
        crystalUpdateTimer.update(Constants.TICKS_PER_SECOND);
        if(crystalUpdateTimer.isDone()) {
            this.updateShards();
            crystalUpdateTimer.reset();
        }
 
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable() && this.iceShards.size() > 0 && this.hasStableCrystal() != -1) {
            double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius()*1.2)));
            IceCrystal newFire = new IceCrystal(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 10, this.getInfo().getAnimalSpecies(), this.getAngle(), 20, this.getId(), this.getClient().getRoom(), this.getClient());
            this.getClient().getRoom().addObj(newFire);
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 5;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                this.iceShards.get(this.hasStableCrystal()).radius = 0;
            }
        }
    }
}
