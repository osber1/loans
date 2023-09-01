#!/bin/bash

while true; do
  RESPONSE=$(curl -sSLw "\n%{http_code}" -H "kbn-xsrf: ''" -H "Content-Type: application/json" -X POST -d @../docker/kibana/$2 http://$1/api/data_views/data_view)
  RESPONSE_BODY=$(echo "$RESPONSE" | awk 'NF{print $0}' | head -n -1)
  RESPONSE_CODE=$(echo "$RESPONSE" | awk 'END{print $NF}')

  if [[ $RESPONSE_BODY == *"Duplicate data view"* ]] || [[ $RESPONSE_CODE -eq 200 ]]; then
    echo "Kibana data view created."
    break
  else
    echo "Unsuccessful response received. HTTP code: $RESPONSE_CODE, HTTP response: $RESPONSE_BODY"
  fi
  sleep 20
done
