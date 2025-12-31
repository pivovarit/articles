CREATE TABLE movies
(
    id    SERIAL PRIMARY KEY,
    title TEXT NOT NULL CHECK (title <> ''),
    type  TEXT NOT NULL CHECK (type <> '')
);
