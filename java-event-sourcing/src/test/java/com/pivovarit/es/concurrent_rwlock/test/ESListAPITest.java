package com.pivovarit.es.concurrent_rwlock.test;

import com.pivovarit.es.concurrent_naive.ESList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

class ESListAPITest {

    @Test
    void no_exception_snapshot() {
        var list = ESList.newInstance();
        try {
            list.remove(42);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        list.snapshot();
    }

    @Test
    void empty() {
        var list = ESList.newInstance();
        assertThat(list.isEmpty()).isTrue();
        list.add(1);
    }

    @Test
    void not_empty() {
        var list = ESList.newInstance();
        list.add(1);
        assertThat(list.isEmpty()).isFalse();
    }

    @Test
    void size_empty() {
        var list = ESList.newInstance();
        assertThat(list.size()).isZero();
    }

    @Test
    void size_not_empty() {
        var list = ESList.newInstance();
        list.add(ThreadLocalRandom.current().nextInt());
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    void contains() {
        var list = ESList.newInstance();
        list.add(1);
        assertThat(list.contains(1)).isTrue();
        assertThat(list.contains(42)).isFalse();
    }

    @Test
    void iterator() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);

        var result = new ArrayList<>();
        for (Iterator<Object> iterator = list.iterator(); iterator.hasNext(); ) {
            result.add(iterator.next());
        }

        assertThat(list).containsExactlyElementsOf(result);
    }

    @Test
    void toArray() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);

        assertThat(list.toArray()).containsExactly(1, 2, 3);
    }

    @Test
    void toArrayGeneric() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);

        assertThat(list.toArray(new Object[0])).containsExactly(1, 2, 3);
    }

    @Test
    void add() {
        var sut = ESList.newInstance();

        sut.add(42);
        sut.add(13);

        assertThat(sut.size()).isEqualTo(2);
        assertThat(sut.contains(42)).isTrue();
        assertThat(sut.contains(13)).isTrue();
        assertThat(sut.contains(12398)).isFalse();
    }

    @Test
    void remove() {
        var sut = ESList.newInstance();

        sut.add(42);
        sut.add(13);
        sut.remove(Integer.valueOf(42));

        assertThat(sut.size()).isEqualTo(1);
        assertThat(sut.contains(13)).isTrue();
        assertThat(sut.contains(42)).isFalse();
    }

    @Test
    void containsAll() {
        var sut = ESList.newInstance();

        sut.add(42);
        sut.add(13);

        assertThat(sut.containsAll(List.of(42, 13))).isTrue();
    }

    @Test
    void addAll() {
        var sut = ESList.newInstance();

        sut.addAll(List.of(42, 13));

        assertThat(sut).containsExactly(42, 13);
    }

    @Test
    void removeAll() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);
        list.removeAll(List.of(2, 3));

        assertThat(list).containsExactly(1);
    }

    @Test
    void retainAll() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);
        list.retainAll(List.of(3));

        assertThat(list).containsExactly(3);
    }

    @Test
    void clear() {
        var list = ESList.newInstance();
        list.add(1);
        list.clear();
        assertThat(list).isEmpty();
    }

    @Test
    void get() {
        var list = ESList.newInstance();
        list.add(1);
        var result = list.get(0);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void set() {
        var list = ESList.newInstance();
        list.add(1);
        list.set(0, 0);
        var result = list.get(0);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void indexOf() {
        var sut = ESList.newInstance();
        sut.add(3);
        sut.add(2);
        sut.add(3);

        assertThat(sut.indexOf(3)).isEqualTo(0);
    }

    @Test
    void lastIndexOf() {
        var sut = ESList.newInstance();
        sut.add(3);
        sut.add(2);
        sut.add(3);

        assertThat(sut.lastIndexOf(3)).isEqualTo(2);
    }

    @Test
    void listIterator() {
        var list = ESList.newInstance();
        list.add(1);
        list.add(2);
        list.add(3);

        var result = new ArrayList<>();
        for (Iterator<Object> iterator = list.listIterator(); iterator.hasNext(); ) {
            result.add(iterator.next());
        }

        assertThat(list).containsExactlyElementsOf(result);

    }

    @Test
    void subList() {
        var sut = ESList.newInstance();
        sut.add(1);
        sut.add(2);
        sut.add(3);
        assertThat(sut.subList(1, 3)).containsExactly(2, 3);
    }

    @Test
    void exceptionPropagation() {
        var sut = ESList.newInstance();
        sut.add(1);
        sut.add(2);
        sut.add(3);

        try {
            sut.set(1000, 1000);
        } catch (Exception e) { }

        assertThat(sut.snapshot()).containsExactly(1, 2, 3);
    }
}