package com.pivovarit.fakes;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

@Testcontainers
class PostgresMovieRepositoryTest extends MovieRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PostgresMovieRepositoryTest.class);

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18")
      .withNetworkAliases("postgres")
      .withDatabaseName("postgres")
      .withUsername("postgres")
      .withPassword("password")
      .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("postgres"))
      .waitingFor(Wait.forListeningPort());

    @Override
    MovieRepository getRepository() {
        return new PostgresMovieRepository(getDatasource());
    }

    private DataSource getDatasource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(postgres.getJdbcUrl());
        ds.setPassword(postgres.getPassword());
        ds.setUser(postgres.getUsername());

        Flyway.configure()
          .dataSource(ds)
          .load()
          .migrate();

        return ds;
    }
}
