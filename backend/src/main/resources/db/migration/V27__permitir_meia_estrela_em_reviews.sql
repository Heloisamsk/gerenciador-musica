-- Permite notas em passos de 0.5 (de 0.5 a 5), em vez de só inteiros
-- de 1 a 5. NUMERIC(2,1) guarda um dígito antes e um depois da vírgula
-- (ex.: 4.5), suficiente pra faixa 0.5..5.0.
ALTER TABLE review
    ALTER COLUMN nota TYPE NUMERIC(2,1) USING nota::numeric(2,1);

ALTER TABLE review
    DROP CONSTRAINT ck_review_nota;

-- nota * 2 tem que ser um inteiro pra garantir passos de 0.5
-- (1, 1.5, 2, ..., 5), rejeitando algo como 3.3.
ALTER TABLE review
    ADD CONSTRAINT ck_review_nota
        CHECK (
            nota BETWEEN 0.5 AND 5
            AND (nota * 2) = FLOOR(nota * 2)
        );
