package com.netflix.karyon.transport;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.metrics.MetricEventsListener;
import io.reactivex.netty.protocol.http.websocket.WebSocketServer;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;
import io.reactivex.netty.servo.ServoEventsListenerFactory;

/**
 * @author Tomasz Bak
 */
public interface MetricEventsListenerFactory<I, O, E extends ServerMetricsEvent<?>> {

    MetricEventsListener<? extends ServerMetricsEvent<?>> createListener(RxServer<I, O> server);

    class TcpMetricEventsListenerFactory<I, O> implements MetricEventsListenerFactory<I, O, ServerMetricsEvent<EventType>> {
        @Override
        public MetricEventsListener<? extends ServerMetricsEvent<?>> createListener(RxServer<I, O> server) {
            return new ServoEventsListenerFactory().forTcpServer(server);
        }
    }

    class WebSocketsEventsListenerFactory<I extends WebSocketFrame, O extends WebSocketFrame>
            implements MetricEventsListenerFactory<I, O, WebSocketServerMetricsEvent<WebSocketServerMetricsEvent.EventType>> {
        @Override
        public MetricEventsListener<? extends ServerMetricsEvent<?>> createListener(RxServer<I, O> server) {
            return new ServoEventsListenerFactory().forWebSocketServer((WebSocketServer) server);
        }
    }
}
