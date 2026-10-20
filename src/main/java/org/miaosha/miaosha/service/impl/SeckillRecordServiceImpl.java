package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.SeckillRecordCodeEnum;
import org.miaosha.miaosha.common.enums.SeckillRecordStatusEnum;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.entity.SeckillRecord;
import org.miaosha.miaosha.mapper.SeckillGoodsMapper;
import org.miaosha.miaosha.mapper.SeckillRecordMapper;
import org.miaosha.miaosha.service.ISeckillGoodsService;
import org.miaosha.miaosha.service.ISeckillRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 秒杀抢购记录服务实现类
 * 负责一人一单校验、创建抢购记录、修改抢购记录状态，控制用户抢购资格
 */
@Slf4j
@Service
public class SeckillRecordServiceImpl extends ServiceImpl<SeckillRecordMapper, SeckillRecord>
        implements ISeckillRecordService {

    @Resource
    SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 判断用户是否已经抢购过该秒杀商品（一人一单校验）
     * 业务规则：只判断状态0【已抢购有效记录】；状态1【已取消释放资格】不计入，可重新抢购
     * @param seckillGoodsId 秒杀商品ID
     * @return true：存在有效抢购记录（已经抢到，不能重复抢）；false：无有效记录，可以参与抢购
     */
    @Override
    public boolean userHasBoughtSeckillGoods(Long seckillGoodsId) {
        log.info("【一人一单校验】开始执行，seckillGoodsId:{}, userId:{}", seckillGoodsId, UserContext.getUserId());
        Long userId = UserContext.getUserId();
        //  通过秒杀活动id，查询对应的基础商品goodsId
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (seckillGoods == null) {
            log.warn("【一人一单校验】秒杀商品不存在 seckillGoodsId:{}，直接放行", seckillGoodsId);
            // 秒杀活动不存在
            return false;
        }
        Long goodsId = seckillGoods.getGoodsId();
        LambdaQueryWrapper<SeckillRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillRecord::getUserId, userId);
        wrapper.eq(SeckillRecord::getGoodsId, goodsId);
        // 核心：只查询状态=0（有效占用资格），状态1取消的不算
        wrapper.eq(SeckillRecord::getRecordStatus, SeckillRecordStatusEnum.HAS_BUY.getCode().byteValue());
        // exists高效判断，找到第一条即返回，不会全表count
        boolean hasRecord = this.exists(wrapper);
        log.info("【一人一单校验】执行完成，seckillGoodsId:{}, userId:{}, 是否已有抢购记录:{}", seckillGoodsId, userId, hasRecord);
        return hasRecord;
    }


    /**
     * 创建秒杀抢购记录（抢购中状态）
     * 业务规则：新增抢购记录，标记为有效占用资格，用于一人一单控制
     * @param seckillGoodsId 秒杀商品id
     * @param orderId 正式订单id
     * @return SeckillRecord 新增的抢购记录实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillRecord createSeckillRecord(Long seckillGoodsId, long orderId) {
        log.info("【创建秒杀抢购记录】开始执行，userId:{}, seckillGoodsId:{}", UserContext.getUserId(), seckillGoodsId);
        Long userId = UserContext.getUserId();
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(seckillGoodsId);
        Long goodId = seckillGoods.getGoodsId();
        SeckillRecord seckillRecord = new SeckillRecord();
        seckillRecord.setUserId(userId);
        seckillRecord.setSeckillGoodsId(seckillGoodsId);
        seckillRecord.setGoodsId(goodId);
        seckillRecord.setOrderId(orderId);
        // recordStatus：0 有效占用
        seckillRecord.setRecordStatus(SeckillRecordStatusEnum.HAS_BUY.getCode());
        seckillRecord.setCreateTime(LocalDateTime.now());
        boolean saveRecord = save(seckillRecord);
        if (!saveRecord) {
            log.error("【创建秒杀抢购记录失败】入库失败！userId:{}, seckillGoodsId:{}, orderId:{}", userId, seckillGoodsId, orderId);
            // 抛出业务异常，触发事务回滚，前面创建的订单会一起回滚
            throw new BusinessException(SeckillRecordCodeEnum.CREATE_RECORD_FAIL);
        }
        log.info("【创建秒杀抢购记录】执行成功，recordId:{}", seckillRecord.getId());
        return seckillRecord;
    }

    /**
     * 修改秒杀抢购记录状态
     * 业务规则：更新抢购记录状态，0=有效占用资格，1=已取消释放抢购资格
     * @param recordId 抢购记录主键ID
     * @param recordStatus 目标状态 0:有效占用资格 1:已取消释放资格
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillRecordStatus(Long recordId, Integer recordStatus) {
        log.info("【更新抢购记录状态】开始执行，recordId:{},目标状态:{}", recordId, recordStatus);
        // 参数校验
        if (recordId == null || recordId <= 0) {
            log.error("【更新抢购记录状态失败】recordId非法");
            throw new BusinessException(SeckillRecordCodeEnum.RECORD_ID_INVALID);
        }
        if (recordStatus == null || (!recordStatus.equals(SeckillRecordStatusEnum.HAS_BUY.getCode()) && !recordStatus.equals(SeckillRecordStatusEnum.CANCEL.getCode()))) {
            log.error("【更新抢购记录状态失败】状态值非法，recordStatus:{}", recordStatus);
            throw new BusinessException(SeckillRecordCodeEnum.RECORD_STATUS_INVALID);
        }

        // LambdaUpdateWrapper：只更新recordStatus字段
        LambdaUpdateWrapper<SeckillRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeckillRecord::getId, recordId);
        updateWrapper.set(SeckillRecord::getRecordStatus, recordStatus);
        boolean updateSuccess = update(updateWrapper);
        if (!updateSuccess) {
            log.error("【更新抢购记录状态失败】数据库未更新，recordId:{}", recordId);
            throw new BusinessException(SeckillRecordCodeEnum.UPDATE_RECORD_FAIL);
        }
        log.info("【更新抢购记录状态】执行成功，recordId:{},更新后状态:{}", recordId, recordStatus);
    }

}
