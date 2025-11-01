package com.model.Util;

import java.util.*;

public class OrderedUniqueList<E> implements Iterable<E> {
    private final List<E> list = new ArrayList<>();
    private final Set<E> set = new HashSet<>();

    public boolean add(E element) {
        if (set.add(element)) { // only add if not present
            list.add(element);
            return true;
        }
        return false;
    }

    public E get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(E element) {
        return set.contains(element);
    }

    public boolean remove(E element) {
        if (set.remove(element)) {
            list.remove(element);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    public List<E> getImmutableList() {
        return Collections.unmodifiableList(list);
    }

    public void clear() {
        list.clear();
        set.clear();
    }
}
