# 🎧 — Sistema Gerenciador de Músicas




## 👥 » Integrantes

#### [Álvaro Henrique Nunes de Andrade](https://github.com/alwvaro) | [Arthur Oliveira Ramos](https://github.com/thuramos) | [Maria Heloisa da Silva Montebelo](https://github.com/Heloisamsk) | [Vinicius Freire Pereira](https://github.com/VinFpe)

## 📍 » Sobre o Projeto

Projeto de Sistema web para gerenciamento de músicas, artistas, álbuns e playlists, desenvolvido para as disciplinas de Engenharia de Software, ministrada pela professora [Thais Burity](https://github.com/taburity), e Banco de Dados, ministrada pela professora Priscila Kelly, da UFAPE, referente ao período de 2026.1, com intuito de avaliação para a 2ª Verificação de Aprendizagem.



## 🤖 » Tecnologias

### [Angular](https://angular.dev/)
- Desenvolvimento do frontend.

### [Spring Boot](https://spring.io/projects/spring-boot)
- Desenvolvimento da API backend.

### [PostgreSQL](https://www.postgresql.org/)
- Banco de dados relacional.

### [Docker](https://www.docker.com/)
- Execução e configuração do banco de dados.

### [JWT](https://jwt.io/)
- Autenticação e autorização dos usuários.


## 🎶 » Status do Projeto

- Em andamento



## 🎏 » Diagrama UML
<img width="3332" height="1792" alt="Diagrama_Spotify_BD drawio" src="https://github.com/user-attachments/assets/5d18dd92-99a7-4091-8edc-ffeb6d852892" />



## 🗄️ » Configuração do Banco de Dados

O projeto usa **PostgreSQL 18** como SGBD, executado via Docker (serviço `postgres` do `docker-compose.yml`).

| Configuração | Valor |
|---|---|
| Host (fora do container) | `localhost` |
| Porta | `5432` |
| Nome do banco | `gerenciador_musica` |
| Usuário | `gerenciador` |
| Senha | `gerenciador` |

A estrutura das tabelas é criada automaticamente pelo [Flyway](https://flywaydb.org/) a partir dos scripts em [`backend/src/main/resources/db/migration`](backend/src/main/resources/db/migration), toda vez que o backend sobe. O script DDL consolidado (só criação de tabelas e índices, sem a lógica de migração incremental) também está disponível em [`schema.sql`](schema.sql).


## 🌱 » Metodologia de Povoamento

O banco é povoado automaticamente pela migration [`V11__popular_dados_iniciais.sql`](backend/src/main/resources/db/migration/V11__popular_dados_iniciais.sql), que roda junto com as demais assim que o container do backend sobe — não é preciso nenhum passo manual.

Os dados vêm de duas fontes:
- **Catálogo musical real** (artistas, álbuns, músicas e a relação entre eles): coletado via [API Web do Spotify](https://developer.spotify.com/documentation/web-api) (fluxo *Client Credentials*), usando um script Python ([`scripts/povoamento/coletar_spotify.py`](scripts/povoamento/coletar_spotify.py)).
- **Dados fictícios das entidades do próprio app** (usuários, playlists, reviews, curtidas, reproduções, relações de seguidores): gerados com a biblioteca [Faker](https://faker.readthedocs.io/), respeitando as chaves estrangeiras do catálogo real coletado.

Um segundo script ([`scripts/povoamento/gerar_sql.py`](scripts/povoamento/gerar_sql.py)) junta as duas fontes e gera o arquivo `V11__popular_dados_iniciais.sql`. Esse processo roda **uma única vez, offline**, para gerar o SQL — quem clona o repositório não precisa de credenciais do Spotify nem de conexão com a API para ter o banco populado, só precisa subir o Docker.

Resultado final: 51 artistas, 158 álbuns, 1.426 músicas, 60 usuários, 60 playlists e todas as demais tabelas bem acima do mínimo de 50 tuplas (principais) / 15 tuplas (secundárias) exigido.

Todos os usuários fictícios usam a mesma senha para testes: **`Senha@123`**.


## 🔖 » Dicionário de Dados

> Versão completa também disponível em [Dicionário de Dados.pdf](https://github.com/user-attachments/files/30991513/Dicionario.de.Dados.pdf).

### ARTISTA
Armazena os artistas cadastrados no sistema.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_artista | BIGINT | PK | Identificador único do artista. |
| nome | VARCHAR(255) | NOT NULL | Nome do artista. |
| descricao | TEXT | — | Descrição ou informações adicionais sobre o artista. |
| foto_perfil_url | VARCHAR(2048) | — | URL da foto de perfil do artista. |

### USUARIO
Armazena os usuários cadastrados, seus dados de autenticação e nível de acesso.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id | BIGINT | PK | Identificador único do usuário. |
| nome | VARCHAR(255) | NOT NULL | Nome do usuário. |
| email | VARCHAR(255) | NOT NULL, UNIQUE | E-mail do usuário, único no sistema. |
| senha | VARCHAR(255) | NOT NULL | Hash (BCrypt) da senha, usado na autenticação. |
| role | VARCHAR(255) | NOT NULL, CHECK IN ('ADMIN','USER') | Nível de acesso: `USER` (padrão) ou `ADMIN`. |
| username | VARCHAR(30) | UNIQUE (case-insensitive), permite NULL, formato `^[a-z0-9._]{3,30}$` | Nome de usuário opcional, usado para identificação pública. |
| data_cadastro | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data e horário de cadastro do usuário. |

### MUSICA
Armazena as músicas cadastradas, com artista principal, álbum, duração e ano de lançamento.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_musica | BIGINT | PK | Identificador único da música. |
| id_artista | BIGINT | NOT NULL, FK → artista | Artista principal responsável pela música. |
| id_album | BIGINT | FK → album, permite NULL | Álbum ao qual a música pertence (pode não ter álbum). |
| titulo | VARCHAR(255) | NOT NULL | Título da música. |
| letra | TEXT | — | Letra da música. |
| duracao_segundos | INTEGER | NOT NULL, CHECK (> 0) | Duração da música em segundos. |
| ano_lancamento | SMALLINT | NOT NULL, CHECK (entre 1800 e 2100) | Ano de lançamento da música. |

### ALBUM
Armazena os álbuns cadastrados, com artista responsável, título e ano de lançamento.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_album | BIGINT | PK | Identificador único do álbum. |
| id_artista | BIGINT | NOT NULL, FK → artista | Artista responsável pelo álbum. |
| titulo | VARCHAR(255) | NOT NULL | Título do álbum. |
| ano_lancamento | SMALLINT | NOT NULL, CHECK (entre 1800 e 2100) | Ano de lançamento do álbum. |
| capa_url | VARCHAR(2048) | — | URL da capa do álbum. |

### GENERO
Armazena os gêneros musicais cadastrados no sistema.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_genero | BIGINT | PK | Identificador único do gênero. |
| nome | VARCHAR(100) | NOT NULL, UNIQUE | Nome do gênero musical, único no sistema. |

### PERFIL
Armazena a personalização do perfil de cada usuário (foto, banner, biografia e destaques opcionais).

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_perfil | BIGINT | PK | Identificador único do perfil. |
| id_usuario | BIGINT | NOT NULL, FK → usuario, UNIQUE | Usuário dono do perfil (no máximo 1 perfil por usuário). |
| foto_url | VARCHAR(2048) | — | URL da foto do perfil. |
| banner_url | VARCHAR(2048) | — | URL do banner do perfil. |
| biografia | TEXT | — | Texto de apresentação do usuário. |
| frase_destaque | VARCHAR(500) | — | Frase escolhida para destaque no perfil. |
| id_artista_destaque | BIGINT | FK → artista, permite NULL (ON DELETE SET NULL) | Artista em destaque no perfil, se houver. |
| id_album_destaque | BIGINT | FK → album, permite NULL (ON DELETE SET NULL) | Álbum em destaque no perfil, se houver. |
| id_musica_destaque | BIGINT | FK → musica, permite NULL (ON DELETE SET NULL) | Música em destaque no perfil, se houver. |

### REVIEW
Armazena as avaliações dos usuários sobre músicas.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_review | BIGINT | PK | Identificador único da avaliação. |
| id_usuario | BIGINT | NOT NULL, FK → usuario, UNIQUE junto com id_musica | Usuário que fez a avaliação (só pode avaliar cada música uma vez). |
| id_musica | BIGINT | NOT NULL, FK → musica, UNIQUE junto com id_usuario | Música avaliada. |
| nota | SMALLINT | NOT NULL, CHECK (entre 1 e 5) | Nota atribuída à música. |
| texto | TEXT | CHECK (nulo ou não-vazio) | Comentário opcional sobre a música. |
| criada_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data de criação da avaliação. |
| atualizada_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data da última atualização (atualizada automaticamente por trigger a cada edição). |

### PLAYLIST
Armazena as playlists criadas pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_playlist | BIGINT | PK | Identificador único da playlist. |
| id_usuario | BIGINT | NOT NULL, FK → usuario | Usuário dono da playlist. |
| nome | VARCHAR(255) | NOT NULL | Nome da playlist. |
| descricao | TEXT | — | Descrição opcional da playlist. |
| data_criacao | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data de criação da playlist. |

### REPRODUCAO
Armazena o histórico de reproduções de músicas pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_reproducao | BIGINT | PK | Identificador único da reprodução. |
| id_usuario | BIGINT | NOT NULL, FK → usuario | Usuário que reproduziu a música. |
| id_musica | BIGINT | NOT NULL, FK → musica | Música reproduzida. |
| reproduzida_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data e horário da reprodução. |
| segundos_ouvidos | INTEGER | NOT NULL, CHECK (>= 0) | Quantos segundos da música foram ouvidos. |

### MUSICA_ARTISTA (associativa)
Relaciona músicas aos artistas participantes (parcerias/feats).

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_musica | BIGINT | PK, FK → musica | Música associada. |
| id_artista | BIGINT | PK, FK → artista | Artista participante da música. |

### MUSICA_GENERO (associativa)
Relaciona músicas aos seus gêneros musicais.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_musica | BIGINT | PK, FK → musica | Música associada. |
| id_genero | BIGINT | PK, FK → genero | Gênero associado à música. |

### PLAYLIST_MUSICA (associativa)
Relaciona músicas às playlists, com ordem de exibição.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_playlist | BIGINT | PK, FK → playlist | Playlist associada. |
| id_musica | BIGINT | PK, FK → musica | Música associada. |
| ordem | INTEGER | NOT NULL, CHECK (> 0), UNIQUE junto com id_playlist | Posição da música dentro da playlist. |
| data_criacao | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a música foi adicionada à playlist. |

### CURTIDA_MUSICA (associativa)
Registra as curtidas dos usuários em músicas.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_usuario | BIGINT | PK, FK → usuario | Usuário que curtiu. |
| id_musica | BIGINT | PK, FK → musica | Música curtida. |
| curtida_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data da curtida. |

### USUARIO_SEGUE_USUARIO (associativa)
Registra relações de seguidores entre usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_seguidor | BIGINT | PK, FK → usuario, CHECK (id_seguidor ≠ id_seguido) | Usuário que segue. |
| id_seguido | BIGINT | PK, FK → usuario | Usuário que é seguido. |
| seguido_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a relação começou. |

### USUARIO_SEGUE_ARTISTA (associativa)
Registra os artistas seguidos pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_usuario | BIGINT | PK, FK → usuario | Usuário que segue o artista. |
| id_artista | BIGINT | PK, FK → artista | Artista seguido. |
| seguido_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a relação começou. |


## 🗺️ » Como executar

### Pré-requisitos

- Java 21
- Node.js
- Docker Desktop
- Git

### Banco de dados

```bash
docker compose up -d
```

### Backend

```bash
cd backend/gerenciador-musica-backend
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

O backend ficará disponível em `http://localhost:8080`.

### Frontend

```bash
cd frontend/gerenciador_musica_frontend
npm install
npm start
```

O frontend ficará disponível em `http://localhost:4200`.


## 🔗 » Endpoints principais

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastrar usuário |
| POST | `/api/auth/login` | Público | Realizar login |
| POST | `/api/auth/logout` | Autenticado | Realizar logout |

