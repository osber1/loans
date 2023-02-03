#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel
docker tag risk-checker:$VERSION osvasldas97/risk-checker:$VERSION
docker tag back-office:$VERSION osvasldas97/back-office:$VERSION
docker tag notification-service:$VERSION osvasldas97/notification-service:$VERSION
docker push osvasldas97/risk-checker:$VERSION
docker push osvasldas97/back-office:$VERSION
docker push osvasldas97/notification-service:$VERSION

# Uncomment only running for the first time
minikube start --memory 16384 --cpus=4 --driver=kvm2
#istioctl install -y

kubectl apply -f k8s
#kubectl apply -f k8s/istio

# Seed Vault
MINIKUBE_IP=`minikube ip`
VAULT_PORT=`kubectl get service vault -n loans -o jsonpath='{.spec.ports[0].nodePort}'`

while true; do
  RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @vault/application.json http://$MINIKUBE_IP:$VAULT_PORT/v1/secret/data/application)
  if [ $RESPONSE_CODE -eq 200 ]; then
    kubectl apply -f k8s/services
    break
  else
    echo "Unsuccessful response received (HTTP code $RESPONSE_CODE)"
  fi
  sleep 5
done

minikube service list