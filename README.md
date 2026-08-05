# Sistema Gerenciador de Músicas





## Integrantes

 Arthur Oliveira Ramos / Alvaro Henrique Nunes de Andrade / Maria Heloisa da Silva Montebelo / Vinicius Freire Pereira.



## Sobre o Projeto

Projeto de Sistema web para gerenciamento de músicas, artistas, álbuns e playlists para a disciplina de Engenharia de Software ministrado pela professora Thais Burity, da UFAPE, referente ao período de 2026.1 com intuito de avaliação para a 2- Verificação de Aprendizagem.



## Tecnologias

- Angular
- Spring Boot
- Java
- Maven
- Git
- Docker
- Postgresql


## Status do Projeto

- Em andamento 



## Estrutura

```
gerenciador_musica
│
├── frontend
└── backend
```

### Estratégia de logout com JWT

A aplicação utiliza JWT de maneira stateless. O backend valida o
token recebido em POST /api/auth/logout e confirma o logout.

Após a resposta, o frontend remove o token e a Role armazenados no
localStorage, deixando de enviar o JWT nas próximas requisições.

Nesta versão acadêmica não é utilizada uma lista de tokens revogados.
O token também deixa de ser aceito automaticamente após sua expiração.