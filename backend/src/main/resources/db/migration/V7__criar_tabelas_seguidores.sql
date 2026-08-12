CREATE TABLE usuario_segue_usuario (
    id_seguidor BIGINT NOT NULL,
    id_seguido BIGINT NOT NULL,
    seguido_em TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario_segue_usuario
        PRIMARY KEY (id_seguidor, id_seguido),

    CONSTRAINT fk_usuario_segue_usuario_seguidor
        FOREIGN KEY (id_seguidor)
        REFERENCES usuario (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_segue_usuario_seguido
        FOREIGN KEY (id_seguido)
        REFERENCES usuario (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_usuario_nao_segue_si_mesmo
        CHECK (id_seguidor <> id_seguido)
);

CREATE INDEX idx_usuario_segue_usuario_seguido
    ON usuario_segue_usuario (id_seguido);


CREATE TABLE usuario_segue_artista (
    id_usuario BIGINT NOT NULL,
    id_artista BIGINT NOT NULL,
    seguido_em TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario_segue_artista
        PRIMARY KEY (id_usuario, id_artista),

    CONSTRAINT fk_usuario_segue_artista_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_segue_artista_artista
        FOREIGN KEY (id_artista)
        REFERENCES artista (id_artista)
        ON DELETE CASCADE
);

CREATE INDEX idx_usuario_segue_artista_artista
    ON usuario_segue_artista (id_artista);