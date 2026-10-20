-- 秒杀项目初始化脚本
CREATE DATABASE IF NOT EXISTS miaosha DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miaosha;

create table if not exists goods
(
    id              bigint auto_increment comment '商品主键ID'
    primary key,
    goods_name      varchar(200)                       not null comment '商品名称',
    goods_img       varchar(255)                       null comment '商品图片地址',
    original_price  decimal(10, 2)                     not null comment '商品原价',
    original_stock  int                                not null comment '原始总库存',
    remaining_stock int                                not null comment '剩余可售库存',
    goods_desc      text                               null comment '商品描述',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除'
    )
    comment '基础商品表';

create table if not exists mq_consume_idempotent
(
    id               bigint auto_increment comment '主键id'
    primary key,
    order_no         varchar(64)                        not null comment '订单编号',
    seckill_goods_id bigint                             not null comment '秒杀商品id',
    biz_status       tinyint                            not null comment '业务状态码：1扣减库存，2归还库存',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_order_bizstatus
    unique (order_no, biz_status)
    )
    comment 'MQ消费幂等记录表';

create table if not exists mq_local_message
(
    id              bigint auto_increment comment '主键'
    primary key,
    msg_id          varchar(128)                       not null comment '消息唯一ID',
    msg_content     text                               null comment '消息JSON内容',
    exchange        varchar(128)                       not null comment '交换机名称',
    routing_key     varchar(128)                       not null comment '路由Key',
    status          tinyint  default 0                 not null comment '0待发送 1已发送 2发送失败 3永久失败',
    retry_count     int      default 0                 not null comment '重试次数',
    next_retry_time datetime                           null comment '下次重试时间',
    create_time     datetime default CURRENT_TIMESTAMP null,
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_msg_id
    unique (msg_id)
    )
    comment '本地消息表';

create index idx_status_next_retry
    on mq_local_message (status, next_retry_time);

create table if not exists seckill_goods
(
    id            bigint auto_increment comment '秒杀活动ID（本场秒杀唯一编号）'
    primary key,
    goods_id      bigint                             not null comment '关联基础商品ID，指向goods.id',
    seckill_price decimal(10, 2)                     not null comment '秒杀活动价格',
    stock_count   int                                not null comment '秒杀库存数量（秒杀独立库存，和基础商品普通库存分开）',
    start_time    datetime                           not null comment '秒杀开始时间',
    end_time      datetime                           not null comment '秒杀结束时间',
    status        tinyint  default 0                 not null comment '活动状态：0未开始，1进行中，2已结束，3手动下架',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除'
    )
    comment '秒杀活动场次表';

create index idx_goods_id
    on seckill_goods (goods_id);

create index idx_time_status
    on seckill_goods (start_time, end_time, status);

create table if not exists seckill_order
(
    id               bigint auto_increment comment '订单主键ID'
    primary key,
    order_no         varchar(64)                        not null comment '订单编号（雪花算法，业务唯一）',
    user_id          bigint                             not null comment '下单用户ID user.id',
    seckill_goods_id bigint                             not null comment '秒杀活动ID seckill_goods.id',
    order_status     tinyint  default 0                 not null comment '订单状态：0待支付，1已支付，2超时取消，3手动取消',
    pay_time         datetime                           null comment '支付成功时间',
    expire_time      datetime                           not null comment '订单过期时间（抢购成功后+15分钟）',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    receiver_name    varchar(50)                        null comment '收货人',
    receiver_phone   varchar(20)                        null comment '收货人手机号',
    receiver_address varchar(255)                       null comment '完整收货地址',
    address_id       bigint                             null comment '关联用户收货地址id',
    is_delete        tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除',
    constraint uk_order_no
    unique (order_no)
    )
    comment '秒杀订单表';

create index idx_expire_status
    on seckill_order (expire_time, order_status);

create index idx_user_id
    on seckill_order (user_id);

create table if not exists seckill_pre_order
(
    id               bigint auto_increment comment '预订单主键 preOrderId'
    primary key,
    user_id          bigint                             not null comment '当前登录用户id',
    seckill_goods_id bigint                             not null comment '秒杀商品id',
    expire_time      datetime                           not null comment '预订单过期时间',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_user_seckill_pre
    unique (user_id, seckill_goods_id)
    )
    comment '秒杀预订单表';

create index idx_expire_time
    on seckill_pre_order (expire_time);

create index idx_user_id
    on seckill_pre_order (user_id);

create table if not exists seckill_record
(
    id               bigint auto_increment comment '抢购记录主键'
    primary key,
    user_id          bigint                             not null comment '用户id user.id',
    seckill_goods_id bigint                             not null comment '秒杀活动id seckill_goods.id',
    goods_id         bigint                             null comment '基础商品id，关联goods表主键',
    order_id         bigint                             null comment '关联秒杀订单id seckill_order.id',
    record_status    tinyint  default 0                 not null comment '抢购记录状态 0:有效(占用资格) 1:已取消(释放资格)',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '抢购记录创建时间',
    is_delete        tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除'
)
    comment '用户抢购记录表，一人一单';

create index idx_seckill_goods_id
    on seckill_record (seckill_goods_id);

create table if not exists seckill_stock
(
    id               bigint auto_increment comment '库存主键'
    primary key,
    seckill_goods_id bigint                             not null comment '关联秒杀活动id seckill_goods.id，一场秒杀一条库存',
    remaining_stock  int      default 0                 not null comment '剩余可秒杀库存',
    original_stock   int      default 0                 not null comment '本场秒杀原始总库存，用来统计',
    version          int      default 0                 not null comment '乐观锁版本号，每次库存扣减+1',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除',
    constraint uk_seckill_goods_id
    unique (seckill_goods_id)
    )
    comment '秒杀库存表';

create table if not exists user
(
    id          bigint auto_increment comment '用户主键ID'
    primary key,
    username    varchar(32)                        not null comment '账号名，全局唯一',
    password    varchar(128)                       not null comment '加密后的密码',
    phone       varchar(11)                        null comment '手机号',
    avatar      varchar(255)                       null comment '头像URL地址',
    role        tinyint  default 0                 not null comment '角色：0普通用户，1管理员',
    status      tinyint  default 1                 not null comment '账号状态：0禁用拉黑，1正常，2已注销',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除：0未删除，1已删除',
    constraint uk_username
    unique (username)
    )
    comment '用户表';

create table if not exists user_address
(
    id             bigint auto_increment comment '主键ID'
    primary key,
    user_id        bigint                             not null comment '用户id',
    receiver_name  varchar(50)                        not null comment '收货人姓名',
    receiver_phone varchar(20)                        not null comment '收货人手机号',
    province       varchar(20)                        null comment '省',
    city           varchar(20)                        null comment '市',
    district       varchar(20)                        null comment '区',
    detail_address varchar(200)                       not null comment '详细地址',
    is_default     int      default 0                 null comment '是否默认地址：0否，1是',
    is_delete      int      default 0                 null comment '逻辑删除 0未删除 1已删除',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
    )
    comment '用户收货地址表';

create index idx_user_id
    on user_address (user_id);

