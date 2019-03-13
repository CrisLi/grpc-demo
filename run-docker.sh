#!/bin/bash

docker run -d -p 8080:8080 -p 8085:8085 --name grpc-demo --rm -v $(pwd)/logs:/usr/app/logs grpc-demo
