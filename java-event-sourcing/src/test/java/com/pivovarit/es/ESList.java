package com.pivovarit.es;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

public class ESList<T> implements List<T> {

    private static final int INITIAL_VERSION = -1;

    private static final com.pivovarit.es.InitOp<?> EMPTY_INIT = new com.pivovarit.es.InitOp<>();

    /**
     * append-only bin log (infinite retention for now)
     */
    private final List<com.pivovarit.es.ListOp<T>> binLog = new ArrayList<>();

    private ESList() {
        handle((com.pivovarit.es.InitOp<T>) EMPTY_INIT);
    }

    public static <T> com.pivovarit.es.ESList<T> newInstance() {
        return new com.pivovarit.es.ESList<>();
    }

    @Override
    public int size() {
        return snapshot().size();
    }

    @Override
    public boolean isEmpty() {
        return snapshot().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return snapshot().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return snapshot().iterator();
    }

    @Override
    public Object[] toArray() {
        return snapshot().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return snapshot().toArray(a);
    }

    @Override
    public boolean add(T t) {
        return (boolean) handle(new com.pivovarit.es.AddOp<>(t));
    }

    @Override
    public boolean remove(Object o) {
        return (boolean) handle(new com.pivovarit.es.RemoveOp<T>(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return snapshot().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return (boolean) handle(new com.pivovarit.es.AddAllOp<>(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return (boolean) handle(new com.pivovarit.es.AddAllIdxOp<>(index, c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return (boolean) handle(new RemoveAllOp<>(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return (boolean) handle(new com.pivovarit.es.RetainAllOp<>(c));
    }

    @Override
    public void clear() {
        handle(new com.pivovarit.es.ClearOp<>());
    }

    @Override
    public T get(int index) {
        return snapshot().get(index);
    }

    @Override
    public T set(int index, T element) {
        return (T) handle(new SetOp<>(index, element));
    }

    @Override
    public void add(int index, T element) {
        handle(new com.pivovarit.es.AddIdxOp<>(index, element));
    }

    @Override
    public T remove(int index) {
        return (T) handle(new com.pivovarit.es.RemoveIdxOp<T>(index));
    }

    @Override
    public int indexOf(Object o) {
        return snapshot().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return snapshot().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return snapshot().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return snapshot().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return snapshot().subList(fromIndex, toIndex);
    }

    public List<T> snapshot() {
        return snapshot(binLog.size());
    }

    public List<T> snapshot(int version) {
        var snapshot = new ArrayList<T>();
        for (int i = 0; i <= version; i++) {
            try {
                binLog.get(i).apply(snapshot);
            } catch (Exception ignored) {
            }
        }
        return snapshot;
    }

    public void displayLog() {
        for (int i = 0; i < binLog.size(); i++) {
            System.out.printf("v%d :: %s%n", i, binLog.get(i).toString());
        }
    }

    private Object handle(com.pivovarit.es.ListOp<T> op) {
        List<T> snapshot = snapshot();
        append(op);
        return op.apply(snapshot);
    }

    private void append(com.pivovarit.es.ListOp<T> op) {
        binLog.add(op);
    }

    @Override
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            T e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
}
