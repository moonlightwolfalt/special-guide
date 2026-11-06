package io.mopesbox.Objects.PvP;

import io.mopesbox.Animals.Animal;
import io.mopesbox.Animals.Tier11.Falcon;
import io.mopesbox.Animals.Tier13.Ostrich;
import io.mopesbox.Animals.Tier8.Snowyowl;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.ETC.Ability;
import io.mopesbox.Server.MsgWriter;

public class Target extends Ability {
    private GameClient owner;
    public int timer;
    private String devStr;
    public Animal targ;
    public Animal targ1;
    private int isMarkhorOrOther = 0;
    public Animal attachedTo;
    public boolean frozen = false;
    public Target(int id, double x, double y, GameClient owner, int type, int isMarkhorOrOther) {
        super(id, x, y, 59, 35, type);
        this.owner = owner;
        this.timer = 20;
        this.setCollideable(false);
        this.isMarkhorOrOther = isMarkhorOrOther;
        this.setCollideCallbacking(true);
        this.setHasCustomCollisionRadius(true);
        this.setCustomCollisionRadius(42);
    }

    @Override
    public void onCollision(GameObject o) {
        if(o instanceof Animal && ((Animal) o) != this.owner.getPlayer() && this.isMarkhorOrOther == 0) {
            this.targ = (Animal) o;
            if(this.isMarkhorOrOther == 0){
                 this.changeStr(this.targ.getPlayerName());
                //  this.secondaryType2 = 3;
            }
        }
        if(this.owner.getPlayer() instanceof Ostrich && o instanceof Animal){
             targ1 = (Animal) o;
            this.changeStr(this.targ1.getPlayerName());

        }
        if(this.owner.getPlayer() instanceof Falcon){
        if(this.owner.falconTarget != null && this.owner.falconTarget.isFreezed()){
            ((Falcon)this.owner.getPlayer()).attacking = false;
            
            ((Falcon)this.owner.getPlayer()).disableAbility();
            ((Falcon)this.owner.getPlayer()).landing = true;
        }
    }
            if(this.owner.getPlayer() instanceof Snowyowl){
        if(this.owner.snowyowlTarget != null && this.owner.snowyowlTarget.isFreezed()){
            ((Snowyowl)this.owner.getPlayer()).attacking = false;
            
            ((Snowyowl)this.owner.getPlayer()).disableAbility();
            ((Snowyowl)this.owner.getPlayer()).landing = true;
        }
    }
    }

    public void setAttach(Animal o){
        this.attachedTo = o;
        // System.out.print("Attached!\n");
    }
    public void updateTimer() {
        this.timer--;
    }

    public void changeStr(String a) {
        this.devStr = a;
    }

    @Override
    public void onCollisionExit(GameObject o) {
        if(this.targ == o) {
            this.targ = null;
            if(o.getType() == 2){
                 this.changeStr(null);
                //  if(this.getSecondaryType() == 3) this.secondaryType2 = ;
            }
        }
            if(this.targ1 == o) {
            this.targ1 = null;
            this.changeStr(null);

        }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        if(this.is1v1Target == 1) {
            writer.writeUInt16(this.timer*100);
        } else if(this.is1v1Target == 2) {
            writer.writeString(this.devStr != null && this.targ1 == null ? this.devStr : " ");
        }
    }

    @Override
    public boolean canBeVisible(GameClient gameClient) {
        if(this.owner != gameClient) return false;
        else return true;
    }

    public Animal getTarget() {
        return this.targ;
    }
    public void setFreeze(boolean a){
         this.frozen = a;
    }
    public boolean isFreezed(){
        return this.frozen;
    }

    public void startUpd(){
        while (true){
        this.update();
        }
    }

    public void update(){
        if(this.attachedTo != null && !this.attachedTo.isDead()){
        this.setRadius(this.attachedTo.getRadius());
        this.setX(this.attachedTo.getX());
        this.setY(this.attachedTo.getY());
        if(this.secondaryType2 != 3) this.secondaryType2 = 3;
        // System.out.print("a\n");
        }
    
    }

}
