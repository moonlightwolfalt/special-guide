package io.mopesbox.Animals.Tier15;

import io.mopesbox.Constants;
import io.mopesbox.Ability.PlayerAbility;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Info.AnimalInfo;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Objects.Fun.Sleigh;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;


public class Rudolph extends Animal {
    private Sleigh sleigh;
    public Rudolph(int id, double x, double y, AnimalInfo info, String playerName, GameClient client) {
        super(id, x, y, info, playerName, client);
        info.getAbility().setPossible(true);
        info.getAbility().setUsable(true);
        info.getAbility().setHoldAbility(true);
        this.setSpeed(8);
        getClient().setCamzoom(1400);
    }
    private Animal owner;
    private int rowNumber;

    public void setOwner(Animal owner) {
	this.owner = owner;
    }
    public void setRow(int a){
        this.rowNumber = a;
    }
    public void setSleigh(Sleigh a){
        this.sleigh = a;
        this.sleigh.deers.add(this);

    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        // int bot = 0
        // writer.writeUInt8(bot); // isbot
        // if(bot == 1){
            writer.writeUInt32(this.owner.getId());
            writer.writeUInt8(rowNumber);
            writer.writeUInt8(this.getPlayerName().equals("Rudoplh")  ? 1 : 0);
        // }
        writer.writeUInt16(((short) 450));

                writer.writeUInt16(((short) 450));

          if(this.sleigh.deers != null && this.sleigh.deers.size() > 3){
        writer.writeUInt16(((short)this.sleigh.deers.get(0).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(1).getY() * 4));

        writer.writeUInt16(((short)this.sleigh.deers.get(0).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(1).getY() * 4));

                writer.writeUInt16(((short)this.sleigh.deers.get(2).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(2).getY() * 4));

        writer.writeUInt16(((short)this.sleigh.deers.get(2).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(2).getY() * 4));
        }else{
               writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

        writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

                       writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

        writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));
        }


    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {

        super.writeCustomData_onUpdate(writer);
        
        writer.writeUInt16(450);
        if(this.sleigh.deers != null && this.sleigh.deers.size() > 3){
        writer.writeUInt16(((short)this.sleigh.deers.get(0).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(1).getY() * 4));

        writer.writeUInt16(((short)this.sleigh.deers.get(0).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(1).getY() * 4));

                writer.writeUInt16(((short)this.sleigh.deers.get(2).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(2).getY() * 4));

        writer.writeUInt16(((short)this.sleigh.deers.get(2).getX() * 4));
        writer.writeUInt16(((short)this.sleigh.deers.get(2).getY() * 4));
        }else{
               writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

        writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

                       writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));

        writer.writeUInt16(((short)getX() * 4));
        writer.writeUInt16(((short)getY() * 4));
        }

        
    }
    
    
    Timer timeOutTimer = new Timer(3000);

    Ability abil;


    @Override
    public void update() {
        super.update();
        abilityRecharge();
        timeOutTimer.update(Constants.TICKS_PER_SECOND);

        

          PlayerAbility abil = this.getInfo().getAbility();

        if(this.getBiome() == 1) {
            abil.setUsable(false);
        }

    }

    public void useAbility() {
        super.useAbility();
        PlayerAbility abil = this.getInfo().getAbility();
        if(abil.isPossible() && !abil.isRecharging() && abil.isUsable()) {
         
            // this.abil = digfood;

           
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
    public void disableAbility(){
                PlayerAbility abil = this.getInfo().getAbility();

         if(!this.getClient().instantRecharge) {
                abil.setRecharging(true);
                abilT = 8;
                this.getClient().send(Networking.rechargingAbility(false, abilT, this.getClient().getRoom()));
            }else{
                abil.setRecharging(false);
                abilT = 0;

            }
            abilTimer.reset();

        this.setMove(true);
     
        this.getInfo().getAbility().setActive(false);
       
        
    }
     @Override
    public void onCollision(GameObject o) {
        super.onCollision(o);
        if(getCollideableIs(o) && o instanceof Animal && ((Animal)o).getInfo().getTier() >= 6 && this.biteCooldown <= 0 && !this.isInHole() && !this.flag_flying) {
            
        }
    }

    public boolean getCollideableIs(GameObject o) {
        if(o instanceof Animal) {
            if(((Animal)o).flag_flying) return false;
            if(((Animal)o).isInHole()) return false;
            if(((Animal)o).isDiveActive()) return false;
        }
        return true;
    }

  
}
