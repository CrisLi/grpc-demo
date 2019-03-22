package com.example.demo;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.example.demo.service.ChatRoomImpl;
import com.example.demo.service.GreeterImpl;
import com.example.demo.service.NotificationImpl;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAdminServer
@Slf4j
public class Application implements CommandLineRunner, DisposableBean {

    private Server server;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        int port = 8085;

        server = ServerBuilder.forPort(port)
                .addService(applicationContext.getBean(GreeterImpl.class))
                .addService(applicationContext.getBean(NotificationImpl.class))
                .addService(applicationContext.getBean(ChatRoomImpl.class))
                .build()
                .start();

        log.info("gRpc server start at port {}", port);

    }

    @Override
    public void destroy() throws Exception {
        log.info("gRpc server is shuting down");
        if (server != null) {
            server.shutdown();
        }
    }

}
