package com.netflix.karyon.transport.http.websockets;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.karyon.transport.AbstractRxServerBootstrap;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import com.netflix.karyon.transport.Ports;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerBuilder;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
public class WebSocketsServerBootstrap<I extends WebSocketFrame, O extends WebSocketFrame>
        extends AbstractRxServerBootstrap<I, O, WebSocketServerBuilder<I, O>, WebSocketServerMetricsEvent<EventType>> {
    @Inject
    public WebSocketsServerBootstrap(Ports ports, Injector injector,
                                     MetricEventsListenerFactory<I, O, WebSocketServerMetricsEvent<EventType>> metricEventsListenerFactory,
                                     WebSocketServerBuilder<I, O> serverBuilder) {
        super(ports, injector, metricEventsListenerFactory, serverBuilder);
    }
}
