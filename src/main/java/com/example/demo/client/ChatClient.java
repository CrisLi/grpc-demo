package com.example.demo.client;

import java.util.concurrent.CountDownLatch;

import com.example.demo.proto.chat.ChatRoomGrpc;
import com.example.demo.proto.chat.Message;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatClient {

    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8085)
                .usePlaintext()
                .build();

        ChatRoomGrpc.ChatRoomStub stub = ChatRoomGrpc.newStub(channel);

        join("Chris", stub);

        latch.await();

    }

    private static void join(String username, ChatRoomGrpc.ChatRoomStub stub) throws InterruptedException {

        final StreamObserver<Message> requestObserver = stub.join(new StreamObserver<Message>() {

            @Override
            public void onNext(Message value) {
                log.info("<<[{}] {}: {} ", value.getFrom(), value.getUsername(), value.getMessage());
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

        });

        Message message = Message.newBuilder()
                .setUsername(username)
                .setMessage("Hi there, I'm " + username)
                .setFrom("Client")
                .build();

        requestObserver.onNext(message);

        Thread.sleep(5_000);

        message = Message.newBuilder()
                .setUsername(username)
                .setMessage("I'm still there")
                .setFrom("Client")
                .build();

        requestObserver.onNext(message);
    }
}
