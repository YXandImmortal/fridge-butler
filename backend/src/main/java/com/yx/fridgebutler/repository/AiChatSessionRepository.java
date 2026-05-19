package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * AI 聊天会话数据访问层。
 */
@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {

    Optional<AiChatSession> findBySessionIdAndUserIdAndIsDeleted(String sessionId, Long userId, Byte isDeleted);

    List<AiChatSession> findByUserIdAndIsDeletedOrderByLastActiveTimeDesc(Long userId, Byte isDeleted);

    List<AiChatSession> findByLastActiveTimeBeforeAndIsDeleted(Instant time, Byte isDeleted);
}
