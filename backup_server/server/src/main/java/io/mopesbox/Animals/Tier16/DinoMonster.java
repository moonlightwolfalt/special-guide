package io.mopesbox.Animals.Tier16;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.Client.GameClient;

public class DinoMonster extends Animal {
    private double mainSpeed = 12;
    public DinoMonster(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(false);
        this.speed = mainSpeed;
        this.setCanClimbHills(true);
        this.biteDamage = 60;
        this.setMaxHealth(450);
        this.bypassAbilSpeed = true;
        this.setCamzoom(1300);
    }

    public void onHurt() {
        if (this.isRunning)
            this.disableAbility();
    }

    private boolean isRunning = false;
    private double runSpeed = 0;

    private int abilT = 0;

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

    private void disableAbility() {
        this.getInfo().getAbility().setActive(false);
        this.isRunning = false;
        runSpeed = 0;
        this.speed = mainSpeed;
        if (!this.getClient().instantRecharge) {
            this.getInfo().getAbility().setRecharging(true);
            abilT = 8;
            this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
        }
    }
    private void runningUpdate() {
        if (this.isRunning) {
            this.getInfo().getAbility().setActive(true);
            this.speed = mainSpeed + runSpeed;
            if (runSpeed < 35) {
                runSpeed++;
            } else {
                disableAbility();
            }
        }
    }


    @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        
        if (o instanceof Animal) {
            if (!((Animal) o).flag_flying && !((Animal) o).isInHole() && !this.isInHole() && !this.flag_flying
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 36)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 36)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.isRunning && this.runSpeed >= 12
                        && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
                    Animal obj = (Animal) o;
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 8)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 8)));
                   
                        
                    obj.makeRam(x, y, ((Animal) this), 70);
                    obj.flag_usingAbility = false;

                    

                
                    

                    


                    disableAbility();

            
                }
            }
        }
    }



    @Override
    public void onCollisionEnter(GameObject o) {
        super.onCollisionEnter(o);
        if (o instanceof Animal) {
            if (!((Animal) o).flag_flying && !this.flag_flying && !((Animal) o).isInHole() && !this.isInHole()
                    && !this.isInvincible() && !this.isDiveActive() && !((Animal) o).isDiveActive()
                    && !((Animal) o).isInvincible()
                    && (((Animal) o).flag_fliesLikeDragon || !((Animal) o).isOverTree())) {
                double xx = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                double yy = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius() * 2)));
                xx = this.getX() + xx;
                yy = this.getY() + yy;
                if (this.isRunning
                        && ((o instanceof DinoMonster && this.runSpeed > ((DinoMonster) o).runSpeed - 1)
                                || !(o instanceof DinoMonster))
                        && Utils.distance(xx, ((Animal) o).getX(), yy, ((Animal) o).getY()) <= (this.getRadius() * 2)) {
                    Animal obj = (Animal) o;
                    double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.speed / 12)));
                    double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.speed / 12)));


                    
                    obj.makeRam(x, y, ((Animal) this), 40);


        
     


                    disableAbility();

                
             
            }
        }
    }
}


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        runningUpdate();


        PlayerAbility abil = this.getInfo().getAbility();


        if(!this.isInvincible() && !this.isStunned() && !this.isRammed && !this.isInHole()) {
            abil.setUsable(true);
        }

 
    }

    @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if (!this.isDiveMain() && !this.isDiveActive()) {
            if (abil.isPossible() && !abil.isRecharging() && abil.isUsable() && !this.isRunning) {
                this.isRunning = true;
            }
        }
    }
}
