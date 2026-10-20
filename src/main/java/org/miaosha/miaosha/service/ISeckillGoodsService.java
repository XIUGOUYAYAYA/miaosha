package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsAddDTO;
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsUpdateDTO;
import org.miaosha.miaosha.common.dto.seckill.goods.SeckillGoodsUserQueryDTO;
import org.miaosha.miaosha.common.vo.seckill.goods.SeckillGoodsAdminVO;
import org.miaosha.miaosha.common.vo.seckill.goods.SeckillGoodsUserVO;
import org.miaosha.miaosha.entity.SeckillGoods;
import org.miaosha.miaosha.entity.SeckillOrder;

/**
 * 秒杀商品服务接口
 */
public interface ISeckillGoodsService extends IService<SeckillGoods> {

    /**
     * 新增秒杀商品
     * @param addDTO 新增入参
     * @return 是否成功
     */
    boolean addSeckillGoods(SeckillGoodsAddDTO addDTO);

    /**
     * 修改秒杀商品
     * @param updateDTO 修改入参
     * @return 是否成功
     */
    boolean updateSeckillGoods(SeckillGoodsUpdateDTO updateDTO);

    /**
     * 逻辑删除秒杀商品
     * @param id 秒杀id
     * @return 是否成功
     */
    boolean deleteSeckillGoods(Long id);

    /**
     * 发布秒杀（上架）
     * @param id 秒杀id
     * @return 是否成功
     */
    boolean publish(Long id);

    /**
     * 下架秒杀
     * @param id 秒杀id
     * @return 是否成功
     */
    boolean offline(Long id);

    /**
     * 管理员分页查询秒杀商品（后台）
     * @param queryDTO 查询条件
     * @return 分页VO
     */
    IPage<SeckillGoodsAdminVO> adminPageQuery(SeckillGoodsAdminQueryDTO queryDTO);

    /**
     * 用户端分页查询秒杀商品（前端展示）
     * @param queryDTO 查询条件
     * @return 分页VO
     */
    IPage<SeckillGoodsUserVO> userPageQuery(SeckillGoodsUserQueryDTO queryDTO);
}
