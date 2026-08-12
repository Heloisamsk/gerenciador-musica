ALTER TABLE usuario
    ADD COLUMN username VARCHAR(30),
    ADD COLUMN data_cadastro TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE usuario
    ADD CONSTRAINT ck_usuario_username_formato
        CHECK (
            username IS NULL
            OR username ~ '^[a-z0-9._]{3,30}$'
        );

CREATE UNIQUE INDEX uk_usuario_username_ci
    ON usuario (LOWER(username))
    WHERE username IS NOT NULL;