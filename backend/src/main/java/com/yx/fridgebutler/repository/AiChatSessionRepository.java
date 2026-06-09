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

    /**
     * 根据会话ID、用户ID和删除状态查询会话。
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param isDeleted 删除状态（0=未删除，1=已删除）
     * @return 符合条件的会话Optional对象
     */
    Optional<AiChatSession> findBySessionIdAndUserIdAndIsDeleted(String sessionId, Long userId, Byte isDeleted);

    /**
     * 根据会话ID查询会话。
     *
     * @param sessionId 会话ID
     * @return 符合条件的会话Optional对象
     */
    Optional<AiChatSession> findBySessionId(String sessionId);

    /**
     * 根据用户ID和删除状态查询会话列表，按最后活跃时间降序排列。
     *
     * @param userId    用户ID
     * @param isDeleted 删除状态（0=未删除，1=已删除）
     * @return 会话列表
     */
    List<AiChatSession> findByUserIdAndIsDeletedOrderByLastActiveTimeDesc(Long userId, Byte isDeleted);

    /**
     * 查询最后活跃时间早于指定时间且处于指定删除状态的会话列表。
     *
     * @param time      时间阈值
     * @param isDeleted 删除状态（0=未删除，1=已删除）
     * @return 符合条件的会话列表
     */
    List<AiChatSession> findByLastActiveTimeBeforeAndIsDeleted(Instant time, Byte isDeleted);
}
