package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.miaosha.miaosha.entity.MqLocalMessage;
import java.util.List;

/**
 * MQ本地消息表服务接口（生产者本地消息，实现可靠消息最终一致性）
 */
public interface IMqLocalMessageService extends IService<MqLocalMessage> {




    /**
     * 增加消息重试次数
     * @param msgId 消息唯一id
     */
    void increaseRetryCount(String msgId);

    /**
     * 保存本地待投递消息
     * @param stockMsgDTO 组装完成的本地消息实体，调用方填充：msg_content、exchange、routing_key、status、retry_count、next_retry_time
     */
    MqLocalMessage saveLocalMessage(SeckillStockMsgDTO stockMsgDTO);

    /**
     * 查询待重试消息
     * @param limit 单次查询条数，防止一次性加载大量数据压库
     * @param maxRetry 最大重试次数
     * @return 符合条件的本地消息列表
     */
    List<MqLocalMessage> listWaitSendMsg(int limit, int maxRetry);

    /**
     * 重试发送单条本地消息
     * @param mqLocalMessage 本地消息实体
     */
    boolean retrySend(MqLocalMessage mqLocalMessage);


}
