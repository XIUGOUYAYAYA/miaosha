package org.miaosha.miaosha.service;

import org.miaosha.miaosha.entity.SeckillPreOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 秒杀预订单表 服务类
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-10
 */
public interface ISeckillPreOrderService extends IService<SeckillPreOrder> {

    /**
     * 预下单：抢占库存，创建预订单
     * @param seckillGoodsId 秒杀商品id
     * @return preOrderId 预订单id
     */
    Long preSeckill(Long seckillGoodsId);

    /**
     * 删除预订单，释放库存
     * @param preOrderId 预订单id
     * @return 是否删除成功
     */
    boolean deletePreOrder(Long preOrderId);

}
