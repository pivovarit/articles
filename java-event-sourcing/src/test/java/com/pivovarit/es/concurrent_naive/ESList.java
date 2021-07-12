package com.pivovarit.es.concurrent_naive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class ESList<T> implements List<T> {

    private static final InitOp<?> EMPTY_INIT = new InitOp<>();

    private final List<ListOp<T>> opLog = new ArrayList<>();

    private ESList() {
        handle((InitOp<T>) EMPTY_INIT);
    }

    public static <T> ESList<T> newInstance() {
        return new ESList<>();
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
        return (boolean) handle(new AddOp<>(t));
    }

    @Override
    public boolean remove(Object o) {
        return (boolean) handle(new RemoveOp<T>(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return snapshot().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return (boolean) handle(new AddAllOp<>(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return (boolean) handle(new AddAllIdxOp<>(index, c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return (boolean) handle(new RemoveAllOp<>(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return (boolean) handle(new RetainAllOp<>(c));
    }

    @Override
    public void clear() {
        handle(new ClearOp<>());
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
        handle(new AddIdxOp<>(index, element));
    }

    @Override
    public T remove(int index) {
        return (T) handle(new RemoveIdxOp<T>(index));
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
        return snapshot(opLog.size()).orElseThrow(IllegalStateException::new);
    }

    public int version() {
        synchronized (opLog) {
            return opLog.size();
        }
    }

    public Optional<List<T>> snapshot(int version) {
        synchronized (opLog) {
            if (version > opLog.size()) {
                return Optional.empty();
            }
            var snapshot = new ArrayList<T>();
            for (int i = 0; i <= version; i++) {
                try {
                    opLog.get(i).apply(snapshot);
                } catch (Exception ignored) {
                }
            }
            return Optional.of(snapshot);
        }

    }

    public void displayLog() {
        synchronized (opLog) {
            for (int i = 0; i < opLog.size(); i++) {
                System.out.printf("v%d :: %s%n", i, opLog.get(i).toString());
            }
        }
    }

    private Object handle(ListOp<T> op) {
        synchronized (opLog) {
            List<T> snapshot = snapshot();
            opLog.add(op);
            return op.apply(snapshot);
        }
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
