package com.pivovarit.es.single_threaded;

class Demo {
    public static void main(String[] args) {
        ESList<Integer> objects = ESList.newInstance();

        objects.add(1);
        objects.add(2);
        objects.add(3);
        objects.displayLog();

        System.out.println("\n");

        System.out.println("v0 :" + objects.snapshot(0));
        System.out.println("v1 :" + objects.snapshot(1));
        System.out.println("v2 :" + objects.snapshot(2));
        System.out.println("v3 :" + objects.snapshot(3));

    }
}
