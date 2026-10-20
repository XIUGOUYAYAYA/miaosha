package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderUserQueryDTO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderAdminVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderDetailVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderUserVO;
import org.miaosha.miaosha.entity.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 秒杀订单表 服务类
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {



    /**
     * 确认下单：预订单 + 收货地址，生成正式订单
     * @param preOrderId 预订单id
     * @param addressId 收货地址id
     * @return 正式订单id
     */
    Long confirmSeckillOrder(Long preOrderId, Long addressId);

    /**
     * 订单支付
     * @param orderId 订单id
     * @return 是否支付成功
     */
    boolean payOrder(Long orderId);

    /**
     * 查询订单详情（前端展示订单详情）
     * @param orderId 订单id
     * @return 订单详情VO
     */
    SeckillOrderDetailVO getOrderDetail(Long orderId);

    /**
     * 手动取消秒杀订单
     * @param orderNo 订单ID
     * @return 取消结果
     */
    boolean cancelOrder(String orderNo);


    /**
     * 关闭超时未支付订单，定时任务调用
     */
    void closeExpireOrder();

    /**
     * 用户分页查询自己订单（精简列表VO）
     * @param queryDTO 查询条件DTO
     * @return 分页VO
     */
    IPage<SeckillOrderUserVO> userPageOrder( SeckillOrderUserQueryDTO queryDTO);

    /**
     * 管理员分页查询所有订单
     * @param queryDTO 查询条件DTO
     * @return 分页VO
     */
    IPage<SeckillOrderAdminVO> getAdminOrderPage(SeckillOrderAdminQueryDTO queryDTO);




}
