package com.pivovarit.hibernatealternatives.movie;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JdbcMovieRepository implements MovieRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMovieRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Movie> findOneById(long id) {
        return Optional.empty(); // TODO
    }

    @Override
    public long save(Movie movie) {
        return 0; // TODO
    }
}
