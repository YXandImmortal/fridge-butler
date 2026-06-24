create table ai_chat_message
(
	id bigint auto_increment
		primary key,
	session_id varchar(32) not null comment '关联会话ID',
	role varchar(20) not null comment 'user / assistant / system',
	content text null comment '消息文本内容',
	message_type varchar(30) null comment '结构化类型：text/fridge_list/...（assistant消息有）',
	structured_data json null comment '结构化数据JSON（assistant消息有）',
	create_time datetime default CURRENT_TIMESTAMP null,
	attachments json null comment '用户附件列表（引用冰箱/物品的快照）'
)
comment 'AI聊天消息';

create index idx_session_time
	on ai_chat_message (session_id, create_time);

create table ai_chat_session
(
	id bigint auto_increment
		primary key,
	session_id varchar(32) not null comment '业务会话ID，如 sess_abc123',
	user_id bigint not null comment '用户ID',
	title varchar(100) null comment '会话标题（AI自动生成，如"查看冰箱"）',
	last_active_time datetime not null comment '最后活跃时间',
	is_deleted tinyint default 0 null comment '是否软删除',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	constraint uk_session
		unique (session_id)
)
comment 'AI聊天会话';

create index idx_user_active
	on ai_chat_session (user_id, last_active_time);

create table biz_fridge
(
	id bigint auto_increment
		primary key,
	fridge_name varchar(30) not null,
	owner_id bigint not null comment '所有者ID',
	fridge_type_id bigint null comment '冰箱类型ID，关联 biz_fridge_type',
	is_default tinyint(1) default 0 not null comment '是否为默认冰箱',
	fridge_address varchar(255) null,
	total_capacity int null comment '冰箱总容量',
	remark varchar(255) null,
	create_time datetime default CURRENT_TIMESTAMP not null,
	status tinyint(1) default 1 null comment '状态',
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	is_deleted tinyint(1) default 0 null
)
comment '系统冰箱表';

create index idx_owner_id
	on biz_fridge (owner_id);

create table biz_fridge_capacity_rate
(
	id bigint auto_increment
		primary key,
	fridge_id bigint not null comment '冰箱ID',
	rate int default 0 not null comment '容量利用率百分比(0-100)',
	item_count int default 0 not null comment '物品数量',
	total_capacity int null comment '冰箱总容量',
	fridge_type_id bigint null comment '冰箱类型ID快照，用于判断类型变化后触发重算',
	last_calculate_time timestamp default CURRENT_TIMESTAMP not null comment '上次计算时间',
	create_time timestamp default CURRENT_TIMESTAMP not null,
	update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
	is_deleted tinyint default 0 not null comment '软删除标记',
	constraint uk_fridge_id
		unique (fridge_id)
)
comment '冰箱容量利用率缓存表' collate=utf8mb4_unicode_ci;

create index idx_calculate_time
	on biz_fridge_capacity_rate (last_calculate_time);

create table biz_fridge_item
(
	id bigint auto_increment
		primary key,
	fridge_id bigint not null,
	item_name varchar(50) not null,
	item_unit_id bigint null,
	stored_date date null comment '存入冰箱日期',
	production_date date null comment '生产日期',
	shelf_life_days int null comment '保质期',
	operator_id bigint not null comment '操作用户ID',
	category_id bigint null comment '物品分类ID',
	item_num decimal(10,2) default 0.00 not null comment '物品数量',
	remark varchar(255) default '' null comment '备注',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	is_deleted tinyint(1) default 0 null,
	storage_location varchar(50) null comment 'AI推荐的存储位置，如冷藏室、冷冻室'
);

create index idx_fridge_id
	on biz_fridge_item (fridge_id);

create index idx_item_name
	on biz_fridge_item (item_name);

create table biz_fridge_type
(
	id bigint auto_increment comment '类型ID'
		primary key,
	type_name varchar(30) not null comment '类型名称，如单门、双门、三门、对开门、多门、迷你冰箱、车载冰箱等',
	create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
	update_time timestamp default CURRENT_TIMESTAMP null comment '更新时间',
	is_deleted tinyint(1) default 0 null comment '是否删除：1-已删除，0-未删除'
)
comment '冰箱类型表（系统预设，不支持用户自定义）';

create table biz_item_add_record
(
	id bigint auto_increment comment '记录ID'
		primary key,
	item_id bigint not null comment '物品ID，关联 biz_fridge_item',
	fridge_id bigint not null comment '冰箱ID，关联 biz_fridge',
	item_name varchar(50) not null comment '物品名称（冗余存储，便于查询）',
	add_num decimal(10,2) not null comment '本次添加数量',
	remaining_num decimal(10,2) not null comment '添加后剩余数量',
	operator_id bigint not null comment '操作人ID，关联 sys_user',
	remark varchar(255) default '' null comment '备注',
	create_time timestamp(6) default CURRENT_TIMESTAMP(6) not null comment '添加时间',
	item_unit_id bigint null comment '物品单位ID（快照）',
	unit_name varchar(20) null comment '物品单位名称（快照）'
)
comment '物品添加记录表' collate=utf8mb4_unicode_ci;

create index idx_create_time
	on biz_item_add_record (create_time);

create index idx_fridge_id
	on biz_item_add_record (fridge_id);

create index idx_item_id
	on biz_item_add_record (item_id);

create index idx_operator_id
	on biz_item_add_record (operator_id);

create table biz_item_category
(
	id bigint auto_increment
		primary key,
	category_name varchar(30) not null comment '分类名称',
	owner_id bigint null comment '创建人',
	is_system_default tinyint(1) default 1 null,
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	is_deleted tinyint(1) default 0 null
)
comment '物品分类表';

create index idx_owner_id
	on biz_item_category (owner_id);

create table biz_item_change_record
(
	id bigint auto_increment comment '记录ID'
		primary key,
	item_id bigint not null comment '物品ID，关联 biz_fridge_item',
	fridge_id bigint not null comment '冰箱ID，关联 biz_fridge',
	change_type varchar(50) not null comment '变更类型：UPDATE_NAME / UPDATE_NUM / UPDATE_SHELF_LIFE / UPDATE_CATEGORY / UPDATE_UNIT / UPDATE_STORED_DATE / UPDATE_PRODUCTION_DATE / UPDATE_REMARK 等',
	field_name varchar(50) not null comment '变更字段名，如 item_name、item_num、shelf_life_days 等',
	old_value varchar(255) null comment '变更前值',
	new_value varchar(255) null comment '变更后值',
	operator_id bigint not null comment '操作人ID，关联 sys_user',
	remark varchar(255) default '' null comment '备注',
	create_time timestamp(6) default CURRENT_TIMESTAMP(6) not null comment '变更时间'
)
comment '物品变更记录表' collate=utf8mb4_unicode_ci;

create index idx_change_type
	on biz_item_change_record (change_type);

create index idx_create_time
	on biz_item_change_record (create_time);

create index idx_fridge_id
	on biz_item_change_record (fridge_id);

create index idx_item_id
	on biz_item_change_record (item_id);

create index idx_operator_id
	on biz_item_change_record (operator_id);

create table biz_item_take_out_record
(
	id bigint unsigned auto_increment comment '主键ID'
		primary key,
	item_id bigint not null comment '物品ID（关联biz_fridge_item）',
	fridge_id bigint not null comment '冰箱ID',
	item_name varchar(50) not null comment '物品名称（取出时的快照）',
	take_out_num decimal(10,2) not null comment '取出数量',
	remaining_num decimal(10,2) not null comment '取出后剩余数量',
	operator_id bigint not null comment '操作人ID',
	create_time datetime(6) default CURRENT_TIMESTAMP(6) null comment '操作时间',
	item_unit_id bigint null comment '物品单位ID（快照）',
	unit_name varchar(20) null comment '物品单位名称（快照）'
)
comment '物品取出记录表' collate=utf8mb4_unicode_ci;

create index idx_create_time
	on biz_item_take_out_record (create_time);

create index idx_fridge_id
	on biz_item_take_out_record (fridge_id);

create index idx_item_id
	on biz_item_take_out_record (item_id);

create index idx_operator_id
	on biz_item_take_out_record (operator_id);

create table biz_item_unit
(
	id bigint auto_increment
		primary key,
	unit_name varchar(20) not null,
	unit_type_id bigint not null,
	is_system_default tinyint(1) default 1 not null,
	owner_id bigint null,
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	is_deleted tinyint(1) default 0 null
);

create index idx_owner_id
	on biz_item_unit (owner_id);

create index idx_unit_type
	on biz_item_unit (unit_type_id);

create table biz_purchase_plan
(
	id bigint auto_increment
		primary key,
	user_id bigint not null comment '所属用户ID',
	fridge_id bigint not null comment '目标冰箱ID（本版仅支持单冰箱）',
	plan_name varchar(100) not null comment '方案名称',
	source varchar(50) default 'MANUAL_CREATE' not null comment '计划来源：DAILY_RECOMMEND | SPECIAL_GENERATE | MANUAL_CREATE | TEMPLATE',
	plan_status tinyint default 1 not null comment '1=待采购, 2=已完成, 3=已取消',
	scene_desc varchar(255) null comment '场景描述或模板标签，用户可填写',
	total_items int default 0 not null comment '物品总数',
	completed_items int default 0 not null comment '已完成/跳过数',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
comment '采购方案' collate=utf8mb4_unicode_ci;

create index idx_fridge_id
	on biz_purchase_plan (fridge_id);

create index idx_status
	on biz_purchase_plan (plan_status);

create index idx_user_id
	on biz_purchase_plan (user_id);

create table biz_purchase_plan_item
(
	id bigint auto_increment
		primary key,
	plan_id bigint not null comment '关联方案ID',
	item_name varchar(100) not null comment '物品名称',
	category_id bigint null comment '建议分类ID',
	planned_num decimal(10,2) not null comment '计划数量',
	item_unit_id bigint not null comment '单位ID',
	actual_num decimal(10,2) null comment '实际采购数量（核对时填写）',
	production_date date null comment '生产日期（核对时填写）',
	shelf_life_days int null comment '保质期天数（核对时填写）',
	storage_location varchar(100) null comment '存放位置（核对时填写）',
	status tinyint default 1 not null comment '1=待采购, 2=已核对, 3=已入库, 4=跳过',
	remark varchar(255) null comment '备注',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	store_in_fridge tinyint(1) default 1 not null comment '是否建议存入冰箱：1=是，0=否'
)
comment '采购方案物品清单' collate=utf8mb4_unicode_ci;

create index idx_plan_id
	on biz_purchase_plan_item (plan_id);

create index idx_status
	on biz_purchase_plan_item (status);

create table biz_purchase_plan_template
(
	id bigint auto_increment
		primary key,
	user_id bigint not null comment '所属用户ID',
	template_name varchar(100) not null comment '模板名称（同一用户下唯一）',
	scene_desc varchar(255) null comment '场景描述或备注，仅用于展示和管理',
	item_count int default 0 not null comment '物品数量',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
comment '用户采购计划模板（物品清单模板，与特殊场景提示词模板无关）' collate=utf8mb4_unicode_ci;

create index idx_user_id
	on biz_purchase_plan_template (user_id);

create table biz_purchase_plan_template_item
(
	id bigint auto_increment
		primary key,
	template_id bigint not null comment '关联模板ID',
	item_name varchar(100) not null comment '物品名称',
	category_id bigint null comment '建议分类ID',
	planned_num decimal(10,2) not null comment '计划数量',
	item_unit_id bigint not null comment '单位ID',
	sort_order int default 0 null comment '排序序号',
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	store_in_fridge tinyint(1) default 1 not null comment '是否建议存入冰箱：1=是，0=否'
)
comment '用户采购计划模板物品清单' collate=utf8mb4_unicode_ci;

create index idx_template_id
	on biz_purchase_plan_template_item (template_id);

create table biz_unit_type
(
	id bigint auto_increment
		primary key,
	unit_type_name varchar(20) not null,
	is_system_default tinyint(1) default 1 not null,
	owner_id bigint null,
	create_time datetime default CURRENT_TIMESTAMP null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
	is_deleted tinyint(1) default 0 null
)
comment '计量单位类型表';

create index idx_owner_id
	on biz_unit_type (owner_id);

create table sys_activation_key
(
	id bigint auto_increment
		primary key,
	key_code varchar(16) not null comment '密钥字符串，如 FB-A3F9K2M1',
	status varchar(16) default 'UNUSED' not null comment '状态：UNUSED-未使用, BOUND-已绑定, REVOKED-已收回, DESTROYED-已销毁',
	bound_user_id bigint null comment '绑定用户ID',
	bound_time datetime null comment '绑定时间',
	remark varchar(255) null comment '备注',
	create_time datetime default CURRENT_TIMESTAMP not null,
	update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
	constraint uk_key_code
		unique (key_code)
)
comment '用户激活密钥表';

create index idx_bound_user_id
	on sys_activation_key (bound_user_id);

create index idx_status
	on sys_activation_key (status);

create table sys_config
(
	id bigint auto_increment
		primary key,
	config_key varchar(64) not null comment '配置键',
	config_value text null comment '配置值',
	description varchar(255) null comment '配置描述',
	update_time datetime default CURRENT_TIMESTAMP null,
	constraint config_key
		unique (config_key)
)
comment '系统配置表';

create table sys_daily_tip
(
	id bigint auto_increment comment '主键ID'
		primary key,
	tip_type varchar(20) not null comment '小贴士类型',
	emoji varchar(10) not null comment '表情符号',
	title varchar(20) not null comment '标题',
	content longtext not null comment '内容',
	tip_date date not null comment '小贴士日期',
	answer longtext null comment '回答/反馈',
	create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
	update_time timestamp default CURRENT_TIMESTAMP null comment '更新时间',
	constraint uk_tip_date
		unique (tip_date)
)
comment '每日小贴士表' collate=utf8mb4_unicode_ci;

create index idx_tip_date
	on sys_daily_tip (tip_date);

create table sys_important_notice
(
	id bigint auto_increment comment '主键ID'
		primary key,
	title varchar(100) not null comment '通知标题',
	content text null comment '通知内容',
	priority tinyint default 0 not null comment '优先级：0普通 1警告 2紧急',
	create_time timestamp default CURRENT_TIMESTAMP not null comment '发布时间',
	is_deleted tinyint default 0 not null comment '软删除标记：0未删除 1已删除',
	status tinyint default 0 not null comment '广播状态：0=活跃，1=已关闭',
	broadcast_time datetime null comment '最近一次广播时间',
	broadcast_count int default 0 not null comment '广播次数'
)
comment '重要通知模板表' collate=utf8mb4_unicode_ci;

create index idx_important_notice_create_time
	on sys_important_notice (create_time);

create table sys_notification
(
	id bigint auto_increment comment '消息ID'
		primary key,
	user_id bigint not null comment '接收用户ID',
	fridge_id bigint null comment '关联冰箱ID',
	item_id bigint null comment '关联物品ID',
	title varchar(100) not null comment '消息标题',
	content text null,
	type varchar(30) not null comment '消息类型：EXPIRED(已过期)/EXPIRING_CRITICAL(1天内过期)/EXPIRING_WARNING(3天内过期)/EXPIRING_NOTICE(7天内过期)/CAPACITY_WARNING(容量预警)/SYSTEM(系统通知)',
	priority tinyint default 0 not null comment '优先级：0普通 1警告 2紧急',
	status tinyint default 0 not null comment '状态：0未读 1已读',
	action_type varchar(30) null comment '点击动作类型：VIEW_ITEM(查看物品)/VIEW_FRIDGE(查看冰箱)/NONE(无)',
	action_payload json null comment '动作参数，JSON格式，如 {"itemId": 1, "fridgeId": 2}',
	create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
	read_time timestamp null comment '阅读时间',
	is_deleted tinyint default 0 not null comment '是否删除：0否 1是'
)
comment '消息通知表' collate=utf8mb4_unicode_ci;

create index idx_notification_fridge_type_status
	on sys_notification (fridge_id, type, status, is_deleted);

create index idx_notification_user_item_type
	on sys_notification (user_id, item_id, type, status, is_deleted);

create index idx_notification_user_status
	on sys_notification (user_id, status, is_deleted);

create index idx_notification_user_time
	on sys_notification (user_id, is_deleted, create_time);

create index idx_notification_user_type
	on sys_notification (user_id, type, status, is_deleted);

create table sys_oper_log
(
	id bigint auto_increment
		primary key,
	trace_id varchar(64) null comment '链路ID',
	user_id bigint null comment '操作用户ID',
	username varchar(64) null comment '操作用户名',
	method varchar(10) null comment '请求方法',
	uri varchar(512) null comment '请求URI',
	ip varchar(128) null comment 'IP地址',
	params text null comment '请求参数（脱敏后）',
	status_code int null comment '响应状态码',
	duration_ms int null comment '耗时毫秒',
	error_msg text null comment '错误信息',
	create_time datetime default CURRENT_TIMESTAMP null
)
comment '操作日志表';

create table sys_role
(
	id bigint auto_increment
		primary key,
	role_name varchar(20) not null,
	role_code varchar(20) not null comment '角色英文代码',
	remark varchar(255) default '' null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	is_deleted tinyint(1) default 0 null comment '逻辑删除 0=未删除 1=已删除',
	constraint uk_role_code
		unique (role_code)
)
comment '系统角色表';

create table sys_user
(
	id bigint auto_increment
		primary key,
	username varchar(50) not null,
	password varchar(60) not null,
	mobile varchar(11) null comment '用户手机号',
	role_id bigint not null comment '角色ID',
	avatar varchar(20) default 'bot' not null,
	guide_completed tinyint(1) default 0 not null comment '是否已完成新手指引：0-未完成，1-已完成',
	is_activated tinyint(1) default 1 not null comment '是否已激活：1-已激活，0-未激活',
	create_time datetime default CURRENT_TIMESTAMP not null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	is_deleted tinyint(1) default 0 null comment '逻辑删除',
	last_login_time datetime null comment '最后登录时间',
	password_updated_at datetime null comment '密码最后修改时间',
	email varchar(100) null comment '用户邮箱，用于忘记密码和邮件提醒',
	constraint email
		unique (email),
	constraint uk_mobile
		unique (mobile),
	constraint uk_username
		unique (username)
)
comment '系统用户表';

create table daily_freshness_snapshot
(
	id bigint auto_increment comment '主键ID'
		primary key,
	user_id bigint not null comment '用户ID',
	snapshot_date date not null comment '快照日期',
	freshness_score int null comment '保鲜评分 0-100',
	score_grade varchar(1) null comment '评分等级 S/A/B/C/D',
	has_expired tinyint default 0 null comment '当日是否有过期物品：1-是 0-否',
	item_count int default 0 null comment '当日物品总数',
	expired_count int default 0 null comment '当日过期物品数',
	expiring_3d_count int default 0 null comment '当日3天内临期物品数',
	fridge_count int default 0 null comment '当日冰箱数量',
	capacity_rate_avg decimal(5,2) null comment '当日平均容量利用率',
	created_at datetime(3) default CURRENT_TIMESTAMP(3) null comment '创建时间',
	freshness_score_freshness decimal(5,2) default 0.00 not null comment '新鲜度维度得分（临期处理）',
	freshness_score_turnover decimal(5,2) default 0.00 not null comment '周转效率维度得分（库存周转）',
	freshness_score_expired decimal(5,2) default 0.00 not null comment '过期控制维度得分',
	freshness_score_capacity decimal(5,2) default 0.00 not null comment '空间利用维度得分（分类整理）',
	constraint uk_user_date
		unique (user_id, snapshot_date),
	constraint daily_freshness_snapshot_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '每日保鲜评分快照（用于热力图和历史趋势）';

create table monthly_report
(
	id bigint auto_increment comment '主键ID'
		primary key,
	user_id bigint not null comment '用户ID',
	`year_month` varchar(7) not null comment '报告年月，如 2026-05',
	avg_score int default 0 null comment '当月平均保鲜评分',
	max_score int default 0 null comment '当月最高保鲜评分',
	min_score int default 0 null comment '当月最低保鲜评分',
	expired_count int default 0 null comment '当月过期物品数',
	expiring_count int default 0 null comment '当月临期物品数',
	wasted_amount decimal(10,2) default 0.00 null comment 'AI估算浪费金额（元）',
	saved_kg decimal(10,2) default 0.00 null comment '避免浪费食材重量（kg）',
	co2_saved decimal(10,2) default 0.00 null comment '减少CO2排放（kg）',
	water_saved decimal(10,2) default 0.00 null comment '节约用水（L）',
	items_added int default 0 null comment '当月添加物品数',
	items_taken_out int default 0 null comment '当月取出物品数',
	new_badges int default 0 null comment '当月新解锁徽章数',
	level_start int default 1 null comment '月初等级',
	level_end int default 1 null comment '月末等级',
	streak_max int default 0 null comment '当月最高连续天数',
	viewed_at datetime(3) null comment '首次查看时间（用于EXP发放控制）',
	generated_at datetime(3) default CURRENT_TIMESTAMP(3) null comment '生成时间',
	constraint uk_user_month
		unique (user_id, `year_month`),
	constraint monthly_report_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户月度报告';

create index idx_sys_user_mobile
	on sys_user (mobile);

create table user_achievement_setting
(
	id bigint auto_increment
		primary key,
	user_id bigint not null,
	panel_hidden tinyint default 0 null,
	auto_streak_protect tinyint default 1 null,
	streak_protect_notify tinyint default 1 null,
	updated_at datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3),
	constraint user_id
		unique (user_id),
	constraint user_achievement_setting_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户成就系统个性化设置';

create table user_action_counter
(
	id bigint auto_increment comment '主键ID'
		primary key,
	user_id bigint not null comment '用户ID',
	counter_type varchar(50) not null comment '计数类型：NIGHT_OWL(夜猫子), EARLY_BIRD(早起鸟), DATA_CENTER_VIEW(数据控), CHEF_COOK(大厨认证), ORGANIZE_DAY(整理专家)',
	count_value int default 0 null comment '累计计数',
	count_date date null comment '按日计数时使用（如 ORGANIZE_DAY）',
	updated_at datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3) comment '更新时间',
	constraint uk_user_type_date
		unique (user_id, counter_type, count_date),
	constraint user_action_counter_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户行为计数器（用于徽章解锁判定）';

create table user_badge
(
	id bigint auto_increment comment '主键ID'
		primary key,
	user_id bigint not null comment '用户ID',
	badge_code varchar(50) not null comment '徽章唯一编码',
	unlocked_at datetime(3) default CURRENT_TIMESTAMP(3) null comment '解锁时间',
	constraint uk_user_badge
		unique (user_id, badge_code),
	constraint user_badge_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户已解锁徽章';

create table user_exp
(
	id bigint auto_increment
		primary key,
	user_id bigint not null,
	current_exp int default 0 null,
	total_exp int default 0 null,
	current_level int default 1 null,
	daily_exp_today int default 0 null,
	daily_exp_date date null,
	title_custom varchar(50) null,
	created_at datetime(3) default CURRENT_TIMESTAMP(3) null,
	updated_at datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3),
	constraint user_id
		unique (user_id),
	constraint user_exp_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户经验值与等级';

create table user_exp_log
(
	id bigint auto_increment
		primary key,
	user_id bigint not null,
	action_type varchar(50) not null,
	action_desc varchar(200) null,
	exp_gained int not null,
	exp_balance int not null,
	related_id bigint null,
	created_at datetime(3) default CURRENT_TIMESTAMP(3) null,
	constraint user_exp_log_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '经验值变动日志';

create index idx_user_date
	on user_exp_log (user_id, created_at);

create table user_streak
(
	id bigint auto_increment
		primary key,
	user_id bigint not null,
	current_streak int default 0 null,
	max_streak int default 0 null,
	protect_count_remaining int default 2 null,
	protect_count_total int default 2 null,
	protect_count_used int default 0 null,
	protect_reset_month varchar(7) default '' null,
	auto_protect_enabled tinyint default 1 null,
	protect_notify_enabled tinyint default 1 null,
	last_check_date date null,
	last_check_result tinyint default 0 null,
	created_at datetime(3) default CURRENT_TIMESTAMP(3) null,
	updated_at datetime(3) default CURRENT_TIMESTAMP(3) null on update CURRENT_TIMESTAMP(3),
	constraint user_id
		unique (user_id),
	constraint user_streak_ibfk_1
		foreign key (user_id) references sys_user (id)
)
comment '用户冰鲜连续天数与保护机制';


