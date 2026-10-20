package org.miaosha.miaosha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Update;
import org.miaosha.miaosha.entity.Goods;

/**
 * <p>
 * 基础商品表 Mapper 接口
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 扣减商品库存
     * @param goodsId 商品id
     * @param count 扣减数量
     */
    @Update("UPDATE goods SET remaining_stock = remaining_stock - #{param2} WHERE id = #{param1} AND remaining_stock >= #{param2}")
    int deductStockSql(Long goodsId, Integer count);



}
