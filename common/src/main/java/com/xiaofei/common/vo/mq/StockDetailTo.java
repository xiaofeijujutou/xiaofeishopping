package com.xiaofei.common.vo.mq;

import lombok.Data;

/**
 * @Description: Created by IntelliJ IDEA.
 * @Author : 小肥居居头
 * @create 2024/2/22 20:37
 */

@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 锁定状态;
     */
    private Integer lockStatus;
}
