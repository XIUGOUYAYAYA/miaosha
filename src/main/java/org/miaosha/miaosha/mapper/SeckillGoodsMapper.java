package org.miaosha.miaosha.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param; // ✅ MyBatis原生Param，不要用lettuce那个
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsUserQueryDTO;
import org.miaosha.miaosha.common.vo.seckill.goods.SeckillGoodsAdminVO;
import org.miaosha.miaosha.common.vo.seckill.goods.SeckillGoodsUserVO;
import org.miaosha.miaosha.entity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 秒杀活动场次表 Mapper 接口
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods>{
    /**
     * 管理员秒杀商品分页查询
     */
    IPage<SeckillGoodsAdminVO> selectAdminPage(Page<SeckillGoodsAdminVO> page,
                                               @Param("query") SeckillGoodsAdminQueryDTO queryDTO);

    /**
     * 用户秒杀商品分页查询
     */
    IPage<SeckillGoodsUserVO> selectUserPage(Page<SeckillGoodsUserVO> page,
                                             @Param("query") SeckillGoodsUserQueryDTO queryDTO);

}
