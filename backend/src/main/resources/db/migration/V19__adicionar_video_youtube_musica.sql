ALTER TABLE musica
    ADD COLUMN youtube_video_id VARCHAR(11);

ALTER TABLE musica
    ADD CONSTRAINT ck_musica_youtube_video_id
        CHECK (
            youtube_video_id IS NULL
            OR youtube_video_id ~ '^[A-Za-z0-9_-]{11}$'
        );

COMMENT ON COLUMN musica.youtube_video_id IS
    'Identificador opcional de um vídeo do YouTube usado no player da música.';
