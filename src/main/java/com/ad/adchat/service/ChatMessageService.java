package com.ad.adchat.service;

import com.ad.adchat.model.ChatMessage;
import com.ad.adchat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistory() {
        List<ChatMessage> history = chatMessageRepository.findTop100ByOrderByTimestampDesc();
        Collections.reverse(history); // To display in chronological order
        return history;
    }

    public List<ChatMessage> getChatHistoryByRoom(String room) {
        List<ChatMessage> history = chatMessageRepository.findTop100ByRoomOrderByTimestampDesc(room);
        Collections.reverse(history);
        return history;
    }
}
