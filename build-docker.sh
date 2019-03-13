#!/bin/bash
mvn clean package -Dmaven.test.skip=true
docker build -t grpc-demo . 
