# 🎧 — Sistema Gerenciador de Músicas

<details>
<summary><h2><strong>👥 » Integrantes</strong></h2></summary>

#### [Álvaro Henrique Nunes de Andrade](https://github.com/alwvaro) | [Arthur Oliveira Ramos](https://github.com/thuramos) | [Maria Heloisa da Silva Montebelo](https://github.com/Heloisamsk) | [Vinicius Freire Pereira](https://github.com/VinFpe)

</details>

<details>
<summary><h2><strong>📍 » Sobre o Projeto</strong></h2></summary>

Projeto de Sistema web para gerenciamento de músicas, artistas, álbuns e playlists, desenvolvido para as disciplinas de Engenharia de Software, ministrada pela professora [Thais Burity](https://github.com/taburity), e Banco de Dados, ministrada pela professora Priscilla Kelly, da UFAPE, referente ao período de 2026.1, com intuito de avaliação para a 2ª Verificação de Aprendizagem.

</details>

<details>
<summary><h2><strong>🚀 » Aplicação em Produção</strong></h2></summary>

- **Frontend:** [gerenciador-musica.onrender.com](https://gerenciador-musica.onrender.com)
- **Backend:** [gerenciador-musica-1.onrender.com](https://gerenciador-musica-1.onrender.com)

→ Acesso ADMIN:  
**Email**: eloa93@example.net | **Senha**: Senha@123

</details>

<details>
<summary><h2><strong>🤖 » Tecnologias</strong></h2></summary>

<details>
<summary><h3><strong>Frontend</strong></h3></summary>

<p>
  <img src="https://skillicons.dev/icons?i=angular,ts,html,css,reactivex,nodejs,express,npm&theme=dark" alt="Angular, TypeScript, HTML5, CSS3, RxJS, Node.js, Express e npm" />
</p>

#### [Angular](https://angular.dev/)

- Desenvolvimento da interface web baseada em componentes.
- Utilização de formulários reativos, injeção de dependências, rotas protegidas e integração com a API REST.
- Organização da aplicação utilizando componentes standalone e Angular Router.

#### [TypeScript](https://www.typescriptlang.org/)

- Linguagem principal do frontend.
- Tipagem de componentes, serviços, formulários, modelos e respostas da API.

#### [HTML5](https://developer.mozilla.org/docs/Web/HTML) e [CSS3](https://developer.mozilla.org/docs/Web/CSS)

- Estruturação semântica e acessível das páginas.
- Desenvolvimento de layouts responsivos para diferentes tamanhos de tela.
- Estilização dos componentes e estados visuais da aplicação.

#### [RxJS](https://rxjs.dev/)

- Gerenciamento das operações assíncronas e requisições HTTP.
- Tratamento dos estados de carregamento, sucesso e erro.
- Controle do fluxo de dados por meio de Observables.

#### [Node.js](https://nodejs.org/) e [Express](https://expressjs.com/)

- Ambiente de execução e suporte à infraestrutura do frontend Angular.
- Configuração do servidor utilizado pelo projeto Angular com suporte ao pacote de SSR.

#### [npm](https://www.npmjs.com/)

- Gerenciamento das dependências, scripts de desenvolvimento, testes e build do frontend.

---

</details>

<details>
<summary><h3><strong>Backend</strong></h3></summary>

<p>
  <img src="https://skillicons.dev/icons?i=java,spring,hibernate,maven&theme=dark" alt="Java, Spring Boot, Hibernate e Maven" />
</p>

#### [Java 21](https://www.java.com/)

- Linguagem utilizada no desenvolvimento do backend.
- Implementação das regras de negócio, serviços, validações e modelos da aplicação.

#### [Spring Boot](https://spring.io/projects/spring-boot)

- Desenvolvimento e configuração da API REST.
- Organização da aplicação em controllers, services, repositories, DTOs e configurações.
- Gerenciamento de dependências e inicialização dos componentes do backend.

#### [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)

- Implementação dos endpoints HTTP da API.
- Recebimento, validação e retorno dos dados em formato JSON.
- Padronização dos códigos de resposta e tratamento global de exceções.

#### [Spring Security](https://spring.io/projects/spring-security)

- Autenticação e autorização dos usuários.
- Proteção dos endpoints de acordo com as roles `USER` e `ADMIN`.
- Configuração da aplicação como uma API stateless.

#### [JSON Web Token](https://jwt.io/)

- Geração e validação dos tokens de autenticação.
- Identificação do usuário autenticado em cada requisição protegida.
- Implementação realizada com a biblioteca JJWT.

#### [Spring Data JPA](https://spring.io/projects/spring-data-jpa) e [Hibernate](https://hibernate.org/orm/)

- Persistência das entidades e comunicação com o banco de dados.
- Criação de consultas com repositories, métodos derivados e specifications.
- Mapeamento dos relacionamentos entre usuários, artistas, músicas, álbuns, gêneros, perfis e playlists.

#### [Jakarta Bean Validation](https://beanvalidation.org/)

- Validação automática dos dados recebidos nos DTOs.
- Verificação de campos obrigatórios, tamanhos máximos, valores positivos e formatos permitidos.

#### [Flyway](https://documentation.red-gate.com/flyway)

- Versionamento e execução automática das migrations do banco de dados.
- Criação, evolução e povoamento inicial da estrutura do PostgreSQL.

#### [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)

- Disponibilização do endpoint de monitoramento da saúde do backend.
- Verificação do estado da aplicação durante a execução e o deploy.

#### [Apache Maven](https://maven.apache.org/)

- Gerenciamento das dependências e plugins do backend.
- Execução dos testes, geração de cobertura e criação do arquivo executável da aplicação.

---

</details>

<details>
<summary><h3><strong>Banco de dados, infraestrutura e deploy</strong></h3></summary>

<p>
  <img src="https://skillicons.dev/icons?i=postgres,docker,nginx&theme=dark" alt="PostgreSQL, Docker e Nginx" />
</p>

#### [PostgreSQL](https://www.postgresql.org/)

- Banco de dados relacional utilizado para persistência das informações.
- Armazenamento dos dados do catálogo musical, usuários, perfis, playlists e demais relacionamentos.

#### [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)

- Containerização do frontend Angular, backend Spring Boot e banco de dados PostgreSQL.
- Construção das imagens e execução dos serviços em ambientes isolados.
- Orquestração da comunicação, dependências e inicialização dos containers.
- Utilização de health checks e volume persistente para o banco de dados.

#### [Nginx](https://nginx.org/)

- Servidor utilizado para disponibilizar os arquivos de produção do frontend.
- Execução do Angular em uma imagem Docker leve e sem privilégios administrativos.

#### [Render](https://render.com/)

- Hospedagem do frontend e do backend em ambiente de produção.
- Configuração das URLs públicas e das variáveis de ambiente da aplicação.

---

</details>

<details>
<summary><h3><strong>Testes, qualidade e automação</strong></h3></summary>

<p>
  <img src="https://skillicons.dev/icons?i=git,github,githubactions,vitest&theme=dark" alt="Git, GitHub, GitHub Actions e Vitest" />
</p>

#### [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/) e MockMvc

- Desenvolvimento dos testes unitários, de integração e de segurança do backend.
- Simulação das dependências da aplicação com mocks.
- Validação dos endpoints, códigos HTTP, permissões e respostas JSON.

#### [Vitest](https://vitest.dev/) e Angular TestBed

- Desenvolvimento dos testes de componentes, serviços, formulários, guards e requisições HTTP do frontend.
- Validação dos estados de carregamento, sucesso, erro e navegação.
- Geração de relatórios de cobertura do frontend.

#### [JaCoCo](https://www.jacoco.org/jacoco/)

- Medição da cobertura dos testes automatizados do backend.
- Geração de relatórios em HTML e XML durante o processo de verificação do Maven.

#### [SonarCloud](https://sonarcloud.io/)

- Análise estática e acompanhamento da qualidade do código.
- Verificação de bugs, vulnerabilidades, duplicações, cobertura e code smells.
- Validação automática do Quality Gate nos pull requests.

#### [GitHub Actions](https://github.com/features/actions)

- Automação dos pipelines de integração contínua do frontend e do backend.
- Execução automática dos testes, builds, cobertura e análise do SonarCloud.
- Utilização de PostgreSQL como serviço durante os testes de integração.

#### [Git](https://git-scm.com/) e [GitHub](https://github.com/)

- Controle de versão e colaboração no desenvolvimento do projeto.
- Organização das alterações em branches, commits, pull requests e revisões de código.

#### [Prettier](https://prettier.io/)

- Padronização da formatação dos arquivos do frontend.
- Manutenção de um estilo consistente no código TypeScript, HTML e CSS.

</details>

</details>

<details>
<summary><h2><strong>🎶 » Status do Projeto</strong></h2></summary>

- Em andamento

</details>

<details>
<summary><h2><strong>🎏 » Diagrama UML</strong></h2></summary>

<img width="3462" height="1792" alt="Diagrama_Spotify_BD drawio (2)" src="https://github.com/user-attachments/assets/5f0af97e-1fb0-42f2-bbb7-4adfad160c89" />

</details>

<details>
<summary><h2><strong>🗄️ » Configuração do Banco de Dados</strong></h2></summary>

O projeto usa **PostgreSQL 18** como SGBD. O Docker Compose cria automaticamente um banco principal e outro isolado para os testes de integração.

| Finalidade | Serviço | Host | Porta | Banco | Usuário | Senha |
|---|---|---|---|---|---|---|
| Aplicação | `postgres` | `localhost` | `5432` | `gerenciador_musica` | `gerenciador` | `gerenciador` |
| Testes | `postgres-test` | `localhost` | `5433` | `gerenciador_musica_test` | `postgres` | `postgres` |

A estrutura do banco é criada e atualizada automaticamente pelo [Flyway](https://flywaydb.org/) a partir das migrations disponíveis em [`backend/src/main/resources/db/migration`](backend/src/main/resources/db/migration), executadas quando o backend é iniciado.

</details>

<details>
<summary><h2><strong>🌱 » Metodologia de Povoamento</strong></h2></summary>

O banco é povoado automaticamente pela migration [`V11__popular_dados_iniciais.sql`](backend/src/main/resources/db/migration/V11__popular_dados_iniciais.sql), que roda junto com as demais assim que o container do backend sobe — não é preciso nenhum passo manual.

Os dados vêm de duas fontes:

- **Catálogo musical real** (artistas, álbuns, músicas e a relação entre eles): coletado via [API Web do Spotify](https://developer.spotify.com/documentation/web-api) (fluxo *Client Credentials*), usando um script Python ([`scripts/povoamento/coletar_spotify.py`](scripts/povoamento/coletar_spotify.py)).
- **Dados fictícios das entidades do próprio app** (usuários, playlists, reviews, curtidas, reproduções, relações de seguidores): gerados com a biblioteca [Faker](https://faker.readthedocs.io/), respeitando as chaves estrangeiras do catálogo real coletado.

Um segundo script ([`scripts/povoamento/gerar_sql.py`](scripts/povoamento/gerar_sql.py)) junta as duas fontes e gera o arquivo `V11__popular_dados_iniciais.sql`. Esse processo roda **uma única vez, offline**, para gerar o SQL — quem clona o repositório não precisa de credenciais do Spotify nem de conexão com a API para ter o banco populado, só precisa subir o Docker.

Todos os usuários fictícios usam a mesma senha para testes: **`Senha@123`**.

</details>

<details>
<summary><h2><strong>🔖 » Dicionário de Dados</strong></h2></summary>

<details>
<summary><h3><strong>ARTISTA</strong></h3></summary>

Armazena os artistas cadastrados no sistema.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_artista | BIGINT | PK | Identificador único do artista. |
| nome | VARCHAR(255) | NOT NULL | Nome artístico do artista. |
| descricao | TEXT | NOT NULL, CHECK (não vazia), CHECK (até 500 caracteres) | Descrição ou informações adicionais sobre o artista. |
| foto_perfil_url | VARCHAR(2048) | — | URL da foto de perfil do artista. |
| nome_completo | VARCHAR(255) | NOT NULL | Nome completo do artista. |

</details>

<details>
<summary><h3><strong>USUARIO</strong></h3></summary>

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

</details>

<details>
<summary><h3><strong>MUSICA</strong></h3></summary>

Armazena somente os dados próprios da música. A autoria e as participações são representadas pela tabela associativa `musica_artista`.

| Atributo         | Tipo de Dado | Restrições                          | Semântica                                                                                  |
| ---------------- | ------------ | ----------------------------------- | ------------------------------------------------------------------------------------------ |
| id_musica        | BIGINT       | PK                                  | Identificador único da música.                                                             |
| id_album         | BIGINT       | FK → album, permite NULL            | Álbum ao qual a música pertence. O valor `NULL` representa uma música lançada como single. |
| titulo           | VARCHAR(255) | NOT NULL                            | Título da música.                                                                          |
| letra            | TEXT         | —                                   | Letra opcional da música.                                                                  |
| duracao_segundos | INTEGER      | NOT NULL, CHECK (> 0)               | Duração da música em segundos.                                                             |
| ano_lancamento   | SMALLINT     | NOT NULL, CHECK (entre 1800 e 2100) | Ano de lançamento da música.                                                               |

O artista principal não é armazenado diretamente em `musica`. Essa informação é representada por um registro em `musica_artista` com `papel_participacao = 'PRINCIPAL'`, evitando dois caminhos diferentes para representar a autoria da mesma música.

</details>

<details>
<summary><h3><strong>ALBUM</strong></h3></summary>

Armazena os álbuns cadastrados, com artista responsável, título e ano de lançamento.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_album | BIGINT | PK | Identificador único do álbum. |
| id_artista | BIGINT | NOT NULL, FK → artista | Artista responsável pelo álbum. |
| titulo | VARCHAR(255) | NOT NULL, não vazio | Título do álbum. |
| ano_lancamento | SMALLINT | NOT NULL, CHECK (entre 1800 e 2100) | Ano de lançamento do álbum. |
| capa_url | VARCHAR(2048) | — | URL da capa do álbum. |

A combinação de artista, título e ano é única, sem diferenciar letras
maiúsculas e minúsculas. No cadastro de música, `albumId` é opcional; quando
informado, a API valida se o álbum pertence ao artista principal selecionado.

</details>

<details>
<summary><h3><strong>GENERO</strong></h3></summary>

Armazena os gêneros musicais cadastrados no sistema.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_genero | BIGINT | PK | Identificador único do gênero. |
| nome | VARCHAR(100) | NOT NULL, UNIQUE | Nome do gênero musical, único no sistema. |

</details>

<details>
<summary><h3><strong>PERFIL</strong></h3></summary>

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

</details>

<details>
<summary><h3><strong>REVIEW</strong></h3></summary>

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

</details>

<details>
<summary><h3><strong>PLAYLIST</strong></h3></summary>

Armazena as playlists criadas pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_playlist | BIGINT | PK | Identificador único da playlist. |
| id_usuario | BIGINT | NOT NULL, FK → usuario | Usuário dono da playlist. |
| nome | VARCHAR(255) | NOT NULL | Nome da playlist. |
| descricao | TEXT | — | Descrição opcional da playlist. |
| data_criacao | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data de criação da playlist. |

</details>

<details>
<summary><h3><strong>REPRODUCAO</strong></h3></summary>

Armazena o histórico de reproduções de músicas pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_reproducao | BIGINT | PK | Identificador único da reprodução. |
| id_usuario | BIGINT | NOT NULL, FK → usuario | Usuário que reproduziu a música. |
| id_musica | BIGINT | NOT NULL, FK → musica | Música reproduzida. |
| reproduzida_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data e horário da reprodução. |
| segundos_ouvidos | INTEGER | NOT NULL, CHECK (>= 0) | Quantos segundos da música foram ouvidos. |

</details>

<details>
<summary><h3><strong>MUSICA_ARTISTA (associativa)</strong></h3></summary>

Relaciona as músicas a todos os seus artistas creditados, incluindo o artista principal e os artistas participantes.

| Atributo           | Tipo de Dado | Restrições                                           | Semântica                                  |
| ------------------ | ------------ | ---------------------------------------------------- | ------------------------------------------ |
| id_musica          | BIGINT       | PK composta, FK → musica, ON DELETE CASCADE          | Música associada ao crédito.               |
| id_artista         | BIGINT       | PK composta, FK → artista, ON DELETE CASCADE         | Artista creditado na música.               |
| papel_participacao | VARCHAR(20)  | NOT NULL, CHECK IN (`PRINCIPAL`, `FEAT`, `PRODUTOR`) | Papel desempenhado pelo artista na música. |

A chave primária composta impede que o mesmo artista seja associado mais de uma vez à mesma música. Gatilhos de restrição adiáveis garantem que toda música possua exatamente um artista com o papel `PRINCIPAL`.

Atualmente, o cadastro e a edição de músicas utilizam os papéis `PRINCIPAL` e `FEAT`. O papel `PRODUTOR` está previsto no modelo para futuras extensões. Na API, um crédito `FEAT` é apresentado ao frontend como uma participação.

</details>

<details>
<summary><h3><strong>MUSICA_GENERO (associativa)</strong></h3></summary>

Relaciona músicas aos seus gêneros musicais.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_musica | BIGINT | PK, FK → musica | Música associada. |
| id_genero | BIGINT | PK, FK → genero | Gênero associado à música. |

</details>

<details>
<summary><h3><strong>PLAYLIST_MUSICA (associativa)</strong></h3></summary>

Relaciona músicas às playlists, com ordem de exibição.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_playlist | BIGINT | PK, FK → playlist | Playlist associada. |
| id_musica | BIGINT | PK, FK → musica | Música associada. |
| ordem | INTEGER | NOT NULL, CHECK (> 0), UNIQUE junto com id_playlist | Posição da música dentro da playlist. |
| data_criacao | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a música foi adicionada à playlist. |

</details>

<details>
<summary><h3><strong>CURTIDA_MUSICA (associativa)</strong></h3></summary>

Registra as curtidas dos usuários em músicas.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_usuario | BIGINT | PK, FK → usuario | Usuário que curtiu. |
| id_musica | BIGINT | PK, FK → musica | Música curtida. |
| curtida_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data da curtida. |

</details>

<details>
<summary><h3><strong>USUARIO_SEGUE_USUARIO (associativa)</strong></h3></summary>

Registra relações de seguidores entre usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_seguidor | BIGINT | PK, FK → usuario, CHECK (id_seguidor ≠ id_seguido) | Usuário que segue. |
| id_seguido | BIGINT | PK, FK → usuario | Usuário que é seguido. |
| seguido_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a relação começou. |

</details>

<details>
<summary><h3><strong>USUARIO_SEGUE_ARTISTA (associativa)</strong></h3></summary>

Registra os artistas seguidos pelos usuários.

| Atributo | Tipo de Dado | Restrições | Semântica |
|---|---|---|---|
| id_usuario | BIGINT | PK, FK → usuario | Usuário que segue o artista. |
| id_artista | BIGINT | PK, FK → artista | Artista seguido. |
| seguido_em | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT `now()` | Data em que a relação começou. |

</details>

</details>

<details>
<summary><h2><strong>🗺️ » Como executar</strong></h2></summary>

**Pré-requisitos para executar toda a aplicação:** [Git](https://git-scm.com/) e [Docker Desktop](https://www.docker.com/products/docker-desktop/). Java 21, Node.js e npm só são necessários para executar o código ou os testes diretamente na máquina.

<details>
<summary><h3><strong>Clonar e iniciar com Docker</strong></h3></summary>

```bash
git clone https://github.com/projeto-gerenciador-musica/gerenciador-musica.git
cd gerenciador-musica
docker compose up
```

Esse único comando constrói as imagens quando necessário, cria os dois bancos, executa as migrations, gera uma chave JWT temporária para o ambiente local e inicia os serviços na ordem correta.

Serviços disponíveis após a inicialização completa:

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- PostgreSQL da aplicação: `localhost:5432`
- PostgreSQL de testes: `localhost:5433`

| Ação                       | Comando                                 |
| -------------------------- | --------------------------------------- |
| Verificar containers       | `docker compose ps`                     |
| Visualizar logs            | `docker compose logs -f`                |
| Iniciar em segundo plano   | `docker compose up -d`                  |
| Reconstruir tudo           | `docker compose up --build`             |
| Reconstruir frontend       | `docker compose up -d --build frontend` |
| Reconstruir backend        | `docker compose up -d --build backend`  |
| Parar os serviços          | `docker compose stop`                   |
| Parar e remover containers | `docker compose down`                   |

> `docker compose down` mantém os dados dos bancos PostgreSQL. Utilize `docker compose down -v` somente quando desejar apagar também os volumes dos bancos principal e de testes.

</details>

<details>
<summary><h3><strong>Executar localmente para desenvolvimento</strong></h3></summary>

Inicie os bancos principal e de testes com Docker:

```bash
docker compose up -d postgres postgres-test
```

Em outro terminal, inicie o backend:

```bash
cd backend
./mvnw spring-boot:run
```

No Windows PowerShell, utilize:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Em um terceiro terminal, inicie o frontend:

```bash
cd frontend
npm install
npm start
```

</details>

<details>
<summary><h3><strong>Executar os testes</strong></h3></summary>

O banco `postgres-test` já é criado pelo `docker compose up`; não é necessário criar outro container manualmente. Com os serviços ativos, execute em outro terminal.

Backend:

```bash
cd backend
./mvnw test
```

No Windows PowerShell, substitua o último comando por `.\mvnw.cmd test`.

Frontend:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

</details>

<details>
<summary><h3><strong>Atualizar alterações no Docker</strong></h3></summary>

Caso uma alteração não apareça ao utilizar Docker, reconstrua o respectivo serviço e atualize a página com `Ctrl + F5`.

</details>

</details>

<details>
<summary><h2><strong>🔗 » Endpoints principais</strong></h2></summary>

As rotas autenticadas exigem o envio do token JWT no cabeçalho da requisição:

`Authorization: Bearer <token>`

<details>
<summary><h3><strong>Autenticação</strong></h3></summary>

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `POST` | `/api/auth/register` | Público | Cadastrar um novo usuário |
| `POST` | `/api/auth/login` | Público | Autenticar o usuário e gerar o token JWT |
| `POST` | `/api/auth/logout` | Autenticado | Encerrar a sessão do usuário |

</details>

<details>
<summary><h3><strong>Catálogo</strong></h3></summary>

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `GET` | `/api/artistas` | `USER` ou `ADMIN` | Listar os artistas disponíveis no catálogo |
| `GET` | `/api/artistas/{id}` | `USER` ou `ADMIN` | Consultar os dados completos de um artista pelo ID |
| `GET` | `/api/artistas/{id}/detalhes` | `USER` ou `ADMIN` | Consultar o resumo, os álbuns e as músicas de um artista utilizando as views do catálogo |
| `GET` | `/api/albuns` | `USER` ou `ADMIN` | Listar álbuns; aceita o filtro `?artistaId={id}` |
| `GET` | `/api/albuns/{id}` | `USER` ou `ADMIN` | Consultar um álbum por ID |
| `GET` | `/api/musicas` | `USER` ou `ADMIN` | Listar as músicas disponíveis no catálogo |
| `GET` | `/api/musicas/{id}` | `USER` ou `ADMIN` | Consultar os dados completos de uma música pelo ID |

</details>

<details>
<summary><h3><strong>Administração</strong></h3></summary>

| Método | Endpoint | Acesso | Descrição |
|:------:|----------|--------|-----------|
| `POST` | `/api/admin/artistas` | `ADMIN` | Cadastrar um novo artista |
| `PUT` | `/api/admin/artistas/{id}` | `ADMIN` | Atualizar os dados de um artista sem alterar seu ID ou suas associações |
| `DELETE` | `/api/admin/artistas/{id}` | `ADMIN` | Excluir um artista que não possua álbuns ou músicas associados |
| `POST` | `/api/admin/albuns` | `ADMIN` | Cadastrar um álbum vinculado a um artista |
| `PUT` | `/api/admin/albuns/{id}` | `ADMIN` | Atualizar título, ano e capa sem alterar o artista ou as músicas associadas |
| `DELETE` | `/api/admin/albuns/{id}` | `ADMIN` | Excluir um álbum somente quando ele não possuir músicas associadas |
| `POST` | `/api/admin/musicas` | `ADMIN` | Cadastrar uma nova música |
| `PUT` | `/api/admin/musicas/{id}` | `ADMIN` | Atualizar uma música e suas associações sem alterar seu ID |
| `DELETE` | `/api/admin/musicas/{id}` | `ADMIN` | Excluir uma música e limpar suas associações dependentes |
| `GET` | `/api/admin/banco/usuarios` | `ADMIN` | Listar os usuários cadastrados no banco de dados |

> Os endpoints iniciados por `/api/admin` são protegidos e podem ser acessados somente por usuários com a role `ADMIN`. Usuários autenticados com a role `USER` recebem a resposta `403 Forbidden` ao tentar acessar essas rotas.

</details>

</details>

<details>
<summary><h2><strong>👁️ » Views SQL do catálogo de artistas</strong></h2></summary>

A página de detalhes do artista utiliza três views SQL para esconder a complexidade dos relacionamentos entre artistas, músicas, álbuns e gêneros. As views não armazenam cópias dos dados: seus resultados são calculados a partir das tabelas atuais sempre que são consultadas.

| View                          | Tabelas e operações utilizadas                                                                     | Finalidade                                                                                                                               |
| ----------------------------- | -------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `vw_artista_resumo_catalogo`  | `artista`, `album`, `musica_artista` e `musica`, com `COUNT`, `SUM` e agrupamentos                 | Retornar os dados do artista, quantidade de álbuns, músicas como artista principal, participações e duração total do catálogo principal. |
| `vw_albuns_artista_catalogo`  | `album`, `artista` e `musica`, com agrupamento por álbum                                           | Retornar os álbuns do artista junto com capa, ano, quantidade de músicas e duração total.                                                |
| `vw_musicas_artista_catalogo` | `musica`, `musica_artista`, `artista`, `album`, `musica_genero` e `genero`, incluindo `STRING_AGG` | Retornar as músicas relacionadas ao artista, identificando se ele é principal ou participante e reunindo álbum, capa e gêneros.          |

<details>
<summary><h3><strong>Uso das views no site</strong></h3></summary>

Na página inicial, cada cartão de artista possui um link para a rota `/artistas/{id}`. Ao selecionar um artista, o frontend solicita o endpoint `GET /api/artistas/{id}/detalhes`.

O backend consulta as três views e combina seus resultados em uma única resposta composta por:

- `artista`: informações pessoais e totais calculados por `vw_artista_resumo_catalogo`;
- `albuns`: cartões obtidos de `vw_albuns_artista_catalogo`;
- `musicas`: listagem obtida de `vw_musicas_artista_catalogo`.

Na interface, a primeira view alimenta o cabeçalho e os indicadores do artista, a segunda alimenta a seção de álbuns e a terceira alimenta a seção de músicas. Cada música também informa se o artista consultado aparece como artista principal ou como participação.

Essa abordagem concentra os múltiplos `JOINs`, agrupamentos e cálculos no banco de dados. O frontend recebe uma estrutura simplificada, enquanto o backend não precisa repetir as mesmas consultas complexas em diferentes repositories.

A migration [`V17__criar_views_catalogo_artista.sql`](backend/src/main/resources/db/migration/V17__criar_views_catalogo_artista.sql) cria as três views utilizadas pela página de detalhes do artista. Posteriormente, a migration [`V18__normalizar_creditos_artistas_musica.sql`](backend/src/main/resources/db/migration/V18__normalizar_creditos_artistas_musica.sql) normaliza os créditos das músicas, consolida registros duplicados do povoamento e recria as views de acordo com o novo modelo.

</details>

</details>
