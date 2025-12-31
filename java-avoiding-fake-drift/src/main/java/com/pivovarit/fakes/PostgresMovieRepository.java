package com.pivovarit.fakes;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.postgres.PostgresPlugin;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class PostgresMovieRepository implements MovieRepository {

    private final Jdbi jdbi;

    public PostgresMovieRepository(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource).installPlugin(new PostgresPlugin());
    }

    @Override
    public void save(Movie movie) {
        jdbi.useHandle(handle ->
          handle.createUpdate("INSERT INTO movies (title, type) VALUES (:title, :type)")
            .bind("title", movie.title())
            .bind("type", movie.type())
            .execute()
        );
    }

    @Override
    public List<Movie> findAll() {
        return jdbi.withHandle(handle ->
          handle.createQuery("SELECT title, type FROM movies")
            .map(toMovie())
            .list()
        );
    }

    @Override
    public List<Movie> findAllByType(String type) {
        return jdbi.withHandle(handle ->
          handle.createQuery("SELECT title, type FROM movies WHERE type = :type")
            .bind("type", type)
            .map(toMovie())
            .list()
        );
    }

    @Override
    public Optional<Movie> findById(long id) {
        return jdbi.withHandle(handle ->
          handle.createQuery("SELECT title, type FROM movies WHERE id = :id")
            .bind("id", id)
            .map(toMovie())
            .findOne()
        );
    }

    private static RowMapper<Movie> toMovie() {
        return (rs, _) -> new Movie(rs.getString("title"), rs.getString("type"));
    }
}
