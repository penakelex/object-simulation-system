module org.penakelex.server {
    requires org.penakelex.shared;
    requires spring.boot;
    requires spring.context;
    requires java.base;
    requires jakarta.annotation;
    requires spring.boot.autoconfigure;

    exports org.penakelex.server.service;
    opens org.penakelex.server to spring.core;
    exports org.penakelex.server;
}