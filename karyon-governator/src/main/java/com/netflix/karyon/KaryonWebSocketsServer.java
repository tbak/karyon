package com.netflix.karyon;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.transport.http.websockets.WebSocketsServerBootstrap;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomasz Bak
 */
public class KaryonWebSocketsServer {
    private static final Logger logger = LoggerFactory.getLogger(KaryonWebSocketsServer.class);

    private final Class<?> mainClass;

    public KaryonWebSocketsServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void startAndAwait() throws Exception {
        Injector injector = LifecycleInjector.bootstrap(mainClass);
        TypeLiteral<WebSocketsServerBootstrap<WebSocketFrame, WebSocketFrame>> bootstrapTypeLiteral = new TypeLiteral<WebSocketsServerBootstrap<WebSocketFrame, WebSocketFrame>>() {
        };
        WebSocketsServerBootstrap<WebSocketFrame, WebSocketFrame> serverBootstrap = injector.getInstance(Key.get(bootstrapTypeLiteral));
        serverBootstrap.startServerAndWait();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonWebSocketsServer.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        KaryonWebSocketsServer server;
        try {
            server = new KaryonWebSocketsServer(Class.forName(mainClassName));
            server.startAndAwait();
        } catch (ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
        }
    }

}
