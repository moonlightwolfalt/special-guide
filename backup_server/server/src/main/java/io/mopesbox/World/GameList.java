package io.mopesbox.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.mopesbox.Objects.GameObject;

public class GameList implements Iterable<GameObject> {
    public final ConcurrentHashMap<Integer, GameObject> gameMap;

    public GameList() {
        this.gameMap = new ConcurrentHashMap<>();
    }

    public GameList(final GameList copy) {
        this.gameMap = new ConcurrentHashMap<>(copy.gameMap);
    }

    public List<GameObject> subList(int startIndex, int endIndex) {
        List<GameObject> list = new ArrayList<>();
        int size = gameMap.size();
        if (endIndex > size)
            endIndex = size;
list.addAll(gameMap.values().stream().skip(startIndex).limit(endIndex - startIndex).collect(Collectors.toList()));
        return list;
    }

    public List<GameObject> toList() {
        return new ArrayList<>(gameMap.values());
    }

    public GameObject get(final int id) {
        return this.gameMap.get(id);
    }

    public void add(final GameObject object) {
        this.gameMap.put(object.getId(), object);
    }

    public void add(final int id, final GameObject object) {
        this.gameMap.put(id, object);
    }

    public void remove(final GameObject object) {
        this.gameMap.remove(object.getId(), object);
    }

    @Override
    public Iterator<GameObject> iterator() {
        return this.gameMap.values().iterator();
    }

    public int size() {
        return this.gameMap.size();
    }
}
