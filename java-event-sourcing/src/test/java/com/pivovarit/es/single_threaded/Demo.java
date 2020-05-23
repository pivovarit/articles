package com.pivovarit.es.single_threaded;

class Demo {
    public static void main(String[] args) {
        ESList<Integer> objects = ESList.newInstance();

        objects.add(1);
        objects.add(2);
        objects.add(3);
        objects.displayLog();
        System.out.println(objects.snapshot());
    }
}
