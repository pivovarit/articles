package com.pivovarit.hibernatealternatives;

import com.pivovarit.hibernatealternatives.movie.Movie;
import com.pivovarit.hibernatealternatives.movie.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class Runner implements CommandLineRunner {

    private final MovieRepository movieRepository;

    Runner(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public void run(String... args) {
        movieRepository.save(new Movie(42, "The Hitchhiker's Guide to the Galaxy"));
        movieRepository.findOneById(42).ifPresent(System.out::println);
    }
}
