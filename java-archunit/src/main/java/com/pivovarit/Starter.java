package com.pivovarit;

import com.pivovarit.movies.Rentals;

public class Starter {
    public static void main(String[] args) {
        Rentals instance = Rentals.instance();

        boolean rent = instance.rent(42);
    }
}
