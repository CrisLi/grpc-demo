package com.example.demo.client;

import com.example.demo.proto.greeting.GreeterGrpc;
import com.example.demo.proto.greeting.HelloReply;
import com.example.demo.proto.greeting.HelloRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreetingClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8085)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);

        HelloRequest request = HelloRequest.newBuilder()
                .setName("Chris")
                .build();

        HelloReply response = blockingStub.sayHello(request);

        String message = response.getMessage();

        log.info("sayHello response: {}", message);

        response = blockingStub.sayHelloAgain(request);

        message = response.getMessage();

        log.info("sayHelloAgain response: {}", message);
    }
}
