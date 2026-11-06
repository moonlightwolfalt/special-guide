package io.mopesbox.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.mopesbox.Objects.GameObject;

public class RemoveList implements Iterable<List<GameObject>> {
    private List<GameObject> listObj;
    private List<GameObject> listKiller;

    public RemoveList() {
        this.listObj = new ArrayList<>();
        this.listKiller = new ArrayList<>();
    }

    public void add(GameObject o, GameObject killer) {
        this.listObj.add(o);
        this.listKiller.add(killer);
    }
    public void removeAll_ListObj(Collection<?> o, Collection<?> killer) {
        this.listObj.removeAll(o);
        this.listKiller.removeAll(killer);
    }

    public void remove(GameObject o, GameObject killer) {
        this.listObj.remove(o);
        this.listKiller.remove(killer);
    }

    public boolean contains(GameObject o) {
        return this.listObj.contains(o);
    }

    public void clear() {
        this.listObj.clear();
        this.listKiller.clear();
    }

    public int size() {
        return this.listObj.size();
    }

    public Iterator<List<GameObject>> iterator() {
        List<GameObject> copyListObj = new ArrayList<>(listObj);
        List<GameObject> copyListKiller = new ArrayList<>(listKiller);

        return new Iterator<List<GameObject>>() {
            private final Iterator<GameObject> iter1 = copyListObj.iterator();
            private final Iterator<GameObject> iter2 = copyListKiller.iterator();

            @Override
            public boolean hasNext() {
                return iter1.hasNext() && iter2.hasNext();
            }

            @Override
            public List<GameObject> next() {
                List<GameObject> ret = new ArrayList<>();
                ret.add(iter1.next());
                ret.add(iter2.next());
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("no changes allowed");
            }
        };
    }
}
