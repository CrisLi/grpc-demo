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

        String payload = request.getUsername() + " connected";

        Message message = Message.newBuilder()
                .setToken(request.getToken())
                .setPayload(payload)
                .build();

        responseObserver.onNext(message);

        log.info(payload);
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

    public synchronized void startPing() {
        if (pingDisposable == null) {
            pingDisposable = Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(10)).subscribe(i -> push());
        }
    }

    public synchronized void stopPing() {
        if (pingDisposable != null) {
            pingDisposable.dispose();
        }
    }

    private void pushToOneUser(User user, ServerCallStreamObserver<Message> stream) {

        if (!stream.isReady()) {
            log.info("User [{}] connection is not ready", user.getUsername());
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
                .filter(ServerCallStreamObserver::isReady)
                .forEach(stream -> stream.onCompleted());
    }
}
