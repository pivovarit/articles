package com.pivovarit.fakes;

import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    void save(Movie movie);

    List<Movie> findAll();

    List<Movie> findAllByType(String type);

    Optional<Movie> findById(long id);
}
