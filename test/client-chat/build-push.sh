#!/bin/bash

docker build -t client-chat .

aws ecr get-login-password --region us-east-2 --profile ariel | docker login --username AWS --password-stdin 806721437369.dkr.ecr.us-east-2.amazonaws.com
docker tag client-chat:latest 806721437369.dkr.ecr.us-east-2.amazonaws.com/client-chat:latest
docker push 806721437369.dkr.ecr.us-east-2.amazonaws.com/client-chat:latest
