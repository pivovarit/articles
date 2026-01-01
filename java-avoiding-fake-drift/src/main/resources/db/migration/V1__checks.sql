ALTER TABLE movies
    ADD CONSTRAINT movies_title_not_empty CHECK (title <> '');

ALTER TABLE movies
    ADD CONSTRAINT movies_type_not_empty CHECK (type <> '');
