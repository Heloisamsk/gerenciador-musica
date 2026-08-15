UPDATE artista
SET descricao = CASE id_artista
    WHEN 1 THEN 'Cantora, compositora e atriz norte-americana conhecida por seu trabalho no pop e no R&B.'
    WHEN 2 THEN 'Cantor, compositor e produtor norte-americano reconhecido por sua abordagem autoral ao R&B alternativo.'
    WHEN 3 THEN 'Cantora, compositora e atriz norte-americana, uma das principais referências da música pop.'
    WHEN 4 THEN 'Cantora, compositora e atriz norte-americana conhecida por canções de pop e rock alternativo.'
    WHEN 5 THEN 'Cantora e compositora brasileira que combina elementos de pop, MPB e música eletrônica.'
    WHEN 6 THEN 'Grupo feminino sul-coreano conhecido por combinar K-pop, hip-hop, música eletrônica e pop.'
    WHEN 7 THEN 'Cantora, rapper e atriz sul-coreana, integrante do grupo BLACKPINK e também artista solo.'
    WHEN 8 THEN 'Cantor, compositor e produtor canadense conhecido por trabalhos que misturam pop e R&B.'
    WHEN 9 THEN 'Cantora brasileira de destaque na MPB e no movimento tropicalista.'
    WHEN 10 THEN 'Cantor e rapper porto-riquenho conhecido por trabalhos de reggaeton e trap latino.'
    WHEN 11 THEN 'Cantora, compositora e empresária brasileira conhecida por trabalhos no pop e no funk carioca.'
    WHEN 12 THEN 'Cantora, atriz e personalidade norte-americana com trabalhos voltados à música pop.'
    WHEN 13 THEN 'Cantora e compositora britânica de origem albanesa conhecida por seu trabalho na música pop.'
    WHEN 14 THEN 'Cantora e compositora norte-americana conhecida por seu estilo de pop alternativo e atmosfera cinematográfica.'
    WHEN 15 THEN 'Cantora e compositora britânica conhecida por explorar vertentes experimentais e eletrônicas do pop.'
    WHEN 16 THEN 'Dupla brasileira de hip-hop formada pelas irmãs Tasha e Tracie, com trabalhos ligados ao rap, à moda e à cultura periférica.'
    WHEN 17 THEN 'Cantora, compositora, produtora e dançarina britânica conhecida por sua abordagem experimental ao pop e ao R&B.'
    WHEN 18 THEN 'Cantora e compositora norte-americana reconhecida por seu trabalho no R&B contemporâneo.'
    WHEN 19 THEN 'Cantora, compositora e dançarina eslovaca conhecida por seu trabalho como artista pop solo.'
    WHEN 20 THEN 'Cantor, compositor e ator britânico, ex-integrante do One Direction e artista solo de música pop.'
    WHEN 21 THEN 'Grupo vocal britânico-irlandês conhecido mundialmente por seu repertório de música pop.'
    WHEN 22 THEN 'Rapper, cantora, compositora e empresária brasileira de Mogi das Cruzes, destaque da nova geração do rap nacional.'
    WHEN 23 THEN 'Cantora e compositora norte-americana conhecida por sua abordagem alternativa à música pop.'
    WHEN 24 THEN 'Cantora, compositora e produtora espanhola conhecida por combinar flamenco, pop e música urbana.'
    WHEN 25 THEN 'Cantora, compositora e atriz norte-americana conhecida por seu trabalho na música pop.'
    WHEN 26 THEN 'Cantora e compositora britânica conhecida por trabalhos que combinam soul, pop e R&B.'
    WHEN 27 THEN 'Cantora e compositora neozelandesa conhecida por seu estilo de pop alternativo.'
    WHEN 28 THEN 'Cantora, compositora e produtora norte-americana reconhecida por seu trabalho no pop e no R&B.'
    WHEN 29 THEN 'Rapper, cantora e compositora norte-americana conhecida por combinar hip-hop, pop e R&B.'
    WHEN 30 THEN 'Cantora, compositora e atriz norte-americana conhecida por trabalhos de pop e rock.'
    WHEN 31 THEN 'Cantora, atriz e produtora norte-americana conhecida por seu trabalho na música pop.'
    WHEN 32 THEN 'Cantora e compositora cubano-americana conhecida por seu trabalho na música pop.'
    WHEN 33 THEN 'Cantora, compositora e atriz norte-americana conhecida por trabalhos de pop e rock.'
    WHEN 34 THEN 'Cantora e compositora norte-americana conhecida por sucessos da música pop.'
    WHEN 35 THEN 'Cantora, compositora e artista visual norte-americana conhecida por trabalhos de R&B e soul.'
    WHEN 36 THEN 'Rapper e cantora brasileira conhecida por combinar hip-hop, funk, R&B, afrobeats e música eletrônica.'
    WHEN 37 THEN 'Cantora e compositora britânica, vocalista da banda Sade, conhecida por trabalhos de soul e R&B.'
    WHEN 38 THEN 'Cantora e compositora norte-americana conhecida por trabalhos de pop eletrônico.'
    WHEN 39 THEN 'Cantora, atriz e empresária barbadiana conhecida por trabalhos de pop e R&B.'
    WHEN 40 THEN 'Cantora e compositora brasileira conhecida por combinar pop, MPB e influências do brega.'
    WHEN 41 THEN 'Cantora e compositora brasileira que se tornou uma das principais referências do sertanejo.'
    WHEN 42 THEN 'Rapper e compositor norte-americano reconhecido por sua produção artística no hip-hop.'
    WHEN 43 THEN 'Banda britânica de rock conhecida por trabalhos alternativos e experimentais.'
    WHEN 44 THEN 'Cantora, compositora, produtora e atriz norte-americana reconhecida por trabalhos de pop e R&B.'
    WHEN 45 THEN 'Banda britânica de rock conhecida por seu trabalho no indie rock e no rock alternativo.'
    WHEN 46 THEN 'Cantora e compositora sul-africana conhecida por combinar pop, R&B e amapiano.'
    WHEN 47 THEN 'Rapper, compositor e produtor norte-americano conhecido por sua abordagem criativa ao hip-hop.'
    WHEN 48 THEN 'Cantora e compositora sueca conhecida por seu trabalho na música pop.'
    WHEN 49 THEN 'Cantora e compositora brasileira conhecida por trabalhos que combinam pop, música eletrônica e R&B.'
    WHEN 50 THEN 'Cantora e compositora brasileira do Pará conhecida por trabalhos de tecnobrega e música pop.'
    WHEN 51 THEN 'Cantor e compositor brasileiro conhecido por trabalhos de piseiro e forró.'
    ELSE descricao
END
WHERE id_artista BETWEEN 1 AND 51;

UPDATE artista
SET descricao = 'Artista presente no catálogo musical.'
WHERE descricao IS NULL
   OR BTRIM(descricao) = '';

ALTER TABLE artista
    ALTER COLUMN descricao SET NOT NULL;

ALTER TABLE artista
    ADD CONSTRAINT ck_artista_descricao_nao_vazia
        CHECK (BTRIM(descricao) <> '');

ALTER TABLE artista
    ADD CONSTRAINT ck_artista_descricao_tamanho
        CHECK (CHAR_LENGTH(descricao) <= 500);