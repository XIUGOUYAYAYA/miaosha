package org.miaosha.miaosha.common.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.enums.SeckillGoodsStatusEnum;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.service.ISeckillGoodsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillEndTask {

    private final ISeckillGoodsService seckillGoodsService;
    // 可选：Redis锁工具类，防止多实例重复执行任务
    // private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时任务：每30秒扫描一次，检查到期秒杀活动，自动下架
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void autoOfflineExpireSeckill() {
        log.info("【秒杀定时下架任务】开始执行");
        LocalDateTime now = LocalDateTime.now();

        // 查询条件：状态=已上架PUBLISH，并且 结束时间 <= 当前时间
        List<SeckillGoods> expireList = seckillGoodsService.list(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getStatus, SeckillGoodsStatusEnum.PUBLISH.getCode())
                        .le(SeckillGoods::getEndTime, now)
        );

        if (expireList.isEmpty()) {
            log.info("【秒杀定时下架任务】暂无到期秒杀活动");
            return;
        }

        log.info("【秒杀定时下架任务】扫描到{}条到期秒杀，准备自动下架", expireList.size());
        for (SeckillGoods seckillGoods : expireList) {
            try {
                // 调用你写好的下架方法，复用结算逻辑
                seckillGoodsService.offline(seckillGoods.getId());
                log.info("【秒杀自动下架成功】seckillId={}", seckillGoods.getId());
            } catch (Exception e) {
                log.error("【秒杀自动下架失败】seckillId={}, 异常：", seckillGoods.getId(), e);
            }
        }
        log.info("【秒杀定时下架任务】本轮执行结束");
    }
}

