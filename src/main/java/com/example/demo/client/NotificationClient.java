package com.example.demo.client;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.example.demo.proto.notification.Message;
import com.example.demo.proto.notification.NotificationGrpc;
import com.example.demo.proto.notification.User;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationClient {

    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8085)
                .usePlaintext()
                .build();

        NotificationGrpc.NotificationStub stub = NotificationGrpc.newStub(channel);

        subscribe(stub);

        latch.await();

    }

    private static void subscribe(NotificationGrpc.NotificationStub stub) {

        User request = User.newBuilder()
                .setUsername("Chris")
                .setToken(UUID.randomUUID().toString())
                .build();

        StreamObserver<Message> responseObserver = new StreamObserver<Message>() {

            @Override
            public void onNext(Message value) {
                log.info("Message from server: {}", value.getPayload());
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error from server", t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Disconnect from server");
                latch.countDown();
            }

        };

        stub.subscribe(request, responseObserver);

    }
}
