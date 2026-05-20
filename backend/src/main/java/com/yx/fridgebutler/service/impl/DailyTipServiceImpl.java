package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.dto.dailytip.DailyTipGenerateResult;
import com.yx.fridgebutler.entity.DailyTip;
import com.yx.fridgebutler.enums.DailyTipType;
import com.yx.fridgebutler.repository.DailyTipRepository;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.DailyTipService;
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
    private DeepSeekService deepSeekService;

    private final Random random = new Random();

    /**
     * {@inheritDoc}
     * <p>优先查询数据库，不存在则实时生成并保存。</p>
     */
    @Override
    public DailyTipVO getTodayTip() {
        LocalDate today = LocalDate.now();
        Optional<DailyTip> optional = dailyTipRepository.findByTipDate(today);
        if (optional.isPresent()) {
            log.debug("命中数据库今日小贴士，date={}", today);
            return convertToVO(optional.get());
        }
        log.info("数据库中无今日小贴士，实时调用 AI 生成，date={}", today);
        DailyTip tip = generateAndSave(today);
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
     *
     * @param date 目标日期
     * @return 保存后的实体
     */
    private DailyTip generateAndSave(LocalDate date) {
        DailyTipType type = TIP_TYPES.get(random.nextInt(TIP_TYPES.size()));
        String dateStr = date.format(DATE_FORMATTER);
        String weekday = date.format(WEEKDAY_FORMATTER);

        String userPrompt = String.format("今天是 %s（%s），请生成一条\"%s\"类别的冰箱每日小贴士。",
                dateStr, weekday, type.name());

        String response = deepSeekService.chat(SYSTEM_PROMPT, userPrompt);
        DailyTipGenerateResult result = parseResponse(response);

        DailyTip tip = DailyTip.builder()
                .tipType(resolveType(result.getType(), type))
                .emoji(truncate(result.getEmoji(), 10))
                .title(truncate(result.getTitle(), 20))
                .content(result.getContent())
                .tipDate(date)
                .answer(result.getAnswer() != null ? result.getAnswer() : "")
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();

        return dailyTipRepository.save(tip);
    }

    /**
     * 解析 DeepSeek 返回的 JSON 响应。
     */
    private DailyTipGenerateResult parseResponse(String response) {
        String cleaned = cleanJsonResponse(response);
        try {
            return JSONUtil.toBean(cleaned, DailyTipGenerateResult.class);
        } catch (Exception e) {
            log.warn("每日小贴士 JSON 解析失败，尝试兜底解析。原始响应：{}", cleaned, e);
            // 兜底：构造一个简单对象
            DailyTipGenerateResult fallback = new DailyTipGenerateResult();
            fallback.setType("FACT");
            fallback.setEmoji("🧊");
            fallback.setTitle("冰箱小贴士");
            fallback.setContent("冰箱门不要频繁开关，每次开门冷气会流失约30%，既费电又影响保鲜效果。");
            fallback.setDate(LocalDate.now().format(DATE_FORMATTER));
            fallback.setAnswer("");
            return fallback;
        }
    }

    /**
     * 清理 DeepSeek 可能返回的 markdown 代码块等包装。
     */
    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).trim();
            }
        }
        return cleaned;
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
    private DailyTipVO convertToVO(DailyTip tip) {
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
