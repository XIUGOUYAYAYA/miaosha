package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.dto.seckill.stock.SeckillStockAdminQueryDTO;
import org.miaosha.miaosha.common.vo.seckill.stock.SeckillStockAdminVO;
import org.miaosha.miaosha.entity.SeckillStock;

/**
 * <p>
 * 秒杀库存表 服务类
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */
public interface ISeckillStockService extends IService<SeckillStock> {

    /**
     * 根据秒杀活动id，查询该秒杀活动已经卖出的库存数量
     * @param seckillGoodsId 秒杀活动id
     * @return 已卖出数量
     */
    Integer getSoldStockNum(Long seckillGoodsId);

    /**
     * 加载数据库秒杀库存到Redis（预热缓存，秒杀前调用）
     * @param seckillGoodsId 秒杀商品id
     * @return 是否加载成功
     */
    boolean loadStockToRedis(Long seckillGoodsId);

    /**
     * Redis预扣秒杀库存（秒杀下单时调用）
     * @param seckillGoodsId 秒杀商品id
     * @return true预扣成功，false库存不足
     */
    boolean preDeductRedisStock(Long seckillGoodsId);

    /**
     * Redis归还秒杀库存（事务回滚时自动归还Redis预扣库存，Lua原子脚本防止重复归还）
     * @param seckillGoodsId 秒杀商品id
     */
    void returnRedisStock(Long seckillGoodsId);

    /**
     * 管理员分页查询库存
     * @param queryDTO 查询条件：分页、秒杀商品ID、剩余库存区间
     * @return 分页VO
     */
    IPage<SeckillStockAdminVO> adminStockPage(SeckillStockAdminQueryDTO queryDTO);

    /**
     * MQ消费：数据库真实扣减库存（监听RabbitMQ消息执行，最终落库）
     * @param seckillGoodsId 秒杀商品id
     * @param orderNo 订单号，用于幂等控制
     * @return true扣减成功；false扣减失败（库存不足/乐观锁冲突，需要重试）
     */
    boolean deductStockInDb(Long seckillGoodsId, String orderNo);

    /**
     * 归还数据库秒杀库存（订单超时/取消订单时执行，数据库库存+1，乐观锁+幂等防止重复归还）
     * @param seckillGoodsId 秒杀商品id
     * @param orderNo 订单号，用于幂等控制
     * @return true归还成功；false归还失败，需要重试
     */
    boolean returnStockInDb(Long seckillGoodsId, String orderNo);

    /**
     * 创建秒杀库存记录
     * @param seckillGoodsId 秒杀商品id
     * @param stockCount 库存数量
     * @return 成功/失败
     */
    boolean createStockRecord(Long seckillGoodsId, Integer stockCount);

    /**
     * 清除指定秒杀商品在Redis中的库存缓存
     * @param seckillGoodsId 秒杀商品ID
     * @return true 删除成功；false key不存在或者删除失败
     */
    boolean clearRedisStock(Long seckillGoodsId);


}
