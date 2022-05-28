#!/bin/bash

#./mvnw package -Pnative
./mvnw package

docker build -t middleware .

aws ecr get-login-password --region us-east-2 --profile ariel | docker login --username AWS --password-stdin 806721437369.dkr.ecr.us-east-2.amazonaws.com
docker tag middleware:latest 806721437369.dkr.ecr.us-east-2.amazonaws.com/middleware:latest
docker push 806721437369.dkr.ecr.us-east-2.amazonaws.com/middleware:latest
