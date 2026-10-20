package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.dto.goods.GoodsAddDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsQueryDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsUpdateDTO;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.GoodsCodeEnum;
import org.miaosha.miaosha.common.vo.goods.GoodsPageVO;
import org.miaosha.miaosha.entity.Goods;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.mapper.GoodsMapper;
import org.miaosha.miaosha.service.IGoodsService;
import org.miaosha.miaosha.service.ISeckillGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * <p>
 * 基础商品表 服务实现类
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */

/**
 * 商品服务实现类
 * 实现商品模块所有业务逻辑，参数校验、数据组装、数据库操作
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private ISeckillGoodsService seckillGoodsService;


    /**
     * 扣减基础商品库存【只扣减】
     * @param goodsId 商品ID
     * @param count 扣减数量（正数，例如扣60件传60）
     * @return 是否扣减成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long goodsId, Integer count) {
        log.info("【商品库存扣减】开始执行，商品id:{},扣减数量:{}", goodsId, count);
        // 参数校验：扣减数量必须大于0
        if(count == null || count < 0){
            log.error("【商品库存扣减失败】扣减数量非法，goodsId:{},count:{}", goodsId, count);
            throw new RuntimeException("扣减数量必须大于0");
        }
        // 调用mapper原子扣减SQL
        int row = goodsMapper.deductStockSql(goodsId, count);
        // row>0 代表更新成功（库存足够，扣减成功）
        if (row <= 0) {
            // 扣减失败，在此处抛出业务异常，交给上层事务回滚
            log.error("【商品库存扣减失败】库存不足或商品不存在，goodsId:{},扣减数量:{}", goodsId, count);
            throw new RuntimeException("商品库存扣减失败，库存不足或商品不存在，goodsId:" + goodsId + ",扣减数量:" + count);
        }
        log.info("【商品库存扣减】执行完成，商品id:{},扣减数量:{}", goodsId, count);
        return true;
    }


    /**
     * 新增商品
     * 业务规则：商品名称不能重复
     * @param goodsAddDTO 新增商品入参DTO
     * @return boolean
     */
    @Override
    public boolean addGoods(GoodsAddDTO goodsAddDTO) {
        log.info("【新增商品】开始执行，商品名称:{}", goodsAddDTO.getGoodsName());
        // 业务校验：判断商品名称是否已经存在
        LambdaQueryWrapper<Goods> nameCheckWrapper = new LambdaQueryWrapper<>();
        nameCheckWrapper.eq(Goods::getGoodsName, goodsAddDTO.getGoodsName());
        long count = count(nameCheckWrapper);
        if (count > 0) {
            log.error("【新增商品失败】商品名称已存在，商品名称:{}", goodsAddDTO.getGoodsName());
            throw new BusinessException(GoodsCodeEnum.GOODS_NAME_EXIST);
        }

        // DTO转实体：组装入库数据
        Goods goods = new Goods();
        goods.setGoodsName(goodsAddDTO.getGoodsName());
        goods.setGoodsImg(goodsAddDTO.getGoodsImg());
        goods.setOriginalPrice(goodsAddDTO.getOriginalPrice());
        goods.setOriginalStock(goodsAddDTO.getOriginalStock());
        goods.setGoodsDesc(goodsAddDTO.getGoodsDesc());
        // 新增商品：剩余库存 = 原始库存
        goods.setRemainingStock(goodsAddDTO.getOriginalStock());

        // 执行插入
        boolean result = save(goods);
        log.info("【新增商品】执行完成，商品名称:{},操作结果:{}", goodsAddDTO.getGoodsName(), result);
        return result;
    }

    /**
     * 修改商品
     * 业务规则：商品变更后，同步更新秒杀活动关联信息（Spring事件扩展）
     * @param goodsUpdateDTO 修改商品入参DTO
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGoods(GoodsUpdateDTO goodsUpdateDTO) {
        log.info("【修改商品】开始执行，商品id:{}", goodsUpdateDTO.getId());
        // 1.查询原有商品
        Goods oldGoods = getById(goodsUpdateDTO.getId());
        if (oldGoods == null) {
            log.error("【修改商品失败】商品不存在，商品id:{}", goodsUpdateDTO.getId());
            throw new BusinessException(GoodsCodeEnum.GOODS_NOT_EXIST);
        }

        // 2.DTO转实体，组装更新数据
        Goods goods = new Goods();
        goods.setId(goodsUpdateDTO.getId());
        goods.setGoodsName(goodsUpdateDTO.getGoodsName());
        goods.setGoodsImg(goodsUpdateDTO.getGoodsImg());
        goods.setOriginalPrice(goodsUpdateDTO.getOriginalPrice());
        goods.setOriginalStock(goodsUpdateDTO.getOriginalStock());
        goods.setGoodsDesc(goodsUpdateDTO.getGoodsDesc());
        // 修改原始库存，同步更新剩余库存
        goods.setRemainingStock(goodsUpdateDTO.getOriginalStock()-oldGoods.getOriginalStock()+oldGoods.getRemainingStock());

        // 3.执行更新
        boolean updateResult = updateById(goods);
        log.info("【修改商品】执行完成，商品id:{},操作结果:{}", goodsUpdateDTO.getId(), updateResult);
        return updateResult;
    }

    /**
     * 删除商品（逻辑删除）
     * 业务规则：存在关联秒杀活动 / 关联订单时，禁止删除
     * @param goodsId 商品主键id
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGoods(Long goodsId) {
        log.info("【商品删除】开始执行，商品id:{}", goodsId);
        // 根据id查询商品，判断商品是否存在
        Goods goods = getById(goodsId);
        if (goods == null) {
            log.error("【商品删除失败】商品不存在，商品id:{}", goodsId);
            throw new BusinessException(GoodsCodeEnum.GOODS_NOT_EXIST);
        }

        // 业务校验：检查是否关联秒杀活动
        LambdaQueryWrapper<SeckillGoods> seckillWrapper = new LambdaQueryWrapper<>();
        seckillWrapper.eq(SeckillGoods::getGoodsId, goodsId);
        boolean hasSeckill = seckillGoodsService.getOne(seckillWrapper, false) != null;
        if (hasSeckill) {
            log.error("【商品删除失败】商品关联秒杀活动，商品id:{}, hasSeckill:{}", goodsId, hasSeckill);
            throw new BusinessException(GoodsCodeEnum.GOODS_CAN_NOT_DELETE_HAS_SECKILL);
        }

        // 执行逻辑删除
        boolean result = removeById(goodsId);
        log.info("【商品删除】执行完成，商品id:{}, 删除结果:{}", goodsId, result);
        return result;
    }

    /**
     * 商品分页条件查询
     * 业务规则：支持商品名称模糊检索，分页返回商品基础信息列表
     * @param queryDTO 分页查询条件DTO（页码、页大小、商品名称）
     * @return IPage<GoodsPageVO> 分页VO结果
     */
    @Override
    public IPage<GoodsPageVO> pageGoods(GoodsQueryDTO queryDTO) {
        log.info("【商品分页查询】开始执行，查询条件:{}", queryDTO);

        Page<Goods> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        // 商品名称模糊查询，非空才拼接条件，避免无效like查询
        if (queryDTO.getGoodsName() != null && !queryDTO.getGoodsName().trim().isEmpty()) {
            wrapper.like(Goods::getGoodsName, queryDTO.getGoodsName().trim());
        }

        // 执行分页查询，拿到实体分页结果
        IPage<Goods> goodsPage = this.page(page, wrapper);

        // 实体Goods转换成出参VO，MP自带convert方法，自动保留分页信息（总条数、总页数等）
        IPage<GoodsPageVO> resultPage = goodsPage.convert(goods -> {
            GoodsPageVO vo = new GoodsPageVO();
            vo.setId(goods.getId());
            vo.setGoodsName(goods.getGoodsName());
            vo.setGoodsImg(goods.getGoodsImg());
            vo.setOriginalPrice(goods.getOriginalPrice());
            vo.setOriginalStock(goods.getOriginalStock());
            vo.setRemainingStock(goods.getRemainingStock());
            vo.setGoodsDesc(goods.getGoodsDesc());
            return vo;
        });

        log.info("【商品分页查询】执行完成，总条数：{}", goodsPage.getTotal());
        return resultPage;
    }
}
