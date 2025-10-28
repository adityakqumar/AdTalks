package com.ad.adchat.controller;

import com.ad.adchat.model.ChatMessage;
import com.ad.adchat.service.ChatMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added for logging
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j // Added for logging
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    // Track connected users per room
    private static final Map<String, Set<String>> connectedUsers = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(); // For serializing the user list

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String username = principal.getName();
        chatMessage.setSender(username); // Set sender from authenticated principal
        chatMessage.setType(ChatMessage.MessageType.CHAT); // Ensure type is CHAT
        String room = chatMessage.getRoom() == null || chatMessage.getRoom().isBlank() ? "public" : chatMessage.getRoom();
        chatMessage.setRoom(room);

        // Save the CHAT message to the database
        ChatMessage savedMessage = chatMessageService.saveMessage(chatMessage);

        // Broadcast the saved message to room
        messagingTemplate.convertAndSend("/topic/rooms/" + room, savedMessage);
    }

    @MessageMapping("/chat.addUser") // This single endpoint handles JOIN and LEAVE per room
    public void addUser(@Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor,
                        Principal principal) {

        String username = principal.getName();
        if(username == null) {
            log.error("Principal name is null, cannot process user event.") ;
            return;
        }

        // Set sender from principal for security, overriding payload
        chatMessage.setSender(username);
        String room = chatMessage.getRoom() == null || chatMessage.getRoom().isBlank() ? "public" : chatMessage.getRoom();
        chatMessage.setRoom(room);

        // --- THIS IS THE FIX ---
        // Check the message type to see if user is joining or leaving
        if (chatMessage.getType() == ChatMessage.MessageType.JOIN) {
            // Add username in web socket session and our user set
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("username", username);
                headerAccessor.getSessionAttributes().put("room", room);
            }
            connectedUsers.computeIfAbsent(room, r -> new HashSet<>()).add(username);
            log.info("User JOIN: {} room: {}. Users in room: {}", username, room, connectedUsers.get(room).size());
        } else if (chatMessage.getType() == ChatMessage.MessageType.LEAVE) {
            // Remove user from our set
            Set<String> users = connectedUsers.computeIfAbsent(room, r -> new HashSet<>());
            users.remove(username);
            log.info("User LEAVE: {} room: {}. Users in room: {}", username, room, users.size());
        } else {
            log.warn("Unknown message type received at addUser endpoint: {}", chatMessage.getType());
            return; // Don't process unknown types
        }
        // --- End of Fix ---

        try {
            // Store the *current* user list as a JSON string in the content
            String userListJson = objectMapper.writeValueAsString(connectedUsers.getOrDefault(room, new HashSet<>()));
            chatMessage.setContent(userListJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing user list", e);
            chatMessage.setContent("[]"); // Send empty list on error
        }

        // Save the JOIN/LEAVE message to the database
        ChatMessage savedMessage = chatMessageService.saveMessage(chatMessage);

        // Broadcast the saved JOIN/LEAVE message to room
        messagingTemplate.convertAndSend("/topic/rooms/" + room, savedMessage);
    }

    // Public method to be called by WebSocketEventListener
    public static Set<String> getConnectedUsers(String room) {
        return connectedUsers.getOrDefault(room, new HashSet<>());
    }
}

