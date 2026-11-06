package io.mopesbox.Collision;

import java.util.ArrayList;
import java.util.List;

import io.mopesbox.Objects.GameObject;

public class QuadTree<T> {
    private int MAX_OBJECTS = 5;
    private int MAX_LEVELS = 10;

    private int level;
    private List<GameObject> objects;
    private Rectangle bounds;
    @SuppressWarnings("unchecked")
    private QuadTree<T>[] nodes = (QuadTree<T>[]) new QuadTree<?>[4];
    
    public QuadTree(int level, Rectangle bounds) {
        this.level = level;
        objects = new ArrayList<>();
        this.bounds = bounds;
        List<QuadTree<T>> nodes = new ArrayList<>(4);
        int a = nodes.size();
        if(a == 0){//avoid warns from java

        }
    }

    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    private void split() {
        int subWidth = (int) (bounds.getWidth() / 2);
        int subHeight = (int) (bounds.getHeight() / 2);
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();

        nodes[0] = new QuadTree<T>(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new QuadTree<T>(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new QuadTree<T>(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new QuadTree<T>(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    private int getIndex(GameObject p) {
        int index = -1;
        double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = (p.getY() - p.getRadius() > bounds.getY())
                && (p.getY() + p.getRadius() < horizontalMidpoint);
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (p.getY() - p.getRadius() > horizontalMidpoint)
                && (p.getY() + p.getRadius() < bounds.getY() + bounds.getHeight());
        // Object can completely fit within the left quadrants
        boolean leftQuadrant = (p.getX() - p.getRadius() > bounds.getX())
                && (p.getX() + p.getRadius() < verticalMidpoint);
        // Object can completely fit within the right quadrants
        boolean rightQuadrant = (p.getX() - p.getRadius() > verticalMidpoint)
                && (p.getX() + p.getRadius() < bounds.getX() + bounds.getWidth());

        if (leftQuadrant) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        } else if (rightQuadrant) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    public void insert(GameObject p) {
        if (nodes[0] != null) {
            int index = getIndex(p);
            if (index != -1) {
                nodes[index].insert(p);
                return;
            }
        }
        objects.add(p);
        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }
            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    public List<GameObject> retrieve(List<GameObject> returnObjects, GameObject p) {
        int index = getIndex(p);
        if (index != -1 && nodes[0] != null && nodes[index] != null) {
            nodes[index].retrieve(returnObjects, p);
        }
        returnObjects.addAll(objects);
        return returnObjects;
    }
}