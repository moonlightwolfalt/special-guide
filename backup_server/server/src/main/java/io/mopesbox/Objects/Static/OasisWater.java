package io.mopesbox.Objects.Static;
import io.mopesbox.Constants;
import io.mopesbox.Animals.Animal;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Timer;
public class OasisWater extends GameObject
{
    private boolean isOasisWater = true;
    private Oasis oasis;
    private boolean isShrinking = false;
    private boolean isActive = false;
    private Timer toggleTimer = new Timer(20000);

    public OasisWater(int id, Oasis oasis) {
        super(id, oasis.getX(), oasis.getY(), oasis.getRadius()/6, 100);
        this.oasis = oasis;
        this.setCollideable(false);
        this.setCollideCallbacking(true);
    }

    @Override
    public void update() {
        super.update();
        if(isActive) {
            if(isShrinking) {
                this.setRadius(this.getRadius() - 1);
                if(this.getRadius() < oasis.getRadius()/6) {
                    this.isShrinking = false;
                    this.isActive = false;
                }
            } else {
                this.setRadius(this.getRadius() + 1);
                if(this.getRadius() > oasis.getRadius()/1.2) {
                    this.isShrinking = true;
                    this.isActive = false;
                }
            }
        } else {
            toggleTimer.update(Constants.TICKS_PER_SECOND);
            if(toggleTimer.isDone()) {
                isActive = true;
                toggleTimer.reset();
            }
        }
    }

    @Override
    public void onCollision(GameObject object) {
        if(object instanceof Animal && !((Animal)object).inArena && !((Animal)object).isRammed && !((Animal)object).flag_flying) {
            object.setBiome(1);
            ((Animal)object).ignoreBiomes();
        }
    }

    @Override
    public void writeCustomData_onUpdate(MsgWriter writer) {
        super.writeCustomData_onUpdate(writer);
        writer.writeBoolean(isOasisWater);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeBoolean(isOasisWater);
    }
}
