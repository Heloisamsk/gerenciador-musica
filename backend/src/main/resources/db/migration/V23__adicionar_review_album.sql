ALTER TABLE review
    ALTER COLUMN id_musica DROP NOT NULL;

ALTER TABLE review
    ADD COLUMN id_album BIGINT;

ALTER TABLE review
    ADD CONSTRAINT fk_review_album
        FOREIGN KEY (id_album)
        REFERENCES album (id_album)
        ON DELETE CASCADE;

ALTER TABLE review
    ADD CONSTRAINT uk_review_usuario_album
        UNIQUE (id_usuario, id_album);

ALTER TABLE review
    ADD CONSTRAINT ck_review_alvo
        CHECK (
            (id_musica IS NOT NULL AND id_album IS NULL)
            OR (id_musica IS NULL AND id_album IS NOT NULL)
        );

CREATE INDEX idx_review_album
    ON review (id_album);
