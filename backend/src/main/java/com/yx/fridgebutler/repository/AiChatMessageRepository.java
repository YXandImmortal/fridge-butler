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

    /**
     * 根据会话ID查询消息列表，按创建时间升序排列。
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<AiChatMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    /**
     * 根据会话ID列表批量删除消息。
     *
     * @param sessionIds 会话ID集合
     */
    void deleteBySessionIdIn(Collection<String> sessionIds);
}
