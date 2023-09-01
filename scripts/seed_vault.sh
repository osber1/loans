#!/bin/bash

while true; do
  RESPONSE=$(curl -sSLw "\n%{http_code}" -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @../docker/vault/application.json http://$1/v1/secret/data/application)
  RESPONSE_BODY=$(echo "$RESPONSE" | awk 'NF{print $0}' | head -n -1)
  RESPONSE_CODE=$(echo "$RESPONSE" | awk 'END{print $NF}')

  if [[ $RESPONSE_CODE -eq 200 ]]; then
    echo "Vault seeded."
    break
  else
    echo "Unsuccessful response received. HTTP code: $RESPONSE_CODE, HTTP response: $RESPONSE_BODY"
  fi
  sleep 5
done
