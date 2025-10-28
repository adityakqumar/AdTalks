package com.ad.adchat.repository;

import com.ad.adchat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByOrderByTimestampDesc();
    List<ChatMessage> findTop100ByRoomOrderByTimestampDesc(String room);
}
