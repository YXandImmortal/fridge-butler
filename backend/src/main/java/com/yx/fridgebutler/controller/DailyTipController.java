package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.service.DailyTipService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.dailytip.DailyTipVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 每日小贴士控制器。
 * <p>提供今日小贴士查询接口，无需登录即可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/daily-tip")
public class DailyTipController {

    /** 每日小贴士服务 */
    @Autowired
    private DailyTipService dailyTipService;

    /**
     * 获取今日小贴士。
     * <p>如果数据库中不存在，则实时调用 AI 生成并保存。</p>
     *
     * @return 今日小贴士
     */
    @GetMapping("/today")
    public Result<DailyTipVO> getTodayTip() {
        DailyTipVO tip = dailyTipService.getTodayTip();
        log.info("获取今日小贴士成功，type={}，title={}", tip.getType(), tip.getTitle());
        return Result.success(tip);
    }

    /**
     * 获取指定日期的小贴士。
     *
     * @param date 日期（格式：yyyy-MM-dd）
     * @return 小贴士，不存在返回 data 为 null 的成功响应
     */
    @GetMapping("/by-date")
    public Result<DailyTipVO> getTipByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyTipVO tip = dailyTipService.getTipByDate(date);
        if (tip == null) {
            return Result.success("该日期暂无小贴士", null);
        }
        return Result.success(tip);
    }
}
