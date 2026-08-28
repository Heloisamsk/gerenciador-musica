CREATE FUNCTION fn_validar_duracao_reproducao()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    duracao_musica INTEGER;
BEGIN
    SELECT duracao_segundos
    INTO duracao_musica
    FROM musica
    WHERE id_musica = NEW.id_musica;

    IF duracao_musica IS NOT NULL
       AND NEW.segundos_ouvidos > duracao_musica THEN
        RAISE EXCEPTION
            'Os segundos ouvidos (%) não podem superar a duração da música (%).',
            NEW.segundos_ouvidos,
            duracao_musica
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_duracao_reproducao
BEFORE INSERT OR UPDATE OF id_musica, segundos_ouvidos
ON reproducao
FOR EACH ROW
EXECUTE FUNCTION fn_validar_duracao_reproducao();

COMMENT ON FUNCTION fn_validar_duracao_reproducao() IS
    'Garante que uma reprodução não registre mais segundos que a duração da música.';
