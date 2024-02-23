package com.xiaofei.common.vo.mq;

import lombok.Data;

import java.util.List;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/22 19:32
 */

@Data
public class StockLockedTo {
    /**
     * 库存工作单的id,保存了某个用户一次下单的所有数据(是一个实体类id,里面保存了orderSn);
     */
    private Long id;
    /**
     * TODO 改成用户一次下单多件商品,这就是每件商品的锁库存情况;private List<Long> detailId;
     * 数据库锁库存表的实体类;这里改成实体类而不是id的原因是:
     *      防止这种情况==>mq里面存了两个id,数据库里面没有记录(因为回滚了),但是库存锁了n个没有回滚;
     *                   这个时候要回滚,数据库查不到;(本服务不会有这种情况,但是其他业务可能会有);
     */
    private StockDetailTo detailTo;
}
