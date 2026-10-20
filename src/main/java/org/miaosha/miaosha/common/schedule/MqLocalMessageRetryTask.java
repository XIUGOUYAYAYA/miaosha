package org.miaosha.miaosha.common.schedule;

import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.entity.MqLocalMessage;
import org.miaosha.miaosha.service.IMqLocalMessageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class MqLocalMessageRetryTask {

    // 单次最多查询处理条数，防止一次性加载大量数据压垮数据库
    private static final int BATCH_LIMIT = 10;
    // 生产者最大投递重试次数：最多重试3次
    private static final int MAX_SEND_RETRY = 3;

    @Resource
    private IMqLocalMessageService mqLocalMessageService;

    /**
     * 定时扫描待重试消息，每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void scanAndRetryMessage() {
        log.info("【本地消息重试定时任务】开始执行");
        // 查询待重试消息
        List<MqLocalMessage> waitMsgList = mqLocalMessageService.listWaitSendMsg(BATCH_LIMIT, MAX_SEND_RETRY);
        if (waitMsgList == null || waitMsgList.isEmpty()) {
            log.info("【本地消息重试定时任务】暂无待重试消息");
            return;
        }
        for (MqLocalMessage message : waitMsgList) {
           mqLocalMessageService.retrySend(message);
        }
    }
}
