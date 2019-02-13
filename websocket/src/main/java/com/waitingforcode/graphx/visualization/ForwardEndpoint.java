package com.waitingforcode.graphx.visualization;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ClientEndpoint
@ServerEndpoint(value="/forward")
public class ForwardEndpoint {

    @OnOpen
    public void onWebSocketConnect(Session connectedSession) throws IOException {
        System.out.println("New forwarder connected:"+connectedSession);
        Forwarder.INSTANCE.subscribe(connectedSession);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        System.out.println("FORWARD Socket Closed: " + reason);
        Forwarder.INSTANCE.filterOutClosedSessions();
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
        Forwarder.INSTANCE.filterOutClosedSessions();
    }
}
