package com.pivovarit.hibernatealternatives.movie;

import org.springframework.dao.EmptyResultDataAccessException;
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
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
              "SELECT * FROM MOVIES WHERE id = ?",
              (rs, rowId) -> new Movie(rs.getLong("id"), rs.getString("title")), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Movie movie) {
        jdbcTemplate.update("INSERT INTO MOVIES(id, title) VALUES(?, ?)", movie.getId(), movie.getTitle());
    }
}
