ALTER TABLE playlist_musica
    ADD COLUMN ordem INTEGER,
    ADD COLUMN data_criacao TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP;

WITH ordem_inicial AS (
    SELECT
        id_playlist,
        id_musica,
        ROW_NUMBER() OVER (
            PARTITION BY id_playlist
            ORDER BY id_musica
        )::INTEGER AS nova_ordem
    FROM playlist_musica
)
UPDATE playlist_musica pm
SET ordem = oi.nova_ordem
FROM ordem_inicial oi
WHERE pm.id_playlist = oi.id_playlist
  AND pm.id_musica = oi.id_musica;

ALTER TABLE playlist_musica
    ALTER COLUMN ordem SET NOT NULL;

ALTER TABLE playlist_musica
    ADD CONSTRAINT ck_playlist_musica_ordem
        CHECK (ordem > 0),

    ADD CONSTRAINT uk_playlist_musica_ordem
        UNIQUE (id_playlist, ordem);