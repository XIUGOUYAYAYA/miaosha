package org.miaosha.miaosha.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.SeckillGoodsCodeEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillPreOrderCodeEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillRecordCodeEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillStockCodeEnum;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.entity.SeckillPreOrder;
import org.miaosha.miaosha.mapper.SeckillGoodsMapper;
import org.miaosha.miaosha.mapper.SeckillPreOrderMapper;
import org.miaosha.miaosha.service.ISeckillPreOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.miaosha.miaosha.service.ISeckillRecordService;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 秒杀预订单服务实现类
 * 实现秒杀预下单抢占Redis库存、创建临时预订单、删除预订单并归还Redis库存的业务逻辑
 */
@Service
@Slf4j
public class SeckillPreOrderServiceImpl extends ServiceImpl<SeckillPreOrderMapper, SeckillPreOrder> implements ISeckillPreOrderService {


    @Resource
    private ISeckillRecordService seckillRecordService;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Resource
    private ISeckillStockService seckillStockService;
    @Resource
    private SeckillPreOrderMapper seckillPreOrderMapper;

    /**
     * 预下单：抢占Redis库存，创建临时预订单
     * 业务规则：校验秒杀商品及活动时间；一人一单校验；Redis预扣库存；创建有效期3分钟的预订单
     * @param seckillGoodsId 秒杀商品ID
     * @return Long 预订单主键id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long preSeckill(Long seckillGoodsId) {
        log.info("【秒杀预下单】开始执行，seckillGoodsId:{}，userId:{}", seckillGoodsId, UserContext.getUserId());
        Long userId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // 校验秒杀商品
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (seckillGoods == null) {
            log.error("【秒杀预下单失败】秒杀商品不存在 seckillGoodsId:{}", seckillGoodsId);
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_GOODS_NOT_EXIST);
        }
        if (now.isBefore(seckillGoods.getStartTime()) || now.isAfter(seckillGoods.getEndTime())) {
            log.error("【秒杀预下单失败】不在秒杀活动时间范围内 seckillGoodsId:{}", seckillGoodsId);
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_TIME_INVALID);
        }

        // 一人一单：校验该用户是否已经存在【正式秒杀订单】
        boolean count = seckillRecordService.userHasBoughtSeckillGoods(seckillGoodsId);
        if (count) {
            log.error("【秒杀预下单失败】用户已经抢购过该商品，一人一单限制 userId:{}, seckillGoodsId:{}", userId, seckillGoodsId);
            throw new BusinessException(SeckillRecordCodeEnum.SECKILL_USER_ALREADY_BUY);
        }

        // Redis扣库存
        boolean deductOk = seckillStockService.preDeductRedisStock(seckillGoodsId);
        if (!deductOk) {
            log.error("【秒杀预下单失败】Redis预扣库存失败 seckillGoodsId:{}", seckillGoodsId);
            throw new BusinessException(SeckillStockCodeEnum.DB_STOCK_RETURN_FAIL);
        }

        // 创建预订单，有效期3分钟
        SeckillPreOrder preOrder = new SeckillPreOrder();
        preOrder.setUserId(userId);
        preOrder.setSeckillGoodsId(seckillGoodsId);
        preOrder.setCreateTime(now);
        preOrder.setExpireTime(now.plusMinutes(3));
        seckillPreOrderMapper.insert(preOrder);

        log.info("【秒杀预下单】执行完成，预订单id:{}, seckillGoodsId:{}", preOrder.getId(), seckillGoodsId);
        return preOrder.getId();
    }


    /**
     * 删除预订单，同时归还Redis库存
     * 业务规则：查询预订单，物理删除；归还Redis预扣库存
     * @param preOrderId 预订单id
     * @return boolean 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePreOrder(Long preOrderId) {
        log.info("【删除预订单】开始执行，preOrderId:{}", preOrderId);
        // 根据预订单id查询预订单
        SeckillPreOrder preOrder = seckillPreOrderMapper.selectById(preOrderId);
        if (preOrder == null) {
            log.warn("【删除预订单】预订单不存在 preOrderId:{}，直接返回true", preOrderId);
            // 预订单不存在，直接返回true
            return true;
        }

        // 物理删除预订单记录
        int deleteCount = seckillPreOrderMapper.deleteById(preOrderId);
        if (deleteCount <= 0) {
            log.error("【删除预订单失败】删除数据库记录无影响行数 preOrderId:{}", preOrderId);
            throw new BusinessException(SeckillPreOrderCodeEnum.PRE_ORDER_NOT_EXIST);
        }
        // 获取秒杀商品id
        Long seckillGoodsId = preOrder.getSeckillGoodsId();
        // Redis库存 +1（和预下单扣库存成对）
        seckillStockService.returnRedisStock(seckillGoodsId);
        log.info("【删除预订单】执行完成，preOrderId:{}, seckillGoodsId:{}", preOrderId, seckillGoodsId);
        return true;
    }

}
