package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.dto.dailytip.DailyTipGenerateResult;
import com.yx.fridgebutler.entity.SysDailyTip;
import com.yx.fridgebutler.enums.DailyTipType;
import com.yx.fridgebutler.repository.DailyTipRepository;
import com.yx.fridgebutler.config.PromptTemplateLoader;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.DailyTipService;
import com.yx.fridgebutler.util.AiResponseUtils;
import com.yx.fridgebutler.vo.dailytip.DailyTipVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 每日小贴士服务实现类。
 */
@Slf4j
@Service
public class DailyTipServiceImpl implements DailyTipService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter WEEKDAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final List<DailyTipType> TIP_TYPES = List.of(DailyTipType.FACT, DailyTipType.TIP, DailyTipType.JOKE, DailyTipType.RIDDLE);

    private static final String SYSTEM_PROMPT = """
            你是"冰箱管家"的每日小贴士生成专家，擅长用轻松有趣的方式向用户传递冰箱相关的冷知识、实用技巧、冷笑话和谜语。

            【生成规则】
            - 冷知识需有科学依据，但表达要通俗有趣；
            - 实用技巧要真实可行，用户可立即操作；
            - 冷笑话要带反转、够"冷"，让人会心一笑；
            - 谜语需与冰箱或食物有关，并附答案。
            - 正文内容控制在80字以内，适合手机通知栏显示。
            - 如遇节日或特殊节气，可巧妙融入冰箱使用场景。
            - Emoji 必须选择1个与内容主题最贴切的表情符号。
            - 标题简短有力，不超过8个字。

            【输出要求】
            - 必须输出纯 JSON，不要 Markdown 代码块，不要任何额外说明。
            - type 字段只能从以下四个值中选择：FACT（冷知识）、TIP（实用技巧）、JOKE（冷笑话）、RIDDLE（谜语）。
            - 非谜语类型，answer 字段必须为空字符串。
            """;

    @Autowired
    private DailyTipRepository dailyTipRepository;

    @Autowired
    private PromptTemplateLoader promptLoader;

    @Autowired
    private DeepSeekService deepSeekService;

    private final Random random = new Random();

    /**
     * {@inheritDoc}
     * <p>优先查询数据库，不存在则实时生成并保存。</p>
     */
    @Override
    public DailyTipVO getTodayTip() {
        LocalDate today = LocalDate.now();
        Optional<SysDailyTip> optional = dailyTipRepository.findByTipDate(today);
        if (optional.isPresent()) {
            log.debug("命中数据库今日小贴士，date={}", today);
            return convertToVO(optional.get());
        }
        log.info("数据库中无今日小贴士，实时调用 AI 生成，date={}", today);
        SysDailyTip tip = generateAndSave(today);
        return convertToVO(tip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DailyTipVO getTipByDate(LocalDate date) {
        return dailyTipRepository.findByTipDate(date)
                .map(this::convertToVO)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     * <p>用于定时任务，仅在不存在时才生成。</p>
     */
    @Override
    public boolean generateTodayTipIfAbsent() {
        LocalDate today = LocalDate.now();
        if (dailyTipRepository.existsByTipDate(today)) {
            log.info("今日小贴士已存在，跳过生成，date={}", today);
            return false;
        }
        generateAndSave(today);
        log.info("定时任务成功生成今日小贴士，date={}", today);
        return true;
    }

    /**
     * 调用 DeepSeek 生成小贴士并保存到数据库。
     * <p>首次调用 AI 后，如果 content 为空或解析失败，会自动重试一次；
     * 两次均失败则使用兜底内容。</p>
     *
     * @param date 目标日期
     * @return 保存后的实体
     */
    private SysDailyTip generateAndSave(LocalDate date) {
        DailyTipType type = TIP_TYPES.get(random.nextInt(TIP_TYPES.size()));
        String dateStr = date.format(DATE_FORMATTER);
        String weekday = date.format(WEEKDAY_FORMATTER);

        String userPrompt = String.format("今天是 %s（%s），请生成一条\"%s\"类别的冰箱每日小贴士。",
                dateStr, weekday, type.name());

        String systemPrompt = promptLoader.getPrompt("daily-tip", SYSTEM_PROMPT);

        DailyTipGenerateResult result = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            String response = deepSeekService.chat(systemPrompt, userPrompt);
            log.info("每日小贴士 AI 第{}次响应，date={}：{}", attempt, date, response);
            result = parseResponse(response);
            if (result != null) {
                break;
            }
            log.warn("每日小贴士第{}次生成结果无效，准备重试，date={}", attempt, date);
        }

        if (result == null) {
            log.error("每日小贴士 AI 两次生成均无效，使用兜底内容，date={}", date);
            result = createFallbackResult(date);
        }

        ensureRequiredFields(result);

        SysDailyTip tip = SysDailyTip.builder()
                .tipType(resolveType(result.getType(), type))
                .emoji(truncate(result.getEmoji(), 10))
                .title(truncate(result.getTitle(), 20))
                .content(result.getContent())
                .tipDate(date)
                .answer(result.getAnswer())
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();

        return dailyTipRepository.save(tip);
    }

    /**
     * 解析 DeepSeek 返回的 JSON 响应。
     * <p>仅在 JSON 解析成功且关键字段 {@code content} 非空时返回结果；
     * 否则返回 {@code null}，由上层决定是否重试或使用兜底。</p>
     *
     * @param response 原始 AI 响应
     * @return 解析后的结果；无效时返回 {@code null}
     */
    private DailyTipGenerateResult parseResponse(String response) {
        String cleaned = AiResponseUtils.cleanJsonResponse(response);
        if (cleaned.isBlank()) {
            log.warn("每日小贴士 AI 响应为空或清洗后无内容");
            return null;
        }
        try {
            DailyTipGenerateResult result = JSONUtil.toBean(cleaned, DailyTipGenerateResult.class);
            if (result == null) {
                log.warn("每日小贴士 JSON 解析结果为空，原始响应：{}", response);
                return null;
            }
            if (result.getContent() == null || result.getContent().trim().isEmpty()) {
                log.warn("每日小贴士 AI 返回的 content 为空或缺失，原始响应：{}", response);
                return null;
            }
            return result;
        } catch (Exception e) {
            log.warn("每日小贴士 JSON 解析失败，原始响应：{}", response, e);
            return null;
        }
    }

    /**
     * 创建兜底小贴士结果。
     *
     * @param date 目标日期
     * @return 兜底结果
     */
    private DailyTipGenerateResult createFallbackResult(LocalDate date) {
        DailyTipGenerateResult fallback = new DailyTipGenerateResult();
        fallback.setType("FACT");
        fallback.setEmoji("🧊");
        fallback.setTitle("冰箱小贴士");
        fallback.setContent("冰箱门不要频繁开关，每次开门冷气会流失约30%，既费电又影响保鲜效果。");
        fallback.setDate(date.format(DATE_FORMATTER));
        fallback.setAnswer("");
        return fallback;
    }

    /**
     * 确保关键字段（title、emoji、answer）不为空，避免前端展示异常。
     *
     * @param result AI 生成结果
     */
    private void ensureRequiredFields(DailyTipGenerateResult result) {
        if (result.getTitle() == null || result.getTitle().trim().isEmpty()) {
            result.setTitle("冰箱小贴士");
        }
        if (result.getEmoji() == null || result.getEmoji().trim().isEmpty()) {
            result.setEmoji("🧊");
        }
        if (result.getAnswer() == null) {
            result.setAnswer("");
        }
    }

    /**
     * 解析并校验类型，解析失败则使用后端随机选中的类型兜底。
     */
    private DailyTipType resolveType(String typeStr, DailyTipType fallback) {
        DailyTipType resolved = DailyTipType.fromString(typeStr);
        return resolved != null ? resolved : fallback;
    }

    /**
     * 截断字符串到指定长度。
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    /**
     * 将实体转换为 VO。
     */
    private DailyTipVO convertToVO(SysDailyTip tip) {
        return DailyTipVO.builder()
                .id(tip.getId())
                .type(tip.getTipType().name())
                .typeLabel(tip.getTipType().getLabel())
                .emoji(tip.getEmoji())
                .title(tip.getTitle())
                .content(tip.getContent())
                .date(tip.getTipDate().format(DATE_FORMATTER))
                .answer(tip.getAnswer() != null ? tip.getAnswer() : "")
                .build();
    }
}
