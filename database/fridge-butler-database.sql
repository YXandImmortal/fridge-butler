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
	is_deleted tinyint(1) default 0 null
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
	create_time timestamp(6) default CURRENT_TIMESTAMP(6) not null comment '添加时间'
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
	create_time datetime(6) default CURRENT_TIMESTAMP(6) null comment '操作时间'
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

create table daily_tip
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
	on daily_tip (tip_date);

create table sys_notification
(
	id bigint auto_increment comment '消息ID'
		primary key,
	user_id bigint not null comment '接收用户ID',
	fridge_id bigint null comment '关联冰箱ID',
	item_id bigint null comment '关联物品ID',
	title varchar(100) not null comment '消息标题',
	content varchar(500) null comment '消息内容',
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
	create_time datetime default CURRENT_TIMESTAMP not null,
	update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
	is_deleted tinyint(1) default 0 null comment '逻辑删除',
	constraint uk_mobile
		unique (mobile),
	constraint uk_username
		unique (username)
)
comment '系统用户表';

create index idx_sys_user_mobile
	on sys_user (mobile);


