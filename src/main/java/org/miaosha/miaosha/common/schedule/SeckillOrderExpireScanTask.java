package org.miaosha.miaosha.common.schedule;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.service.ISeckillOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Slf4j
@Component
public class SeckillOrderExpireScanTask {
    @Resource
    private ISeckillOrderService seckillOrderService;

    // 每分钟执行一次，扫描超时订单
    @Scheduled(fixedRate = 60 * 1000)
    public void scanExpireOrderTask(){
        log.info("开始扫描超时未支付秒杀订单");
        seckillOrderService.closeExpireOrder();
    }
}
