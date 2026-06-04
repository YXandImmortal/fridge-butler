package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.admin.AdminLogQueryRequest;
import com.yx.fridgebutler.entity.SysOperLog;
import com.yx.fridgebutler.repository.SysOperLogRepository;
import com.yx.fridgebutler.service.AdminLogService;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.admin.AdminLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 管理员操作日志服务实现类
 */
@Slf4j
@Service
public class AdminLogServiceImpl implements AdminLogService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SysOperLogRepository sysOperLogRepository;

    /**
     * {@inheritDoc}
     * <p>支持按用户名、URI、状态码、日期范围筛选。</p>
     */
    @Override
    public PageResult<AdminLogVO> getLogList(AdminLogQueryRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page - 1, size);

        // 解析日期范围
        Instant startTime = parseDateStart(request.getStartDate());
        Instant endTime = parseDateEnd(request.getEndDate());

        // 状态码范围转换：200→200~299, 400→400~499, 500→500~599
        Integer statusStart = null;
        Integer statusEnd = null;
        if (request.getStatusCode() != null) {
            statusStart = request.getStatusCode();
            statusEnd = request.getStatusCode() + 99;
        }

        Page<SysOperLog> logPage = sysOperLogRepository.findByConditions(
                request.getKeyword(),
                request.getMethod(),
                statusStart,
                statusEnd,
                startTime,
                endTime,
                pageable
        );

        return PageResult.of(logPage.map(this::convertToVO));
    }

    /**
     * 将 SysOperLog 实体转换为 AdminLogVO
     *
     * @param operLog 操作日志实体
     * @return 操作日志 VO
     */
    private AdminLogVO convertToVO(SysOperLog operLog) {
        return AdminLogVO.builder()
                .id(operLog.getId())
                .traceId(operLog.getTraceId())
                .username(operLog.getUsername())
                .method(operLog.getMethod())
                .uri(operLog.getUri())
                .ip(operLog.getIp())
                .params(operLog.getParams())
                .statusCode(operLog.getStatusCode())
                .durationMs(operLog.getDurationMs())
                .errorMsg(operLog.getErrorMsg())
                .createTime(operLog.getCreateTime() != null
                        ? operLog.getCreateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }

    /**
     * 解析开始日期（当天 00:00:00）
     *
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return Instant 或 null
     */
    private Instant parseDateStart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析结束日期（当天 23:59:59）
     *
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return Instant 或 null
     */
    private Instant parseDateEnd(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.atTime(23, 59, 59).atZone(ZONE_ID_SHANGHAI).toInstant();
        } catch (Exception e) {
            return null;
        }
    }
}
