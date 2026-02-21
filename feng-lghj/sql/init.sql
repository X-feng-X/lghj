-- 量股化金 - AI股市量化预测系统

-- ------------------------------
-- 用户表 user
-- ------------------------------
drop table if exists user;
create table user
(
    `id`          bigint                                                 not null auto_increment comment '主键ID',
    `username`    varchar(50) character set utf8mb4 collate utf8mb4_bin  not null unique comment '登录用户名',
    `password`    varchar(100) character set utf8mb4 collate utf8mb4_bin not null comment '密码',
    `nick_name`   varchar(32) character set utf8mb4 collate utf8mb4_bin           default null comment '昵称',
    `icon`        varchar(255) character set utf8mb4 collate utf8mb4_bin          default null comment '人物头像',
    `email`       varchar(100) character set utf8mb4 collate utf8mb4_bin          default null comment '邮箱',
    `phone`       varchar(20) character set utf8mb4 collate utf8mb4_bin           default null comment '手机号',
    `sex`         tinyint unsigned                                                default null comment '性别（1: 男，2: 女）',
    `user_type`   tinyint unsigned                                       not null comment '简化身份标识（1: 普通用户、2: 机构用户、3: 管理员）',
    `status`      tinyint unsigned                                       not null default 1 comment '状态（0: 禁用、1: 正常）',
    `create_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                          default null comment '创建人',
    `update_user` bigint                                                          default null comment '更新人',
    `is_deleted`  tinyint unsigned                                       not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='用户表';

-- ------------------------------
-- 角色表 role
-- ------------------------------
drop table if exists role;
create table role
(
    `id`          int                                                   not null auto_increment comment '主键ID',
    `role_name`   varchar(20) character set utf8mb4 collate utf8mb4_bin not null comment '角色名称（0: 普通用户，1: 机构用户，2: 超级管理员，3:系统管理员，4: 运营管理员，5: 内容管理员）',
    `role_desc`   varchar(200) character set utf8mb4 collate utf8mb4_bin         default null comment '角色描述',
    `status`      tinyint unsigned                                      not null default 1 comment '状态（0: 禁用、1: 正常）',
    `create_time` datetime                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                         default null comment '创建人',
    `update_user` bigint                                                         default null comment '更新人',
    `is_deleted`  tinyint unsigned                                      not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='角色表';

-- ------------------------------
-- 权限表 permissions（后台权限）
-- ------------------------------
drop table if exists permissions;
create table permissions
(
    `id`               int                                                   not null auto_increment comment '主键ID',
    `permissions_name` varchar(20) character set utf8mb4 collate utf8mb4_bin not null comment '权限名称',
    `create_time`      datetime                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`      bigint                                                         default null comment '创建人',
    `update_user`      bigint                                                         default null comment '更新人',
    `is_deleted`       tinyint unsigned                                      not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='权限表';

-- ------------------------------
-- 用户-角色关联表 user_role
-- ------------------------------
drop table if exists user_role;
create table user_role
(
    `id`          bigint           not null auto_increment comment '主键ID',
    `user_id`     bigint           not null comment '关联用户ID',
    `role_id`     int              not null comment '关联角色ID',
    `create_time` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                    default null comment '创建人',
    `update_user` bigint                    default null comment '更新人',
    `is_deleted`  tinyint unsigned not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='用户-角色关联表';

-- ------------------------------
-- 角色-权限关联表 role_permission
-- ------------------------------
drop table if exists role_permission;
create table role_permission
(
    `id`          bigint           not null auto_increment comment '主键ID',
    `role_id`     int              not null comment '关联角色ID',
    `permission`  int              not null comment '关联权限ID',
    `create_time` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                    default null comment '创建人',
    `update_user` bigint                    default null comment '更新人',
    `is_deleted`  tinyint unsigned not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='角色-权限关联表';


-- ------------------------------
-- 博客表 blog
-- ------------------------------
drop table if exists `blog`;
create table `blog`
(
    `id`          bigint                                                  not null auto_increment comment '主键ID',
    `user_id`     bigint unsigned                                         not null comment '用户ID',
    `stock_id`    varchar(255) character set utf8mb4 collate utf8mb4_bin           default null comment '关联股票代码，多个用","隔开',
    `title`       varchar(255) character set utf8mb4 collate utf8mb4_bin  not null comment '标题',
    `images`      varchar(2048) character set utf8mb4 collate utf8mb4_bin          default null comment '照片，最多9张，多张以","隔开',
    `context`     varchar(2048) character set utf8mb4 collate utf8mb4_bin not null comment '正文',
    `liked`       int unsigned                                                     default 0 comment '点赞数量',
    `comments`    int unsigned                                                     default 0 comment '评论数量',
    `status`      tinyint unsigned                                        not null default 1 comment '状态（0: 禁用、1: 正常）',
    `create_time` datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                           default null comment '创建人',
    `update_user` bigint                                                           default null comment '更新人',
    `is_deleted`  tinyint unsigned                                        not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='博客表';

-- ------------------------------
-- 博客评论表 blog_comments
-- ------------------------------
drop table if exists `blog_comments`;
create table `blog_comments`
(
    `id`          bigint                                                 not null auto_increment comment '主键ID',
    `user_id`     bigint unsigned                                        not null comment '用户id',
    `blog_id`     bigint unsigned                                        not null comment '关联博客id',
    `parent_id`   bigint unsigned                                                 default 0 comment '关联的1级评论id，如果是1级评论，则为0',
    `content`     varchar(255) character set utf8mb4 collate utf8mb4_bin not null comment '评论内容',
    `liked`       int unsigned                                                    default 0 comment '点赞数量',
    `status`      tinyint unsigned                                       not null default 1 comment '状态（0: 禁用、1: 正常）',
    `create_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                          default null comment '创建人',
    `update_user` bigint                                                          default null comment '更新人',
    `is_deleted`  tinyint unsigned                                       not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='博客评论表';

-- ------------------------------
-- 关注关联表 follow
-- ------------------------------
drop table if exists `follow`;
create table `follow`
(
    `id`             bigint           not null auto_increment comment '主键ID',
    `user_id`        bigint unsigned  not null comment '用户id',
    `follow_user_id` bigint unsigned  not null comment '粉丝id',
    `create_time`    datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`    bigint                    default null comment '创建人',
    `update_user`    bigint                    default null comment '更新人',
    `is_deleted`     tinyint unsigned not null default 0 comment '是否已删除（0: 否，1: 是）',
    primary key (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment ='关注关联表';

-- ------------------------------
-- A股基础信息表 stock_basic
-- ------------------------------
drop table if exists stock_basic;
CREATE TABLE `stock_basic`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `symbol`           varchar(10) NOT NULL COMMENT '股票代码',
    `name`             varchar(50) NOT NULL COMMENT '股票名称',
    `short_name`       varchar(50)          DEFAULT NULL COMMENT '股票简称',
    `total_shares`     bigint               DEFAULT NULL COMMENT '总股本（股）',
    `float_shares`     bigint               DEFAULT NULL COMMENT '流通股（股）',
    `total_market_cap` bigint               DEFAULT NULL COMMENT '总市值（元）',
    `float_market_cap` bigint               DEFAULT NULL COMMENT '流通市值（元）',
    `industry`         varchar(100)         DEFAULT NULL COMMENT '所属行业',
    `market_type`      tinyint              DEFAULT NULL COMMENT '市场类型 (0-未知 1-沪A, 2-深A, 3-创业板, 4-科创板，5-北交所，6-新三版)',
    `list_date`        date                 DEFAULT NULL COMMENT '上市日期',
    `create_time`      datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`      varchar(50)          DEFAULT NULL COMMENT '创建人',
    `update_user`      varchar(50)          DEFAULT NULL COMMENT '修改人',
    `is_deleted`       tinyint     NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='A股基础信息表';

-- ------------------------------
-- 委托单表 trade_order
-- ------------------------------
drop table if exists `trade_order`;
CREATE TABLE `trade_order`
(
    `id`              bigint             NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`        varchar(50) UNIQUE NOT NULL comment '委托单号（唯一标识）',
    `user_id`         bigint unsigned    not null comment '用户ID',
    `symbol`          varchar(10)        NOT NULL COMMENT '股票代码',
    `direction`       tinyint                     DEFAULT NULL COMMENT '买/卖（1-买，2-卖）',
    `price`           decimal(10, 2)     NOT NULL COMMENT '委托价格',
    `quantity`        int                NOT NULL COMMENT '委托数量（股）',
    `traded_quantity` int                NOT NULL COMMENT '已成交数量',
    `status`          tinyint            NOT NULL default 1 COMMENT '订单状态（1-待定，2-部分完成，3-已完成，4-）',
    `cancel_time`     datetime                    default null comment '撤销时间（未成交时）',
    `create_time`     datetime                    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`     varchar(50)                 DEFAULT NULL COMMENT '创建人',
    `update_user`     varchar(50)                 DEFAULT NULL COMMENT '修改人',
    `is_deleted`      tinyint            NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='委托单表';

-- ------------------------------
-- 成交记录表 trade_deal
-- 一笔委托单可能对应多笔成交记录
-- ------------------------------
drop table if exists `trade_deal`;
CREATE TABLE `trade_deal`
(
    `id`             bigint             NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `deal_no`        varchar(50) UNIQUE NOT NULL comment '成交单号（唯一）',
    `order_id`       bigint             NOT NULL comment '关联委托单ID',
    `user_id`        bigint unsigned    not null comment '用户ID',
    `symbol`         varchar(10)        NOT NULL COMMENT '股票代码',
    `deal_direction` tinyint            NOT NULL COMMENT '成交方向（1 - 买入，2 - 卖出）',
    `price`          decimal(10, 2)     NOT NULL COMMENT '成交价',
    `quantity`       int                NOT NULL COMMENT '委托数量（股）',
    `create_time`    datetime                    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`    varchar(50)                 DEFAULT NULL COMMENT '创建人',
    `update_user`    varchar(50)                 DEFAULT NULL COMMENT '修改人',
    `is_deleted`     tinyint            NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='成交记录表';

-- ------------------------------
-- 模拟账户表 sim_account
-- ------------------------------
drop table if exists `sim_account`;
CREATE TABLE `sim_account`
(
    `id`             bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        bigint unsigned not null comment '用户ID',
    `total_cash`     decimal(16, 2)  not null default 200000.00 comment '账户总资金',
    `available_cash` decimal(16, 2)  not null default 200000.00 comment '可用资金（总 - 冻结）',
    `frozen_cash`    decimal(16, 2)  not null default 0.00 comment '冻结资金（待成交买单）',
    `total_asset`    decimal(16, 2)  not null default 200000.00 comment '账户总资产（资金 + 持仓）',
    `version`        int(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`    datetime                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`    varchar(50)              DEFAULT NULL COMMENT '创建人',
    `update_user`    varchar(50)              DEFAULT NULL COMMENT '修改人',
    `is_deleted`     tinyint         NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='模拟账户表';

-- ------------------------------
-- 用户持仓表 user_position
-- ------------------------------
drop table if exists `user_position`;
CREATE TABLE `user_position`
(
    `id`                 bigint          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`            bigint unsigned not null comment '用户ID',
    `account_id`         bigint unsigned not null comment '关联账户ID',
    `symbol`             varchar(10)     NOT NULL COMMENT '股票代码',
    `total_quantity`     int             not null default 0 comment '总持仓数量',
    `frozen_quantity`    int             not null default 0 comment '冻结数量（待卖出）',
    `available_quantity` int             not null default 0 comment '可用数量（总 - 冻结）',
    `cost_price`         decimal(16, 2)  not null default 0.00 comment '持仓成本价（平均）',
    `profit_loss`        decimal(16, 2)  not null default 0.00 comment '浮盈浮亏（市值 - 成本）',
    `version`            int(11)         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`        datetime                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`        varchar(50)              DEFAULT NULL COMMENT '创建人',
    `update_user`        varchar(50)              DEFAULT NULL COMMENT '修改人',
    `is_deleted`         tinyint         NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户持仓表';

-- ------------------------------
-- 资金流水表 account_flow
-- ------------------------------
drop table if exists `account_flow`;
CREATE TABLE `account_flow`
(
    `id`            bigint             NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_no`       varchar(32) UNIQUE NOT NULL COMMENT '流水单号（唯一标识，建议格式：FLOW+日期+随机串，如FLOW20260215123456789）',
    `user_id`       bigint             NOT NULL COMMENT '关联用户ID',
    `account_id`    bigint             NOT NULL COMMENT '关联模拟账户ID',
    `flow_type`     tinyint            NOT NULL COMMENT '流水类型：1-充值，2-提现，3-交易扣款（买入股票），4-交易回款（卖出股票），5-手续费扣减，6-系统调账',
    `amount`        decimal(16, 2)     NOT NULL COMMENT '资金变动金额：正数=增加，负数=减少',
    `balance_after` decimal(16, 2)     NOT NULL COMMENT '变动后账户可用余额（核心字段，用于对账）',
    `related_no`    varchar(32)                 DEFAULT NULL COMMENT '关联单号：委托单号/成交单号/充值单号等，便于溯源',
    `remark`        varchar(200)                DEFAULT NULL COMMENT '流水备注：如“买入600000浦发银行成交扣款（含手续费）”',
    `create_time`   datetime                    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`   varchar(50)                 DEFAULT NULL COMMENT '创建人',
    `update_user`   varchar(50)                 DEFAULT NULL COMMENT '修改人',
    `is_deleted`    tinyint            NOT NULL DEFAULT 0 COMMENT '是否已删除（0: 否，1: 是）',
    PRIMARY KEY (`id`) using btree
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资金流水表';

-- ------------------------------
-- 用户自选股表 user_stock_follow
-- ------------------------------
DROP TABLE IF EXISTS `user_stock_follow`;
CREATE TABLE `user_stock_follow`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint      NOT NULL COMMENT '用户ID',
    `stock_id`    bigint      NOT NULL COMMENT '股票基础信息ID',
    `symbol`      varchar(20) NOT NULL COMMENT '股票代码(冗余)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`) using btree,
    UNIQUE KEY `idx_user_stock` (`user_id`, `stock_id`) COMMENT '用户不能重复关注同一只股票',
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户自选股表';