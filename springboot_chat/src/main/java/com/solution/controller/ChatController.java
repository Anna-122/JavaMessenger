package com.solution.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.solution.domain.Message;

@Controller
public class ChatController {

    @MessageMapping("/message")
    @SendTo("/chat/messages")
    public Message getMessages(Message message) {
        System.out.println(message);
        return message;
    }
}
