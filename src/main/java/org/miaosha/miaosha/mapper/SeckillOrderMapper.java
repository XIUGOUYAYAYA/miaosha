package org.miaosha.miaosha.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// ✅ 正确MyBatis的@Param
import org.apache.ibatis.annotations.Param;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderUserQueryDTO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderAdminVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderUserVO;
import org.miaosha.miaosha.entity.SeckillOrder;

/**
 * <p>
 * 秒杀订单表 Mapper 接口
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-11
 */
// SeckillOrderMapper.java 【Mapper接口】
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    /**
     * 管理员订单分页查询
     */
    IPage<SeckillOrderAdminVO> selectAdminOrderPage(Page<SeckillOrderAdminVO> page,
                                                    @Param("query") SeckillOrderAdminQueryDTO queryDTO);

    /**
     * 用户订单分页查询
     */
    IPage<SeckillOrderUserVO> selectUserOrderPage(Page<SeckillOrderUserVO> page,
                                                  @Param("query") SeckillOrderUserQueryDTO query,
                                                  @Param("userId") Long userId);

}


