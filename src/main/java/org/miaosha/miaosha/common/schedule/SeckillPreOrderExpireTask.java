package org.miaosha.miaosha.common.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.entity.SeckillPreOrder;
import org.miaosha.miaosha.mapper.SeckillPreOrderMapper;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SeckillPreOrderExpireTask {

    @Resource
    private SeckillPreOrderMapper seckillPreOrderMapper;

    @Resource
    private ISeckillStockService seckillStockService;

    /**
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void scanExpirePreOrder() {
        LocalDateTime now = LocalDateTime.now();
        log.info("【预订单过期定时任务】开始执行，当前时间：{}", now);

        // ========= MyBatis-Plus Lambda条件构造器查询 =========
        List<SeckillPreOrder> expirePreOrderList = seckillPreOrderMapper.selectList(
                new LambdaQueryWrapper<SeckillPreOrder>()
                        .le(SeckillPreOrder::getExpireTime, now)
                        .last("LIMIT 100")
        );

        if (expirePreOrderList == null || expirePreOrderList.isEmpty()) {
            log.info("【预订单过期定时任务】暂无过期预订单");
            return;
        }
        log.info("【预订单过期定时任务】扫描到{}条过期预订单", expirePreOrderList.size());

        for (SeckillPreOrder preOrder : expirePreOrderList) {
            try {
                // 归还redis库存
                seckillStockService.returnRedisStock(preOrder.getSeckillGoodsId());
                // 物理删除预订单
                seckillPreOrderMapper.deleteById(preOrder.getId());
                log.info("【预订单过期定时任务】处理完成，预订单id:{},秒杀商品id:{}", preOrder.getId(), preOrder.getSeckillGoodsId());
            } catch (Exception e) {
                log.error("【预订单过期定时任务】处理预订单id:{}异常", preOrder.getId(), e);
                // 单个订单失败不阻断其他订单
            }
        }
        log.info("【预订单过期定时任务】本轮任务执行结束");
    }
}
