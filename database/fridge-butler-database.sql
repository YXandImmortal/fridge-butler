create table biz_fridge
(
    id             bigint auto_increment
        primary key,
    fridge_name    varchar(30)                          not null,
    owner_id       bigint                               not null comment '所有者ID',
    is_default     tinyint(1) default 0                 not null comment '是否为默认冰箱',
    fridge_address varchar(255)                         null,
    total_capacity int                                  null comment '冰箱总容量',
    remark         varchar(255)                         null,
    create_time    datetime   default CURRENT_TIMESTAMP not null,
    status         tinyint(1) default 1                 null comment '状态',
    update_time    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted     tinyint(1) default 0                 null
)
    comment '系统冰箱表';

create index idx_owner_id
    on biz_fridge (owner_id);

create table biz_fridge_item
(
    id              bigint auto_increment
        primary key,
    fridge_id       bigint                                   not null,
    item_name       varchar(50)                              not null,
    item_unit_id    bigint                                   null,
    stored_date     date                                     null comment '存入冰箱日期',
    production_date date                                     null comment '生产日期',
    shelf_life_days int                                      null comment '保质期',
    operator_id     bigint                                   not null comment '操作用户ID',
    category_id     bigint                                   null comment '物品分类ID',
    item_num        decimal(10, 2) default 0.00              not null comment '物品数量',
    remark          varchar(255)   default ''                null comment '备注',
    create_time     datetime       default CURRENT_TIMESTAMP null,
    update_time     datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      tinyint(1)     default 0                 null
);

create index idx_fridge_id
    on biz_fridge_item (fridge_id);

create index idx_item_name
    on biz_fridge_item (item_name);

create table biz_item_add_record
(
    id            bigint auto_increment comment '记录ID'
        primary key,
    item_id       bigint                                    not null comment '物品ID，关联 biz_fridge_item',
    fridge_id     bigint                                    not null comment '冰箱ID，关联 biz_fridge',
    item_name     varchar(50)                               not null comment '物品名称（冗余存储，便于查询）',
    add_num       decimal(10, 2)                            not null comment '本次添加数量',
    remaining_num decimal(10, 2)                            not null comment '添加后剩余数量',
    operator_id   bigint                                    not null comment '操作人ID，关联 sys_user',
    remark        varchar(255) default ''                   null comment '备注',
    create_time   timestamp(6) default CURRENT_TIMESTAMP(6) not null comment '添加时间'
)
    comment '物品添加记录表' collate = utf8mb4_unicode_ci;

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
    id                bigint auto_increment
        primary key,
    category_name     varchar(30)                          not null comment '分类名称',
    owner_id          bigint                               null comment '创建人',
    is_system_default tinyint(1) default 1                 null,
    create_time       datetime   default CURRENT_TIMESTAMP null,
    update_time       datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted        tinyint(1) default 0                 null
)
    comment '物品分类表';

create index idx_owner_id
    on biz_item_category (owner_id);

create table biz_item_change_record
(
    id          bigint auto_increment comment '记录ID'
        primary key,
    item_id     bigint                                    not null comment '物品ID，关联 biz_fridge_item',
    fridge_id   bigint                                    not null comment '冰箱ID，关联 biz_fridge',
    change_type varchar(50)                               not null comment '变更类型：UPDATE_NAME / UPDATE_NUM / UPDATE_SHELF_LIFE / UPDATE_CATEGORY / UPDATE_UNIT / UPDATE_STORED_DATE / UPDATE_PRODUCTION_DATE / UPDATE_REMARK 等',
    field_name  varchar(50)                               not null comment '变更字段名，如 item_name、item_num、shelf_life_days 等',
    old_value   varchar(255)                              null comment '变更前值',
    new_value   varchar(255)                              null comment '变更后值',
    operator_id bigint                                    not null comment '操作人ID，关联 sys_user',
    remark      varchar(255) default ''                   null comment '备注',
    create_time timestamp(6) default CURRENT_TIMESTAMP(6) not null comment '变更时间'
)
    comment '物品变更记录表' collate = utf8mb4_unicode_ci;

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
    id            bigint unsigned auto_increment comment '主键ID'
        primary key,
    item_id       bigint                                   not null comment '物品ID（关联biz_fridge_item）',
    fridge_id     bigint                                   not null comment '冰箱ID',
    item_name     varchar(50)                              not null comment '物品名称（取出时的快照）',
    take_out_num  decimal(10, 2)                           not null comment '取出数量',
    remaining_num decimal(10, 2)                           not null comment '取出后剩余数量',
    operator_id   bigint                                   not null comment '操作人ID',
    create_time   datetime(6) default CURRENT_TIMESTAMP(6) null comment '操作时间'
)
    comment '物品取出记录表' collate = utf8mb4_unicode_ci;

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
    id                bigint auto_increment
        primary key,
    unit_name         varchar(20)                          not null,
    unit_type_id      bigint                               not null,
    is_system_default tinyint(1) default 1                 not null,
    owner_id          bigint                               null,
    create_time       datetime   default CURRENT_TIMESTAMP null,
    update_time       datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted        tinyint(1) default 0                 null
);

create index idx_owner_id
    on biz_item_unit (owner_id);

create index idx_unit_type
    on biz_item_unit (unit_type_id);

create table biz_unit_type
(
    id                bigint auto_increment
        primary key,
    unit_type_name    varchar(20)                          not null,
    is_system_default tinyint(1) default 1                 not null,
    owner_id          bigint                               null,
    create_time       datetime   default CURRENT_TIMESTAMP null,
    update_time       datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted        tinyint(1) default 0                 null
)
    comment '计量单位类型表';

create index idx_owner_id
    on biz_unit_type (owner_id);

create table sys_role
(
    id          bigint auto_increment
        primary key,
    role_name   varchar(20)                            not null,
    role_code   varchar(20)                            not null comment '角色英文代码',
    remark      varchar(255) default ''                null,
    update_time datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint(1)   default 0                 null comment '逻辑删除 0=未删除 1=已删除',
    constraint uk_role_code
        unique (role_code)
)
    comment '系统角色表';

create table sys_user
(
    id          bigint auto_increment
        primary key,
    username    varchar(50)                           not null,
    password    varchar(60)                           not null,
    mobile      varchar(11)                           null comment '用户手机号',
    role_id     bigint                                not null comment '角色ID',
    avatar      varchar(20) default 'bot'             not null,
    create_time datetime    default CURRENT_TIMESTAMP not null,
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint(1)  default 0                 null comment '逻辑删除',
    constraint uk_mobile
        unique (mobile),
    constraint uk_username
        unique (username)
)
    comment '系统用户表';

create index idx_sys_user_mobile
    on sys_user (mobile);


