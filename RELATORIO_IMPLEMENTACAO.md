# Relatorio de implementacao — Bairro Vivo Java

**Escopo:** API Java/Spring Boot, conforme selecao do usuario. Aplicativo mobile e API .NET ficaram fora deste repositorio/escopo.

## O que foi criado

- Projeto Maven executavel com Spring Boot, Java 17+, Spring Data JPA, Spring Security, Bean Validation, Spring AI, RabbitMQ, Actuator e Swagger/OpenAPI.
- Entidades JPA para `TB_USUARIO`, `TB_PREFERENCIA_USUARIO`, `TB_LOCAL_FAVORITO` e `TB_ORIENTACAO_IA`, com tipos de coluna e flags compativeis com o dicionario Oracle.
- Cadastro e login com senha BCrypt, papel publico fixo `CIDADAO`, JWT RS256, emissor/audiencia/expiracao validados e endpoint JWKS. Conta `ADMIN` e provisionada somente por variaveis de ambiente.
- Perfil pessoal, preferencias e CRUD de locais favoritos com isolamento por `sub` e paginacao real.
- Respostas de erro padronizadas com detalhes de validacao e identificador de correlacao.
- Publicacao apos commit e consumidor RabbitMQ do evento `usuario.cadastrado`; mensagens nao incluem dados pessoais ou credenciais.
- Fluxo de orientacao cidada: consulta a ocorrencia na API .NET, recuperacao de trechos oficiais aprovados, filtro de canais verificados, Spring AI opcional com protecao contra instrucoes maliciosas no relato e persistencia via `PKG_BASE_IA.PR_REGISTRAR_ORIENTACAO`.
- Validacao de `iss`, `aud`, `exp` e conta ativa nos JWT; limite inicial de cinco solicitacoes de orientacao por usuario/minuto, em memoria por instancia.
- Configuracao local, Docker Compose para RabbitMQ, orientacoes de seguranca e matriz de rotas no README.
- Colecao Postman importavel com 18 requisicoes para os endpoints implementados, variaveis de ambiente/colecao e captura automatica do JWT apos cadastro ou login.

## Verificacao e limitacoes

- O diretorio de destino estava vazio (exceto `Teste.txt`); nao havia codigo nem testes preexistentes.
- Oracle, DDL, packages/views, RabbitMQ, API .NET, chaves JWT e credenciais do modelo nao foram fornecidos. Esses elementos permanecem configuraveis e sao requisitos para validar o fluxo integrado.
- O contrato de leitura .NET usado pelo Java e explicito e configuravel; deve ser alinhado com o time .NET.
- A entrega RabbitMQ ocorre apos o commit, mas nao possui outbox; indisponibilidade do broker e registrada, sem prometer entrega garantida.
- O esquema Oracle nao e criado nem alterado automaticamente.
- Nenhum segredo, chave ou senha foi incluido no repositorio.

## Validacao

Validacao: `.\mvnw.cmd clean verify` — build empacotado com sucesso e 8 testes aprovados, incluindo inicializacao HTTP com H2, registro/login, protecao da rota ADMIN, bloqueio de token apos desativar a conta, limitacao de requisicoes e verificacao de contatos/URLs com fontes. O ambiente tem Java 20; o projeto compila para Java 17. Oracle, RabbitMQ real, API .NET e provedor do modelo nao estavam configurados e continuam pendentes de validacao integrada.
