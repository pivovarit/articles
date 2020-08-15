package com.pivovarit.hibernatealternatives.movie;

public class Movie {
    private long id;
    private String title;

    public Movie(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Movie{" +
          "id=" + id +
          ", title='" + title + '\'' +
          '}';
    }
}
