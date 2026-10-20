package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.dto.goods.GoodsAddDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsQueryDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsUpdateDTO;
import org.miaosha.miaosha.common.vo.goods.GoodsPageVO;
import org.miaosha.miaosha.entity.Goods;

/**
 * 基础商品表 服务接口
 *
 * @author YY
 * @since 2026-09-11
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 扣减商品基础库存（只扣不减，传入扣减数量，正数）
     * @param goodsId 商品id
     * @param count 需要扣减多少件（正数）
     * @return true成功；false库存不足/商品不存在
     */
    boolean deductStock(Long goodsId, Integer count);

    /**
     * 新增商品
     * 业务规则：商品名校验，不能重复
     * @param goodsAddDTO 商品实体
     * @return 成功true
     */
    boolean addGoods(GoodsAddDTO goodsAddDTO);

    /**
     * 修改商品
     * 业务规则：非活动开始且上架期间，同步更新秒杀活动关联信息
     * @param goodsUpdateDTO 商品实体
     * @return 成功true
     */
    boolean updateGoods(GoodsUpdateDTO goodsUpdateDTO);

    /**
     * 删除商品
     * 业务规则：没有关联秒杀活动、没有关联订单才允许删除
     * @param goodsId 商品id
     * @return 成功true
     */
    boolean deleteGoods(Long goodsId);

    /**
     * 商品分页查询
     * @param queryVO 分页+查询条件
     * @return 分页结果
     */
    IPage<GoodsPageVO> pageGoods(GoodsQueryDTO queryVO);

}
