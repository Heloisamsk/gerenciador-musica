# 🎧 — Sistema Gerenciador de Músicas




---
## 👥 » Integrantes

#### [Álvaro Henrique Nunes de Andrade](https://github.com/alwvaro) | [Arthur Oliveira Ramos](https://github.com/thuramos) | [Maria Heloisa da Silva Montebelo](https://github.com/Heloisamsk) | [Vinicius Freire Pereira](https://github.com/VinFpe)

---
## 📍 » Sobre o Projeto

Projeto de Sistema web para gerenciamento de músicas, artistas, álbuns e playlists para a disciplina de Engenharia de Software ministrado pela professora [Thais Burity](https://github.com/taburity), da UFAPE, referente ao período de 2026.1 com intuito de avaliação para a 2° Verificação de Aprendizagem.

---


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
---
## 🎶 » Status do Projeto

- Em andamento


---
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

---

## 🔗 » Endpoints principais

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastrar usuário |
| POST | `/api/auth/login` | Público | Realizar login |
| POST | `/api/auth/logout` | Autenticado | Realizar logout |

---
