use simulated_trading_db;

select sum(marginOpe), userId
from positions
group by userId;

select id, balance + frozenMargin + usedMargin
from user;

select sum(limitPrice * volumeTotal / 10), userId
from orders
where combOffsetFlag = 0 and isDelete=0
group by userId
order by userId;

select max(id) from orders;

update user set balance=1000000.00, frozenMargin=0.00, usedMargin=0.00;