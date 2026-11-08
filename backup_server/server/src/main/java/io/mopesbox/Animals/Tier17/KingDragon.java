package io.mopesbox.Animals.Tier17;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Animals.Tier6.Bee;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Milestones.MilestoneState;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Bones.AnimalCarcass;
import io.mopesbox.Objects.ETC.IceCrystal;
import io.mopesbox.Objects.ETC.KingDragonFx;
import io.mopesbox.Objects.ETC.Tailslap;
import io.mopesbox.Objects.Static.SandboxArena;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import java.util.ArrayList;
import java.util.List;

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
            MilestoneState ms = this.getClient().milestones;
            Tailslap tailslap = new Tailslap(this.getClient().getRoom().getID(), this.getX() - x, this.getY() - y,
                    (int) Math.round(this.getRadius() * ms.tailSizeMult()), this.getClient().getRoom(), this.getAngle(), this,
                    this.getInfo().getAnimalSpecies());
            tailslap.setDamage((int) Math.round(200 * ms.tailDamageMult()));
            this.getClient().getRoom().addObj(tailslap);
            if (!this.getClient().instantRecharge)
                canTailslap = false;
            // milestones shorten the 15 s tail slap recharge
            tailSlapTimer.setMaximumTime((int) Math.round(15000 * ms.tailCooldownMult()));
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
                    fireStreaming = this.getClient().milestones.streamLength();
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
            this.fireStreaming = this.getClient().milestones.streamLength();
            if (abilT < 1 && !this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = this.milestoneCooldown(7);
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
            MilestoneState ms = this.getClient().milestones;
            int species = MilestoneState.fireSpecies(this);
            for (int i = 0; i < 4; i++) {
                int mangle = 20;
                double ran = Utils.randomDouble(-2, 2);
                double x = ((Math.cos(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
                double y = ((Math.sin(Math.toRadians(this.getAngle()-mangle+ran)) * (this.getRadius())));
                ms.fireBall(this, this.getX()+x, this.getY()+y, species, this.getAngle());

                if(getClient().getXP() >= 20000000){
                    ran = Utils.randomDouble(-2, 2);
                    x = ((Math.cos(Math.toRadians(this.getAngle()+ran)) * (this.getRadius())));
                    y = ((Math.sin(Math.toRadians(this.getAngle()+ran)) * (this.getRadius())));
                    ms.fireBall(this, this.getX()+x, this.getY()+y, species, this.getAngle());
                }

                ran = Utils.randomDouble(-2, 2);
                x = ((Math.cos(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                y = ((Math.sin(Math.toRadians(this.getAngle()+20+ran)) * (this.getRadius())));
                ms.fireBall(this, this.getX()+x, this.getY()+y, species, this.getAngle());
            }
            // skill-tree extras: a fan of side fireballs once per burst
            int extra = ms.volley();
            if (extra > 0) ms.fireFan(this, species, extra + 1, 10 * ms.spreadMult(), this.getAngle());

            this.fireStreaming--;
            this.takeWater(0.5 * ms.lavaMult());

            if (this.fireStreaming < 0) {
                if (!this.getClient().instantRecharge) {
                    abil.setRecharging(true);
                    abilT = this.milestoneCooldown(7);
                    this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
                }
                this.fireStreaming = ms.streamLength();
            }
            }
        }
    }

    // ---------------------------------------------------------------- Wing Slash, Wingspan, Shoot Meteor
    // Every King Dragon has these, on keys Z, X and C (the client sends message 120, action 4).
    // The frames they show are made by tools/kingdragon_fx/make_fx_frames.py.

    public static final String[] EXTRA_NAMES = { "Wing Slash", "Wingspan", "Shoot Meteor" };
    public static final String[] EXTRA_KEYS = { "Z", "X", "C" };
    public static final String[] EXTRA_ICONS = { "wingslash", "wingspan", "meteor" };
    public static final int[] EXTRA_COOLDOWN = { 6, 14, 20 }; // seconds
    private static final int METEOR_LAVA = 20;                // % of lava a meteor costs

    private final long[] extraReady = new long[3];

    /** Milliseconds until an extra ability can be used again. */
    public long extraLeft(int i) {
        return Math.max(0, extraReady[i] - System.currentTimeMillis());
    }

    /** Uses Wing Slash (0), Wingspan (1) or Shoot Meteor (2). Returns whether it happened. */
    public boolean castExtra(int i) {
        if (i < 0 || i > 2) return false;
        if (this.isRammed || this.inArena || this.flag_flying || this.isInHole() || this.isGhost || this.isStunned()
                || this.health <= 0)
            return false;
        long now = System.currentTimeMillis();
        if (now < extraReady[i]) return false;
        if (i == 2 && this.getWater() < METEOR_LAVA) {
            this.getClient().send(Networking.personalGameEvent(255, "Not enough lava for a meteor!"));
            return false;
        }
        switch (i) {
            case 0: wingSlash(); break;
            case 1: wingspan(); break;
            default: shootMeteor(); break;
        }
        extraReady[i] = this.getClient().instantRecharge ? now
                : now + this.milestoneCooldown(EXTRA_COOLDOWN[i]) * 1000L;
        return true;
    }

    /** Animals the extra abilities can hit: not this dragon, nor anything flying, hiding or protected. */
    private List<Animal> targets() {
        Room room = this.getClient().getRoom();
        List<GameClient> cs = new ArrayList<>(room.clients);
        cs.addAll(room.aiClients);
        List<Animal> out = new ArrayList<>();
        for (GameClient c : cs) {
            Animal o = c == null ? null : c.getPlayer();
            if (o == null || o == this || o.inArena || o.isInvincible() || o.flag_flying || o.isInHole()
                    || o.isDiveActive() || o.isGhost || o.health <= 0)
                continue;
            out.add(o);
        }
        return out;
    }

    private void push(Animal o, double fromX, double fromY, double force) {
        double th = Math.atan2(o.getY() - fromY, o.getX() - fromX);
        o.setVelocityX(Math.cos(th) * force);
        o.setVelocityY(Math.sin(th) * force);
    }

    private void addFx(double x, double y, int radius, int kind, double angle, int lifeMs) {
        Room room = this.getClient().getRoom();
        room.addObj(new KingDragonFx(room.getID(), x, y, radius, kind, angle, lifeMs, room));
    }

    /** A claw of fire sweeping across the front: hits everything in a 140-degree cone. */
    private void wingSlash() {
        double reach = this.getRadius() * 2.4;
        for (Animal o : targets()) {
            double d = Utils.distance(this.getX(), o.getX(), this.getY(), o.getY());
            if (d > reach + o.getRadius()) continue;
            double ang = Math.toDegrees(Math.atan2(o.getY() - this.getY(), o.getX() - this.getX()));
            double diff = ((ang - this.getAngle()) % 360 + 540) % 360 - 180;
            if (Math.abs(diff) > 70) continue;
            o.hurt(55, 0, this);
            o.setFire(3, this, 2);
            push(o, this.getX(), this.getY(), 18);
        }
        addFx(this.getX(), this.getY(), (int) reach, KingDragonFx.WINGSLASH, this.getAngle(), 400);
    }

    /** Both wings snap open: pushes every animal around away, hurts and stuns it. */
    private void wingspan() {
        double reach = this.getRadius() * 3.2;
        for (Animal o : targets()) {
            double d = Utils.distance(this.getX(), o.getX(), this.getY(), o.getY());
            if (d > reach + o.getRadius() || d < 1) continue;
            o.hurt(35, 0, this);
            o.setStun(1);
            push(o, this.getX(), this.getY(), 18 + 22 * (1 - Math.min(1, d / reach)));
        }
        addFx(this.getX(), this.getY(), (int) reach, KingDragonFx.WINGSPAN, this.getAngle(), 600);
    }

    /** Launches a meteor that lands about 650 pixels ahead (meteorLanded does the damage). */
    private void shootMeteor() {
        this.takeWater(METEOR_LAVA);
        double a = this.getAngle();
        double x = this.getX() + Math.cos(Math.toRadians(a)) * this.getRadius();
        double y = this.getY() + Math.sin(Math.toRadians(a)) * this.getRadius();
        Room room = this.getClient().getRoom();
        KingDragonFx m = new KingDragonFx(room.getID(), x, y, 120, KingDragonFx.METEOR, a, 0, room)
                .flyAsMeteor(this, 24, 26);
        m.setSpawnID(this.getId());
        room.addObj(m);
    }

    /** The meteor hit the ground: blast damage that drops off with distance, and burning. */
    public void meteorLanded(double x, double y) {
        double blast = 230;
        for (Animal o : targets()) {
            double d = Utils.distance(x, o.getX(), y, o.getY());
            if (d > blast + o.getRadius()) continue;
            double f = d < blast / 2 ? 1 : 1 - (d - blast / 2) / (blast / 2 + o.getRadius()) * 0.6;
            o.hurt(140 * f, 0, this);
            o.setFire(5, this, 3);
            push(o, x, y, 26 * f);
        }
        addFx(x, y, (int) blast, KingDragonFx.IMPACT, 0, 700);
    }
}
