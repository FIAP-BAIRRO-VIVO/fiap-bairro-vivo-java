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
