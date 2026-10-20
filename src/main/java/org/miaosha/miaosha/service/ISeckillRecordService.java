package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.entity.SeckillRecord;

/**
 * 秒杀抢购记录服务接口
 */
public interface ISeckillRecordService extends IService<SeckillRecord> {

    /**
     * 判断用户是否已经抢购过该秒杀商品（一人一单校验）
     * @param seckillGoodsId 秒杀商品id
     * @return true：已抢购；false：未抢购
     */
    boolean userHasBoughtSeckillGoods(Long seckillGoodsId);


    /**
     * 创建秒杀抢购记录（抢购中状态）
     * @param seckillGoodsId 秒杀商品id
     * @return 记录实体
     */
    SeckillRecord createSeckillRecord( Long seckillGoodsId,long orderId);

    /**
     * 修改秒杀抢购记录状态
     * @param recordId 抢购记录主键ID
     * @param recordStatus 目标状态 0:有效占用资格 1:已取消释放资格
     */
    void updateSeckillRecordStatus(Long recordId, Integer recordStatus);

}
