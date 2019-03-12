package com.example.demo.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.proto.chat.ChatRoomGrpc.ChatRoomImplBase;
import com.example.demo.proto.chat.Message;
import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatRoomImpl extends ChatRoomImplBase {

    private Set<StreamObserver<Message>> users = ConcurrentHashMap.newKeySet();

    @Override
    public StreamObserver<Message> join(StreamObserver<Message> responseObserver) {

        users.add(responseObserver);

        return new StreamObserver<Message>() {

            @Override
            public void onNext(Message request) {

                log.info(">>[{}] {}: {}", request.getFrom(), request.getUsername(), request.getMessage());

                Message response = Message.newBuilder()
                        .setUsername(request.getUsername())
                        .setMessage(request.getMessage())
                        .setFrom("Server")
                        .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                        .build();

                users.stream()
                        .filter(user -> user != responseObserver)
                        .forEach(user -> user.onNext(response));

            }

            @Override
            public void onError(Throwable t) {
                log.error("Error from client", t);
                users.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                log.error("Client disconnect");
                users.remove(responseObserver);
            }

        };
    }

    public void sayHiToAllUsers() {

        Message response = Message.newBuilder()
                .setUsername("Admin")
                .setMessage("Welcome everyone")
                .setFrom("Server")
                .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                .build();

        users.forEach(user -> user.onNext(response));

    }
}
