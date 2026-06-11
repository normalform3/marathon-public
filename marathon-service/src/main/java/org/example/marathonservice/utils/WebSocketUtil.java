package org.example.marathonservice.utils;

import org.example.marathonservice.server.NoticeWebSocket;


public class WebSocketUtil {
    public static void sendNotification(Long userId, String message) {
        NoticeWebSocket.sendToUser(userId, message);
    }
}
