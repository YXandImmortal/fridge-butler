package com.yx.fridgebutler.job;

import com.yx.fridgebutler.entity.AiChatSession;
import com.yx.fridgebutler.repository.AiChatMessageRepository;
import com.yx.fridgebutler.repository.AiChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * AI 聊天历史清理定时任务。
 * <p>两阶段清理策略：</p>
 * <ol>
 *   <li>软删除：将超期会话标记为已删除（前端不可见）</li>
 *   <li>物理删除：彻底删除已软删一段时间的数据，释放存储空间</li>
 * </ol>
 */
@Slf4j
@Component
public class AIChatHistoryCleanupJob {

    @Autowired
    private AiChatSessionRepository sessionRepository;

    @Autowired
    private AiChatMessageRepository messageRepository;

    @Value("${ai.history.retention-days:7}")
    private int retentionDays;

    @Value("${ai.history.purge-days:30}")
    private int purgeDays;

    /**
     * 每天凌晨 3:00 执行：软删除超期会话。
     * <p>将 last_active_time 早于 retentionDays 的会话标记为已删除。</p>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void softDeleteExpiredSessions() {
        Instant cutoff = Instant.now().minusSeconds((long) retentionDays * 24 * 3600);
        List<AiChatSession> expiredSessions = sessionRepository
                .findByLastActiveTimeBeforeAndIsDeleted(cutoff, (byte) 0);

        if (expiredSessions.isEmpty()) {
            log.debug("无超期 AI 会话需要软删除");
            return;
        }

        for (AiChatSession session : expiredSessions) {
            session.setIsDeleted((byte) 1);
        }
        sessionRepository.saveAll(expiredSessions);
        log.info("软删除超期 AI 会话 {} 条，cutoff={}", expiredSessions.size(), cutoff);
    }

    /**
     * 每天凌晨 4:00 执行：物理删除已软删的数据。
     * <p>将已软删且 last_active_time 早于 purgeDays 的会话及其消息彻底删除。</p>
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void purgeSoftDeletedData() {
        Instant cutoff = Instant.now().minusSeconds((long) purgeDays * 24 * 3600);
        List<AiChatSession> sessionsToPurge = sessionRepository
                .findByLastActiveTimeBeforeAndIsDeleted(cutoff, (byte) 1);

        if (sessionsToPurge.isEmpty()) {
            log.debug("无已软删 AI 会话需要物理删除");
            return;
        }

        List<String> sessionIds = sessionsToPurge.stream()
                .map(AiChatSession::getSessionId)
                .toList();

        messageRepository.deleteBySessionIdIn(sessionIds);
        sessionRepository.deleteAll(sessionsToPurge);
        log.info("物理删除已软删 AI 会话 {} 条及其消息", sessionsToPurge.size());
    }
}
