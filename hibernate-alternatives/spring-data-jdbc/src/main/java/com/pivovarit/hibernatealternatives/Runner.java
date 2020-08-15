package com.pivovarit.hibernatealternatives;

import com.pivovarit.hibernatealternatives.movie.Movie;
import com.pivovarit.hibernatealternatives.movie.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Component;

@Component
class Runner implements CommandLineRunner {

    private final MovieRepository movieRepository;

    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public Runner(MovieRepository movieRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.movieRepository = movieRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcAggregateTemplate.insert(new Movie(42, "The Hitchhiker's Guide to the Galaxy"));
        movieRepository.findById(42L).ifPresent(System.out::println);
    }
}
