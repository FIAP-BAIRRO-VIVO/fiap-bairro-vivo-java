# Bairro Vivo — API Java

API Java do Bairro Vivo para cadastro/autenticacao, JWT, preferencias, locais favoritos e orientacao cidada com Spring AI e fontes oficiais. Ocorrencias, votos, fotos, historico e relatorios pertencem a API .NET.

## Requisitos

- JDK 17 ou superior
- Oracle configurado com o esquema descrito em `Dicionario_Dados_Integracao.md`
- RabbitMQ para demonstrar a publicacao e o consumo de eventos
- Chaves RSA PEM para emitir tokens; nunca as armazene no Git
- Credencial de modelo OpenAI compativel somente se a IA estiver habilitada

Esta API nao cria nem altera o esquema Oracle (`ddl-auto=none`). Configure o DDL fornecido pelo grupo antes de iniciar. A especificacao de dominio e o dicionario fisico fornecidos devem ser revisados em conjunto com os responsaveis pelo banco/.NET antes da integracao.

## Configuracao local

Defina as variaveis de ambiente conforme necessario:

| Variavel | Uso |
|---|---|
| `ORACLE_URL` | URL JDBC, por exemplo `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` |
| `ORACLE_USER`, `ORACLE_PASSWORD` | Usuario Oracle com as permissoes do dominio Java |
| `JWT_PRIVATE_KEY_PATH`, `JWT_PUBLIC_KEY_PATH` | Caminhos de chaves PEM RSA (PKCS#8 e X.509, respectivamente) |
| `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_TTL` | Contrato JWT compartilhado com .NET |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Broker RabbitMQ |
| `DOTNET_API_BASE_URL` | URL da API .NET usada para obter o contexto canonico da ocorrencia |
| `DOTNET_OCCURRENCE_PATH` | Rota de leitura, padrao `/api/v1/ocorrencias/{id}` |
| `DOTNET_SERVICE_TOKEN` | Token de servico opcional, se exigido pelo contrato .NET |
| `BAIRROVIVO_AI_ENABLED` | `true` para habilitar geracao com Spring AI; padrao `false` |
| `OPENAI_API_KEY`, `OPENAI_MODEL` | Credenciais/modelo usados pelo Spring AI |
| `BAIRROVIVO_ADMIN_EMAIL`, `BAIRROVIVO_ADMIN_PASSWORD` | Seed opcional de ADMIN; senha deve ter ao menos 12 caracteres |

Gere as chaves RSA em um diretorio local ignorado pelo Git, por exemplo `.secrets`. Exemplo com OpenSSL:

```powershell
New-Item -ItemType Directory -Force .secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out .secrets\jwt-private.pem
openssl pkey -in .secrets\jwt-private.pem -pubout -out .secrets\jwt-public.pem
$env:JWT_PRIVATE_KEY_PATH = (Resolve-Path .secrets\jwt-private.pem).Path
$env:JWT_PUBLIC_KEY_PATH = (Resolve-Path .secrets\jwt-public.pem).Path
```

Inicie o broker e a API:

```powershell
docker compose up -d rabbitmq
$env:ORACLE_USER = "seu_usuario"
$env:ORACLE_PASSWORD = "sua_senha"
.\mvnw.cmd spring-boot:run
```

O exemplo nao inclui credenciais nem dados seed. O Oracle deve conter as tabelas, views, packages e grants definidos pelo grupo. A conexao com .NET e o fornecedor de LLM tambem precisam estar disponiveis para o fluxo completo de orientacao.

## Endpoints implementados

Prefixo: `/api/v1`. Swagger UI em `/swagger-ui.html`.

A colecao importavel do Postman esta em `postman/Bairro-Vivo-Java.postman_collection.json`.

| Metodo | Rota | Acesso |
|---|---|---|
| `POST` | `/auth/registrar` | Publico; cria sempre `CIDADAO` e retorna JWT |
| `POST` | `/auth/login` | Publico |
| `GET` | `/usuarios/me` | CIDADAO ou ADMIN, propria conta |
| `PATCH` | `/usuarios/me` | CIDADAO ou ADMIN, propria conta |
| `DELETE` | `/usuarios/me` | CIDADAO ou ADMIN, desativacao logica da conta |
| `GET`, `PUT`, `DELETE` | `/usuarios/me/preferencias` | CIDADAO ou ADMIN |
| CRUD | `/usuarios/me/locais-favoritos` | CIDADAO; listagem paginada |
| `POST` | `/ia/orientacoes` | CIDADAO; solicita orientacao por `ocorrenciaId` |
| `GET` | `/ia/orientacoes/ocorrencias/{ocorrenciaId}` | Publico; somente orientacoes publicadas |
| `GET` | `/ia/orientacoes/{id}` | Publico; somente orientacoes publicadas |
| `GET` | `/.well-known/jwks.json` | Publico; chave publica RSA para validacao pelo .NET |
| `GET` | `/api/v1/admin/health` | Somente ADMIN; diagnostico de dependencias |

Use `Authorization: Bearer <accessToken>` nas rotas privadas. O `sub` do JWT e o ID numerico Oracle convertido para string; os papéis sao emitidos em `roles`, com `iss`, `aud`, `exp` e assinatura RS256. O cadastro publico nao aceita papel de administrador. Logout no MVP significa remover o token do cliente; tokens ja emitidos continuam validos ate expirarem.

Erros seguem o formato `{ codigo, mensagem, detalhes, correlationId }`; o identificador tambem e enviado no cabecalho `X-Correlation-Id`. Favoritos e orientacoes publicas retornam pagina, tamanho e totalizadores. O tamanho maximo de pagina e 100.

## RabbitMQ

O cadastro publica `usuario.cadastrado` depois do commit, sem email, senha, hash ou token. A mensagem tem `eventId`, tipo, versao, data e `usuarioId`; o consumidor registrado demonstra a entrega em log estruturado. O envio usa um listener assicrono apos commit, mas nao implementa outbox transacional: uma indisponibilidade do broker pode impedir a entrega e fica registrada no log.

## Orientacao com RAG

O Java consulta `DOTNET_OCCURRENCE_PATH`, recupera ate cinco trechos de `VW_TRECHOS_RAG_APROVADOS`, busca canais com `FL_VERIFICADO = 'S'` e usa Spring AI somente com municipio/categoria cobertos por fontes aprovadas. A instrucao do modelo trata a descricao como dado nao confiavel, bloqueia prompt injection e proibe inventar contatos/horarios; uma validacao rejeita numeros de contato, horarios e URLs que nao estejam nas fontes aprovadas. Respostas e fontes sao persistidas via `PKG_BASE_IA.PR_REGISTRAR_ORIENTACAO`; os estados sem fonte ou municipio confirmado nao citam contatos.

O fluxo com modelo fica desabilitado ate `BAIRROVIVO_AI_ENABLED=true` e configuracao de `OPENAI_API_KEY`; se habilitado sem credencial, a inicializacao falha explicitamente. Falhas do modelo retornam erro `503` padronizado e registram estado `ERRO`; nao devolvem uma resposta ficticia. Cada usuario pode solicitar ate cinco orientacoes por minuto por instancia da API; para ambientes com varias instancias, use um limitador compartilhado. Orientacoes sao limitadas ao municipio de Sao Paulo nesta versao, conforme o escopo.

### Contrato esperado da API .NET

A rota configurada retorna um JSON publico/canonico com `id`, `titulo`, `descricao`, `categoriaId`, `categoria`, `municipioId`, `municipio` e `uf`. O ID do solicitante e obtido exclusivamente do JWT, nunca do corpo. Confirme e documente este contrato no OpenAPI compartilhado antes da integracao.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes unitarios cobrem regras de cadastro/autorizacao e isolamento de favoritos. Testes ponta a ponta dependem de Oracle, RabbitMQ, API .NET, chaves e credencial do modelo; esses servicos externos nao estao incluidos no repositorio.
