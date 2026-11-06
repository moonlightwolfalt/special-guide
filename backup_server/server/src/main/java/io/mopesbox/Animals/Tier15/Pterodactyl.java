package io.mopesbox.Animals.Tier15;
import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Animals.Info.Tier;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Eatable.AloeVera;
import io.mopesbox.Objects.Eatable.AloeVeraLeaf;
import io.mopesbox.Objects.Eatable.Banana;
import io.mopesbox.Objects.Eatable.Cactus;
import io.mopesbox.Objects.Eatable.Clam;
import io.mopesbox.Objects.Eatable.Coconut;
import io.mopesbox.Objects.Eatable.ConchShell;
import io.mopesbox.Objects.Eatable.Kelp;
import io.mopesbox.Objects.Eatable.LilyPad;
import io.mopesbox.Objects.Eatable.Meat;
import io.mopesbox.Objects.Eatable.Mushroom;
import io.mopesbox.Objects.Eatable.MushroomBush;
import io.mopesbox.Objects.Eatable.RedMushroom;
import io.mopesbox.Objects.Eatable.SeaWeed;
import io.mopesbox.Objects.Eatable.StarFish;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Utils.Timer;
import java.util.Map;

public class Pterodactyl extends Animal {
    public Pterodactyl(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setBarType(3);
        this.setMaxHealth(300);
        this.setDesertAnimal(true);
        this.setCanClimbRocks(true);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.setSpeed(8);
        radiusTimer.isRunning = false;
        getClient().setCamzoom((int) (Tier.byOrdinal(getInfo().getTier()).getBaseZoom() * 950));
        getClient().setCamzoom(1400);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(tryingToPick || landing ? 1 : 0); // is gliding

    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(tryingToPick || landing ? 1 : 0); // is gliding
    }

    @Override
    public double speedManipulation(double speed) {
        if (this.getSpecies() == 1) {
            if (isFlying)
                return speed * 1.5 * 1.5;
            else if (landing || tryingToPick)
                return speed * 1.6 * 1.6;
            else
                return speed;
        }

        return speed;
    }

    private Timer abilTimer = new Timer(950);

    public void abilityRecharge() {
        if (abilT > 0) {
            abilTimer.update(Constants.TICKS_PER_SECOND);
            if (abilTimer.isDone()) {
                abilT--;
                if (abilT < 1) {
                    this.getInfo().getAbility().setRecharging(false);
                }
                abilTimer.reset();
            }
        }
    }

    public boolean getCollideableIs(GameObject o) {
        if (o instanceof Animal) {
            if (((Animal) o).flag_flying)
                return false;
            if (((Animal) o).isInHole())
                return false;
            if (((Animal) o).isDiveActive())
                return false;
        }
        return true;
    }

    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if (!this.flag_flying) {

            // this.getClient().setCamzoom((this.getClient().getBaseZoom()));
        
        if(o instanceof Cactus || o instanceof AloeVera || o instanceof AloeVeraLeaf || o instanceof Kelp || o instanceof SeaWeed || o instanceof Clam || o instanceof ConchShell || o instanceof Coconut || o instanceof Banana || o instanceof Meat || o instanceof LilyPad || o instanceof Mushroom || o instanceof MushroomBush || o instanceof RedMushroom || o instanceof StarFish){
            if(o instanceof Cactus){
                if(((Cactus)o).health <= 0){
                    this.addWater(12);
                    // return;
                }
                // return;
            }else
                if(o instanceof ConchShell){
                if(((ConchShell)o).health <= 0){
                    this.addWater(12);
                    // return;
                }
                // return;
            }else
                            if(o instanceof MushroomBush){
                if(((MushroomBush)o).health <= 0){
                    this.addWater(12);
                    // return;
                }
                // return;
            }else
                if(o instanceof AloeVera){
                if(((AloeVera)o).health <= 0){
                    this.addWater(12);
                    // return;
                }
                // return;
            }else
                if(o instanceof Meat){
                    this.addWater(17);
                    // return;
                
            }else
            this.addWater(4);
            

        }
    }
    }

    private boolean isRunning = false;

    private boolean isFlying = false;

    private boolean landing = false;

    private boolean attemptedToPick = false;
    private boolean tryingToPick = false;
    private boolean pickedUp = false;
    private Animal inClaws;
    private int abilT = 0;

    private double step = 0;
    private double currentZ = 0;

    @Override
    public void onAbilityDisable(){
        getClient().setCamzoom((int) (Tier.byOrdinal(getInfo().getTier()).getBaseZoom() * 950));

    }
    @Override
    public void onHurt() {
        if (this.isRunning && !this.getInfo().getAbility().isRecharging())
            this.forceDisable();
    }

    private Timer runTimer = new Timer(3000);
    private Timer flyTimer = new Timer(10000);
    private Timer radiusTimer = new Timer(1000);

    private Timer takeEnergy = new Timer(1000);

    @Override
    public void update() {
        super.update();
        abilityRecharge();

        if(this.getInfo().getAbility().isActive()){
            takeEnergy.update(Constants.TICKS_PER_SECOND);
            if(takeEnergy.isDone()){
                this.takeWater(3);
                takeEnergy.reset();
            }
        }
        if(this.tryingToPick){
            this.setHasCustomCollisionRadius(true);
            this.setCustomCollisionRadius(this.getRadius() * 1.5);
        }else if(!this.tryingToPick && this.isHasCustomCollisionRadius()){
            this.setHasCustomCollisionRadius(false);
        }

        if (this.isFlying || landing && !isRammed && !pickedByBird) {
            this.flag_flying = true;
            this.setSpecies(1);

        } else {
            if (!this.isRunning)
                this.setSpecies(0);
            else
                this.setSpecies(1);

        }
        if(this.pickedByBird && this.getSpecies() > 0){
            this.setSpecies(0);
        }
        if (this.isFlying) {
            flyTimer.update(Constants.TICKS_PER_SECOND);
            if (flyTimer.isDone() || this.water <= 25) {
                disableAbility();
                // this.getClient().getRoom().chat(this.getClient(), "ABILITY DISABLED 2.
                // RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed)));
                double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed)));
                if (inClaws != null && pickedUp == true) {
                    inClaws.makeFall(x, y, this);
                    pickedUp = false;
                    inClaws = null;
                }
                flyTimer.reset();
            }
        }

        if (this.pickedByBird) {
            radiusTimer.isRunning = false;
        }

        radiusTimer.update(Constants.TICKS_PER_SECOND);
        if (!radiusTimer.isDone()) {

            if (landing || tryingToPick) {
                // decrease size to before size
                currentZ -= step;
                if (currentZ >= 0)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = 0;

                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));

            } else {
                currentZ += step;
                if (currentZ < this.getRadius() * 1.5)
                    this.setZ((int) Math.round(currentZ));
                else
                    currentZ = this.getRadius() * 1.5;

                this.getClient().setCamzoom((int) (Math.round(this.getClient().getBaseZoom() - currentZ)));

            }

        } else {
            if (landing) {
                this.setZ(0);
                this.flag_flying = false;
                this.landing = false;
                this.getInfo().getAbility().setActive(false);
                // attemptedToPick = true;
                // чо за скам поч
                getClient().setCamzoom((int) (Tier.byOrdinal(getInfo().getTier()).getBaseZoom() * 950));
            } else if (tryingToPick) {
                currentZ = this.getRadius() * 1.5;
                this.setZ((int) Math.round(currentZ));
                tryingToPick = false;
                attemptedToPick = true;

                for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
                    GameObject obj = entry.getValue();
                    if (obj instanceof Animal) {
                        if (!((Animal) obj).pickedByBird && !((Animal) obj).flag_flying
                                && !((Animal) obj).isStunned()
                                && ((Animal) obj).getInfo().getTier() <= this.getInfo().getTier()) {
                            pickedUp = true;
                            inClaws = (Animal) obj;
                            // inClaws.getClient().getRoom().chat(inClaws.getClient(),
                            // "Oh no! I got picked up by ptero. :insert here epic flying:");

                            return;
                        }
                    }
                }
                // attemptedToPick = false;
            }

        }

        if (pickedUp && inClaws != null) {
            double x = ((Math.cos(Math.toRadians(this.getAngle()))
                    * (inClaws.getRadius() / 2 + (this.getRadius() * 2))));
            double y = ((Math.sin(Math.toRadians(this.getAngle()))
                    * (inClaws.getRadius() / 2 + (this.getRadius() * 2))));
            inClaws.setX(this.getX() - x);
            inClaws.setY(this.getY() - y);
            inClaws.setZ((int) currentZ);
            inClaws.flag_flying = true;
            inClaws.pickedByBird = true;
            inClaws.pickedBy = this;

            // inClaws.stun(1);
        }

        if (inClaws != null && !inClaws.isDead()) {
            inClaws.birdFallX = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed)));
            inClaws.birdFallY = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed)));
        }
        if (this.isRunning) {
            runTimer.update(Constants.TICKS_PER_SECOND);
            if (runTimer.isDone()) {
                this.isRunning = false;
                this.isFlying = true;

                step = Math.round((this.getRadius() * 1.5) / (1000 / Constants.TICKS_PER_SECOND));
                if (step < 0)
                    step = 1;

                radiusTimer.reset();

                runTimer.reset();
            }
        }
    }

    private void disableAbility() {

        this.isRunning = false;
        this.isFlying = false;
        flyTimer.reset();
        runTimer.reset();
        radiusTimer.reset();
        landing = true;
        tryingToPick = false;
        attemptedToPick = false;
        takeEnergy.reset();
        pickedUp = false;
        getClient().setCamzoom((int) (Tier.byOrdinal(getInfo().getTier()).getBaseZoom() * 950));
        if (inClaws != null) {
            inClaws.setZ(0);
            inClaws.flag_flying = false;
            inClaws = null;
        }

        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }

    }

    public void forceDisable() {
        this.isRunning = false;
        this.isFlying = false;
        landing = false;

        this.getInfo().getAbility().setActive(false);
        this.takeEnergy.reset();
        tryingToPick = false;
        attemptedToPick = false;
        pickedUp = false;
        if (inClaws != null) {
            inClaws.flag_flying = false;
            inClaws = null;
        }
        getClient().setCamzoom((int) (Tier.byOrdinal(getInfo().getTier()).getBaseZoom() * 950));

        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }

    @Override
    public void useAbility() {
        super.useAbility();

        // if (this.getClient().getAccount().admin < 3) return;

        if (!this.isFlying) {
            PlayerAbility abil = this.getInfo().getAbility();
            if (!this.isDiveMain() && !this.isDiveActive()) {
                if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() &&
                        !this.isRunning && !this.isFlying) {
                    this.isRunning = true;
                    abil.setActive(true);
                    // Main.log.info("ABIL STARTED");
                    // this.getClient().getRoom().chat(this.getClient(), "ABILITY START. RUNNING:" +
                    // this.isRunning + " FLYING: " + this.isFlying);
                }
            } else {
                // if(isDivePossible() && !isDiveRecharging() && isDiveUsable() &&
                // !isDiveActive() && this.getBiome() == 1) {
                // this.setDiveActive(true);
                // }
                // this.getClient().getRoom().chat(this.getClient(), "CAN'T START ABILITY.
                // RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
            }
        } else {
            if (radiusTimer.isDone()) {
                if (!attemptedToPick) {
                    if (!tryingToPick) {
                        tryingToPick = true;
                        flyTimer.reset();
                        radiusTimer.reset();
                        // this.getClient().getRoom().chat(this.getClient(), "RESET TIMERS. RUNNING:" +
                        // this.isRunning + " FLYING: " + this.isFlying);
                    }
                } else {//
                    if (inClaws != null && pickedUp == true) {
                        double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed)));
                        double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed)));
                        inClaws.makeFall(x, y, this);
                        pickedUp = false;
                        inClaws = null;
                        // this.getClient().getRoom().chat(this.getClient(), "FALL VELOCITY STUFF.
                        // RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                    } else {
                        disableAbility();
                        // this.getClient().getRoom().chat(this.getClient(), "ABILITY DISABLED 1.
                        // RUNNING:" + this.isRunning + " FLYING: " + this.isFlying);
                    }
                }
            }
        }
    }
}
