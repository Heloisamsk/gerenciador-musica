DROP VIEW IF EXISTS vw_musicas_artista_catalogo;
DROP VIEW IF EXISTS vw_albuns_artista_catalogo;
DROP VIEW IF EXISTS vw_artista_resumo_catalogo;


-- A V16 reuniu álbuns repetidos, mas as faixas desses álbuns ainda podem ter
-- IDs diferentes. O agrupamento abaixo segue as regras de duplicidade usadas
-- pelo MusicaService: álbum + título ou, para single, artista + título + ano.
UPDATE musica
SET titulo = REGEXP_REPLACE(
        BTRIM(titulo),
        '[[:space:]]+',
        ' ',
        'g'
    );

CREATE TEMPORARY TABLE musica_para_canonica
ON COMMIT DROP
AS
SELECT
    id_musica,
    MIN(id_musica) OVER (
        PARTITION BY
            CASE
                WHEN id_album IS NULL THEN 'SINGLE'
                ELSE 'ALBUM'
            END,
            id_album,
            CASE
                WHEN id_album IS NULL THEN id_artista
                ELSE NULL
            END,
            LOWER(BTRIM(titulo)),
            CASE
                WHEN id_album IS NULL THEN ano_lancamento
                ELSE NULL
            END
    ) AS id_musica_canonica
FROM musica;

INSERT INTO musica_artista (id_musica, id_artista)
SELECT DISTINCT
    mapa.id_musica_canonica,
    credito.id_artista
FROM musica_artista credito
JOIN musica_para_canonica mapa
    ON mapa.id_musica = credito.id_musica
WHERE mapa.id_musica <> mapa.id_musica_canonica
ON CONFLICT (id_musica, id_artista) DO NOTHING;

INSERT INTO musica_genero (id_musica, id_genero)
SELECT DISTINCT
    mapa.id_musica_canonica,
    musica_genero.id_genero
FROM musica_genero
JOIN musica_para_canonica mapa
    ON mapa.id_musica = musica_genero.id_musica
WHERE mapa.id_musica <> mapa.id_musica_canonica
ON CONFLICT (id_musica, id_genero) DO NOTHING;

CREATE TEMPORARY TABLE playlist_musica_canonica
ON COMMIT DROP
AS
SELECT
    playlist_musica.id_playlist,
    mapa.id_musica_canonica AS id_musica,
    MIN(playlist_musica.ordem) AS ordem,
    MIN(playlist_musica.data_criacao) AS data_criacao
FROM playlist_musica
JOIN musica_para_canonica mapa
    ON mapa.id_musica = playlist_musica.id_musica
WHERE mapa.id_musica <> mapa.id_musica_canonica
GROUP BY
    playlist_musica.id_playlist,
    mapa.id_musica_canonica;

DELETE FROM playlist_musica item
USING musica_para_canonica mapa
WHERE item.id_musica = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica;

INSERT INTO playlist_musica (
    id_playlist,
    id_musica,
    ordem,
    data_criacao
)
SELECT
    id_playlist,
    id_musica,
    ordem,
    data_criacao
FROM playlist_musica_canonica
ON CONFLICT (id_playlist, id_musica) DO NOTHING;

UPDATE perfil perfil_atual
SET id_musica_destaque = mapa.id_musica_canonica
FROM musica_para_canonica mapa
WHERE perfil_atual.id_musica_destaque = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica;

DELETE FROM review review_repetida
USING musica_para_canonica mapa
WHERE review_repetida.id_musica = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica
  AND EXISTS (
      SELECT 1
      FROM review review_canonica
      WHERE review_canonica.id_usuario = review_repetida.id_usuario
        AND review_canonica.id_musica = mapa.id_musica_canonica
  );

WITH reviews_ordenadas AS (
    SELECT
        review.id_review,
        ROW_NUMBER() OVER (
            PARTITION BY
                review.id_usuario,
                mapa.id_musica_canonica
            ORDER BY
                review.atualizada_em DESC,
                review.id_review DESC
        ) AS posicao
    FROM review
    JOIN musica_para_canonica mapa
        ON mapa.id_musica = review.id_musica
    WHERE mapa.id_musica <> mapa.id_musica_canonica
)
DELETE FROM review review_repetida
USING reviews_ordenadas
WHERE review_repetida.id_review = reviews_ordenadas.id_review
  AND reviews_ordenadas.posicao > 1;

UPDATE review review_atual
SET id_musica = mapa.id_musica_canonica
FROM musica_para_canonica mapa
WHERE review_atual.id_musica = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica;

INSERT INTO curtida_musica (
    id_usuario,
    id_musica,
    curtida_em
)
SELECT
    curtida.id_usuario,
    mapa.id_musica_canonica,
    MIN(curtida.curtida_em)
FROM curtida_musica curtida
JOIN musica_para_canonica mapa
    ON mapa.id_musica = curtida.id_musica
WHERE mapa.id_musica <> mapa.id_musica_canonica
GROUP BY
    curtida.id_usuario,
    mapa.id_musica_canonica
ON CONFLICT (id_usuario, id_musica) DO NOTHING;

UPDATE reproducao reproducao_atual
SET id_musica = mapa.id_musica_canonica
FROM musica_para_canonica mapa
WHERE reproducao_atual.id_musica = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica;

DELETE FROM musica musica_repetida
USING musica_para_canonica mapa
WHERE musica_repetida.id_musica = mapa.id_musica
  AND mapa.id_musica <> mapa.id_musica_canonica;


ALTER TABLE musica_artista
    ADD COLUMN papel_participacao VARCHAR(20);

UPDATE musica_artista credito
SET papel_participacao = CASE
    WHEN credito.id_artista = musica.id_artista THEN 'PRINCIPAL'
    ELSE 'FEAT'
END
FROM musica
WHERE musica.id_musica = credito.id_musica;

INSERT INTO musica_artista (
    id_musica,
    id_artista,
    papel_participacao
)
SELECT
    id_musica,
    id_artista,
    'PRINCIPAL'
FROM musica
ON CONFLICT (id_musica, id_artista)
DO UPDATE SET papel_participacao = EXCLUDED.papel_participacao;

ALTER TABLE musica_artista
    ALTER COLUMN papel_participacao SET NOT NULL;

ALTER TABLE musica_artista
    ADD CONSTRAINT ck_musica_artista_papel
        CHECK (
            papel_participacao IN (
                'PRINCIPAL',
                'FEAT',
                'PRODUTOR'
            )
        );

COMMENT ON COLUMN musica_artista.papel_participacao IS
    'Papel do artista na musica: PRINCIPAL, FEAT ou PRODUTOR.';

ALTER TABLE musica
    DROP CONSTRAINT fk_musica_artista_principal;

DROP INDEX IF EXISTS idx_musica_id_artista;

ALTER TABLE musica
    DROP COLUMN id_artista;

CREATE INDEX idx_musica_artista_principal
    ON musica_artista (id_musica)
    WHERE papel_participacao = 'PRINCIPAL';


CREATE FUNCTION validar_artista_principal_musica()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    id_musica_validada BIGINT;
    total_principais BIGINT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        id_musica_validada := OLD.id_musica;
    ELSE
        id_musica_validada := NEW.id_musica;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM musica
        WHERE id_musica = id_musica_validada
    ) THEN
        RETURN NULL;
    END IF;

    SELECT COUNT(*)
    INTO total_principais
    FROM musica_artista
    WHERE id_musica = id_musica_validada
      AND papel_participacao = 'PRINCIPAL';

    IF total_principais <> 1 THEN
        RAISE EXCEPTION
            'A musica % deve possuir exatamente um artista principal.',
            id_musica_validada
            USING ERRCODE = '23514';
    END IF;

    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER ct_musica_exige_artista_principal
AFTER INSERT OR UPDATE ON musica
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION validar_artista_principal_musica();

CREATE CONSTRAINT TRIGGER ct_credito_exige_artista_principal
AFTER INSERT OR UPDATE OR DELETE ON musica_artista
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION validar_artista_principal_musica();


CREATE VIEW vw_artista_resumo_catalogo AS
WITH albuns_por_artista AS (
    SELECT
        id_artista,
        COUNT(*) AS total_albuns
    FROM album
    GROUP BY id_artista
),
musicas_principais_por_artista AS (
    SELECT
        credito.id_artista,
        COUNT(*) AS total_musicas_principais,
        COALESCE(
            SUM(musica.duracao_segundos),
            0
        ) AS duracao_total_segundos
    FROM musica_artista credito
    JOIN musica
        ON musica.id_musica = credito.id_musica
    WHERE credito.papel_participacao = 'PRINCIPAL'
    GROUP BY credito.id_artista
),
participacoes_por_artista AS (
    SELECT
        id_artista,
        COUNT(*) AS total_participacoes
    FROM musica_artista
    WHERE papel_participacao = 'FEAT'
    GROUP BY id_artista
)
SELECT
    artista.id_artista,
    artista.nome,
    artista.nome_completo,
    artista.descricao,
    artista.foto_perfil_url,
    COALESCE(albuns.total_albuns, 0) AS total_albuns,
    COALESCE(
        musicas.total_musicas_principais,
        0
    ) AS total_musicas_principais,
    COALESCE(
        participacoes.total_participacoes,
        0
    ) AS total_participacoes,
    COALESCE(
        musicas.duracao_total_segundos,
        0
    ) AS duracao_total_segundos
FROM artista
LEFT JOIN albuns_por_artista albuns
    ON albuns.id_artista = artista.id_artista
LEFT JOIN musicas_principais_por_artista musicas
    ON musicas.id_artista = artista.id_artista
LEFT JOIN participacoes_por_artista participacoes
    ON participacoes.id_artista = artista.id_artista;


CREATE VIEW vw_albuns_artista_catalogo AS
SELECT
    album.id_album,
    album.id_artista,
    artista.nome AS nome_artista,
    album.titulo,
    album.ano_lancamento,
    album.capa_url,
    COUNT(musica.id_musica) AS total_musicas,
    COALESCE(
        SUM(musica.duracao_segundos),
        0
    ) AS duracao_total_segundos
FROM album
JOIN artista
    ON artista.id_artista = album.id_artista
LEFT JOIN musica
    ON musica.id_album = album.id_album
GROUP BY
    album.id_album,
    album.id_artista,
    artista.nome,
    album.titulo,
    album.ano_lancamento,
    album.capa_url;


CREATE VIEW vw_musicas_artista_catalogo AS
WITH generos_por_musica AS (
    SELECT
        musica_genero.id_musica,
        STRING_AGG(
            DISTINCT genero.nome,
            ', '
            ORDER BY genero.nome
        ) AS generos
    FROM musica_genero
    JOIN genero
        ON genero.id_genero = musica_genero.id_genero
    GROUP BY musica_genero.id_musica
)
SELECT
    credito_contexto.id_artista AS id_artista_contexto,
    artista_contexto.nome AS nome_artista_contexto,
    musica.id_musica,
    musica.titulo,
    musica.duracao_segundos,
    musica.ano_lancamento,
    artista_principal.id_artista AS id_artista_principal,
    artista_principal.nome AS nome_artista_principal,
    album.id_album,
    album.titulo AS titulo_album,
    album.capa_url,
    COALESCE(generos.generos, '') AS generos,
    CASE credito_contexto.papel_participacao
        WHEN 'FEAT' THEN 'PARTICIPANTE'
        ELSE 'PRINCIPAL'
    END::VARCHAR(20) AS papel_artista
FROM musica_artista credito_contexto
JOIN musica
    ON musica.id_musica = credito_contexto.id_musica
JOIN artista artista_contexto
    ON artista_contexto.id_artista = credito_contexto.id_artista
JOIN musica_artista credito_principal
    ON credito_principal.id_musica = musica.id_musica
   AND credito_principal.papel_participacao = 'PRINCIPAL'
JOIN artista artista_principal
    ON artista_principal.id_artista = credito_principal.id_artista
LEFT JOIN album
    ON album.id_album = musica.id_album
LEFT JOIN generos_por_musica generos
    ON generos.id_musica = musica.id_musica
WHERE credito_contexto.papel_participacao IN ('PRINCIPAL', 'FEAT');
