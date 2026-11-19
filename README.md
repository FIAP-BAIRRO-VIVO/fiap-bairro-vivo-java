# Bairro Vivo — API Java

API Java do Bairro Vivo para cadastro/autenticacao, JWT, preferencias, locais favoritos e orientacao cidada com Spring AI e fontes oficiais. Ocorrencias, votos, fotos, historico e relatorios pertencem a API .NET; a Java consulta os dados de ocorrencia diretamente nas tabelas compartilhadas do Oracle.

## Como executar o projeto

### Pre-requisitos

- JDK 17 ou superior
- Maven Wrapper incluido no repositorio
- Docker Desktop ou Docker Engine para o RabbitMQ
- Oracle com o esquema definido em `Dicionario_Dados_Integracao.md`
- Chaves RSA PEM para assinatura dos JWT

O projeto nao cria nem altera o esquema Oracle; configure o banco antes da inicializacao.

### Executar localmente no Windows (PowerShell)

1. Gere as chaves RSA em `.secrets`:

```powershell
New-Item -ItemType Directory -Force .secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out .secrets\jwt-private.pem
openssl pkey -in .secrets\jwt-private.pem -pubout -out .secrets\jwt-public.pem
$env:JWT_PRIVATE_KEY_PATH = (Resolve-Path .secrets\jwt-private.pem).Path
$env:JWT_PUBLIC_KEY_PATH = (Resolve-Path .secrets\jwt-public.pem).Path
```

2. Configure o banco Oracle e, opcionalmente, o usuario administrador:

```powershell
$env:ORACLE_URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"
$env:ORACLE_USER = "seu_usuario"
$env:ORACLE_PASSWORD = "sua_senha"
$env:BAIRROVIVO_ADMIN_EMAIL = "admin@example.com"
$env:BAIRROVIVO_ADMIN_PASSWORD = "uma_senha_de_pelo_menos_12_caracteres"
```

3. Inicie o RabbitMQ e a aplicacao:

```powershell
docker compose up -d rabbitmq
.\mvnw.cmd spring-boot:run
```

4. Acesse:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- RabbitMQ Management: `http://localhost:15672`

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Requisitos

- JDK 17 ou superior
- Oracle configurado com o esquema descrito em `Dicionario_Dados_Integracao.md`
- RabbitMQ para demonstrar a publicacao e o consumo de eventos
- Chaves RSA PEM para emitir tokens; nunca as armazene no Git
- Credencial de modelo OpenAI compativel somente se a IA estiver habilitada

Esta API nao cria nem altera o esquema Oracle (`ddl-auto=none`). Configure o DDL fornecido pelo grupo antes de iniciar. A especificacao de dominio e o dicionario fisico fornecidos devem ser revisados em conjunto com os responsaveis pelo banco/.NET. A API .NET grava as ocorrencias no Oracle compartilhado; a conta Oracle usada pela Java precisa de permissao somente de leitura nas tabelas de ocorrencia, categoria e municipio.

## Configuracao local

Defina as variaveis de ambiente conforme necessario:

| Variavel | Uso |
|---|---|
| `ORACLE_URL` | URL JDBC, por exemplo `jdbc:oracle:thin:@//localhost:1521/FREEPDB1` |
| `ORACLE_USER`, `ORACLE_PASSWORD` | Usuario Oracle com as permissoes do dominio Java |
| `JWT_PRIVATE_KEY_PATH`, `JWT_PUBLIC_KEY_PATH` | Caminhos de chaves PEM RSA (PKCS#8 e X.509, respectivamente) |
| `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_TTL` | Contrato JWT compartilhado com .NET |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Broker RabbitMQ |
| `BAIRROVIVO_CORS_ALLOWED_ORIGINS` | Origens web permitidas, separadas por virgula; padrao `http://localhost:3000,http://localhost:5173` |
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

O exemplo nao inclui credenciais nem dados seed. O Oracle deve conter as tabelas, views, packages e grants definidos pelo grupo. Para solicitar orientacoes, deve haver ocorrencias inseridas pela API .NET nas tabelas compartilhadas, fontes oficiais aprovadas no Oracle e, para gerar texto, o fornecedor de LLM configurado.

## Usuarios de teste

Nao existem usuarios pre-cadastrados no repositorio e nenhum arquivo contem senhas reais.

| Papel | Como obter acesso | Permissoes |
|---|---|---|
| `CIDADAO` | Chame `POST /api/v1/auth/registrar` com nome, email e senha | Perfil, preferencias, locais favoritos e solicitacao de orientacao |
| `ADMIN` | Defina `BAIRROVIVO_ADMIN_EMAIL` e `BAIRROVIVO_ADMIN_PASSWORD` antes de iniciar a aplicacao | Acesso ao perfil, preferencias e rotas administrativas |

Exemplo de cadastro para obter o papel `CIDADAO`:

```json
{
  "nome": "Usuario Teste",
  "email": "usuario.teste@example.com",
  "senha": "SenhaTeste123!",
  "confirmacaoSenha": "SenhaTeste123!"
}
```

Depois de cadastrar ou autenticar, envie o JWT retornado no cabecalho:

```text
Authorization: Bearer <accessToken>
```

O usuario administrador pode ser criado pelas variaveis de ambiente descritas acima. A senha precisa ter pelo menos 12 caracteres.

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

## Arquitetura

O projeto utiliza uma arquitetura orientada a eventos para o cadastro de usuarios, com Spring Application Events e RabbitMQ como transporte:

1. `AuthService` registra o usuario no banco Oracle.
2. A transacao confirma a alteracao.
3. O evento `usuario.cadastrado` e publicado no RabbitMQ.
4. Um consumidor recebe o evento e registra o processamento em log.

Esta implementacao nao usa microservicos. A API Java e uma parte do ecossistema Bairro Vivo, enquanto ocorrencias, votos, fotos, historico e relatorios permanecem sob responsabilidade da API .NET. Os sistemas sao implantados e executados como componentes separados e compartilham o Oracle; a Java le diretamente as tabelas de ocorrencia, categoria e municipio com permissao somente de leitura.

## Orientacao com RAG

O Java consulta `TB_OCORRENCIA`, `TB_CATEGORIA` e `TB_MUNICIPIO` no Oracle compartilhado, recupera ate cinco trechos de `VW_TRECHOS_RAG_APROVADOS`, busca canais com `FL_VERIFICADO = 'S'` e usa Spring AI somente com municipio/categoria cobertos por fontes aprovadas. A ocorrencia e lida sem incluir dados pessoais do autor e ocorrencias marcadas como excluidas nao sao elegiveis. A instrucao do modelo trata a descricao como dado nao confiavel, bloqueia prompt injection e proibe inventar contatos/horarios; uma validacao rejeita numeros de contato, horarios e URLs que nao estejam nas fontes aprovadas. Respostas e fontes sao persistidas via `PKG_BASE_IA.PR_REGISTRAR_ORIENTACAO`; os estados sem fonte ou municipio confirmado nao citam contatos.

O fluxo com modelo fica desabilitado ate `BAIRROVIVO_AI_ENABLED=true` e configuracao de `OPENAI_API_KEY`; se habilitado sem credencial, a inicializacao falha explicitamente. Falhas do modelo retornam erro `503` padronizado e registram estado `ERRO`; nao devolvem uma resposta ficticia. Cada usuario pode solicitar ate cinco orientacoes por minuto por instancia da API; para ambientes com varias instancias, use um limitador compartilhado. Orientacoes sao limitadas ao municipio de Sao Paulo nesta versao, conforme o escopo.

### Contrato de dados compartilhado no Oracle

Para `POST /api/v1/ia/orientacoes`, o cliente Java envia somente `ocorrenciaId`. A Java le `ID_OCORRENCIA`, `TITULO` e `DESCRICAO` de `TB_OCORRENCIA` e resolve categoria (`ID_CATEGORIA`, `NOME`) e municipio (`ID_MUNICIPIO`, `NOME`, `UF`) por juncoes com `TB_CATEGORIA` e `TB_MUNICIPIO`. A ocorrencia precisa existir, ter referencias validas e nao estar marcada como excluida (`FL_EXCLUIDA = 'N'`). A API .NET continua responsavel por gravar e manter os dados de ocorrencia; a Java nao altera essas tabelas.

Conceda a credencial Oracle da Java acesso de leitura somente as tabelas compartilhadas necessarias (`TB_OCORRENCIA`, `TB_CATEGORIA`, `TB_MUNICIPIO`) e mantenha separadas as permissoes de escrita de cada API. Nao ha chamada HTTP da Java para a API .NET no fluxo de orientacao.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes cobrem regras de cadastro/autorizacao, isolamento de favoritos e leitura do contexto da ocorrencia com H2. A validacao integrada ainda depende de Oracle com os grants e tabelas compartilhados, RabbitMQ, chaves e credencial do modelo; esses servicos externos nao estao incluidos no repositorio. A API .NET nao precisa estar em execucao durante a solicitacao Java, desde que os dados da ocorrencia ja estejam gravados no Oracle.

## Implantacao

Nenhum ambiente de producao ou link publico foi fornecido neste repositorio. Portanto, nao existe um link de implantacao ativo documentado no momento.

A aplicacao pode ser empacotada com:

```powershell
.\mvnw.cmd clean package
java -jar target\bairro-vivo-api-0.1.0-SNAPSHOT.jar
```

Antes da implantacao, devem ser configurados no ambiente: Oracle e grants, chaves RSA, RabbitMQ, usuario administrador e, opcionalmente, Spring AI/OpenAI. A API .NET deve gravar as ocorrencias no Oracle compartilhado; ela nao precisa estar acessivel por HTTP para a Java. Chaves privadas, senhas e tokens nao devem ser versionados.

## Uso de IA no desenvolvimento

A IA foi utilizada como ferramenta de apoio ao desenvolvimento e revisao tecnica deste projeto. As responsabilidades finais permanecem com a equipe:

- A IA pode ter auxiliado na estrutura de codigo, testes, documentacao, revisao de erros e proposta de implementacoes.
- O codigo gerado ou sugerido foi tratado como uma proposta, nao como producao automatica sem verificacao.
- A equipe revisou o comportamento, validou os testes disponiveis e manteve a responsabilidade pelas escolhas de arquitetura, seguranca e integracao.
- Spring AI/OpenAI e uma funcionalidade opcional da aplicacao, identificada pelas variaveis `BAIRROVIVO_AI_ENABLED` e `OPENAI_API_KEY`.
- Quando habilitado, o modelo gera orientacoes somente a partir de fontes aprovadas e o projeto valida o resultado antes da persistencia e entrega.
