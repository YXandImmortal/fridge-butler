CREATE TABLE IF NOT EXISTS ai_request_log
(
    id bigint auto_increment primary key,
    provider varchar(32) not null comment 'AI 厂商：DEEPSEEK / QWEN / OPENAI',
    model varchar(128) not null comment '实际调用的模型 ID',
    status varchar(16) not null comment '调用状态：SUCCESS / FAILURE',
    create_time datetime(6) not null comment '请求发起时间（UTC）',
    index idx_ai_request_create_time (create_time),
    index idx_ai_request_status_time (status, create_time)
) comment 'AI 请求统计日志，不保存提示词、响应内容或密钥';
