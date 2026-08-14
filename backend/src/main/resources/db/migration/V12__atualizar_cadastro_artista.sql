ALTER TABLE artista
ADD COLUMN nome_completo VARCHAR(255);

UPDATE artista
SET nome_completo = nome
WHERE nome_completo IS NULL;

UPDATE artista SET nome_completo = 'Ariana Grande-Butera' WHERE id_artista = 1;
UPDATE artista SET nome_completo = 'Christopher Edwin Breaux' WHERE id_artista = 2;
UPDATE artista SET nome_completo = 'Madonna Louise Ciccone' WHERE id_artista = 3;
UPDATE artista SET nome_completo = 'Olivia Isabel Rodrigo' WHERE id_artista = 4;
UPDATE artista SET nome_completo = 'Marina de Oliveira Sena' WHERE id_artista = 5;
UPDATE artista SET nome_completo = 'BLACKPINK' WHERE id_artista = 6;
UPDATE artista SET nome_completo = 'Jennie Kim' WHERE id_artista = 7;
UPDATE artista SET nome_completo = 'Abel Makkonen Tesfaye' WHERE id_artista = 8;
UPDATE artista SET nome_completo = 'Maria da Graça Costa Penna Burgos' WHERE id_artista = 9;
UPDATE artista SET nome_completo = 'Benito Antonio Martínez Ocasio' WHERE id_artista = 10;
UPDATE artista SET nome_completo = 'Larissa de Macedo Machado' WHERE id_artista = 11;
UPDATE artista SET nome_completo = 'Addison Rae Easterling' WHERE id_artista = 12;
UPDATE artista SET nome_completo = 'Dua Lipa' WHERE id_artista = 13;
UPDATE artista SET nome_completo = 'Elizabeth Woolridge Grant' WHERE id_artista = 14;
UPDATE artista SET nome_completo = 'Charlotte Emma Aitchison' WHERE id_artista = 15;
UPDATE artista SET nome_completo = 'Tasha Okereke e Tracie Okereke' WHERE id_artista = 16;
UPDATE artista SET nome_completo = 'Tahliah Debrett Barnett' WHERE id_artista = 17;
UPDATE artista SET nome_completo = 'Solána Imani Rowe' WHERE id_artista = 18;
UPDATE artista SET nome_completo = 'Adéla Jergová' WHERE id_artista = 19;
UPDATE artista SET nome_completo = 'Harry Edward Styles' WHERE id_artista = 20;
UPDATE artista SET nome_completo = 'One Direction' WHERE id_artista = 21;
UPDATE artista SET nome_completo = 'Julia Roberta da Costa Silva' WHERE id_artista = 22;
UPDATE artista SET nome_completo = 'Billie Eilish Pirate Baird O''Connell' WHERE id_artista = 23;
UPDATE artista SET nome_completo = 'Rosalía Vila Tobella' WHERE id_artista = 24;
UPDATE artista SET nome_completo = 'Sabrina Annlynn Carpenter' WHERE id_artista = 25;
UPDATE artista SET nome_completo = 'Olivia Lauryn Dean' WHERE id_artista = 26;
UPDATE artista SET nome_completo = 'Ella Marija Lani Yelich-O''Connor' WHERE id_artista = 27;
UPDATE artista SET nome_completo = 'Mariah Carey' WHERE id_artista = 28;
UPDATE artista SET nome_completo = 'Amala Ratna Zandile Dlamini' WHERE id_artista = 29;
UPDATE artista SET nome_completo = 'Miley Ray Cyrus' WHERE id_artista = 30;
UPDATE artista SET nome_completo = 'Selena Marie Gomez' WHERE id_artista = 31;
UPDATE artista SET nome_completo = 'Karla Camila Cabello Estrabao' WHERE id_artista = 32;
UPDATE artista SET nome_completo = 'Demetria Devonne Lovato' WHERE id_artista = 33;
UPDATE artista SET nome_completo = 'Katheryn Elizabeth Hudson' WHERE id_artista = 34;
UPDATE artista SET nome_completo = 'Solange Piaget Knowles' WHERE id_artista = 35;
UPDATE artista SET nome_completo = 'Fernanda Xavier Ferreira Santana' WHERE id_artista = 36;
UPDATE artista SET nome_completo = 'Helen Folasade Adu' WHERE id_artista = 37;
UPDATE artista SET nome_completo = 'Catherine Grace Garner' WHERE id_artista = 38;
UPDATE artista SET nome_completo = 'Robyn Rihanna Fenty' WHERE id_artista = 39;
UPDATE artista SET nome_completo = 'Eduarda Bittencourt Simões' WHERE id_artista = 40;
UPDATE artista SET nome_completo = 'Marília Dias Mendonça' WHERE id_artista = 41;
UPDATE artista SET nome_completo = 'Kendrick Lamar Duckworth' WHERE id_artista = 42;
UPDATE artista SET nome_completo = 'Radiohead' WHERE id_artista = 43;
UPDATE artista SET nome_completo = 'Beyoncé Giselle Knowles-Carter' WHERE id_artista = 44;
UPDATE artista SET nome_completo = 'Arctic Monkeys' WHERE id_artista = 45;
UPDATE artista SET nome_completo = 'Tyla Laura Seethal' WHERE id_artista = 46;
UPDATE artista SET nome_completo = 'Tyler Gregory Okonma' WHERE id_artista = 47;
UPDATE artista SET nome_completo = 'Zara Maria Larsson' WHERE id_artista = 48;
UPDATE artista SET nome_completo = 'Lorena Urias Martins da Silva' WHERE id_artista = 49;
UPDATE artista SET nome_completo = 'Gabriela Amaral dos Santos' WHERE id_artista = 50;
UPDATE artista SET nome_completo = 'João Fernando Gomes Valério' WHERE id_artista = 51;

ALTER TABLE artista
ALTER COLUMN nome_completo SET NOT NULL;