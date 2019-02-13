package com.waitingforcode.graphx.visualization;


import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public enum Forwarder {

    INSTANCE;

    private List<Session> sessions = new ArrayList<>();

    public void subscribe(Session connectedSession) {
        sessions.add(connectedSession);
    }

    public void forwardToSubscribers(String message) {
        sessions.stream().forEach(connectedSession -> {
            try {
                synchronized (connectedSession) {
                    connectedSession.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void filterOutClosedSessions() {
        sessions = sessions.stream().filter(session -> session.isOpen()).collect(Collectors.toList());
    }

}
