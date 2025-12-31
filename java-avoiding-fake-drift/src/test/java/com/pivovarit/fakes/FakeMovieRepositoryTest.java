package com.pivovarit.fakes;

class FakeMovieRepositoryTest extends MovieRepositoryTest {

    @Override
    MovieRepository getRepository() {
        return new InMemoryFakeMovieRepository();
    }
}
