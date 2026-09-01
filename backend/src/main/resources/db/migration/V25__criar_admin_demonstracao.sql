-- A migration V15 removeu o papel ADMIN de todas as contas fictícias
-- (ids 1-60), pois esse papel era sorteado por um gerador pseudoaleatório
-- com seed fixa, tornando previsível quais contas de teste tinham
-- privilégio de administrador. Esta migration cria uma única conta de
-- demonstração, fora da faixa de ids das contas fictícias, com uma senha
-- própria (não compartilhada com o restante da carga inicial).
INSERT INTO usuario (nome, email, senha, role, username)
VALUES (
    'Arthur',
    'admin.demo@gerenciador.com',
    '$2b$12$b88D7RN9CzoT7VJo/wwBNOZ/uhJMzDweGemfFfY.uf..MJ7fJ9fp.',
    'ADMIN',
    'admin.demo'
);
