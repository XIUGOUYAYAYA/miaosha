package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.dto.seckill.stock.SeckillStockAdminQueryDTO;
import org.miaosha.miaosha.common.enums.SeckillOrderStatusEnum;
import org.miaosha.miaosha.common.enums.SeckillStockBizStatusEnum;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.SeckillStockCodeEnum;
import org.miaosha.miaosha.common.vo.seckill.stock.SeckillStockAdminVO;
import org.miaosha.miaosha.entity.SeckillOrder;
import org.miaosha.miaosha.entity.SeckillStock;
import org.miaosha.miaosha.mapper.SeckillOrderMapper;
import org.miaosha.miaosha.mapper.SeckillStockMapper;
import org.miaosha.miaosha.service.IMqConsumeIdempotentService;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀库存服务实现类
 * 负责Redis库存预热、Redis预扣减/归还、数据库真实库存扣减与归还、管理员库存分页查询、幂等控制
 */
@Slf4j
@Service
public class SeckillStockServiceImpl extends ServiceImpl<SeckillStockMapper, SeckillStock> implements ISeckillStockService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IMqConsumeIdempotentService mqConsumeIdempotentService;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    // 注入RedisTemplate
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 加载数据库秒杀库存到Redis（预热缓存，秒杀前调用）
     * 业务规则：将DB库存加载进Redis，防止重复预热，Redis作为库存预扣减缓存层
     * @param seckillGoodsId 秒杀商品id
     * @return 是否加载成功
     */
    @Override
    public boolean loadStockToRedis(Long seckillGoodsId) {
        log.info("【库存预热】加载库存到Redis开始，seckillGoodsId:{}", seckillGoodsId);
        String key = "seckill:stock:" + seckillGoodsId;
        // 防止重复预热，存在则直接返回
        Boolean exists = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.info("【库存预热】Redis库存key已存在，无需重复加载 seckillGoodsId:{}", seckillGoodsId);
            return true;
        }

        SeckillStock seckillStock = this.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillStock>()
                        .eq(SeckillStock::getSeckillGoodsId, seckillGoodsId)
        );
        if (seckillStock == null) {
            log.error("【库存预热失败】库存记录不存在 seckillGoodsId:{}", seckillGoodsId);
            return false;
        }
        stringRedisTemplate.opsForValue().set(key, seckillStock.getRemainingStock().toString());
        log.info("【库存预热】库存加载Redis完成 seckillGoodsId:{},剩余库存:{}", seckillGoodsId, seckillStock.getRemainingStock());
        return true;
    }

    /**
     * Redis预扣秒杀库存（秒杀下单时调用）
     * 业务规则：Lua脚本原子执行，判断库存>=1才扣减，保证并发下原子性
     * @param seckillGoodsId 秒杀商品id
     * @return true预扣成功，false库存不足
     */
    @Override
    public boolean preDeductRedisStock(Long seckillGoodsId) {
        log.info("【Redis预扣库存】开始执行 seckillGoodsId:{}", seckillGoodsId);
        String key = "seckill:stock:" + seckillGoodsId;
        // Lua脚本：原子判断库存，>=1才扣减1，返回1成功，0失败
        String luaScript = "local stock = redis.call('get',KEYS[1]);" +
                "if stock == nil then return 0 end;" +
                "local num = tonumber(stock);" +
                "if num >= 1 then " +
                "    redis.call('decr',KEYS[1]);" +
                "    return 1;" +
                "end;" +
                "return 0;";

        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key)
        );
        // result=1 扣减成功；0 库存不足/key不存在
        boolean deductResult = Long.valueOf(1).equals(result);
        log.info("【Redis预扣库存】执行结果 seckillGoodsId:{},扣减结果:{}", seckillGoodsId, deductResult);
        return deductResult;
    }

    /**
     * Redis归还秒杀库存（事务回滚时自动归还Redis预扣库存，Lua原子脚本防止重复归还）
     * 业务规则：Lua脚本原子+1，用于预下单失败/取消订单归还Redis缓存库存
     * @param seckillGoodsId 秒杀商品id
     */
    @Override
    public void returnRedisStock(Long seckillGoodsId) {
        log.info("【Redis归还库存】开始执行 seckillGoodsId:{}", seckillGoodsId);
        String key = "seckill:stock:" + seckillGoodsId;
        // JDK8 不能用三引号文本块，改用单行Lua字符串
        String luaScript = "local stock = redis.call('get',KEYS[1]);" +
                "if stock == nil then return nil end;" +
                "local num = tonumber(stock);" +
                "if num >= 0 then return redis.call('incr',KEYS[1]) end;" +
                "return num;";

        stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key)
        );
        log.info("【Redis归还库存】执行完成 seckillGoodsId:{}", seckillGoodsId);
    }



    /**
     * 管理员分页查询库存
     * 业务规则：管理员后台分页查询库存记录，支持秒杀商品ID、库存区间筛选，entity转VO返回
     * @param queryDTO 查询条件：分页、秒杀商品ID、剩余库存区间
     * @return 分页VO
     */
    @Override
    public IPage<SeckillStockAdminVO> adminStockPage(SeckillStockAdminQueryDTO queryDTO) {
        log.info("【管理员库存分页查询】pageNum:{},pageSize:{}", queryDTO.getPageNum(), queryDTO.getPageSize());
        // 构建分页对象
        Page<SeckillStock> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建动态查询条件
        LambdaQueryWrapper<SeckillStock> wrapper = new LambdaQueryWrapper<>();
        // 秒杀商品id，不为null才拼接
        if (queryDTO.getSeckillGoodsId() != null) {
            wrapper.eq(SeckillStock::getSeckillGoodsId, queryDTO.getSeckillGoodsId());
        }
        // 剩余库存最小值
        if (queryDTO.getMinRemainingStock() != null) {
            wrapper.ge(SeckillStock::getRemainingStock, queryDTO.getMinRemainingStock());
        }
        // 剩余库存最大值
        if (queryDTO.getMaxRemainingStock() != null) {
            wrapper.le(SeckillStock::getRemainingStock, queryDTO.getMaxRemainingStock());
        }
        // 按id升序，也可以改成库存排序
        wrapper.orderByAsc(SeckillStock::getId);

        // 执行分页查询，查Entity
        IPage<SeckillStock> stockPage = this.page(page, wrapper);

        // Entity 转 VO
        IPage<SeckillStockAdminVO> voPage = new Page<>();
        // 分页基础信息拷贝
        voPage.setCurrent(stockPage.getCurrent());
        voPage.setSize(stockPage.getSize());
        voPage.setTotal(stockPage.getTotal());
        voPage.setPages(stockPage.getPages());

        List<SeckillStockAdminVO> voList = stockPage.getRecords().stream()
                .map(stock -> {
                    SeckillStockAdminVO vo = new SeckillStockAdminVO();
                    vo.setId(stock.getId());
                    vo.setSeckillGoodsId(stock.getSeckillGoodsId());
                    vo.setRemainingStock(stock.getRemainingStock());
                    vo.setOriginalStock(stock.getOriginalStock());
                    vo.setCreateTime(stock.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        log.info("【管理员库存分页查询】查询完成，总条数:{}", stockPage.getTotal());
        return voPage;
    }

    /**
     * MQ消费：数据库真实扣减库存（监听RabbitMQ消息执行，最终落库）
     * 业务规则：MQ消费执行DB库存扣减，幂等判断；乐观锁扣减；扣减成功才写入幂等记录
     * @param seckillGoodsId 秒杀商品id
     * @param orderNo 订单号，用于幂等控制、查询订单
     * @return true扣减成功；false扣减失败（库存不足/乐观锁冲突，需要重试）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockInDb(Long seckillGoodsId, String orderNo) {
        log.info("【DB扣减库存MQ消费】开始，orderNo:{},seckillGoodsId:{}", orderNo, seckillGoodsId);
        // 1.校验：是否已经成功扣减过库存（bizStatus=1代表扣减成功）
        boolean alreadyDeductSuccess = mqConsumeIdempotentService.existsByOrderNoAndBizStatus(orderNo, SeckillStockBizStatusEnum.STOCK_DEDUCT_SUCCESS.getCode());
        if (alreadyDeductSuccess) {
            log.info("订单{}已经完成数据库库存扣减，幂等拦截", orderNo);
            return true;
        }

        // 2.通过订单号查询订单（不再用orderId查询）
        LambdaQueryWrapper<SeckillOrder> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(SeckillOrder::getOrderNo, orderNo);
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(orderQueryWrapper);
        if (seckillOrder == null) {
            log.warn("扣库存消息，订单不存在 orderNo:{}", orderNo);
            return true;
        }

        // 订单状态不是待支付、已支付，不执行扣减，不写入扣减成功幂等记录
        Integer status = seckillOrder.getOrderStatus();
        if (!(status.equals(SeckillOrderStatusEnum.WAIT_PAY.getCode())|| status.equals(SeckillOrderStatusEnum.PAID.getCode()))) {
            log.warn("扣库存消息，订单状态已取消/超时，不扣库存 orderNo:{}", orderNo);
            return true;
        }

        // 乐观锁扣减库存
        LambdaUpdateWrapper<SeckillStock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeckillStock::getSeckillGoodsId, seckillGoodsId)
                .gt(SeckillStock::getRemainingStock, 0)
                .setSql("remaining_stock = remaining_stock - 1, version = version + 1");

        int rows = this.baseMapper.update(null, updateWrapper);
        if (rows <= 0) {
            log.warn("数据库库存扣减失败，乐观锁冲突或库存不足 orderNo:{}", orderNo);
            //重点：扣减失败，不插入bizStatus=1幂等记录
            return false;
        }

        // 只有库存扣减SQL执行成功，才插入【扣减成功】幂等记录 bizStatus=1
        mqConsumeIdempotentService.saveIdempotentRecord(orderNo, seckillGoodsId, SeckillStockBizStatusEnum.STOCK_DEDUCT_SUCCESS.getCode());
        log.info("订单{}数据库库存扣减成功", orderNo);
        return true;
    }


    /**
     * 归还数据库秒杀库存（订单超时/取消订单时执行，数据库库存+1，乐观锁+幂等防止重复归还）
     * 业务规则：取消/超时订单归还DB库存；幂等控制，只有扣减成功的订单才允许归还；归还成功写入幂等记录
     * @param seckillGoodsId 秒杀商品id
     * @param orderNo 订单号，用于幂等控制
     * @return true归还成功；false归还失败，需要重试
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnStockInDb(Long seckillGoodsId, String orderNo) {
        log.info("【DB归还库存MQ消费】开始，orderNo:{},seckillGoodsId:{}", orderNo, seckillGoodsId);
        // 【核心校验】必须存在扣减成功记录(bizStatus=1)，才代表DB库存被扣减，才允许归还
        boolean hasDeductSuccessRecord = mqConsumeIdempotentService.existsByOrderNoAndBizStatus(orderNo, SeckillStockBizStatusEnum.STOCK_DEDUCT_SUCCESS.getCode());
        if (!hasDeductSuccessRecord) {
            log.info("订单{}没有数据库扣减成功记录，库存未扣，无需归还", orderNo);
            return true;
        }

        // 判断是否已经归还过
        boolean alreadyReturn = mqConsumeIdempotentService.existsByOrderNoAndBizStatus(orderNo, SeckillStockBizStatusEnum.STOCK_RETURN_SUCCESS.getCode());
        if (alreadyReturn) {
            log.info("订单{}库存已经归还，幂等拦截", orderNo);
            return true;
        }

        // 乐观锁归还库存 +1
        LambdaUpdateWrapper<SeckillStock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeckillStock::getSeckillGoodsId, seckillGoodsId)
                .setSql("remaining_stock = remaining_stock + 1, version = version + 1");

        int rows = this.baseMapper.update(null, updateWrapper);
        if (rows <= 0) {
            log.error("归还数据库库存失败，seckillGoodsId:{},orderNo:{}", seckillGoodsId, orderNo);
            return false;
        }

        // 归还成功，才插入归还成功幂等记录 bizStatus=2
        mqConsumeIdempotentService.saveIdempotentRecord(orderNo, seckillGoodsId, SeckillStockBizStatusEnum.STOCK_RETURN_SUCCESS.getCode());
        log.info("订单{}数据库库存归还成功", orderNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createStockRecord(Long seckillGoodsId, Integer stockCount) {
        log.info("【创建库存记录】开始 seckillGoodsId:{},stockCount:{}", seckillGoodsId, stockCount);
        // 参数校验
        if (seckillGoodsId == null || stockCount == null || stockCount <= 0) {
            log.error("【创建库存记录失败】参数非法 seckillGoodsId:{},stockCount:{}", seckillGoodsId, stockCount);
            throw new BusinessException(SeckillStockCodeEnum.STOCK_PARAM_INVALID);
        }
        // 校验：该秒杀商品是否已经存在库存记录，防止重复创建
        LambdaQueryWrapper<SeckillStock> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SeckillStock::getSeckillGoodsId, seckillGoodsId);
        long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("秒杀商品{}库存记录已存在，禁止重复创建", seckillGoodsId);
            throw new BusinessException(SeckillStockCodeEnum.STOCK_RECORD_EXIST);
        }

        // 组装库存实体
        SeckillStock seckillStock = new SeckillStock();
        seckillStock.setSeckillGoodsId(seckillGoodsId);
        seckillStock.setOriginalStock(stockCount);
        seckillStock.setRemainingStock(stockCount);
        seckillStock.setVersion(0); // 乐观锁初始版本号0
        seckillStock.setCreateTime(LocalDateTime.now());
        seckillStock.setUpdateTime(LocalDateTime.now());

        int insertRows = baseMapper.insert(seckillStock);
        if (insertRows <= 0) {
            log.error("秒杀商品{}库存记录入库失败", seckillGoodsId);
            throw new BusinessException(SeckillStockCodeEnum.STOCK_CREATE_FAIL);
        }
        log.info("【创建库存记录】成功 seckillGoodsId:{}", seckillGoodsId);
        return true;
    }

    /**
     * 清除指定秒杀商品Redis库存缓存
     * 业务规则：删除Redis中的秒杀库存key
     * @param seckillGoodsId 秒杀商品id
     * @return true 删除成功，false key不存在
     */
    @Override
    public boolean clearRedisStock(Long seckillGoodsId) {
        log.info("【清除Redis库存缓存】seckillGoodsId:{}", seckillGoodsId);
        // 参数校验
        if (seckillGoodsId == null || seckillGoodsId <= 0) {
            throw new IllegalArgumentException("秒杀商品ID不能为空且必须大于0");
        }
        String key = "seckill:stock:" + seckillGoodsId;
        // 删除redis key
        boolean delResult = redisTemplate.delete(key);
        log.info("【清除Redis库存缓存】执行结果:{} seckillGoodsId:{}", delResult, seckillGoodsId);
        return delResult;
    }

    /**
     * 统计秒杀活动已经卖出的库存
     * 业务规则：原始库存减去剩余库存，得到已售数量，兜底防止负数
     * @param getSeckillGoodsId 秒杀活动ID
     * @return 已售出库存数量
     */
    @Override
    public Integer getSoldStockNum(Long getSeckillGoodsId) {
        log.info("【统计已售库存】seckillGoodsId:{}", getSeckillGoodsId);
        // 查询这条秒杀库存记录
        SeckillStock stockRecord = this.getOne(
                new LambdaQueryWrapper<SeckillStock>()
                        .eq(SeckillStock::getSeckillGoodsId, getSeckillGoodsId)
        );
        if (stockRecord == null) {
            log.error("【统计已售库存失败】库存记录不存在 seckillGoodsId:{}", getSeckillGoodsId);
            throw new RuntimeException("该秒杀库存记录不存在");
        }
        // 分配秒杀总库存 - 当前剩余库存 = 已经卖出去的数量
        Integer soldNum = stockRecord.getOriginalStock() - stockRecord.getRemainingStock();
        // 兜底，防止负数
        if(soldNum < 0){
            soldNum = 0;
        }
        log.info("【统计已售库存】seckillGoodsId:{},已售数量:{}", getSeckillGoodsId, soldNum);
        return soldNum;
    }


}
