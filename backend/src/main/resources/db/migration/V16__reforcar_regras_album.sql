-- Normaliza os títulos antes de aplicar a mesma regra usada pelo serviço.
UPDATE album
SET titulo = REGEXP_REPLACE(
        BTRIM(titulo),
        '[[:space:]]+',
        ' ',
        'g'
    );

-- A carga inicial possui alguns álbuns repetidos. Mantém o menor ID de cada
-- grupo e preserva todas as referências antes de remover as cópias.
CREATE TEMPORARY TABLE album_para_canonico
ON COMMIT DROP
AS
SELECT
    id_album,
    MIN(id_album) OVER (
        PARTITION BY
            id_artista,
            LOWER(BTRIM(titulo)),
            ano_lancamento
    ) AS id_album_canonico
FROM album;

UPDATE musica AS musica_atual
SET id_album = mapa.id_album_canonico
FROM album_para_canonico AS mapa
WHERE musica_atual.id_album = mapa.id_album
  AND mapa.id_album <> mapa.id_album_canonico;

UPDATE perfil AS perfil_atual
SET id_album_destaque = mapa.id_album_canonico
FROM album_para_canonico AS mapa
WHERE perfil_atual.id_album_destaque = mapa.id_album
  AND mapa.id_album <> mapa.id_album_canonico;

DELETE FROM album AS album_repetido
USING album_para_canonico AS mapa
WHERE album_repetido.id_album = mapa.id_album
  AND mapa.id_album <> mapa.id_album_canonico;

ALTER TABLE album
    ADD CONSTRAINT ck_album_titulo_nao_vazio
        CHECK (BTRIM(titulo) <> '');

-- Garante a unicidade também em cadastros concorrentes e ignora diferenças
-- apenas de maiúsculas/minúsculas, como a validação do AlbumService.
CREATE UNIQUE INDEX uk_album_artista_titulo_ano_ci
    ON album (
        id_artista,
        LOWER(BTRIM(titulo)),
        ano_lancamento
    );
