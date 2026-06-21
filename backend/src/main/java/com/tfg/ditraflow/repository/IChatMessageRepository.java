package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.ChatMessage;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByTimestampDesc(User user);
    List<ChatMessage> findByUserOrderByTimestamp(User user);
    List<ChatMessage> findByUserAndRelatedResultOrderByTimestampDesc(User user, Result result);
}
