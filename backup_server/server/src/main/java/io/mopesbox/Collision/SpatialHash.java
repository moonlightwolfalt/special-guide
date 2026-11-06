package io.mopesbox.Collision;

import cc.redberry.libdivide4j.FastDivision;
import io.mopesbox.Constants;
import io.mopesbox.Objects.GameObject;
import io.mopesbox.Objects.Rectangle;

import java.util.*;

public class SpatialHash {
    private final int cols;
    private final int rows;
    public LinkedHashMap<Integer, ArrayList<GameObject>> buckets;
    private int cellSize;
    FastDivision.Magic magic;
    float width;

    public SpatialHash() {
        this.cellSize = 150;
        magic = FastDivision.magicSigned(cellSize);
        this.width = Constants.WIDTH / this.cellSize;
        this.cols = Math.round(Constants.WIDTH / this.cellSize);
        this.rows = Math.round(Constants.HEIGHT / this.cellSize)+3;

        this.buckets = new LinkedHashMap<Integer, ArrayList<GameObject>>(cols * rows);

        for (int i = 0; i < cols * rows; i++) {
            this.buckets.put(i, new ArrayList<>());
        }
    }

    public void RegisterObject(GameObject obj) {
        ArrayList<Integer> cellID = GetIdForObject(obj);
        for (Integer index : cellID) {
            if (this.buckets.containsKey(index))
                this.buckets.get(index).add(obj);
        }
    }

    public void removeObject(GameObject obj) {
        int bucketid = -1;
        for (Map.Entry<Integer, ArrayList<GameObject>> entry : this.buckets.entrySet()) {
            int key = entry.getKey();
            ArrayList<GameObject> value = entry.getValue();
            for(GameObject it : value) {
                if(obj == it) {
                    bucketid = key;
                }
            }
        }
        if(bucketid == -1)
            return;
        
        this.buckets.get(bucketid).remove(obj);
    }

    public ArrayList<GameObject> getBucket(int i) {
        return this.buckets.get(i);
    }

    public ArrayList<GameObject> getNearby(GameObject obj) {
        ArrayList<GameObject> objects = new ArrayList<>();
        ArrayList<Integer> bucketIDs = GetIdForObject(obj);
        for (Integer item : bucketIDs) {
            objects.addAll(this.buckets.get(item));
        }
        return objects;
    }

    private ArrayList<Integer> GetIdForObject(GameObject obj) {
        ArrayList<Integer> bucketsObjIsIn = new ArrayList<>();

        double minX;
        double minY;
        double maxX;
        double maxY;
        if (obj.isCircle()) {
            minX = obj.getX() - (obj.getRadius()) - this.cellSize / 2;
            minY = obj.getY() - (obj.getRadius()) - this.cellSize / 2;

            maxX = obj.getX() + (obj.getRadius()) + this.cellSize / 2;
            maxY = obj.getY() + (obj.getRadius()) + this.cellSize / 2;
        } else {
            Rectangle rect = (Rectangle) obj;
            minX = obj.getX() - rect.getWidth() / 2;
            maxX = obj.getX() + rect.getWidth() / 2;
            minY = obj.getY() - rect.getHeight() / 2;
            maxY = obj.getY() + rect.getHeight() / 2;
        }

        for (double i = minX; i < maxX; i += cellSize) {
            for (double j = minY; j < maxY; j += cellSize) {
                AddBucket(i, j, width, bucketsObjIsIn);
            }
        }

        return bucketsObjIsIn;
    }

    private void AddBucket(double x, double y, float width, ArrayList<Integer> bucketsObjIsIn) {
        // FastDivision.Magic magic = FastDivision.magicSigned(this.cellSize);

        int cellPosition = (int) ((QuickDivide(x, magic)) +
                (QuickDivide(y, magic)) *
                        width);
        if (!bucketsObjIsIn.contains(cellPosition))
            bucketsObjIsIn.add(cellPosition);
    }

    public int QuickDivide(double a, FastDivision.Magic magic) {

        return (int) FastDivision.divideSignedFast((long) a, magic);
    }
    // std::vector<int> getOccupiedBucketIds(const cv::Rect& rect)
    // {
    // auto minBucketX = rect.x / bucketWidth;
    // auto minBucketY = rect.y / bucketWidth;
    // auto maxBucketX = (rect.x + rect.width) / bucketWidth;
    // auto maxBucketY = (rect.y + rect.height) / bucketWidth;
    //
    // std::vector<int> occupiedBucketIds;
    // occupiedBucketIds.reserve((maxBucketX - minBucketX + 1) * (maxBucketY -
    // minBucketY + 1));
    // for (auto y = minBucketY; y <= maxBucketY; y++)
    // {
    // for (auto x = minBucketX; x <= maxBucketX; x++)
    // {
    // occupiedBucketIds.push_back(y * cellsPerSide + x);
    // }
    // }
    // return occupiedBucketIds;
    // }
    // public int QuickMultiply(double a,double multiplier){
    // return (int)
    // }

    public void ClearBuckets() {
        this.buckets = new LinkedHashMap<Integer, ArrayList<GameObject>>();
        // this.buckets.clear();
        // Arrays.fill(this.buckets,null);
        for (int i = 0; i < cols * rows; i++) {
            // this.buckets.get(i).clear();
            this.buckets.put(i, new ArrayList<>());
        }
    }
}
