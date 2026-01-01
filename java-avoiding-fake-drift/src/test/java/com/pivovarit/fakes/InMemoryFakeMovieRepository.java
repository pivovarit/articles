package com.pivovarit.fakes;

import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class InMemoryFakeMovieRepository implements MovieRepository {

    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();

    @Override
    public long save(Movie movie) {
        if (movie.title() == null || movie.title().isBlank()) {
            throw new IllegalArgumentException("Movie title cannot be blank or null");
        }

        long id = ThreadLocalRandom.current().nextLong();
        movies.put(id, movie);
        return id;
    }

    @Override
    public List<Movie> findAll() {
        return List.copyOf(movies.values());
    }

    @Override
    public List<Movie> findAllByType(String type) {
        return movies.values().stream()
          .filter(movie -> movie.type().equals(type))
          .toList();
    }

    @Override
    public Optional<Movie> findById(long id) {
        return Optional.ofNullable(movies.get(id));
    }
}
