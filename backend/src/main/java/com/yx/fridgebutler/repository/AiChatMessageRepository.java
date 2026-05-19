package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * AI 聊天消息数据访问层。
 */
@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    void deleteBySessionIdIn(Collection<String> sessionIds);
}
