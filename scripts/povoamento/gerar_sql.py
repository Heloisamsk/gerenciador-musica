import json
import os
import secrets
import re
import unicodedata

import bcrypt
from faker import Faker

fake = Faker("pt_BR")
Faker.seed(42)
gerador_seguro = secrets.SystemRandom()

PASTA = os.path.dirname(__file__)
CAMINHO_JSON = os.path.join(PASTA, "dados_coletados.json")
CAMINHO_SQL = os.path.join(
    PASTA, "..", "..", "backend", "src", "main", "resources",
    "db", "migration", "V11__popular_dados_iniciais.sql",
)

SENHA_PADRAO = "Senha@123"
HASH_SENHA_PADRAO = bcrypt.hashpw(SENHA_PADRAO.encode(), bcrypt.gensalt()).decode()

QTD_USUARIOS = 60
QTD_PLAYLISTS = 60
QTD_REVIEWS = 150
QTD_REPRODUCOES = 400
QTD_CURTIDAS = 250
QTD_SEGUE_USUARIO = 120
QTD_SEGUE_ARTISTA = 120
QTD_PERFIS = 35

# Spotify nao devolve mais o genero de cada artista pra apps novos,
# entao classificamos manualmente. Quem nao estiver aqui cai no padrao.
GENERO_PADRAO = "Pop"
POP_ROCK = "Pop Rock"
GENEROS_POR_ARTISTA = {
    "Ariana Grande": ["Pop"],
    "Frank Ocean": ["R&B"],
    "Madonna": ["Pop"],
    "Olivia Rodrigo": [POP_ROCK],
    "Marina Sena": ["MPB"],
    "BLACKPINK": ["K-Pop"],
    "JENNIE": ["K-Pop"],
    "The Weeknd": ["R&B"],
    "Gal Costa": ["MPB"],
    "Bad Bunny": ["Reggaeton"],
    "Anitta": ["Funk"],
    "Addison Rae": ["Pop"],
    "Dua Lipa": ["Pop"],
    "Lana Del Rey": ["Indie Pop"],
    "Charli xcx": ["Eletropop"],
    "Tasha & Tracie": ["MPB"],
    "FKA twigs": ["R&B Alternativo"],
    "SZA": ["R&B"],
    "Harry Styles": [POP_ROCK],
    "One Direction": [POP_ROCK],
    "Billie Eilish": ["Pop Alternativo"],
    "ROSALÍA": ["Flamenco Pop"],
    "Sabrina Carpenter": ["Pop"],
    "Olivia Dean": ["Soul"],
    "Lorde": ["Pop Alternativo"],
    "Mariah Carey": ["R&B"],
    "Doja Cat": ["Hip Hop"],
    "Miley Cyrus": [POP_ROCK],
    "Selena Gomez": ["Pop"],
    "Camila Cabello": ["Pop"],
    "Demi Lovato": [POP_ROCK],
    "Katy Perry": ["Pop"],
    "Solange": ["R&B"],
    "Sade": ["Soul"],
    "Slayyyter": ["Hyperpop"],
    "Rihanna": ["R&B"],
    "DUDA BEAT": ["Pop Nacional"],
    "Marília Mendonça": ["Sertanejo"],
    "Kendrick Lamar": ["Rap"],
    "Radiohead": ["Rock Alternativo"],
    "Beyoncé": ["R&B"],
    "Arctic Monkeys": ["Rock Alternativo"],
    "Tyla": ["Amapiano"],
    "Tyler, The Creator": ["Hip Hop"],
    "Zara Larsson": ["Pop"],
    "Urias": ["Pop Nacional"],
    "Gaby Amarantos": ["Tecnobrega"],
    "João Gomes": ["Piseiro"],
}



def esc(texto):
    if texto is None:
        return "NULL"
    return "'" + str(texto).replace("'", "''") + "'"


def num(valor):
    return "NULL" if valor is None else str(valor)


def gerar_username(nome, usados):
    base = unicodedata.normalize("NFKD", nome).encode("ascii", "ignore").decode()
    base = re.sub(r"[^a-z0-9._]", "", base.lower().replace(" ", "."))
    base = base[:26] or "usuario"
    candidato = base
    contador = 1
    while candidato.lower() in usados:
        contador += 1
        candidato = f"{base}{contador}"[:30]
    usados.add(candidato.lower())
    return candidato


def data_aleatoria(dias_atras_max):
    return fake.date_time_between(
        start_date=f"-{dias_atras_max}d",
        end_date="now"
    )


def carregar_dados():
    with open(CAMINHO_JSON, "r", encoding="utf-8") as arquivo:
        return json.load(arquivo)


def criar_ids(dados):
    return {
        "artista": {
            artista["id"]: indice + 1
            for indice, artista in enumerate(dados["artistas"])
        },
        "album": {
            album["id"]: indice + 1
            for indice, album in enumerate(dados["albuns"])
        },
        "musica": {
            musica["id"]: indice + 1
            for indice, musica in enumerate(dados["musicas"])
        },
    }


def criar_ids_generos(artistas):
    nomes = []

    for artista in artistas:
        generos = GENEROS_POR_ARTISTA.get(
            artista["nome"],
            [GENERO_PADRAO],
        )
        for genero in generos:
            if genero not in nomes:
                nomes.append(genero)

    return {nome: indice + 1 for indice, nome in enumerate(nomes)}


def iniciar_linhas():
    return [
        "-- Gerado automaticamente por scripts/povoamento/gerar_sql.py",
        "-- Catalogo real via API do Spotify + dados ficticios via Faker.\n",
    ]


def adicionar_artistas(linhas, artistas, ids_artistas):
    linhas.append("-- artista")

    for artista in artistas:
        linhas.append(
            "INSERT INTO artista (id_artista, nome, foto_perfil_url) VALUES "
            f"({ids_artistas[artista['id']]}, {esc(artista['nome'])}, "
            f"{esc(artista['foto_url'])});"
        )


def adicionar_generos(linhas, ids_generos):
    linhas.append("\n-- genero")

    for nome, genero_id in ids_generos.items():
        linhas.append(
            "INSERT INTO genero (id_genero, nome) VALUES "
            f"({genero_id}, {esc(nome)});"
        )


def adicionar_albuns(linhas, albuns, ids):
    linhas.append("\n-- album")
    ids_albuns = ids["album"]
    ids_artistas = ids["artista"]

    for album in albuns:
        linhas.append(
            "INSERT INTO album "
            "(id_album, id_artista, titulo, ano_lancamento, capa_url) VALUES "
            f"({ids_albuns[album['id']]}, "
            f"{ids_artistas[album['id_artista']]}, {esc(album['titulo'])}, "
            f"{album['ano_lancamento']}, {esc(album['capa_url'])});"
        )


def adicionar_musicas(linhas, musicas, artistas, ids):
    linhas.append("\n-- musica")
    relacoes_artistas = set()
    relacoes_generos = set()
    musicas_inseridas = []
    nomes_artistas = {
        artista["id"]: artista["nome"]
        for artista in artistas
    }

    for musica in musicas:
        musica_id = ids["musica"][musica["id"]]
        artistas_da_faixa = musica["artistas"]
        artista_spotify_id = (
            artistas_da_faixa[0]["id"]
            if artistas_da_faixa
            else None
        )
        artista_principal_id = ids["artista"].get(artista_spotify_id)

        if artista_principal_id is None:
            continue

        musicas_inseridas.append(musica_id)
        linhas.append(
            "INSERT INTO musica "
            "(id_musica, id_album, id_artista, titulo, "
            "duracao_segundos, ano_lancamento) VALUES "
            f"({musica_id}, {ids['album'][musica['id_album']]}, "
            f"{artista_principal_id}, {esc(musica['titulo'])}, "
            f"{musica['duracao_segundos']}, {musica['ano_lancamento']});"
        )

        for artista in artistas_da_faixa:
            artista_id = ids["artista"].get(artista["id"])
            if artista_id is not None:
                relacoes_artistas.add((musica_id, artista_id))

        nome_artista = nomes_artistas.get(artista_spotify_id)
        generos = GENEROS_POR_ARTISTA.get(nome_artista, [GENERO_PADRAO])
        for genero in generos:
            relacoes_generos.add((musica_id, ids["genero"][genero]))

    adicionar_relacoes_musica(
        linhas,
        relacoes_artistas,
        relacoes_generos,
    )
    return musicas_inseridas


def adicionar_relacoes_musica(linhas, relacoes_artistas, relacoes_generos):
    linhas.append("\n-- musica_artista")
    for musica_id, artista_id in sorted(relacoes_artistas):
        linhas.append(
            "INSERT INTO musica_artista (id_musica, id_artista) VALUES "
            f"({musica_id}, {artista_id});"
        )

    linhas.append("\n-- musica_genero")
    for musica_id, genero_id in sorted(relacoes_generos):
        linhas.append(
            "INSERT INTO musica_genero (id_musica, id_genero) VALUES "
            f"({musica_id}, {genero_id});"
        )


def adicionar_usuarios(linhas):
    linhas.append("\n-- usuario")
    usernames_usados = set()
    usuarios = []

    for usuario_id in range(1, QTD_USUARIOS + 1):
        nome = fake.name()
        email = fake.unique.email()
        username = gerar_username(nome, usernames_usados)
        data_cadastro = data_aleatoria(730)
        usuarios.append({"id": usuario_id, "nome": nome})
        linhas.append(
            "INSERT INTO usuario "
            "(id, nome, email, senha, role, username, data_cadastro) VALUES "
            f"({usuario_id}, {esc(nome)}, {esc(email)}, "
            f"{esc(HASH_SENHA_PADRAO)}, {esc('USER')}, "
            f"{esc(username)}, {esc(data_cadastro.isoformat())});"
        )

    return usuarios


def sortear_destaque(valores, probabilidade):
    if gerador_seguro.random() >= probabilidade:
        return None
    return gerador_seguro.choice(valores)


def adicionar_perfis(linhas, usuarios, ids, musicas_inseridas):
    linhas.append("\n-- perfil")
    usuarios_com_perfil = gerador_seguro.sample(usuarios, QTD_PERFIS)
    artistas = list(ids["artista"].values())
    albuns = list(ids["album"].values())

    for perfil_id, usuario in enumerate(usuarios_com_perfil, start=1):
        artista_destaque = sortear_destaque(artistas, 0.6)
        album_destaque = sortear_destaque(albuns, 0.4)
        musica_destaque = sortear_destaque(musicas_inseridas, 0.4)
        linhas.append(
            "INSERT INTO perfil "
            "(id_perfil, id_usuario, biografia, frase_destaque, "
            "id_artista_destaque, id_album_destaque, "
            "id_musica_destaque) VALUES "
            f"({perfil_id}, {usuario['id']}, "
            f"{esc(fake.sentence(nb_words=12))}, "
            f"{esc(fake.catch_phrase())}, {num(artista_destaque)}, "
            f"{num(album_destaque)}, {num(musica_destaque)});"
        )


def adicionar_playlists(linhas, usuarios):
    linhas.append("\n-- playlist")
    playlists = []

    for playlist_id in range(1, QTD_PLAYLISTS + 1):
        dono = gerador_seguro.choice(usuarios)
        nome = f"{fake.word().capitalize()} {fake.word()}"
        data_criacao = data_aleatoria(600)
        playlists.append({"id": playlist_id, "usuario": dono["id"]})
        linhas.append(
            "INSERT INTO playlist "
            "(id_playlist, nome, descricao, id_usuario, data_criacao) VALUES "
            f"({playlist_id}, {esc(nome)}, "
            f"{esc(fake.sentence(nb_words=10))}, {dono['id']}, "
            f"{esc(data_criacao.isoformat())});"
        )

    return playlists


def adicionar_faixas_playlists(linhas, playlists, musicas_ids):
    linhas.append("\n-- playlist_musica")

    for playlist in playlists:
        quantidade = gerador_seguro.randint(4, 18)
        musicas = gerador_seguro.sample(
            musicas_ids,
            min(quantidade, len(musicas_ids)),
        )
        for ordem, musica_id in enumerate(musicas, start=1):
            linhas.append(
                "INSERT INTO playlist_musica "
                "(id_playlist, id_musica, ordem) VALUES "
                f"({playlist['id']}, {musica_id}, {ordem});"
            )


def adicionar_reviews(linhas, usuarios, musicas_ids):
    linhas.append("\n-- review")
    pares = set()

    while len(pares) < QTD_REVIEWS:
        usuario_id = gerador_seguro.choice(usuarios)["id"]
        musica_id = gerador_seguro.choice(musicas_ids)
        pares.add((usuario_id, musica_id))

    for usuario_id, musica_id in sorted(pares):
        nota = gerador_seguro.randint(1, 5)
        texto = (
            fake.sentence(nb_words=15)
            if gerador_seguro.random() < 0.7
            else None
        )
        linhas.append(
            "INSERT INTO review (id_usuario, id_musica, nota, texto) VALUES "
            f"({usuario_id}, {musica_id}, {nota}, {esc(texto)});"
        )


def adicionar_reproducoes(linhas, usuarios, musicas_ids, musicas, ids_musicas):
    linhas.append("\n-- reproducao")
    duracoes = {
        ids_musicas[musica["id"]]: musica["duracao_segundos"]
        for musica in musicas
    }

    for _ in range(QTD_REPRODUCOES):
        usuario_id = gerador_seguro.choice(usuarios)["id"]
        musica_id = gerador_seguro.choice(musicas_ids)
        duracao = duracoes.get(musica_id, 180)
        segundos_ouvidos = gerador_seguro.randint(5, duracao)
        linhas.append(
            "INSERT INTO reproducao "
            "(id_usuario, id_musica, segundos_ouvidos) VALUES "
            f"({usuario_id}, {musica_id}, {segundos_ouvidos});"
        )


def adicionar_curtidas(linhas, usuarios, musicas_ids):
    linhas.append("\n-- curtida_musica")
    pares = set()

    while len(pares) < QTD_CURTIDAS:
        usuario_id = gerador_seguro.choice(usuarios)["id"]
        musica_id = gerador_seguro.choice(musicas_ids)
        pares.add((usuario_id, musica_id))

    for usuario_id, musica_id in sorted(pares):
        linhas.append(
            "INSERT INTO curtida_musica (id_usuario, id_musica) VALUES "
            f"({usuario_id}, {musica_id});"
        )


def adicionar_seguidores_usuarios(linhas, usuarios):
    linhas.append("\n-- usuario_segue_usuario")
    pares = set()

    while len(pares) < QTD_SEGUE_USUARIO:
        seguidor = gerador_seguro.choice(usuarios)["id"]
        seguido = gerador_seguro.choice(usuarios)["id"]
        if seguidor != seguido:
            pares.add((seguidor, seguido))

    for seguidor, seguido in sorted(pares):
        linhas.append(
            "INSERT INTO usuario_segue_usuario "
            "(id_seguidor, id_seguido) VALUES "
            f"({seguidor}, {seguido});"
        )


def adicionar_seguidores_artistas(linhas, usuarios, ids_artistas):
    linhas.append("\n-- usuario_segue_artista")
    pares = set()
    artistas = list(ids_artistas.values())

    while len(pares) < QTD_SEGUE_ARTISTA:
        usuario_id = gerador_seguro.choice(usuarios)["id"]
        artista_id = gerador_seguro.choice(artistas)
        pares.add((usuario_id, artista_id))

    for usuario_id, artista_id in sorted(pares):
        linhas.append(
            "INSERT INTO usuario_segue_artista "
            "(id_usuario, id_artista) VALUES "
            f"({usuario_id}, {artista_id});"
        )


def adicionar_ajustes_sequences(linhas):
    linhas.append("\n-- ajustar sequences das colunas GENERATED AS IDENTITY")
    tabelas = [
        ("artista", "id_artista"),
        ("genero", "id_genero"),
        ("album", "id_album"),
        ("musica", "id_musica"),
        ("usuario", "id"),
        ("perfil", "id_perfil"),
        ("playlist", "id_playlist"),
        ("review", "id_review"),
        ("reproducao", "id_reproducao"),
    ]

    for tabela, coluna in tabelas:
        linhas.append(
            f"SELECT setval(pg_get_serial_sequence('{tabela}', '{coluna}'), "
            f"(SELECT MAX({coluna}) FROM {tabela}));"
        )


def salvar_sql(linhas):
    with open(CAMINHO_SQL, "w", encoding="utf-8") as arquivo:
        arquivo.write("\n".join(linhas) + "\n")


def imprimir_resumo(dados, ids_generos, linhas):
    total_musicas = sum(
        1
        for linha in linhas
        if linha.startswith("INSERT INTO musica (")
    )
    print(f"Artistas: {len(dados['artistas'])}")
    print(f"Albuns: {len(dados['albuns'])}")
    print(f"Generos: {len(ids_generos)}")
    print(f"Musicas inseridas: {total_musicas}")
    print(f"Usuarios: {QTD_USUARIOS}")
    print(f"Playlists: {QTD_PLAYLISTS}")
    print(f"Reviews: {QTD_REVIEWS}")
    print(f"Reproducoes: {QTD_REPRODUCOES}")
    print(f"Curtidas: {QTD_CURTIDAS}")
    print(f"Senha padrao de todos os usuarios ficticios: {SENHA_PADRAO}")
    print(f"Salvo em {CAMINHO_SQL}")


def main():
    dados = carregar_dados()
    artistas = dados["artistas"]
    albuns = dados["albuns"]
    musicas = dados["musicas"]
    ids = criar_ids(dados)
    ids["genero"] = criar_ids_generos(artistas)

    linhas = iniciar_linhas()
    adicionar_artistas(linhas, artistas, ids["artista"])
    adicionar_generos(linhas, ids["genero"])
    adicionar_albuns(linhas, albuns, ids)

    musicas_inseridas = adicionar_musicas(
        linhas,
        musicas,
        artistas,
        ids,
    )

    usuarios = adicionar_usuarios(linhas)
    adicionar_perfis(linhas, usuarios, ids, musicas_inseridas)

    playlists = adicionar_playlists(linhas, usuarios)
    adicionar_faixas_playlists(linhas, playlists, musicas_inseridas)

    adicionar_reviews(linhas, usuarios, musicas_inseridas)
    adicionar_reproducoes(
        linhas,
        usuarios,
        musicas_inseridas,
        musicas,
        ids["musica"],
    )
    adicionar_curtidas(linhas, usuarios, musicas_inseridas)

    adicionar_seguidores_usuarios(linhas, usuarios)
    adicionar_seguidores_artistas(linhas, usuarios, ids["artista"])

    adicionar_ajustes_sequences(linhas)
    salvar_sql(linhas)
    imprimir_resumo(dados, ids["genero"], linhas)


if __name__ == "__main__":
    main()
