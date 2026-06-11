package org.example.marathonservice.server;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{userId}")
public class NoticeWebSocket {

    // 保存所有客户端连接（userId -> session）
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();

    private Long userId;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.userId = userId;
        sessionMap.put(userId, session);
        System.out.println("WebSocket opened for user: " + userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message from user " + userId + ": " + message);
        // 可选：处理客户端消息
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(userId);
        System.out.println("WebSocket closed for user: " + userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error for user " + userId + ": " + throwable.getMessage());
    }

    // 发送消息给特定用户
    public static void sendToUser(Long userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
