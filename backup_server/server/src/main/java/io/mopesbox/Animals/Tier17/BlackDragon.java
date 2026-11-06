package io.mopesbox.Animals.Tier17;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Animals;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Networking.Selection;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.IceCrystal;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Objects.PvP.PvPArena;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Utils.Apex;
import io.mopesbox.Objects.PvP.Target;
import io.mopesbox.Objects.Static.SandboxArena;

public class BlackDragon extends Animal {
    private List<Apex> apexs = new ArrayList<>();
    public BlackDragon(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.flag_fliesLikeDragon = true;
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        this.setCanBeUnderTree(false);
        this.setFlyingOver(true);
        this.setMaxHealth(775);
        this.setCanBeInHole(false);
        this.biteDamage = 60;
        this.setBarType(2);
        this.setLavaAnimal(true);
        this.setSpeed(8);
        getClient().setCamzoom(1000);
    }

    @Override
    public void onKill(Animal victim) {
        this.addApex(victim.getInfo().getAnimalType(), victim.getClient());
        if(victim instanceof BlackDragon) {
            this.apexs.addAll(((BlackDragon)victim).apexs);
        }
        if(apexRemain()) {
            this.getClient().setInUpgrade(true);
            List<AnimalInfo> infos = new ArrayList<>();
            infos.add(Animals.byID(100));
            this.getClient().send(Selection.createSelection(this.getClient(), 0, 15, 17, infos));
        }
    }

    public boolean apexRemain() {
        if(this.apexs.size() >= 13) return true;
        if(13-this.apexs.size() == 0) return true;

        return false;

    }

    public boolean checkApex(int type) {
        for(Apex apex : this.apexs) {
            if(apex.getType() == type) return true;
        }
        return false;
    }

    public void addApex(int type, GameClient gid) {
        if(type != 46 && type != 71 && type != 73 && type != 70 && type != 72 && type != 95 && type != 68 && type != 14 && type != 53 && type != 24 && type != 61 && type != 32 && type != 96) return;
        for(Apex apex : this.apexs) {
            if(apex.getType() == type) return;
        }
        this.apexs.add(new Apex(type, gid));
    }

    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal && ((Animal)o).getInfo().getTier() <= 16 && ((Animal)o).getInfo().getTier() > 5  && !this.flag_flying && !(((Animal)o) instanceof Bee)) {
            return true;
        }
        if(o instanceof SandboxArena && ((SandboxArena)o).getCollideable(this)){
        return true;
        }
                    if(o instanceof IceCrystal || o instanceof Target) return true;
                    if (o instanceof Bee)
                        return false;


        return false;
    }
    
    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(this.getWater()); // lava
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(this.getWater()); // lava
    }

    private int abilT = 0;

    private Timer abilTimer = new Timer(950);

    private Timer coolDownTimer = new Timer(2000);

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
    }

    @Override
    public void onDeath() {
        AnimalCarcass carcass = new AnimalCarcass(this.getClient().getRoom().getID(), this.getX(), this.getY(), this.getRadius(), 16, true, this.getClient().getNickname(), this.getClient().getRoom(), this.getClient().getXP()/2);
        this.getClient().getRoom().addObj(carcass);
    }

    @Override
    public void useAbility() {
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            if (this.getWater() > this.maxwater / 4){
            int mangle = 20;
            this.takeWater(3);
            if(this.getClient().getXP() >= 20000000) mangle = 20;
            double ran = Utils.randomDouble(-2, 2);
            double x = ((Math.cos(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
            double y = ((Math.sin(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
            FireBall newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies(), this.getAngle() - 12.5, 25, this.getId(), this.getClient().getRoom(), this.getClient(),5);
            this.getClient().getRoom().addObj(newFire);
             mangle = 0;

             if(this.getClient().getXP() >= 20000000){
            ran = Utils.randomDouble(-2, 2);
            x = ((Math.cos(Math.toRadians(this.getAngle()+mangle+ran)) * (this.getRadius())));
            y = ((Math.sin(Math.toRadians(this.getAngle()+mangle+ran)) * (this.getRadius())));
            newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies(), this.getAngle(), 25, this.getId(), this.getClient().getRoom(), this.getClient(),5); //s=20
            this.getClient().getRoom().addObj(newFire);
             }

                ran = Utils.randomDouble(-2, 2);
                x = ((Math.cos(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                y = ((Math.sin(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                newFire = new FireBall(this.getClient().getRoom().getID(), this.getX()+x, this.getY()+y, 30, this.getInfo().getAnimalSpecies(), this.getAngle() + 12.5, 25, this.getId(), this.getClient().getRoom(), this.getClient(),5);
                this.getClient().getRoom().addObj(newFire);
            
            if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 1;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }
        }
        }
    }
        public void biteCollision(double distance, Animal prey, Animal biter, PvPArena pvp, boolean diffAniCollision) {
        if (biter.biteCooldown < 1 && !biter.flag_flying && !biter.isInvincible() && !biter.isInHole()
                && !biter.isDiveActive() && !biter.flag_flying && !prey.flag_flying && !prey.flag_flying
                && !prey.isInvincible() && !prey.isInHole() && !prey.isDiveActive()) {
            double difference = prey.getAngle() - biter.getAngle();
            while (difference < -180)
                difference += 360;
            while (difference > 180)
                difference -= 360;

            if (difference < 0) {
                difference = -difference;
            }
            if ((difference < 26 && Utils.distance(biter.getX(), prey.getX(), biter.getY(),
                    prey.getY()) <= (biter.getRadius() + prey.getRadius()) / 2)) {
                double en1x = ((Math.cos(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en1y = ((Math.sin(Math.toRadians(biter.getAngle()))
                        * (biter.getRadius() - (biter.getRadius() / 2))));
                double en2x = ((Math.cos(Math.toRadians(prey.getAngle())) * (prey.getRadius())));
                double en2y = ((Math.sin(Math.toRadians(prey.getAngle())) * (prey.getRadius())));
                if (Utils.distance(biter.getX() + en1x, prey.getX() - en2x, biter.getY() + en1y,
                        prey.getY() - en2y) <= (biter.getRadius() * 1.2)) {
                    // 40 base xp + 2% of prey xp but can't be more than double biter xp
                    if (pvp == null) {
                        double littleXP = prey.getClient().getXP() / 100 * 2;
                        double calculatedXPGiven = Math.min(Math.max(40.0 + littleXP, 0), biter.getClient().getXP());
                        biter.getClient().addXp((int) Math.floor(calculatedXPGiven));
                        prey.getClient().takeXp((int) Math.floor(calculatedXPGiven));
                        
                        if (biter.getInfo().getTier() >= 17 && biter.getInfo().getTier() == prey.getInfo().getTier()) {
                            double addHp1 = prey.maxHealth / 25;
                            if (biter.health + addHp1 < biter.maxHealth)
                                biter.health += addHp1;
                        }
                    }
                    prey.hurt(prey.maxHealth / 10, 2, biter);
                    // this.getClient().getRoom().getCollisionHandler().biteCollisionDynamic(biter.getRadius(), biter, prey);
                    if (pvp != null)
                        pvp.addBite(biter);
                    biter.setBiteCooldown(1);
                    // double enx = ((Math.cos(Math.toRadians(biter.getAngle())) *
                    // (prey.getRadius())));
                    // double eny = ((Math.sin(Math.toRadians(biter.getAngle())) *
                    // (prey.getRadius())));
                    // prey.forcedBoost(biter.getX()+enx, biter.getY()+eny, 180
                    // Main.log.info("Collision. prey: " + prey.getPlayerName() + " biter: " + biter.getPlayerName());
                    prey.getClient().send(Networking.personalGameEvent(2, null)); //
                                        biter.getClient().send(Networking.personalGameEvent(18, null)); //

                }
            }
        }
    }
}
