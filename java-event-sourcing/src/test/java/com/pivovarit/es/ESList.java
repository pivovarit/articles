package com.pivovarit.es;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * snapshottable cache-aware lock-free concurrent retaining list
 */
public class ESList<T> implements List<T> {

    private static final int INITIAL_VERSION = -1;

    private static final com.pivovarit.es.InitOp<?> EMPTY_INIT = new com.pivovarit.es.InitOp<>();

    private final AtomicInteger version = new AtomicInteger(INITIAL_VERSION);
    private final AtomicInteger ecViewVersion = new AtomicInteger(INITIAL_VERSION);

    /**
     * append-only bin log (infinite retention for now)
     */
    private final List<com.pivovarit.es.ListOp<T>> binLog = new ArrayList<>();

    private final List<T> ecView = new ArrayList<>();

    private ESList() {
        handle((com.pivovarit.es.InitOp<T>) EMPTY_INIT);
    }

    public static <T> com.pivovarit.es.ESList<T> newInstance() {
        return new com.pivovarit.es.ESList<>();
    }

    @Override
    public int size() {
        return ecView.size();
    }

    @Override
    public boolean isEmpty() {
        return ecView.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return ecView.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return ecView.iterator();
    }

    @Override
    public Object[] toArray() {
        return ecView.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return ecView.toArray(a);
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
        return ecView.containsAll(c);
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
        return ecView.get(index);
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
        return ecView.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return ecView.lastIndexOf(o);
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

    public int version() {
        return version.get();
    }

    public List<T> snapshot() {
        return snapshot(version.get() + 1);
    }

    public List<T> snapshot(int version) {
        var snapshot = new ArrayList<T>();
        for (int i = 0; i < version; i++) {
            try {
                binLog.get(i).apply(snapshot);
            } catch (Exception ignored) { }
        }
        return snapshot;
    }

    public void displayLog() {
        for (int i = 0; i < version.get() + 1; i++) {
            System.out.printf("v%d :: %s%n", i, binLog.get(i).toString());
        }
    }

    private Object handle(com.pivovarit.es.ListOp<T> op) {
        return updateEcView(op, append(op));
    }

    private Object updateEcView(com.pivovarit.es.ListOp<T> op, int version) {
        while (version != ecViewVersion.get() + 1) {
            Thread.onSpinWait();
        }
        try {
            return op.apply(ecView);
        } finally {
            ecViewVersion.incrementAndGet();
        }
    }

    private int append(com.pivovarit.es.ListOp<T> op) {
        synchronized (binLog) {
            binLog.add(null); // resize before serializing writes
        }
        int nextVersion = this.version.incrementAndGet();
        binLog.set(nextVersion, op);
        return nextVersion;
    }
}
