CREATE TABLE perfil_artista_favorito (
    id_perfil BIGINT NOT NULL,
    id_artista BIGINT NOT NULL,
    ordem SMALLINT NOT NULL,

    CONSTRAINT pk_perfil_artista_favorito
        PRIMARY KEY (id_perfil, id_artista),
    CONSTRAINT uk_perfil_artista_favorito_ordem
        UNIQUE (id_perfil, ordem),
    CONSTRAINT fk_perfil_artista_favorito_perfil
        FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil)
        ON DELETE CASCADE,
    CONSTRAINT fk_perfil_artista_favorito_artista
        FOREIGN KEY (id_artista) REFERENCES artista (id_artista)
        ON DELETE CASCADE,
    CONSTRAINT ck_perfil_artista_favorito_ordem
        CHECK (ordem BETWEEN 0 AND 2)
);

CREATE TABLE perfil_album_favorito (
    id_perfil BIGINT NOT NULL,
    id_album BIGINT NOT NULL,
    ordem SMALLINT NOT NULL,

    CONSTRAINT pk_perfil_album_favorito
        PRIMARY KEY (id_perfil, id_album),
    CONSTRAINT uk_perfil_album_favorito_ordem
        UNIQUE (id_perfil, ordem),
    CONSTRAINT fk_perfil_album_favorito_perfil
        FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil)
        ON DELETE CASCADE,
    CONSTRAINT fk_perfil_album_favorito_album
        FOREIGN KEY (id_album) REFERENCES album (id_album)
        ON DELETE CASCADE,
    CONSTRAINT ck_perfil_album_favorito_ordem
        CHECK (ordem BETWEEN 0 AND 2)
);

CREATE TABLE perfil_musica_favorita (
    id_perfil BIGINT NOT NULL,
    id_musica BIGINT NOT NULL,
    ordem SMALLINT NOT NULL,

    CONSTRAINT pk_perfil_musica_favorita
        PRIMARY KEY (id_perfil, id_musica),
    CONSTRAINT uk_perfil_musica_favorita_ordem
        UNIQUE (id_perfil, ordem),
    CONSTRAINT fk_perfil_musica_favorita_perfil
        FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil)
        ON DELETE CASCADE,
    CONSTRAINT fk_perfil_musica_favorita_musica
        FOREIGN KEY (id_musica) REFERENCES musica (id_musica)
        ON DELETE CASCADE,
    CONSTRAINT ck_perfil_musica_favorita_ordem
        CHECK (ordem BETWEEN 0 AND 2)
);

CREATE INDEX idx_perfil_artista_favorito_artista
    ON perfil_artista_favorito (id_artista);
CREATE INDEX idx_perfil_album_favorito_album
    ON perfil_album_favorito (id_album);
CREATE INDEX idx_perfil_musica_favorita_musica
    ON perfil_musica_favorita (id_musica);

INSERT INTO perfil_artista_favorito (id_perfil, id_artista, ordem)
SELECT id_perfil, id_artista_destaque, 0
FROM perfil
WHERE id_artista_destaque IS NOT NULL
  AND tipo_destaque_principal IS DISTINCT FROM 'ARTISTA';

INSERT INTO perfil_album_favorito (id_perfil, id_album, ordem)
SELECT id_perfil, id_album_destaque, 0
FROM perfil
WHERE id_album_destaque IS NOT NULL
  AND tipo_destaque_principal IS DISTINCT FROM 'ALBUM';

INSERT INTO perfil_musica_favorita (id_perfil, id_musica, ordem)
SELECT id_perfil, id_musica_destaque, 0
FROM perfil
WHERE id_musica_destaque IS NOT NULL
  AND tipo_destaque_principal IS DISTINCT FROM 'MUSICA';

UPDATE perfil
SET id_artista_destaque = NULL
WHERE tipo_destaque_principal IS DISTINCT FROM 'ARTISTA';

UPDATE perfil
SET id_album_destaque = NULL
WHERE tipo_destaque_principal IS DISTINCT FROM 'ALBUM';

UPDATE perfil
SET id_musica_destaque = NULL
WHERE tipo_destaque_principal IS DISTINCT FROM 'MUSICA';
