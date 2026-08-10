ALTER TABLE musica
    ADD COLUMN id_artista BIGINT;

ALTER TABLE musica
    ADD CONSTRAINT fk_musica_artista_principal
        FOREIGN KEY (id_artista)
        REFERENCES artista (id_artista);

ALTER TABLE musica
    ALTER COLUMN id_artista SET NOT NULL;