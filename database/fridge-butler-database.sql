create table biz_fridge
(
    id             bigint auto_increment
        primary key,
    fridge_name    varchar(30)                          not null,
    owner_id       bigint                               not null comment '所有者ID',
    is_default     tinyint(1) default 0                 not null comment '是否为默认冰箱',
    fridge_address varchar(255)                         null,
    total_capacity int                                  null comment '冰箱总容量',
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
    username    varchar(50)                          not null,
    password    varchar(60)                          not null,
    mobile      varchar(11)                          null comment '用户手机号',
    role_id     bigint                               not null comment '角色ID',
    create_time datetime   default CURRENT_TIMESTAMP not null,
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint(1) default 0                 null comment '逻辑删除',
    constraint uk_username
        unique (username)
)
    comment '系统用户表';

create index idx_sys_user_mobile
    on sys_user (mobile);


