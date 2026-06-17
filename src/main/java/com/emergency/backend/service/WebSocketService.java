package com.emergency.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.emergency.backend.config.CallDTO;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendCallToUser(String userId, CallDTO callDTO) {
        messagingTemplate.convertAndSendToUser(
            userId,   // 🔥 ต้องเป็น "1"
            "/queue/call",
            callDTO
        );
    }
}