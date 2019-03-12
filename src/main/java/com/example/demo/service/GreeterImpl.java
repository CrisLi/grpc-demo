package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.proto.greeting.GreeterGrpc.GreeterImplBase;
import com.example.demo.proto.greeting.HelloReply;
import com.example.demo.proto.greeting.HelloRequest;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GreeterImpl extends GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello " + request.getName())
                .build();

        log.info("sayHello {}", request.getName());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloAgain(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello again " + request.getName())
                .build();

        log.info("sayHelloAgain {}", request.getName());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
