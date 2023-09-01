#!/bin/bash

VERSION="1.0-SNAPSHOT"

minikube delete

sudo ./hosts_remover.sh /etc/hosts
#kill $(ps aux | grep minikube | awk '{print $2}')