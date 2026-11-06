package io.mopesbox.Objects.Static;

import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Fire.FireBall;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

public class Volcano extends GameObject
{
        private Room room;

    public Volcano(int id, double x, double y, Room room, int Biome) {
        super(id, x, y, 110, 42);
        this.setBiome(Biome);
        this.room = room;
    }
    private Timer changeStage = new Timer(480000);
    private int currentStage = 0;
    private Timer eruptionTimer = new Timer(120000);
        private Timer fireTimer = new Timer(2000);

        private Timer lavaTimer = new Timer(6000);
        private int times;

    private int shakeStart = 5;
    public void updateStage(){


        if(currentStage >= 7){
          eruptionTimer.update(Constants.TICKS_PER_SECOND);
                if(eruptionTimer.isDone()){
                    this.eruptionTimer.reset();
                    currentStage = 0;
                    this.times = 0;
                }
            }
    
                  if(currentStage >= 5){
                    fireTimer.update(Constants.TICKS_PER_SECOND);
                    if(fireTimer.isDone()){
                    FireBall fire = new FireBall(this.room.getID(), getX(), getY(), 45, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);

                      fire = new FireBall(this.room.getID(), getX(), getY(), 45, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);

                      fire = new FireBall(this.room.getID(), getX(), getY(), 45, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);
                    fireTimer.reset();
                   
                    }
                }
                if(times < 20 && currentStage >= 6){
                lavaTimer.update(Constants.TICKS_PER_SECOND);
                if(lavaTimer.isDone()){
                    times++;
                    LavaDrop drop = new LavaDrop(this.room.getID(), getX(), getY(), 60, 0, Utils.randomDouble(0, 360), this.getId(), this.room);
                    this.room.addObj(drop);
                    lavaTimer.reset();
                }
                
            }
        // if(currentStage 0){
            changeStage.update(Constants.TICKS_PER_SECOND);
            if(changeStage.isDone()){

                if(currentStage >= 3){
                    FireBall fire = new FireBall(this.room.getID(), getX(), getY(), 35, 0, Utils.randomDouble(0, 360), 25, this.getId(), this.room, null, 0);
                    this.room.addObj(fire);
                    }
                
      
                if(currentStage >= 9){
                currentStage = 100;
                // if(currentStage == 11) currentStage = 0;
                }else
                currentStage++;
                
                if(currentStage >= 6){
                    this.times = 0;
                }

                changeStage.reset();

                
            }

    }

    @Override
    public void update(){
        super.update();
        updateStage();
    }
    @Override
    public boolean getCollideable(GameObject o) {
        if(o instanceof Animal) {
            Animal a = (Animal)o;
            if(a.getFlyingOver()) return false;
            if(a.getCanClimbHills()) return false;
            if(currentStage >= 3) return true;
            return true;
        }
        return true;
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeUInt16(currentStage * 100);
        writer.writeUInt8(shakeStart * 10);
        writer.writeUInt8(11);
    }
    @Override
    public void onCollision(GameObject o){
        if(o instanceof Animal && ((Animal)o).getCollideable(this) && this.currentStage >= 3){
            if(!((Animal)o).isLavaAnimal() && !((Animal)o).flag_flying){
                if(((Animal)o).fireSeconds <= 1){
                    ((Animal)o).setFire(3, null, 5);
                    // ((Animal)o).getClient().send(Networking.personalGameEvent(255, "OUCH! Its hot near volcano."))
                }
            }
        }
    }
}
