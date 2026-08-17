# 🎧 — Sistema Gerenciador de Músicas




## 👥 » Integrantes

#### [Álvaro Henrique Nunes de Andrade](https://github.com/alwvaro) | [Arthur Oliveira Ramos](https://github.com/thuramos) | [Maria Heloisa da Silva Montebelo](https://github.com/Heloisamsk) | [Vinicius Freire Pereira](https://github.com/VinFpe)

## 📍 » Sobre o Projeto

Projeto de Sistema web para gerenciamento de músicas, artistas, álbuns e playlists, desenvolvido para as disciplinas de Engenharia de Software, ministrada pela professora [Thais Burity](https://github.com/taburity), e Banco de Dados, ministrada pela professora Priscilla Kelly, da UFAPE, referente ao período de 2026.1, com intuito de avaliação para a 2ª Verificação de Aprendizagem.



## 🤖 » Tecnologias

### Frontend

#### [Angular](https://angular.dev/)

- Desenvolvimento da interface web em componentes.
- Formulários reativos, rotas protegidas e integração com a API REST.

#### [TypeScript](https://www.typescriptlang.org/)

- Linguagem utilizada no desenvolvimento do frontend.
- Tipagem de serviços, modelos, formulários e respostas da API.

#### [RxJS](https://rxjs.dev/)

- Tratamento de requisições HTTP e operações assíncronas.
- Controle dos estados de carregamento, sucesso e erro.

### Backend

#### [Java](https://www.java.com/)

- Linguagem utilizada no desenvolvimento do backend.

#### [Spring Boot](https://spring.io/projects/spring-boot)

- Desenvolvimento da API REST e organização das regras de negócio.

#### [Spring Security](https://spring.io/projects/spring-security)

- Autenticação e autorização de usuários.
- Proteção de endpoints de acordo com as roles `USER` e `ADMIN`.

#### [JWT](https://jwt.io/)

- Geração e validação dos tokens utilizados na autenticação.

#### [Spring Data JPA](https://spring.io/projects/spring-data-jpa) e [Hibernate](https://hibernate.org/orm/)

- Persistência das entidades e comunicação com o banco de dados.
- Mapeamento dos relacionamentos entre usuários, artistas, músicas, álbuns, gêneros e playlists.

#### [Jakarta Bean Validation](https://beanvalidation.org/)

- Validação dos dados recebidos nos DTOs da API.

### Banco de dados e infraestrutura

#### [PostgreSQL](https://www.postgresql.org/)

- Banco de dados relacional utilizado para persistência das informações.

#### [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)

- Containerização do frontend Angular, backend Spring Boot e banco de dados PostgreSQL.
- Construção das imagens e execução dos serviços em containers isolados.
- Orquestração da comunicação e inicialização dos containers da aplicação.

### Testes e ferramentas

#### [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/) e MockMvc

- Testes unitários e de integração do backend.

#### [Vitest](https://vitest.dev/) e Angular TestBed

- Testes dos componentes, serviços e requisições HTTP do frontend.


## 🎶 » Status do Projeto

- Em andamento



## 🎏 » Diagrama UML
<img width="3332" height="1792" alt="Diagrama_Spotify_BD drawio (1)" src="https://github.com/user-attachments/assets/86e71045-87ff-470c-8384-f3484625b186" />


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

Todos os usuários fictícios usam a mesma senha para testes: **`Senha@123`**.


## 🔖 » Dicionário de Dados

> Versão completa também disponível em [Dicionário de Dados.pdf](https://github.com/user-attachments/files/30991513/Dicionario.de.Dados.pdf).

### ARTISTA
Armazena os artistas cadastrados no sistema.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_artista | BIGINT | PK | Identificador único do artista. |
| nome | VARCHAR(255) | NOT NULL | Nome artístico do artista. |
| descricao | TEXT | — | Descrição ou informações adicionais sobre o artista. |
| foto_perfil_url | VARCHAR(2048) | — | URL da foto de perfil do artista. |
| nome_completo | VARCHAR(255) | NOT NULL | Nome completo do artista. |

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

A aplicação pode ser executada completamente com Docker ou localmente para desenvolvimento.

### Pré-requisitos

Para executar com Docker:

- [Git](https://git-scm.com/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

Para executar localmente:

- Java 21
- Node.js e npm
- PostgreSQL, que também pode ser iniciado pelo Docker

---

### Clonar o repositório

```bash
git clone https://github.com/projeto-gerenciador-musica/gerenciador-musica.git
cd gerenciador-musica
```

---

### Executar com Docker

Esta é a forma recomendada para iniciar o projeto completo.

Certifique-se de que o Docker Desktop está aberto e, na raiz do projeto, execute:

```bash
docker compose up -d --build
```

Esse comando constrói e inicia os containers do:

- frontend Angular;
- backend Spring Boot;
- banco de dados PostgreSQL.

Para verificar o estado dos containers:

```bash
docker compose ps
```

Após a inicialização, a aplicação estará disponível nos seguintes endereços:

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

#### Visualizar os logs

Todos os serviços:

```bash
docker compose logs -f
```

Somente o backend:

```bash
docker compose logs -f backend
```

Somente o frontend:

```bash
docker compose logs -f frontend
```

Somente o PostgreSQL:

```bash
docker compose logs -f postgres
```

Utilize `Ctrl + C` para sair da visualização dos logs. Os containers continuarão em execução.

#### Reconstruir os containers

Após alterações no frontend ou backend:

```bash
docker compose up -d --build backend frontend
```

Somente o frontend:

```bash
docker compose up -d --build frontend
```

Somente o backend:

```bash
docker compose up -d --build backend
```

Para reconstruir sem utilizar o cache:

```bash
docker compose build --no-cache backend frontend
docker compose up -d --force-recreate backend frontend
```

#### Parar a aplicação

Para apenas parar os containers:

```bash
docker compose stop
```

Para parar e remover os containers e a rede criada pelo Compose:

```bash
docker compose down
```

> O comando `docker compose down` mantém os dados persistidos no volume do PostgreSQL. Evite utilizar `docker compose down -v`, pois o parâmetro `-v` remove os volumes e pode apagar os dados do banco.

---

### Executar localmente

Para desenvolvimento, é possível executar somente o PostgreSQL com Docker e iniciar o frontend e o backend nos terminais locais.

#### 1. Iniciar o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d postgres
```

Verifique se o banco está funcionando:

```bash
docker compose ps postgres
```

#### 2. Iniciar o backend

Abra outro terminal e acesse a pasta do backend:

```bash
cd backend
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

O backend ficará disponível em:

```text
http://localhost:8080
```

#### 3. Iniciar o frontend

Abra outro terminal e acesse a pasta do frontend:

```bash
cd frontend
```

Na primeira execução, instale as dependências:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm start
```

O frontend ficará disponível em:

```text
http://localhost:4200
```

---

### Executar os testes

#### Backend

No Linux ou macOS:

```bash
cd backend
./mvnw test
```

No Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd test
```

#### Frontend

```bash
cd frontend
npm test -- --watch=false
```

Para validar também o build do frontend:

```bash
npm run build
```

---

### Problemas comuns

#### Alterações do frontend não aparecem

Reconstrua o container:

```bash
docker compose up -d --build frontend
```

Depois atualize a página utilizando `Ctrl + F5`.

#### Backend indisponível ou reiniciando

Consulte os logs:

```bash
docker compose logs -f backend
```

#### Banco de dados indisponível

Verifique o container e seus logs:

```bash
docker compose ps postgres
docker compose logs -f postgres
```

#### Porta já utilizada

Confirme se não existe outra aplicação utilizando:

- `4200`, para o frontend;
- `8080`, para o backend;
- `5432`, para o PostgreSQL.

Evite executar localmente um serviço que já esteja ativo em um container na mesma porta.


## 🔗 » Endpoints principais

As rotas autenticadas exigem o envio do token JWT no cabeçalho da requisição:

`Authorization: Bearer <token>`

### Autenticação

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `POST` | `/api/auth/register` | Público | Cadastrar um novo usuário |
| `POST` | `/api/auth/login` | Público | Autenticar o usuário e gerar o token JWT |
| `POST` | `/api/auth/logout` | Autenticado | Encerrar a sessão do usuário |

### Catálogo

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `GET` | `/api/artistas` | `USER` ou `ADMIN` | Listar os artistas disponíveis no catálogo |
| `GET` | `/api/musicas` | `USER` ou `ADMIN` | Listar as músicas disponíveis no catálogo |

### Administração

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `GET` | `/api/admin/artistas` | `ADMIN` | Listar artistas para operações administrativas |
| `POST` | `/api/admin/artistas` | `ADMIN` | Cadastrar um novo artista |
| `POST` | `/api/admin/musicas` | `ADMIN` | Cadastrar uma nova música |
| `GET` | `/api/admin/banco/usuarios` | `ADMIN` | Listar os usuários cadastrados no banco de dados |

> Os endpoints iniciados por `/api/admin` são protegidos e podem ser acessados somente por usuários com a role `ADMIN`. Usuários autenticados com a role `USER` recebem a resposta `403 Forbidden` ao tentar acessar essas rotas.

