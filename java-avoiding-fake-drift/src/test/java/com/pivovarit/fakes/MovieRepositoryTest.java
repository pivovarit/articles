package com.pivovarit.fakes;

import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class MovieRepositoryTest {
    abstract MovieRepository getRepository();

    private final MovieRepository repository = getRepository();

    @Test
    void shouldSaveMovie() {
        var m1 = new Movie("Tenet", "NEW");
        var m2 = new Movie("Casablanca", "OLD");

        assertThat(repository.findAll()).isEmpty();

        repository.save(m1);
        repository.save(m2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(m1, m2);
    }

    @Test
    void shouldRejectMovieWithEmptyTitle() {
        assertThatThrownBy(() -> repository.save(new Movie("", "NEW")))
          .isInstanceOf(UnableToExecuteStatementException.class);
    }

    @Test
    void shouldRejectMovieWithNullTitle() {
        assertThatThrownBy(() -> repository.save(new Movie(null, "NEW")))
          .isInstanceOf(UnableToExecuteStatementException.class);
    }
}
