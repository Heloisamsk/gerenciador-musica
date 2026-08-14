import os

from dotenv import load_dotenv
from spotipy import Spotify
from spotipy.oauth2 import SpotifyClientCredentials

load_dotenv()

client_id = os.getenv("SPOTIFY_CLIENT_ID")
client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")

if not client_id or not client_secret:
    raise SystemExit(
        "SPOTIFY_CLIENT_ID / SPOTIFY_CLIENT_SECRET nao encontrados. "
        "Copie .env.example para .env e preencha os valores."
    )

auth_manager = SpotifyClientCredentials(
    client_id=client_id,
    client_secret=client_secret,
)
sp = Spotify(auth_manager=auth_manager)

resultado = sp.search(q="Legiao Urbana", type="artist", limit=1)
artista = resultado["artists"]["items"][0]

print("Autenticado com sucesso!")
print(f"Artista de teste: {artista['name']} (id: {artista['id']})")
print(f"Generos: {artista.get('genres')}")
