-- Indices de apoio a pesquisa do catalogo musical (US06 / #113).
-- Todos os indices sao aditivos: nao alteram dados nem migrations ja aplicadas.

-- Titulo da musica: filtro parcial e case-insensitive (LOWER(titulo) LIKE '%...%').
-- Um indice funcional em LOWER(titulo) acelera comparacoes exatas e por prefixo;
-- para o LIKE com wildcard nas duas pontas o ganho e parcial, mas ainda evita
-- varredura completa em buscas mais seletivas.
CREATE INDEX idx_musica_titulo ON musica (LOWER(titulo));

-- Ano de lancamento: filtro exato.
CREATE INDEX idx_musica_ano_lancamento ON musica (ano_lancamento);

-- Chaves estrangeiras de musica: no Postgres, FOREIGN KEY nao cria indice
-- automaticamente do lado que referencia (so do lado referenciado, via
-- chave primaria). Como id_artista e id_album sao usados nos filtros da
-- pesquisa, precisam de indice proprio.
CREATE INDEX idx_musica_id_artista ON musica (id_artista);
CREATE INDEX idx_musica_id_album ON musica (id_album);

-- Nome do artista: busca case-insensitive.
CREATE INDEX idx_artista_nome ON artista (LOWER(nome));

-- Titulo do album: busca case-insensitive.
CREATE INDEX idx_album_titulo ON album (LOWER(titulo));

-- genero.nome ja possui indice unico (constraint uk_genero_nome, criada na
-- V2 junto com o UNIQUE), entao nenhum indice novo e necessario para ele.

-- Indices reversos nas tabelas de juncao: a chave primaria composta
-- (id_musica, id_artista) e (id_musica, id_genero) so e util para buscas
-- que partem da musica. O filtro de pesquisa parte do artista/genero
-- (id_artista -> musicas, id_genero -> musicas), entao precisa do indice
-- comecando pela outra coluna.
CREATE INDEX idx_musica_artista_id_artista ON musica_artista (id_artista, id_musica);
CREATE INDEX idx_musica_genero_id_genero ON musica_genero (id_genero, id_musica);
