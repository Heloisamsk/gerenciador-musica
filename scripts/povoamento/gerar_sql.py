import json
import os
import random
import re
import unicodedata
from datetime import timedelta

import bcrypt
from faker import Faker

fake = Faker("pt_BR")
random.seed(42)
Faker.seed(42)

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
GENEROS_POR_ARTISTA = {
    "Ariana Grande": ["Pop"],
    "Frank Ocean": ["R&B"],
    "Madonna": ["Pop"],
    "Olivia Rodrigo": ["Pop Rock"],
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
    "Harry Styles": ["Pop Rock"],
    "One Direction": ["Pop Rock"],
    "Billie Eilish": ["Pop Alternativo"],
    "ROSALÍA": ["Flamenco Pop"],
    "Sabrina Carpenter": ["Pop"],
    "Olivia Dean": ["Soul"],
    "Lorde": ["Pop Alternativo"],
    "Mariah Carey": ["R&B"],
    "Doja Cat": ["Hip Hop"],
    "Miley Cyrus": ["Pop Rock"],
    "Selena Gomez": ["Pop"],
    "Camila Cabello": ["Pop"],
    "Demi Lovato": ["Pop Rock"],
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

ROLES = ["USER"] * 9 + ["ADMIN"]


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
    dias = random.randint(0, dias_atras_max)
    segundos = random.randint(0, 86399)
    return fake.date_time_between(start_date=f"-{dias_atras_max}d", end_date="now")


def main():
    with open(CAMINHO_JSON, "r", encoding="utf-8") as f:
        dados = json.load(f)

    artistas_raw = dados["artistas"]
    albuns_raw = dados["albuns"]
    musicas_raw = dados["musicas"]

    id_artista = {a["id"]: i + 1 for i, a in enumerate(artistas_raw)}
    id_album = {a["id"]: i + 1 for i, a in enumerate(albuns_raw)}
    id_musica = {m["id"]: i + 1 for i, m in enumerate(musicas_raw)}

    nomes_generos = []
    for a in artistas_raw:
        for g in GENEROS_POR_ARTISTA.get(a["nome"], [GENERO_PADRAO]):
            if g not in nomes_generos:
                nomes_generos.append(g)
    id_genero = {nome: i + 1 for i, nome in enumerate(nomes_generos)}

    linhas = []
    linhas.append("-- Gerado automaticamente por scripts/povoamento/gerar_sql.py")
    linhas.append("-- Catalogo real via API do Spotify + dados ficticios via Faker.\n")

    # ---------------- artista ----------------
    linhas.append("-- artista")
    for a in artistas_raw:
        linhas.append(
            "INSERT INTO artista (id_artista, nome, foto_perfil_url) VALUES "
            f"({id_artista[a['id']]}, {esc(a['nome'])}, {esc(a['foto_url'])});"
        )

    # ---------------- genero ----------------
    linhas.append("\n-- genero")
    for nome, gid in id_genero.items():
        linhas.append(f"INSERT INTO genero (id_genero, nome) VALUES ({gid}, {esc(nome)});")

    # ---------------- album ----------------
    linhas.append("\n-- album")
    for al in albuns_raw:
        linhas.append(
            "INSERT INTO album (id_album, id_artista, titulo, ano_lancamento, capa_url) VALUES "
            f"({id_album[al['id']]}, {id_artista[al['id_artista']]}, {esc(al['titulo'])}, "
            f"{al['ano_lancamento']}, {esc(al['capa_url'])});"
        )

    # ---------------- musica ----------------
    linhas.append("\n-- musica")
    musica_artistas_pares = set()
    musica_genero_pares = set()
    musicas_inseridas_ids = []

    for m in musicas_raw:
        mid = id_musica[m["id"]]
        artista_principal_spotify = m["artistas"][0]["id"] if m["artistas"] else None
        artista_principal = id_artista.get(artista_principal_spotify)

        if artista_principal is None:
            # faixa cujo artista principal nao esta na nossa lista coletada:
            # nao insere, e por isso nao pode ser referenciada em nenhuma FK
            continue

        musicas_inseridas_ids.append(mid)

        linhas.append(
            "INSERT INTO musica "
            "(id_musica, id_album, id_artista, titulo, duracao_segundos, ano_lancamento) VALUES "
            f"({mid}, {id_album[m['id_album']]}, {artista_principal}, {esc(m['titulo'])}, "
            f"{m['duracao_segundos']}, {m['ano_lancamento']});"
        )

        for art in m["artistas"]:
            aid = id_artista.get(art["id"])
            if aid is not None:
                musica_artistas_pares.add((mid, aid))

        nome_artista_principal = next(
            (a["nome"] for a in artistas_raw if a["id"] == artista_principal_spotify), None
        )
        for g in GENEROS_POR_ARTISTA.get(nome_artista_principal, [GENERO_PADRAO]):
            musica_genero_pares.add((mid, id_genero[g]))

    linhas.append("\n-- musica_artista")
    for mid, aid in sorted(musica_artistas_pares):
        linhas.append(
            f"INSERT INTO musica_artista (id_musica, id_artista) VALUES ({mid}, {aid});"
        )

    linhas.append("\n-- musica_genero")
    for mid, gid in sorted(musica_genero_pares):
        linhas.append(
            f"INSERT INTO musica_genero (id_musica, id_genero) VALUES ({mid}, {gid});"
        )

    # ---------------- usuario ----------------
    linhas.append("\n-- usuario")
    usernames_usados = set()
    usuarios = []
    for i in range(1, QTD_USUARIOS + 1):
        nome = fake.name()
        email = fake.unique.email()
        username = gerar_username(nome, usernames_usados)
        role = random.choice(ROLES)
        data_cadastro = data_aleatoria(730)
        usuarios.append({"id": i, "nome": nome})
        linhas.append(
            "INSERT INTO usuario (id, nome, email, senha, role, username, data_cadastro) VALUES "
            f"({i}, {esc(nome)}, {esc(email)}, {esc(HASH_SENHA_PADRAO)}, {esc(role)}, "
            f"{esc(username)}, {esc(data_cadastro.isoformat())});"
        )

    # ---------------- perfil ----------------
    linhas.append("\n-- perfil")
    usuarios_com_perfil = random.sample(usuarios, QTD_PERFIS)
    for i, u in enumerate(usuarios_com_perfil, start=1):
        artista_destaque = random.choice(list(id_artista.values())) if random.random() < 0.6 else None
        album_destaque = random.choice(list(id_album.values())) if random.random() < 0.4 else None
        musica_destaque = random.choice(musicas_inseridas_ids) if random.random() < 0.4 else None
        linhas.append(
            "INSERT INTO perfil "
            "(id_perfil, id_usuario, biografia, frase_destaque, "
            "id_artista_destaque, id_album_destaque, id_musica_destaque) VALUES "
            f"({i}, {u['id']}, {esc(fake.sentence(nb_words=12))}, {esc(fake.catch_phrase())}, "
            f"{num(artista_destaque)}, {num(album_destaque)}, {num(musica_destaque)});"
        )

    # ---------------- playlist ----------------
    linhas.append("\n-- playlist")
    todas_musicas_ids = musicas_inseridas_ids
    playlists = []
    for i in range(1, QTD_PLAYLISTS + 1):
        dono = random.choice(usuarios)
        nome_playlist = f"{fake.word().capitalize()} {fake.word()}"
        data_criacao = data_aleatoria(600)
        playlists.append({"id": i, "usuario": dono["id"]})
        linhas.append(
            "INSERT INTO playlist (id_playlist, nome, descricao, id_usuario, data_criacao) VALUES "
            f"({i}, {esc(nome_playlist)}, {esc(fake.sentence(nb_words=10))}, "
            f"{dono['id']}, {esc(data_criacao.isoformat())});"
        )

    # ---------------- playlist_musica ----------------
    linhas.append("\n-- playlist_musica")
    for pl in playlists:
        qtd = random.randint(4, 18)
        musicas_da_playlist = random.sample(todas_musicas_ids, min(qtd, len(todas_musicas_ids)))
        for ordem, mid in enumerate(musicas_da_playlist, start=1):
            linhas.append(
                "INSERT INTO playlist_musica (id_playlist, id_musica, ordem) VALUES "
                f"({pl['id']}, {mid}, {ordem});"
            )

    # ---------------- review ----------------
    linhas.append("\n-- review")
    pares_review = set()
    while len(pares_review) < QTD_REVIEWS:
        uid = random.choice(usuarios)["id"]
        mid = random.choice(todas_musicas_ids)
        pares_review.add((uid, mid))
    for uid, mid in sorted(pares_review):
        nota = random.randint(1, 5)
        texto = fake.sentence(nb_words=15) if random.random() < 0.7 else None
        linhas.append(
            "INSERT INTO review (id_usuario, id_musica, nota, texto) VALUES "
            f"({uid}, {mid}, {nota}, {esc(texto)});"
        )

    # ---------------- reproducao ----------------
    linhas.append("\n-- reproducao")
    duracao_por_musica = {id_musica[m["id"]]: m["duracao_segundos"] for m in musicas_raw}
    for _ in range(QTD_REPRODUCOES):
        uid = random.choice(usuarios)["id"]
        mid = random.choice(todas_musicas_ids)
        duracao = duracao_por_musica.get(mid, 180)
        segundos_ouvidos = random.randint(5, duracao)
        linhas.append(
            "INSERT INTO reproducao (id_usuario, id_musica, segundos_ouvidos) VALUES "
            f"({uid}, {mid}, {segundos_ouvidos});"
        )

    # ---------------- curtida_musica ----------------
    linhas.append("\n-- curtida_musica")
    pares_curtida = set()
    while len(pares_curtida) < QTD_CURTIDAS:
        uid = random.choice(usuarios)["id"]
        mid = random.choice(todas_musicas_ids)
        pares_curtida.add((uid, mid))
    for uid, mid in sorted(pares_curtida):
        linhas.append(
            f"INSERT INTO curtida_musica (id_usuario, id_musica) VALUES ({uid}, {mid});"
        )

    # ---------------- usuario_segue_usuario ----------------
    linhas.append("\n-- usuario_segue_usuario")
    pares_segue_usuario = set()
    while len(pares_segue_usuario) < QTD_SEGUE_USUARIO:
        seguidor = random.choice(usuarios)["id"]
        seguido = random.choice(usuarios)["id"]
        if seguidor != seguido:
            pares_segue_usuario.add((seguidor, seguido))
    for seguidor, seguido in sorted(pares_segue_usuario):
        linhas.append(
            "INSERT INTO usuario_segue_usuario (id_seguidor, id_seguido) VALUES "
            f"({seguidor}, {seguido});"
        )

    # ---------------- usuario_segue_artista ----------------
    linhas.append("\n-- usuario_segue_artista")
    pares_segue_artista = set()
    todos_artistas_ids = list(id_artista.values())
    while len(pares_segue_artista) < QTD_SEGUE_ARTISTA:
        uid = random.choice(usuarios)["id"]
        aid = random.choice(todos_artistas_ids)
        pares_segue_artista.add((uid, aid))
    for uid, aid in sorted(pares_segue_artista):
        linhas.append(
            "INSERT INTO usuario_segue_artista (id_usuario, id_artista) VALUES "
            f"({uid}, {aid});"
        )

    # Como inserimos IDs manualmente nas tabelas com coluna GENERATED AS
    # IDENTITY, a sequence interna do Postgres nao fica sabendo disso e
    # continuaria tentando gerar 1, 2, 3... na proxima insercao normal do
    # app, colidindo com o que ja povoamos. Avancamos a sequence pro maior
    # id inserido em cada uma dessas tabelas.
    linhas.append("\n-- ajustar sequences das colunas GENERATED AS IDENTITY")
    tabelas_com_identity = [
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
    for tabela, coluna in tabelas_com_identity:
        linhas.append(
            f"SELECT setval(pg_get_serial_sequence('{tabela}', '{coluna}'), "
            f"(SELECT MAX({coluna}) FROM {tabela}));"
        )

    with open(CAMINHO_SQL, "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")

    print(f"Artistas: {len(artistas_raw)}")
    print(f"Albuns: {len(albuns_raw)}")
    print(f"Generos: {len(id_genero)}")
    print(f"Musicas inseridas: {sum(1 for l in linhas if l.startswith('INSERT INTO musica ('))}")
    print(f"Usuarios: {QTD_USUARIOS}")
    print(f"Playlists: {QTD_PLAYLISTS}")
    print(f"Reviews: {QTD_REVIEWS}")
    print(f"Reproducoes: {QTD_REPRODUCOES}")
    print(f"Curtidas: {QTD_CURTIDAS}")
    print(f"Senha padrao de todos os usuarios ficticios: {SENHA_PADRAO}")
    print(f"Salvo em {CAMINHO_SQL}")


if __name__ == "__main__":
    main()