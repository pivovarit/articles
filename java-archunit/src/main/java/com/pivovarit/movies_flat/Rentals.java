package com.pivovarit.movies_flat;

public class Rentals {

    private final MovieRepository movieRepository;
    private final MovieDetailsRepository movieDetailsRepository;
    private final MoviePriceCalculator moviePriceCalculator;

    Rentals(MovieRepository movieRepository, MovieDetailsRepository movieDetailsRepository, MoviePriceCalculator moviePriceCalculator) {
        this.movieRepository = movieRepository;
        this.movieDetailsRepository = movieDetailsRepository;
        this.moviePriceCalculator = moviePriceCalculator;
    }

    public static Rentals instance() {
        return new Rentals(new MovieRepository(), new MovieDetailsRepository(), new MoviePriceCalculator());
    }

    public boolean rent(int id) {
        return false;
    }
}
