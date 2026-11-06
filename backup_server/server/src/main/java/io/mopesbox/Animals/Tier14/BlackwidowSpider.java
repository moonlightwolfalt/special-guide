package io.mopesbox.Animals.Tier14;

import java.util.Map;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.blackWidowAbility;
import io.mopesbox.Objects.Static.Rock;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;

public class BlackwidowSpider extends Animal {
    public BlackwidowSpider(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        this.setCanClimbHills(true);
        this.setSlidingOnIce(false);
        this.setSpeed(8);
        this.setCamzoom(1500);
        this.setMaxHealth(300);
        this.setCanClimbRocks(true);
        this.setDesertAnimal(true);
        this.getInfo().getAbility().setUsable(true);
        this.getInfo().getAbility().setPossible(true);

    }
    public int webState = 0;
    private int abilT;
    private Timer abilTimer = new Timer(950);
    private boolean gettingBackWeb = false;
    public  blackWidowAbility web;
  @Override
    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
            if(webState == 1){
                gettingBackWeb = true;
                return;
            }
            if(abil.isActive()){
                webState = 1;
                double x = ((Math.cos(Math.toRadians(this.getAngle())) * (this.getRadius())));
                double y = ((Math.sin(Math.toRadians(this.getAngle())) * (this.getRadius())));
             web = new blackWidowAbility(this.getClient().getRoom().getID(), getX()-x, getY()-y, this.getRadius() *  (int) 1.4, this.getClient().getRoom(),this.getAngle(), this, this.getInfo().getAnimalSpecies());
             getRoom().addObj(web);
            }
            if(webState == 0){
                abil.setActive(true);
                this.setSpeed(4);
            }
      
        
    }
}
@Override
public void update(){
super.update();
abilityRecharge();
}
public void disableAbility(){
            PlayerAbility abil = this.getInfo().getAbility();
            abil.setActive(false);
                webState = 0;
                this.setSpecies(0);
                gettingBackWeb = false;
                this.setSpeed(8);

    if (!this.getClient().instantRecharge) {
        abil.setRecharging(true);
        abilT = 8;
        this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            abilTimer.reset();

    }
}
   public void abilityRecharge() {
    if(gettingBackWeb && web != null){
        double diffX = this.getX() - web.getX();
        double diffY = this.getY() - web.getY();
        
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);
        
        if (distance != 0) {
            double normalizedDiffX = diffX / distance;
            double normalizedDiffY = diffY / distance;
        
            double velocidad = 15;
        
            double webVelocityX = normalizedDiffX * velocidad;
            double webVelocityY = normalizedDiffY * velocidad;
        

            web.setVelocityX(webVelocityX);
            web.setVelocityY(webVelocityY);
    }
}
    if(this.getInfo().getAbility().isActive()){
     int a = 0;
    for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
        GameObject obj = entry.getValue();
        if (obj instanceof Rock) {
            a++;
        }
    }
        if(a > 0) {this.setSpecies(1); this.flag_stealth = true;}

}else if(this.getSpecies() == 1) {this.setSpecies(0); this.flag_stealth = false;}
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
    public void onCollision(GameObject o){
        if(o instanceof blackWidowAbility && web != null && web.owner != null && web.owner == this && gettingBackWeb){

            getRoom().removeObj(web, this);
            web = null;
 
            this.disableAbility();
        }
    }


    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt8(webState); // web state
        if(webState > 0 && web != null){//First web posision and then player one!
            writer.writeUInt16(((short)web.getX() * 4));
            writer.writeUInt16(((short)web.getY() * 4));
                        writer.writeUInt16(((short)getX() * 4));
            writer.writeUInt16(((short)getY() * 4));


        }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt8(webState); // web state
               if(webState > 0 && web != null){//First web posision and then player one!
            writer.writeUInt16(((short)web.getX() * 4));
            writer.writeUInt16(((short)web.getY() * 4));
                        writer.writeUInt16(((short)getX() * 4));
            writer.writeUInt16(((short)getY() * 4));


        }
    }
}
