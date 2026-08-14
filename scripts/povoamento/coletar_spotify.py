import json
import os
import time

from dotenv import load_dotenv
from spotipy import Spotify
from spotipy.oauth2 import SpotifyClientCredentials

load_dotenv()

sp = Spotify(
    auth_manager=SpotifyClientCredentials(
        client_id=os.getenv("SPOTIFY_CLIENT_ID"),
        client_secret=os.getenv("SPOTIFY_CLIENT_SECRET"),
    ),
    # sem isso, uma chamada que trava fica esperando resposta pra sempre
    requests_timeout=10,
    retries=3,
)

# album_limit / track_limit = None significa "pegar todos os que existirem".
ARTISTAS_ALVO = [
    {"nome": "Ariana Grande", "album_limit": None, "track_limit": None},
    {"nome": "Frank Ocean", "album_limit": None, "track_limit": None},
    {"nome": "Madonna", "album_limit": None, "track_limit": None},
    {"nome": "Olivia Rodrigo", "album_limit": 3, "track_limit": None},
    {"nome": "Marina Sena", "album_limit": 3, "track_limit": None},
    {"nome": "Blackpink", "album_limit": 3, "track_limit": 8},
    {"nome": "Jennie", "album_limit": 3, "track_limit": 8},
    {"nome": "The Weeknd", "album_limit": 3, "track_limit": 8},
    {"nome": "Gal Costa", "album_limit": 3, "track_limit": 8},
    {"nome": "Bad Bunny", "album_limit": 3, "track_limit": 8},
    {"nome": "Anitta", "album_limit": 3, "track_limit": 8},
    {"nome": "Addison Rae", "album_limit": 3, "track_limit": None},
    {"nome": "Dua Lipa", "album_limit": 3, "track_limit": 8},
    {"nome": "Lana Del Rey", "album_limit": None, "track_limit": None},
    {"nome": "Charli XCX", "album_limit": 3, "track_limit": 8},
    {"nome": "Tasha e Tracie", "album_limit": 3, "track_limit": 8},
    {"nome": "FKA Twigs", "album_limit": 3, "track_limit": 8},
    {"nome": "SZA", "album_limit": 3, "track_limit": 8},
    {"nome": "Adela", "album_limit": 3, "track_limit": 8},
    {"nome": "Harry Styles", "album_limit": 3, "track_limit": 8},
    {"nome": "One Direction", "album_limit": 3, "track_limit": 8},
    {"nome": "AJULLIACOSTA", "album_limit": 3, "track_limit": 8},
    {"nome": "Billie Eilish", "album_limit": 3, "track_limit": 8},
    {"nome": "Rosalia", "album_limit": 3, "track_limit": 8},
    {"nome": "Sabrina Carpenter", "album_limit": 3, "track_limit": 8},
    {"nome": "Olivia Dean", "album_limit": 3, "track_limit": 8},
    {"nome": "Lorde", "album_limit": 3, "track_limit": 8},
    {"nome": "Mariah Carey", "album_limit": 3, "track_limit": 8},
    {"nome": "Doja Cat", "album_limit": 3, "track_limit": 8},
    {"nome": "Miley Cyrus", "album_limit": 3, "track_limit": 8},
    {"nome": "Selena Gomez", "album_limit": 3, "track_limit": 8},
    {"nome": "Camila Cabello", "album_limit": 3, "track_limit": 8},
    {"nome": "Demi Lovato", "album_limit": 3, "track_limit": 8},
    {"nome": "Katy Perry", "album_limit": 3, "track_limit": 8},
    {"nome": "Solange", "album_limit": 3, "track_limit": 8},
    {"nome": "NandaTsunami", "album_limit": 3, "track_limit": None},
    {"nome": "Sade", "album_limit": 3, "track_limit": 8},
    {"nome": "Slayyyter", "album_limit": 3, "track_limit": 8},
    {"nome": "Rihanna", "album_limit": 3, "track_limit": 8},
    {"nome": "Duda Beat", "album_limit": 3, "track_limit": 8},
    {"nome": "Marilia Mendonca", "album_limit": 3, "track_limit": 8},
    {"nome": "Kendrick Lamar", "album_limit": 3, "track_limit": 8},
    {"nome": "Radiohead", "album_limit": 3, "track_limit": 8},
    {"nome": "Beyonce", "album_limit": 3, "track_limit": 8},
    {"nome": "Arctic Monkeys", "album_limit": 3, "track_limit": 8},
    {"nome": "Tyla", "album_limit": 3, "track_limit": 8},
    {"nome": "Tyler, The Creator", "album_limit": 3, "track_limit": 8},
    {"nome": "Zara Larsson", "album_limit": 3, "track_limit": 8},
    {"nome": "Urias", "album_limit": 3, "track_limit": 8},
    {"nome": "Gaby Amarantos", "album_limit": 3, "track_limit": 8},
    {"nome": "João Gomes", "album_limit": 3, "track_limit": 8},
]


def buscar_artista(nome):
    resultado = sp.search(q=nome, type="artist", limit=5)
    itens = resultado["artists"]["items"]
    if not itens:
        return None

    # A busca do Spotify nem sempre traz o nome exato em 1o lugar (o campo
    # de popularidade vem bloqueado pra esse tipo de app), entao procuramos
    # primeiro por uma correspondencia exata de nome entre os resultados.
    for item in itens:
        if item["name"].strip().lower() == nome.strip().lower():
            return item

    return itens[0]


# O endpoint direto /artists/{id}/albums ficou com cota estourada
# (429 QUOTA_EXCEEDED) de tanto testar durante o desenvolvimento.
# Como alternativa, buscamos os albuns via /search?type=album, que
# ainda funciona. Esse endpoint tambem so aceita ate 10 itens por
# pagina nesse tipo de app, mesmo o maximo documentado sendo 50.
ALBUM_PAGE_SIZE = 10
TRACK_PAGE_SIZE = 50


def paginar_albuns(artist_id, nome_artista, limit=None):
    albuns = []
    offset = 0
    while True:
        pagina = sp.search(
            q=f'artist:"{nome_artista}"',
            type="album",
            limit=ALBUM_PAGE_SIZE,
            offset=offset,
        )["albums"]

        candidatos = [
            item
            for item in pagina["items"]
            if item["album_type"] == "album"
            and any(a["id"] == artist_id for a in item["artists"])
        ]
        albuns.extend(candidatos)
        offset += ALBUM_PAGE_SIZE

        if limit is not None and len(albuns) >= limit:
            return albuns[:limit]
        if pagina["next"] is None:
            return albuns


def paginar_faixas(album_id, limit=None):
    faixas = []
    offset = 0
    while True:
        pagina = sp.album_tracks(album_id, limit=TRACK_PAGE_SIZE, offset=offset)
        faixas.extend(pagina["items"])
        offset += TRACK_PAGE_SIZE

        if limit is not None and len(faixas) >= limit:
            return faixas[:limit]
        if pagina["next"] is None:
            return faixas


def coletar():
    artistas = {}
    albuns = {}
    musicas = {}

    for config in ARTISTAS_ALVO:
        nome = config["nome"]
        print(f"buscando: {nome}...", flush=True)
        artista = buscar_artista(nome)
        if not artista:
            print(f"[aviso] artista nao encontrado: {nome}", flush=True)
            continue

        artistas[artista["id"]] = {
            "id": artista["id"],
            "nome": artista["name"],
            "generos": artista.get("genres") or [],
            "foto_url": (artista["images"][0]["url"] if artista["images"] else None),
        }

        albuns_do_artista = paginar_albuns(
            artista["id"], artista["name"], config["album_limit"]
        )
        print(f"  {len(albuns_do_artista)} albuns encontrados", flush=True)
        total_faixas_artista = 0

        vistos_nesse_artista = set()
        for album in albuns_do_artista:
            if album["id"] in vistos_nesse_artista:
                continue
            vistos_nesse_artista.add(album["id"])

            if album["id"] in albuns:
                continue

            albuns[album["id"]] = {
                "id": album["id"],
                "titulo": album["name"],
                "ano_lancamento": int(album["release_date"][:4]),
                "capa_url": (album["images"][0]["url"] if album["images"] else None),
                "id_artista": artista["id"],
            }

            faixas = paginar_faixas(album["id"], config["track_limit"])
            for faixa in faixas:
                if faixa["id"] in musicas:
                    continue
                musicas[faixa["id"]] = {
                    "id": faixa["id"],
                    "titulo": faixa["name"],
                    "duracao_segundos": faixa["duration_ms"] // 1000,
                    "id_album": album["id"],
                    "ano_lancamento": int(album["release_date"][:4]),
                    "artistas": [
                        {"id": a["id"], "nome": a["name"]} for a in faixa["artists"]
                    ],
                }
                total_faixas_artista += 1

            time.sleep(0.1)

        print(
            f"artista ok: {artista['name']} "
            f"({len(albuns_do_artista)} albuns, {total_faixas_artista} faixas)",
            flush=True,
        )

    return artistas, albuns, musicas


if __name__ == "__main__":
    artistas, albuns, musicas = coletar()

    saida = {
        "artistas": list(artistas.values()),
        "albuns": list(albuns.values()),
        "musicas": list(musicas.values()),
    }

    caminho = os.path.join(os.path.dirname(__file__), "dados_coletados.json")
    with open(caminho, "w", encoding="utf-8") as f:
        json.dump(saida, f, ensure_ascii=False, indent=2)

    print()
    print(f"Artistas: {len(saida['artistas'])}")
    print(f"Albuns: {len(saida['albuns'])}")
    print(f"Musicas: {len(saida['musicas'])}")
    print(f"Salvo em {caminho}")
