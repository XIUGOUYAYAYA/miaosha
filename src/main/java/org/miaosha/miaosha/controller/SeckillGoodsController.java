package org.miaosha.miaosha.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.dto.seckill.goods.*;
import org.miaosha.miaosha.common.vo.seckill.goods.*;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.service.ISeckillGoodsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * 秒杀商品模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@RestController
@Api(tags = "秒杀商品模块")
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

    @Resource
    private ISeckillGoodsService seckillGoodsService;

    /**
     * 新增秒杀商品接口
     * @param addDTO 新增秒杀商品入参JSON，@Validated开启DTO参数校验
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "新增秒杀商品", notes = "新增秒杀商品基础信息")
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated SeckillGoodsAddDTO addDTO) {
        boolean res = seckillGoodsService.addSeckillGoods(addDTO);
        return Result.success(res);
    }

    /**
     * 修改秒杀商品接口
     * @param updateDTO 修改秒杀商品入参JSON
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "修改秒杀商品", notes = "更新秒杀商品基础信息")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated SeckillGoodsUpdateDTO updateDTO) {
        boolean res = seckillGoodsService.updateSeckillGoods(updateDTO);
        return Result.success(res);
    }

    /**
     * 逻辑删除秒杀商品接口
     * @param id 秒杀商品主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "逻辑删除秒杀商品", notes = "根据秒杀商品ID逻辑删除秒杀商品")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        boolean res = seckillGoodsService.deleteSeckillGoods(id);
        return Result.success(res);
    }

    /**
     * 秒杀商品上架发布接口
     * @param id 秒杀商品主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "发布秒杀商品，上架到Redis", notes = "将秒杀商品上架，加载库存数据至Redis缓存")
    @PostMapping("/publish")
    public Result<Boolean> publish(@RequestParam Long id) {
        boolean res = seckillGoodsService.publish(id);
        return Result.success(res);
    }

    /**
     * 秒杀商品下架接口
     * @param id 秒杀商品主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "下架秒杀商品，清除Redis缓存", notes = "将秒杀商品下架，清除Redis中的秒杀库存缓存")
    @PostMapping("/offline")
    public Result<Boolean> offline(@RequestParam Long id) {
        boolean res = seckillGoodsService.offline(id);
        return Result.success(res);
    }

    /**
     * 管理员分页条件查询秒杀商品接口
     * @param queryDTO 分页参数+秒杀商品查询条件
     * @return Result<IPage<SeckillGoodsAdminVO>> 分页结果对象
     */
    @ApiOperation(value = "管理员分页查询秒杀商品", notes = "管理员后台，根据条件分页查询秒杀商品列表")
    @PostMapping("/admin/page")
    public Result<IPage<SeckillGoodsAdminVO>> adminPage(@RequestBody SeckillGoodsAdminQueryDTO queryDTO) {
        IPage<SeckillGoodsAdminVO> page = seckillGoodsService.adminPageQuery(queryDTO);
        return Result.success(page);
    }

    /**
     * 用户端分页条件查询秒杀商品接口
     * @param queryDTO 分页参数+秒杀商品查询条件
     * @return Result<IPage<SeckillGoodsUserVO>> 分页结果对象
     */
    @ApiOperation(value = "用户端分页查询秒杀商品", notes = "用户前端页面，根据条件分页查询秒杀商品列表")
    @PostMapping("/user/page")
    public Result<IPage<SeckillGoodsUserVO>> userPage(@RequestBody SeckillGoodsUserQueryDTO queryDTO) {
        IPage<SeckillGoodsUserVO> page = seckillGoodsService.userPageQuery(queryDTO);
        return Result.success(page);
    }
}
