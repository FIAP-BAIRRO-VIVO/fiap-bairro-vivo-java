ORACLE_URL="jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL"
ORACLE_USER=rm564870
ORACLE_PASSWORD=170201
RABBITMQ_USER=rm564870
RABBITMQ_PASSWORD=170201
JWT_ISSUER=bairro-vivo-java
JWT_AUDIENCE=bairro-vivo-apis
JWT_TTL=PT1H
DOTNET_API_BASE_URL=https://fiap-bairro-vivo-csharp.onrender.com
DOTNET_OCCURRENCE_PATH="/api/v1/ocorrencias/{id}"
DOTNET_SERVICE_TOKEN=xhqbeAnYZKh2BeZleiAzXHdAz-Um8YzJTc3PW7-DLo0
OPENAI_API_KEY=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
OPENAI_MODEL=gpt-4o-mini

RG=rg-bairro-vivo
LOC=brazilsouth
PLAN=plan-bairro-vivo
APP=bairro-vivo-java-566385
MQ=rabbitmq-bairro-vivo
MQ_DNS=bairro-vivo-rabbit-566385

az group create -n $RG -l $LOC
az appservice plan create -n $PLAN -g $RG --is-linux --sku B1
az webapp create -n $APP -g $RG --plan $PLAN --runtime "JAVA:17-java17"

az provider register -n Microsoft.ContainerInstance --wait
az container create -g $RG -n $MQ -l $LOC \
  --image rabbitmq:3-management --os-type Linux --cpu 1 --memory 1.5 \
  --dns-name-label $MQ_DNS --ports 5672 15672 \
  --environment-variables RABBITMQ_DEFAULT_USER="$RABBITMQ_USER" \
  --secure-environment-variables RABBITMQ_DEFAULT_PASS="$RABBITMQ_PASSWORD"

az webapp config appsettings set -n $APP -g $RG --settings \
  ORACLE_URL="$ORACLE_URL" \
  ORACLE_USER="$ORACLE_USER" \
  ORACLE_PASSWORD="$ORACLE_PASSWORD" \
  JWT_ISSUER="$JWT_ISSUER" \
  JWT_AUDIENCE="$JWT_AUDIENCE" \
  JWT_TTL="$JWT_TTL" \
  JWT_PRIVATE_KEY_PATH=/home/secrets/jwt-private.pem \
  JWT_PUBLIC_KEY_PATH=/home/secrets/jwt-public.pem \
  RABBITMQ_HOST="$MQ_DNS.$LOC.azurecontainer.io" \
  RABBITMQ_PORT=5672 \
  RABBITMQ_USER="$RABBITMQ_USER" \
  RABBITMQ_PASSWORD="$RABBITMQ_PASSWORD" \
  DOTNET_API_BASE_URL="$DOTNET_API_BASE_URL" \
  DOTNET_OCCURRENCE_PATH="$DOTNET_OCCURRENCE_PATH" \
  DOTNET_SERVICE_TOKEN="$DOTNET_SERVICE_TOKEN" \
  BAIRROVIVO_AI_ENABLED=true \
  OPENAI_API_KEY="$OPENAI_API_KEY" \
  OPENAI_MODEL="$OPENAI_MODEL"
