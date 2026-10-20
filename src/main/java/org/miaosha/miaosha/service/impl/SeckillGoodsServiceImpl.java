package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.dto.seckill.goods.*;
import org.miaosha.miaosha.common.enums.SeckillGoodsStatusEnum;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.CommonErrorCodeEnum;
import org.miaosha.miaosha.common.exception.enums.GoodsCodeEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillGoodsCodeEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillStockCodeEnum;
import org.miaosha.miaosha.common.vo.seckill.goods.*;
import org.miaosha.miaosha.entity.Goods;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.entity.SeckillOrder;
import org.miaosha.miaosha.mapper.GoodsMapper;
import org.miaosha.miaosha.mapper.SeckillGoodsMapper;
import org.miaosha.miaosha.mapper.SeckillOrderMapper;
import org.miaosha.miaosha.service.IGoodsService;
import org.miaosha.miaosha.service.ISeckillGoodsService;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 秒杀商品服务实现类
 * 实现秒杀活动新增、修改、删除、上架、下架、分页查询等业务逻辑，参数校验、数据组装、数据库操作
 */
@Service
@Slf4j
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISeckillGoodsService {

    @Resource
    private IGoodsService goodsService;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private ISeckillStockService iSeckillStockService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增秒杀活动
     * 业务规则：校验基础商品合法性，同一商品不能同时存在多个进行中/待开始秒杀；秒杀价格必须低于原价、秒杀库存不能超过基础商品库存；秒杀时间必须为未来时间
     * @param addDTO 新增秒杀活动入参DTO
     * @return boolean 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSeckillGoods(SeckillGoodsAddDTO addDTO) {
        log.info("【新增秒杀活动】开始执行，入参：{}", addDTO);

        // 参数校验（Service兜底参数判空）
        if (addDTO == null) {
            log.error("【新增秒杀活动失败】入参对象为空");
            throw new BusinessException(CommonErrorCodeEnum.PARAM_IS_NULL);
        }
        // 业务规则校验
        // 基础商品是否存在
        Goods goods = goodsMapper.selectById(addDTO.getGoodsId());
        if (goods == null) {
            log.error("【新增秒杀活动失败】基础商品不存在，goodsId:{}", addDTO.getGoodsId());
            throw new BusinessException(GoodsCodeEnum.GOODS_NOT_EXIST);
        }
        // 根据商品id，统计【未开始、进行中】的秒杀记录数量
        Long activeCount = seckillGoodsMapper.selectCount(
                new LambdaQueryWrapper<SeckillGoods>()
                        .eq(SeckillGoods::getGoodsId, goods.getId())
                        .in(SeckillGoods::getStatus, Arrays.asList(0,1))
        );
        if(activeCount > 0){
            log.error("【新增秒杀活动失败】该商品已有正在进行/待开始的秒杀活动，goodsId:{}", goods.getId());
            throw new RuntimeException("该商品已有正在进行/待开始的秒杀活动，不可重复创建！");
        }
        //秒杀价格小于基础商品价格
        if (addDTO.getSeckillPrice().compareTo(goods.getOriginalPrice()) >= 0) {
            log.error("【新增秒杀活动失败】秒杀价格大于等于商品原价");
            throw new BusinessException(SeckillGoodsCodeEnum.PRICE_MUST_LESS_THAN_ORIGIN);
        }
        //秒杀库存小于基础商品库存
        if (addDTO.getStockCount() > goods.getRemainingStock()) {
            log.error("【新增秒杀活动失败】秒杀库存超过基础商品剩余库存");
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_STOCK_OVER_BASE);
        }
        //时间校验
        LocalDateTime startTime = addDTO.getStartTime();
        LocalDateTime endTime = addDTO.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        // 秒杀开始时间不能早于当前时间（必须是未来时间）
        if (startTime.isBefore(now)) {
            log.error("【新增秒杀活动失败】秒杀开始时间不能早于当前时间");
            throw new BusinessException(SeckillGoodsCodeEnum.START_TIME_CANNOT_BEFORE_NOW);
        }
        // 秒杀结束时间晚于开始时间
        if (endTime.isBefore(startTime)) {
            log.error("【新增秒杀活动失败】秒杀结束时间早于开始时间");
            throw new BusinessException(SeckillGoodsCodeEnum.END_TIME_CANNOT_BEFORE_START);
        }

        // 4.实体转换入库
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setGoodsId(addDTO.getGoodsId());
        seckillGoods.setSeckillPrice(addDTO.getSeckillPrice());
        seckillGoods.setStockCount(addDTO.getStockCount());
        seckillGoods.setStartTime(startTime);
        seckillGoods.setEndTime(endTime);
        // 新增默认状态：未上架
        seckillGoods.setStatus(SeckillGoodsStatusEnum.NOT_PUBLISH.getCode());
        boolean save = this.save(seckillGoods);
        if (!save) {
            log.error("【新增秒杀活动失败】入库数据库保存失败");
            // 你可以新建秒杀商品模块异常枚举，这里先抛业务异常
            throw new BusinessException(SeckillGoodsCodeEnum.GOODS_SAVE_FAIL);
        }
        log.info("【新增秒杀活动】执行完成，秒杀id:{}", seckillGoods.getId());
        iSeckillStockService.createStockRecord(seckillGoods.getId(), seckillGoods.getStockCount());

        return save;
    }

    /**
     * 修改秒杀活动
     * 业务规则：校验秒杀记录存在性，可修改基础商品、价格、库存、时间；价格、库存、时间字段按需校验，仅传入才做校验
     * @param updateDTO 修改秒杀活动入参DTO
     * @return boolean 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSeckillGoods(SeckillGoodsUpdateDTO updateDTO) {
        log.info("【修改秒杀活动】开始执行，入参：{}", updateDTO);

        //入参对象非空校验
        if (updateDTO == null) {
            log.error("【修改秒杀活动失败】入参对象为空");
            throw new BusinessException(CommonErrorCodeEnum.PARAM_IS_NULL);
        }

        //业务规则校验
        //查询当前要修改的秒杀记录
        SeckillGoods oldSeckillGoods = this.getById(updateDTO.getId());
        if (oldSeckillGoods == null) {
            log.error("【修改秒杀活动失败】秒杀活动不存在，id:{}", updateDTO.getId());
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_GOODS_NOT_EXIST);
        }

        //查询基础商品信息
        Goods goods = null;
        if (updateDTO.getGoodsId() != null) {
            goods = goodsMapper.selectById(updateDTO.getGoodsId());
            if (goods == null) {
                log.error("【修改秒杀活动失败】更换的基础商品不存在，goodsId:{}", updateDTO.getGoodsId());
                throw new BusinessException(GoodsCodeEnum.GOODS_NOT_EXIST);
            }
        } else {
            // 没有传goodsId，不更换基础商品，从当前秒杀记录中拿到原有goodsId，去查旧的基础商品
            goods = goodsMapper.selectById(oldSeckillGoods.getGoodsId());
        }

        //秒杀价格不为空才校验：秒杀价格必须小于商品原价
        if (updateDTO.getSeckillPrice() != null) {
            if (updateDTO.getSeckillPrice().compareTo(goods.getOriginalPrice()) >= 0) {
                log.error("【修改秒杀活动失败】秒杀价格大于等于商品原价");
                throw new BusinessException(SeckillGoodsCodeEnum.PRICE_MUST_LESS_THAN_ORIGIN);
            }
        }

        //秒杀库存不为空才校验：秒杀库存不能大于基础商品原始库存
        if (updateDTO.getStockCount() != null) {
            if (updateDTO.getStockCount() > goods.getOriginalStock()) {
                log.error("【修改秒杀活动失败】秒杀库存超过基础商品原始库存");
                throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_STOCK_OVER_BASE);
            }
        }


      //时间校验：有传开始时间才校验不能早于当前时间
        LocalDateTime startTime = updateDTO.getStartTime();
        LocalDateTime endTime = updateDTO.getEndTime();
        LocalDateTime now = LocalDateTime.now();

        if (startTime != null) {
            if (startTime.isBefore(now)) {
                log.error("【修改秒杀活动失败】秒杀开始时间不能早于当前时间");
                throw new BusinessException(SeckillGoodsCodeEnum.START_TIME_CANNOT_BEFORE_NOW);
            }
        }

       //只有【开始时间、结束时间两个都传了】，才校验结束时间晚于开始时间
        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime)) {
                log.error("【修改秒杀活动失败】秒杀结束时间早于开始时间");
                throw new BusinessException(SeckillGoodsCodeEnum.END_TIME_CANNOT_BEFORE_START);
            }
        }

        //DTO转为实体，更新数据库
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(updateDTO, seckillGoods);
        seckillGoods.setUpdateTime(LocalDateTime.now());

        boolean update = this.updateById(seckillGoods);
        log.info("【修改秒杀活动】执行完成，秒杀id:{},操作结果:{}", updateDTO.getId(), update);
        return update;
    }

    /**
     * 删除秒杀活动（逻辑删除）
     * 业务规则：存在关联秒杀订单时禁止删除；无订单才允许执行逻辑删除
     * @param id 秒杀活动id
     * @return boolean 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSeckillGoods(Long id) {
        log.info("【删除秒杀活动】开始执行，秒杀id:{}", id);

        //参数非空校验
        if (id == null) {
            log.error("【删除秒杀活动失败】id为空");
            throw new BusinessException(CommonErrorCodeEnum.PARAM_IS_NULL);
        }

        //业务校验：秒杀活动是否存在
        SeckillGoods seckillGoods = this.getById(id);
        if (seckillGoods == null) {
            log.error("【删除秒杀活动失败】id={}对应的秒杀活动不存在", id);
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_GOODS_NOT_EXIST);
        }

        // 业务校验：查询该秒杀是否存在订单，使用LambdaQueryWrapper，不手写数据库字段
        long orderCount = seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getSeckillGoodsId, id)
        );
        if (orderCount > 0) {
            log.error("【删除秒杀活动失败】id={}已存在秒杀订单，不可删除", id);
            throw new BusinessException(SeckillGoodsCodeEnum.HAS_ORDER_CAN_NOT_DELETE);
        }

        // 执行逻辑删除（MyBatis-Plus自带逻辑删除）
        boolean result = this.removeById(id);
        log.info("【删除秒杀活动】执行完成，id={},操作结果:{}", id, result);
        return result;
    }

    /**
     * 秒杀活动上架（发布）
     * 业务规则：下架后的活动不可重新上架；已上架不可重复上架；活动已结束不能上架；上架时加载库存到Redis
     * @param id 秒杀活动id
     * @return boolean 上架结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        log.info("【秒杀活动上架】开始执行，秒杀id:{}", id);

        //参数非空校验
        if (id == null) {
            log.error("【秒杀活动上架失败】id为空");
            throw new BusinessException(CommonErrorCodeEnum.PARAM_IS_NULL);
        }
        //业务校验：判断秒杀活动是否存在
        SeckillGoods seckillGoods = this.getById(id);
        if (seckillGoods == null) {
            log.error("【秒杀活动上架失败】id={}对应的秒杀活动不存在", id);
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_GOODS_NOT_EXIST);
        }
        // 新增：下架状态禁止重新上架
        if (SeckillGoodsStatusEnum.OFFLINE.getCode().equals(seckillGoods.getStatus())) {
            log.error("【秒杀活动上架失败】id={}已经下架，下架后的活动不能再次上架", id);
            throw new BusinessException(SeckillGoodsCodeEnum.OFFLINE_CANNOT_REPUBLISH);
        }
        //业务校验：已经是上架状态，禁止重复上架
        Integer status = seckillGoods.getStatus();
        if (SeckillGoodsStatusEnum.PUBLISH.getCode().equals(status)) {
            log.error("【秒杀活动上架失败】id={}已是已上架状态，不可重复上架", id);
            throw new BusinessException(SeckillGoodsCodeEnum.ALREADY_PUBLISH);
        }
        //新增校验：活动已结束不能上架
        LocalDateTime now = LocalDateTime.now();
        if (seckillGoods.getEndTime().isBefore(now)) {
            log.error("【秒杀活动上架失败】id={}秒杀活动已经结束，结束活动禁止上架", id);
            throw new BusinessException(SeckillGoodsCodeEnum.END_TIME_CANNOT_PUBLISH);
        }
        iSeckillStockService.loadStockToRedis(seckillGoods.getId());
        // 修改数据库状态为上架
        seckillGoods.setStatus(SeckillGoodsStatusEnum.PUBLISH.getCode());
        boolean result = this.updateById(seckillGoods);
        log.info("【秒杀活动上架】执行完成，id={},操作结果:{}", id, result);
        return result;
    }

    /**
     * 秒杀活动下架
     * 业务规则：仅已上架状态允许下架；下架时结算已售出库存，扣减基础商品库存，清理Redis库存缓存
     * @param id 秒杀活动id
     * @return boolean 下架结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offline(Long id) {
        log.info("【秒杀活动下架】开始执行，秒杀id:{}", id);
        //参数非空校验
        if (id == null) {
            log.error("【秒杀活动下架失败】id为空");
            throw new BusinessException(CommonErrorCodeEnum.PARAM_IS_NULL);
        }
        //业务校验：秒杀活动是否存在
        SeckillGoods seckillGoods = this.getById(id);
        if (seckillGoods == null) {
            log.error("【秒杀活动下架失败】id={}对应的秒杀活动不存在", id);
            throw new BusinessException(SeckillGoodsCodeEnum.SECKILL_GOODS_NOT_EXIST);
        }
        //业务校验：只有上架状态才允许下架
        Integer status = seckillGoods.getStatus();
        if (!SeckillGoodsStatusEnum.PUBLISH.getCode().equals(status)) {
            log.error("【秒杀活动下架失败】id={}不是上架状态，无法执行下架操作，当前状态:{}", id, status);
            throw new BusinessException(SeckillGoodsCodeEnum.NOT_ONLINE_CANNOT_OFFLINE);
        }
        //新增逻辑：结算秒杀卖出库存，扣减基础商品库存
        // ① 查询这个秒杀已经卖出多少件
        Integer soldNum = iSeckillStockService.getSoldStockNum(id);
        log.info("【秒杀活动下架结算】秒杀id:{}, 已售出数量:{}", id, soldNum);
        // ② 如果有卖出的商品，才去扣减goods主表库存；soldNum=0则不执行扣减
        boolean deductResult = goodsService.deductStock(seckillGoods.getGoodsId(), soldNum);
        // 修改状态为下架
        seckillGoods.setStatus(SeckillGoodsStatusEnum.OFFLINE.getCode());
        boolean result = this.updateById(seckillGoods);
        // 清理redis缓存库存
        iSeckillStockService.clearRedisStock(id);

        log.info("【秒杀活动下架】执行完成，id={},操作结果:{},已结算售出数量:{}", id, result, soldNum);
        return result;
    }

    /**
     * 管理端秒杀活动分页查询
     * 业务规则：后台管理员查询秒杀列表，支持分页，联表查询基础商品信息
     * @param queryDTO 后台分页查询DTO
     * @return IPage<SeckillGoodsAdminVO> 分页VO结果
     */
    @Override
    public IPage<SeckillGoodsAdminVO> adminPageQuery(SeckillGoodsAdminQueryDTO queryDTO) {
        log.info("【管理端秒杀分页查询】开始执行，入参:{}", queryDTO);
        // 构建分页对象
        Page<SeckillGoodsAdminVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 调用自定义mapper联表分页查询
        IPage<SeckillGoodsAdminVO> pageResult = seckillGoodsMapper.selectAdminPage(page, queryDTO);
        log.info("【管理端秒杀分页查询】执行完成，总条数:{}", pageResult.getTotal());
        return pageResult;
    }

    /**
     * 用户端秒杀活动分页查询
     * 业务规则：用户前端查询，只查询已上架、活动进行中的秒杀商品
     * @param queryDTO 用户端分页查询DTO
     * @return IPage<SeckillGoodsUserVO> 分页VO结果
     */
    @Override
    public IPage<SeckillGoodsUserVO> userPageQuery(SeckillGoodsUserQueryDTO queryDTO) {
        log.info("【用户端秒杀分页查询】开始执行，入参:{}", queryDTO);
        // 构建分页对象
        Page<SeckillGoodsUserVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 调用mapper执行用户端分页查询，仅查询【已上架、活动正在进行中】的秒杀商品
        IPage<SeckillGoodsUserVO> pageResult = seckillGoodsMapper.selectUserPage(page, queryDTO);
        log.info("【用户端秒杀分页查询】执行完成，总条数:{}", pageResult.getTotal());
        return pageResult;
    }

}
