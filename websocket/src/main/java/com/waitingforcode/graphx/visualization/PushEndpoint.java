package com.waitingforcode.graphx.visualization;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ClientEndpoint
@ServerEndpoint(value="/push")
public class PushEndpoint {

    @OnOpen
    public void onWebSocketConnect(Session session) throws IOException {
        System.out.println("PUSH >>> New session connected: " + session);
    }

    @OnMessage
    public void onWebSocketText(String message)
    {
        Forwarder.INSTANCE.forwardToSubscribers(message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("PUSH Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}
