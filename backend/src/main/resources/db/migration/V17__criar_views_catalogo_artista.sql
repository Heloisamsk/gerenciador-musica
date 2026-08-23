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
        id_artista,
        COUNT(*) AS total_musicas_principais,
        COALESCE(SUM(duracao_segundos), 0) AS duracao_total_segundos
    FROM musica
    GROUP BY id_artista
),
participacoes_por_artista AS (
    SELECT
        id_artista,
        COUNT(*) AS total_participacoes
    FROM musica_artista
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
    musica.id_artista AS id_artista_contexto,
    artista_principal.nome AS nome_artista_contexto,
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
    'PRINCIPAL'::VARCHAR(20) AS papel_artista
FROM musica
JOIN artista artista_principal
    ON artista_principal.id_artista = musica.id_artista
LEFT JOIN album
    ON album.id_album = musica.id_album
LEFT JOIN generos_por_musica generos
    ON generos.id_musica = musica.id_musica

UNION ALL

SELECT
    musica_artista.id_artista AS id_artista_contexto,
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
    'PARTICIPANTE'::VARCHAR(20) AS papel_artista
FROM musica_artista
JOIN musica
    ON musica.id_musica = musica_artista.id_musica
JOIN artista artista_contexto
    ON artista_contexto.id_artista = musica_artista.id_artista
JOIN artista artista_principal
    ON artista_principal.id_artista = musica.id_artista
LEFT JOIN album
    ON album.id_album = musica.id_album
LEFT JOIN generos_por_musica generos
    ON generos.id_musica = musica.id_musica;
