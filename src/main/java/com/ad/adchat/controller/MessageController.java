package com.ad.adchat.controller;

import com.ad.adchat.model.ChatMessage;
import com.ad.adchat.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final ChatMessageService chatMessageService;

    public MessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getMessages() {
        return ResponseEntity.ok(chatMessageService.getChatHistory());
    }

    @GetMapping("/rooms/{room}/messages")
    public ResponseEntity<List<ChatMessage>> getMessagesByRoom(@PathVariable("room") String room) {
        return ResponseEntity.ok(chatMessageService.getChatHistoryByRoom(room));
    }
}
