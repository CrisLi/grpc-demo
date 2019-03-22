package com.example.demo.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import com.example.demo.proto.notification.Message;
import com.example.demo.proto.notification.NotificationGrpc.NotificationImplBase;
import com.example.demo.proto.notification.User;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class NotificationImpl extends NotificationImplBase implements DisposableBean {

    private Map<String, UserConnection> users = new ConcurrentHashMap<>();

    private Disposable pingDisposable;

    @Override
    public void subscribe(User request, StreamObserver<Message> responseObserver) {

        ServerCallStreamObserver<Message> connection = (ServerCallStreamObserver<Message>) responseObserver;

        users.put(request.getToken(), new UserConnection(request, connection));

        connection.setOnCancelHandler(() -> {
            log.info("User [{}] has disconnected", request.getUsername());
            users.remove(request.getToken());
        });

        Message message = Message.newBuilder()
                .setToken(request.getToken())
                .setPayload("connected")
                .build();

        responseObserver.onNext(message);

        log.info("User [{}] connected", request.getUsername());
    }

    public int push() {

        if (users.isEmpty()) {
            return 0;
        }

        users.values().parallelStream().forEach(this::pushToOneUser);

        log.info("Push to {} users(s)", users.size());

        return users.size();
    }

    public boolean pushTo(String token) {

        if (!users.containsKey(token)) {
            return false;
        }

        pushToOneUser(users.get(token));

        return true;
    }

    public List<User> getConnectedUsers() {
        return users.values().stream()
                .map(UserConnection::getUser)
                .collect(Collectors.toList());
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

    private void pushToOneUser(UserConnection userConnection) {

        User user = userConnection.getUser();
        ServerCallStreamObserver<Message> stream = userConnection.getConnection();

        if (stream.isCancelled()) {
            log.info("User [{}] has disconnected", user.getUsername());
            users.remove(user.getToken());
            return;
        }

        String payload = "Ping [" + user.getUsername() + "]";

        Message message = Message.newBuilder()
                .setToken(user.getToken())
                .setPayload(payload)
                .build();

        stream.onNext(message);

        log.info(payload);
    }

    @Override
    public void destroy() throws Exception {
        users.values().stream()
                .map(UserConnection::getConnection)
                .filter(stream -> !stream.isCancelled())
                .forEach(stream -> stream.onCompleted());
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    protected static class UserConnection {

        @NonNull
        private User user;

        @NonNull
        private ServerCallStreamObserver<Message> connection;

    }
}
