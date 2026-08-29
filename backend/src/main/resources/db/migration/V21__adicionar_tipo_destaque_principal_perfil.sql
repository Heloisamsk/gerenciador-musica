ALTER TABLE perfil
    ADD COLUMN tipo_destaque_principal VARCHAR(20);

ALTER TABLE perfil
    ADD CONSTRAINT ck_perfil_tipo_destaque_principal
        CHECK (
            tipo_destaque_principal IS NULL
            OR tipo_destaque_principal IN ('ARTISTA', 'MUSICA', 'ALBUM')
        );

UPDATE perfil
SET tipo_destaque_principal = CASE
    WHEN id_artista_destaque IS NOT NULL THEN 'ARTISTA'
    WHEN id_musica_destaque IS NOT NULL THEN 'MUSICA'
    WHEN id_album_destaque IS NOT NULL THEN 'ALBUM'
    ELSE NULL
END;
