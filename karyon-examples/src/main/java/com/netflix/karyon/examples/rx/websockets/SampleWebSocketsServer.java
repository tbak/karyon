package com.netflix.karyon.examples.rx.websockets;

import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.examples.rx.websockets.SampleWebSocketsServer.SampleWebSocketsModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import com.netflix.karyon.transport.MetricEventsListenerFactory.WebSocketsEventsListenerFactory;
import com.netflix.karyon.transport.http.websockets.WebSocketsRxNettyModule;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent.EventType;
import rx.Observable;
import rx.functions.Func1;

import static java.lang.String.*;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "sample-rxnetty-websockets-noss", healthcheck = HealthCheck.class)
@Modules(include = {
        SampleWebSocketsModule.class
        // Uncomment the following line to enable eureka. Make sure eureka-client.properties is configured to point to your eureka server.
        //, KaryonEurekaModule.class
})
public class SampleWebSocketsServer {

    public static final int DEFAULT_SERVER_PORT = 8888;

    public static class SampleWebSocketsModule extends WebSocketsRxNettyModule<WebSocketFrame, WebSocketFrame> {

        public SampleWebSocketsModule() {
            super(WebSocketFrame.class, WebSocketFrame.class);
        }

        @Override
        public int serverPort() {
            return DEFAULT_SERVER_PORT;
        }

        @Override
        public int shutdownPort() {
            return 8899;
        }

        @Override
        public ConnectionHandler<WebSocketFrame, WebSocketFrame> connectionHandler() {
            final AtomicInteger counter = new AtomicInteger();
            return new ConnectionHandler<WebSocketFrame, WebSocketFrame>() {
                @Override
                public Observable<Void> handle(final ObservableConnection<WebSocketFrame, WebSocketFrame> connection) {
                    System.out.println("Got new connection");
                    return connection.getInput().flatMap(new Func1<WebSocketFrame, Observable<Void>>() {
                        @Override
                        public Observable<Void> call(WebSocketFrame frame) {
                            counter.incrementAndGet();
                            System.out.println(format("Received frame no %s: %s", counter.get(), ((TextWebSocketFrame) frame).text()));
                            return connection.writeAndFlush(new TextWebSocketFrame(format("Frame no %s received - give me more", counter.get())));
                        }
                    });
                }
            };
        }

        @Override
        public MetricEventsListenerFactory<WebSocketFrame, WebSocketFrame, WebSocketServerMetricsEvent<EventType>> metricsEventsListenerFactory() {
            return new WebSocketsEventsListenerFactory<WebSocketFrame, WebSocketFrame>();
        }
    }
}
