package org.penakelex.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public final class ServerApplication {
    static void main(final String[] args)
    throws InterruptedException {
        SpringApplication.run(ServerApplication.class, args);
        new CountDownLatch(1).await();
    }
}