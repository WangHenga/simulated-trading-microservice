create database if not exists simulated_trading_db;

use simulated_trading_db;

drop table if exists user;
create table if not exists user(
    id int not null auto_increment primary key comment 'id',
    account varchar(20) not null comment '账号' unique,
    password varchar(100) not null comment '密码',
    balance decimal(10,2) default 1000000.00 not null comment '可用余额',
    frozenMargin decimal(10,2) default 0.00 not null comment '冻结保证金',
    usedMargin decimal(10,2) default 0.00 not null comment '占用保证金',
    isDelete tinyint default 0 not null comment '是否删除'
)engine=innodb default charset=utf8 comment '用户信息表';

drop table if exists instrument;
create table if not exists instrument
(
    id int not null auto_increment primary key comment 'id',
    name varchar(20) not null comment '合约名称' unique,
    symbol varchar(20) not null comment '合约代码' unique,
    subject varchar(20) not null comment '合约标的',
    quoteUnit varchar(20) not null comment '合约报价单位',
    minPriceChange decimal(10,2) not null comment '最小变动价位',
    maxDailyPriceFluctuation decimal(10,2) not null comment '最大变动幅度',
    minMarginRate decimal(10,2) not null comment '最小保证金率',
    multiplier decimal(10,2) not null comment '合约乘数',
    state tinyint default 0 not null comment '合约状态',
    lastPrice decimal(10,2) not null comment '最新成交价',
    settlementPrice decimal(10,2) not null comment '结算价',
    createTime datetime default current_timestamp not null comment '创建时间',
    updateTime datetime default current_timestamp not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '是否删除'
)engine=innodb default charset=utf8 comment '合约信息表';

drop table if exists orders;
create table if not exists orders
(
    id int not null primary key comment 'id',
    userId int not null comment '用户id',
    instrumentId int not null comment '合约id',
    direction tinyint not null comment '买卖方向',
    combOffsetFlag tinyint not null comment '组合开平标志',
    positionId int comment '持仓id(平仓需要）',
    limitPrice decimal(10,2) not null comment '报价',
    orderStatus tinyint default 0 not null comment '订单状态',
    volumeTraded int not null default 0 comment '成交数量',
    volumeTotal int not null default 1 comment '委托数量',
    createTime datetime default current_timestamp not null comment '创建时间',
    updateTime datetime default current_timestamp not null on update CURRENT_TIMESTAMP comment '更新时间',
    cancelTime datetime comment '撤销时间',
    isDelete tinyint default 0 not null comment '是否删除'
)engine=innodb default charset=utf8 comment '订单信息表';
create index idx_userId on orders(userId);
create index idx_direction on orders(direction);
create index idx_instrumentId on orders(instrumentId);

drop table if exists positions;
create table if not exists positions(
    id int not null auto_increment primary key comment 'id',
    userId int not null comment '用户id',
    instrumentId int not null comment '合约id',
    type tinyint not null comment '持仓类型',
    quantity int not null comment '持仓数量',
    avePrice decimal(10,2) not null comment '持仓平均成本',
    marketPrice decimal(10,2) comment '持仓市场价',
    profitLoss decimal(10,2) not null comment '持仓盈亏',
    marginOpe decimal(10,2) not null comment '持仓保证金占用',
    riskRatio decimal(10,2) comment '持仓风险度',
    openTime datetime default current_timestamp not null comment '开仓时间',
    lastUpdateTime datetime default current_timestamp not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    isDelete tinyint default 0 not null comment '是否删除'
)engine=innodb default charset=utf8 comment '客户持仓表';

CREATE INDEX idx_user_instrument_type ON positions(userId, instrumentId, type);

drop table if exists trades;

create table if not exists trades
(
    id          int not null auto_increment primary key comment 'id',
    sellOrderId int not null comment '卖单id',
    buyOrderId  int not null comment '买单id',
    price       decimal(10,2) not null comment '成交价',
    volume      int not null comment '成交量',
    createTime  datetime default current_timestamp not null comment '创建时间'
)engine=innodb default charset=utf8 comment '成交记录表';
create index idx_sellOrderId on trades(sellOrderId);
create index idx_buyOrderId on trades(buyOrderId);
