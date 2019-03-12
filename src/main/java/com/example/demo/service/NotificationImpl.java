package com.example.demo.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.proto.notification.Message;
import com.example.demo.proto.notification.NotificationGrpc.NotificationImplBase;
import com.example.demo.proto.notification.User;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationImpl extends NotificationImplBase {

    private Map<String, StreamObserver<Message>> connections = new ConcurrentHashMap<>();

    @Override
    public void subscribe(User request, StreamObserver<Message> responseObserver) {

        connections.put(request.getToken(), responseObserver);

        String payload = request.getUsername() + " connected";

        Message message = Message.newBuilder()
                .setToken(request.getToken())
                .setPayload(payload)
                .build();

        responseObserver.onNext(message);

        log.info(payload);
    }

    public void push() {

        Set<String> disconnecteds = new HashSet<>();

        connections.forEach((token, stream) -> {

            String payload = "ping " + token;

            Message message = Message.newBuilder()
                    .setToken(token)
                    .setPayload(payload)
                    .build();

            try {

                stream.onNext(message);

                log.info(payload);

            } catch (StatusRuntimeException e) {

                log.error("Client [" + token + "] error", e);

                disconnecteds.add(token);
            }

        });

        if (!disconnecteds.isEmpty()) {
            disconnecteds.forEach(connections::remove);
        }

        log.info("{} clients connected", connections.size());
    }
}
