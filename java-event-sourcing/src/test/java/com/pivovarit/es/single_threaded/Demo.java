package com.pivovarit.es.single_threaded;

import java.util.List;

class Demo {
    public static void main(String[] args) {
        ESList<Integer> objects = ESList.newInstance();

        objects.add(1);
        objects.add(2);
        objects.add(3);
        objects.addAll(List.of(4, 5));
        objects.remove(Integer.valueOf(1));
        objects.clear();
        objects.displayLog();

        System.out.println();

        for (int i = 0; i < objects.version(); i++) {
            System.out.println("v" + i + " :" + objects.snapshot(i).get());
        }
    }
}
