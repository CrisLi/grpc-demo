package com.example.demo.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import com.example.demo.proto.notification.Message;
import com.example.demo.proto.notification.NotificationGrpc.NotificationImplBase;
import com.example.demo.proto.notification.User;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class NotificationImpl extends NotificationImplBase implements DisposableBean {

    private Map<User, ServerCallStreamObserver<Message>> connections = new ConcurrentHashMap<>();

    private Disposable pingDisposable;

    @Override
    public void subscribe(User request, StreamObserver<Message> responseObserver) {

        ServerCallStreamObserver<Message> connection = (ServerCallStreamObserver<Message>) responseObserver;

        connections.put(request, connection);

        connection.setOnCancelHandler(() -> {
            log.info("User [{}] has disconnected", request.getUsername());
            connections.remove(request);
        });

        Message message = Message.newBuilder()
                .setToken(request.getToken())
                .setPayload("connected")
                .build();

        responseObserver.onNext(message);

        log.info("User [{}] connected", request.getUsername());
    }

    public int push() {

        if (connections.isEmpty()) {
            return 0;
        }

        connections.entrySet().parallelStream().forEach(entry -> pushToOneUser(entry.getKey(), entry.getValue()));

        log.info("Push to {} users(s)", connections.size());

        return connections.size();
    }

    public List<User> getConnectedUsers() {
        return new ArrayList<>(connections.keySet());
    }

    public synchronized void startPing(int interval) {

        if (pingDisposable != null) {
            pingDisposable.dispose();
        }

        pingDisposable = Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(interval)).subscribe(i -> push());

        log.info("Start ping with interval {} second(s)", interval);
    }

    public synchronized void stopPing() {

        if (pingDisposable != null) {
            pingDisposable.dispose();
            pingDisposable = null;
        }

        log.info("Stop ping");
    }

    private void pushToOneUser(User user, ServerCallStreamObserver<Message> stream) {

        if (stream.isCancelled()) {
            log.info("User [{}] has disconnected", user.getUsername());
            connections.remove(user);
            return;
        }

        String payload = "Ping " + user.getUsername();

        Message message = Message.newBuilder()
                .setToken(user.getToken())
                .setPayload(payload)
                .build();

        stream.onNext(message);

        log.info(payload);
    }

    @Override
    public void destroy() throws Exception {
        connections.values().stream()
                .filter(stream -> !stream.isCancelled())
                .forEach(stream -> stream.onCompleted());
    }
}
