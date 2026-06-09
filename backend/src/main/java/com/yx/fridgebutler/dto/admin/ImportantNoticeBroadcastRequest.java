package com.yx.fridgebutler.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重要通知广播请求 DTO。
 * <p>用于管理员向所有在线用户推送全局重要通知或公告。</p>
 */
@Data
public class ImportantNoticeBroadcastRequest {

    /**
     * 通知标题，必填，最大长度100。
     */
    @NotBlank
    @Size(max = 100)
    private String title;

    /**
     * 通知正文内容，必填，最大长度5000。
     */
    @NotBlank
    @Size(max = 5000)
    private String content;
}
