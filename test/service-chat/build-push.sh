#!/bin/bash

docker build -t service-chat .

aws ecr get-login-password --region us-east-2 --profile ariel | docker login --username AWS --password-stdin 806721437369.dkr.ecr.us-east-2.amazonaws.com
docker tag service-chat:latest 806721437369.dkr.ecr.us-east-2.amazonaws.com/service-chat:latest
docker push 806721437369.dkr.ecr.us-east-2.amazonaws.com/service-chat:latest
