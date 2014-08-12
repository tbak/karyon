package com.netflix.karyon.transport.http.websockets;

import java.lang.reflect.ParameterizedType;

import com.google.inject.util.Types;
import com.netflix.karyon.transport.AbstractRxNettyModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerBuilder;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
public abstract class WebSocketsRxNettyModule<I extends WebSocketFrame, O extends WebSocketFrame>
        extends AbstractRxNettyModule<I, O, WebSocketServerBuilder<I, O>, WebSocketServerMetricsEvent<EventType>> {

    private static final ParameterizedType SERVER_METRICS_EVENT_TYPE = Types.newParameterizedType(WebSocketServerMetricsEvent.class, EventType.class);

    protected WebSocketsRxNettyModule(Class<I> iType, Class<O> oType) {
        super(Types.newParameterizedType(WebSocketServerBuilder.class, iType, oType),
                Types.newParameterizedType(WebSocketsServerBootstrap.class, iType, oType),
                Types.newParameterizedType(MetricEventsListenerFactory.class, iType, oType, SERVER_METRICS_EVENT_TYPE));
    }

    @Override
    protected WebSocketServerBuilder<I, O> newServerBuilder(int port, ConnectionHandler<I, O> connectionHandler) {
        return RxNetty.newWebSocketServerBuilder(port, connectionHandler);
    }
}
