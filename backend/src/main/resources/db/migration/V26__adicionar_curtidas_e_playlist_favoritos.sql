-- Marca a playlist "Favoritos", auto-criada e gerenciada pelo sistema
-- quando o usuário curte a primeira música. Diferente de uma playlist
-- comum, ela não pode ser renomeada, ter a capa trocada nem ser excluída
-- pelo usuário (ver PlaylistService).
ALTER TABLE playlist
    ADD COLUMN especial BOOLEAN NOT NULL DEFAULT FALSE;

-- Garante, a nível de banco, no máximo uma playlist especial por usuário.
CREATE UNIQUE INDEX uk_playlist_favoritos_por_usuario
    ON playlist (id_usuario)
    WHERE especial = TRUE;

-- A tabela curtida_musica já existia (criada na V8), mas nunca teve
-- nenhuma entidade/repositório/serviço em cima dela. Esta migration
-- cria a equivalente para álbuns, seguindo o mesmo formato.
CREATE TABLE curtida_album (
    id_usuario BIGINT NOT NULL,
    id_album BIGINT NOT NULL,
    curtida_em TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_curtida_album
        PRIMARY KEY (id_usuario, id_album),

    CONSTRAINT fk_curtida_album_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_curtida_album_album
        FOREIGN KEY (id_album)
        REFERENCES album (id_album)
        ON DELETE CASCADE
);

CREATE INDEX idx_curtida_album_album ON curtida_album (id_album);
