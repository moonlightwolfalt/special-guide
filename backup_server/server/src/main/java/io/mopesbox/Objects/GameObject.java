package io.mopesbox.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.mopesbox.Constants;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;
import io.mopesbox.Objects.BattleRoyale.SafeArea;
import io.mopesbox.Objects.ETC.BigWhirlpool;

public class GameObject {

    private int type;
    private double x;
    private double y;
    private double z;

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    private int radius;
    private int id;
    private double velocityX;
    private double velocityY;
    public int secondaryType2 = 0;
    
    public boolean isSummonedFire = false;

    private boolean clientSpecific;

    public boolean isCircle;

    public boolean isRectangle;

    private int biome;

    private int species;
    private int speciesSub;

    public boolean sendsAngle;

    protected ConcurrentHashMap<Integer, GameObject> collidedList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, GameObject> oldCollidedList = new ConcurrentHashMap<>();

    public boolean collidedWith(GameObject o) {
        return collidedList.contains(o);
    }
    public int calculateSize() {
        return this.baseRadius;
    }
    public void updateCollided() {
        int finalRadius = this.calculateSize();
        int collidedBigWhirlpools = 0;

        for (Map.Entry<Integer, GameObject> entry : getCollidedList().entrySet()) {
            GameObject a = entry.getValue();
            if (a instanceof BigWhirlpool)
                collidedBigWhirlpools++;
        }
        if (collidedBigWhirlpools > 0) {
            finalRadius /= 1.5;
        }
        if (finalRadius < 7)
            finalRadius = 7;
        this.setRadius(finalRadius);
    }
    

    public ConcurrentHashMap<Integer, GameObject> getCollidedList() {
        return collidedList;
    }

    public ConcurrentHashMap<Integer, GameObject> getOldCollidedList() {
        return oldCollidedList;
    }

    public void onCollisionExit(GameObject o) {
        //
    }

    public String toString() {
        return "ID=" + id + " TYPE=" + type + " X=" + x + " Y=" + y + " R=" + radius;
    }

    public void onCollisionEnter(GameObject o) {
        //
    }

    public void resetCollidedList() {
        // Обработка выхода объектов из зоны коллизий
        for (Map.Entry<Integer, GameObject> entry : oldCollidedList.entrySet()) {
            GameObject a = entry.getValue();
            if (a != null && !collidedList.containsKey(entry.getKey())) {
                a.onCollisionExit(this);
            }
        }
    
        // Обработка входа объектов в зону коллизий
        for (Map.Entry<Integer, GameObject> entry : collidedList.entrySet()) {
            GameObject a = entry.getValue();
            if (!oldCollidedList.containsKey(entry.getKey())) {
                a.onCollisionEnter(this);
            }
        }
    
        // Обновление oldCollidedList и collidedList
        oldCollidedList.clear();
        oldCollidedList.putAll(collidedList);
        collidedList.clear();
    }
    
    public Room room;
    public void addCollided(GameObject o) {
        collidedList.put(o.getId(), o);
    }
    private int spawnID;

    public double angle;
    private boolean isMovable = false;
    private boolean isMovableCollision = false;


    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    private boolean isDead;

    public boolean isHasCustomCollisionRadius() {
        return hasCustomCollisionRadius;
    }

    public void setHasCustomCollisionRadius(boolean hasCustomCollisionRadius) {
        this.hasCustomCollisionRadius = hasCustomCollisionRadius;
    }

    public double getCustomCollisionRadius() {
        return customCollisionRadius;
    }

    public void setCustomCollisionRadius(double customCollisionRadius) {
        this.customCollisionRadius = customCollisionRadius;
    }

    private boolean hasCustomCollisionRadius;
    private double customCollisionRadius;
    public int specType = 0;
    public int specType2 = 0;
    public double health = 100;
    public double maxHealth = 100.0;
    public boolean showHP;
    public boolean isHurted = false;
    protected int baseRadius;


    public GameObject(final int id, final double x, final double y, final int radius, final int type) {
        this.type = type;
        this.id = id;
        this.x = Math.max(x, 0);
        this.y = Math.max(y, 0);
        this.radius = radius;
        this.setVelocityX(0);
        this.setVelocityY(0);
        this.baseRadius = radius;

        this.clientSpecific = false;

        this.isCircle = true;
        this.isRectangle = false;

        this.biome = 0;
        this.species = 0;
        this.speciesSub = 0;

        this.spawnID = -1;
        this.sendsAngle = false;

        this.angle = 0;
        this.spawnID = 0;

        this.isDead = false;

        this.hasCustomCollisionRadius = false;
        this.customCollisionRadius = this.radius;
    }

    // should be override if need some stuff when object is added.
    public void onAdd() {

    }

    // should be overriden
    public void writeCustomData_onAdd(MsgWriter writer) {

    }

    // should be overriden
    public void writeCustomData_onUpdate(MsgWriter writer) {

    }

    public void collisionReset() {

    }

    public void onCollision(GameObject object) {

    }

    public boolean isSpawnedFromID() {
        return spawnID != -1;
    }

    public boolean isSendsAngle() {
        return sendsAngle;
    }

    public void setSendsAngle(boolean sendsAngle) {
        this.sendsAngle = sendsAngle;
    }

    public int getSpawnID() {
        return spawnID;
    }

    public void setSpawnID(int spawnID) {
        this.spawnID = spawnID;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setObjAngle(double angle) {
        angle -= 180;
        if (angle < 0)
            angle += 360;
        this.angle = angle;
    }
    public void remVelocityX(final double velocityX) {
        this.velocityX -= velocityX;
    }

    public void remVelocityY(final double velocityY) {
        this.velocityY -= velocityY;
    }

    public int getSpecies() {
        return species;
    }

    public void setSpecies(int species) {
        this.species = species;
    }

    public int getSpeciesSub() {
        return speciesSub;
    }

    public void setSpeciesSub(int speciesSub) {
        this.speciesSub = speciesSub;
    }

    public boolean isClientSpecific() {
        return clientSpecific;
    }

    public void setClientSpecific(boolean clientSpecific) {
        this.clientSpecific = clientSpecific;
    }

    public int getBiome() {
        return biome;
    }

    public void setBiome(int biome) {
        this.biome = biome;
    }

    public int getType() {
        return this.type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public double getX() {
        return this.x;
    }

    public void setX(final double x) {
        if (Utils.isValidDouble(x)) {
            if (x - this.radius - 1 < 1)
                this.x = 1 + this.radius;
            else if (x + this.radius + 1 > Constants.WIDTH)
                this.x = Constants.WIDTH - this.radius - 1;
            else
                this.x = x;
        }
    }

    public void setXUnsafe(final double x){
        this.x = x;
    }

    public void setYUnsafe(final double y){
        this.y = y;
    }

    public double getY() {
        return this.y;
    }

    public void setY(final double y) {
        if (Utils.isValidDouble(y)) {
            if (y - this.radius - 1 < 1)
                this.y = 1 + this.radius;
            else if (y + this.radius + 1 > Constants.HEIGHT)
                this.y = Constants.HEIGHT - this.radius - 1;
            else
                this.y = y;
        }
    }

    public int getRadius() {
        return this.radius;
    }

    public double getMouthDistation() {
        return Utils.distance(this.getX(), this.getX() + (this.getRadius() / 2), this.getY(),
                this.getY() + (this.getRadius() / 2));
    }

    public void setRadius(final int radius) {
        this.radius = radius;
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void addVelocityX(double x) {
        this.setVelocityX(this.getVelocityX() + x);
    }

    public void addVelocityY(double y) {
        this.setVelocityY(this.getVelocityY() + y);
    }

    public void update() {
        this.setX(this.getX() + this.getVelocityX());
        this.setY(this.getY() + this.getVelocityY());

        this.setVelocityX(this.getVelocityX() * 0.8);
        this.setVelocityY(this.getVelocityY() * 0.8);

        if(this instanceof SafeArea) return;
        if (this.x - this.radius - 1 <= 0) {
            this.x = this.radius + 1;
        }
        if (this.y - this.radius - 1 <= 0) {
            this.y = this.radius + 1;
        }
        if (this.x >= Constants.WIDTH - this.radius) {
            this.x = Constants.WIDTH - this.radius - 1;
        }
        if (this.y >= Constants.HEIGHT - this.radius) {
            this.y = Constants.HEIGHT - this.radius - 1;
        }

    }

    private boolean isCollideable = true;
    private boolean collideCallback = false;
    private int collideType = 0;
    private boolean treeCollide = false;

    public double getVelocityX() {
        return this.velocityX;
    }

    public void setVelocityX(final double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return this.velocityY;
    }

    public void setVelocityY(final double velocityY) {
        this.velocityY = velocityY;
    }

    public boolean isCircle() {
        return isCircle;
    }

    public boolean isRectangle() {
        return isRectangle;
    }

    public boolean getCollideable() {
        return this.isCollideable;
    }

    public boolean getCollideable(GameObject o) {
        return true; // must be overriten
    }

    public void setTreeCollide(boolean a) {
        this.treeCollide = a;
    }

    public boolean getTreeCollide() {
        return this.treeCollide;
    }

    public boolean isCollideCallback() {
        return this.collideCallback;
    }

    public boolean isMovable() {
        return this.isMovable;
    }
    public boolean canMoveOnCollision(){
        return this.isMovableCollision;
    }

    public void setMovableInCollision(boolean value) {
        this.isMovableCollision = value;
    }

    public void setMovable(boolean value) {
        this.isMovable = value;
    }

    public void setCollideable(boolean value) {
        this.isCollideable = value;
    }

    public void setCollideType(int type) {
        this.collideType = type;
    }

    public int getCollideType() {
        return this.collideType;
    }

    public void setCollideCallbacking(boolean value) {
        this.collideCallback = value;
    }

    protected boolean isFlyingObject = false;

    public void setObjectFlying(boolean a) {
        isFlyingObject = a;
    }

    public boolean getObjectFlying() {
        return isFlyingObject;
    }

    public int getSecondaryType2() {
        return secondaryType2;
    }

    public boolean canBeVisible(GameClient gameClient) {
        return true;
    }
}
