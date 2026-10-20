package org.miaosha.miaosha.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.service.ISeckillPreOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 秒杀预订单模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@RestController
@RequestMapping("/seckillPreOrder")
@Api(tags = "预约订单")
public class SeckillPreOrderController {

    @Resource
    ISeckillPreOrderService seckillPreOrderService;

    /**
     * 预下单接口
     * @param seckillGoodsId 秒杀商品主键id，url查询参数传递
     * @return Result<Long> 生成的预订单ID
     */
    @PostMapping("/preSeckill")
    @ApiOperation(value = "预下单，抢占库存", notes = "预下单抢占秒杀库存，创建预订单并返回预订单ID")
    public Result<Long> preSeckill(@RequestParam Long seckillGoodsId) {
        Long preOrderId = seckillPreOrderService.preSeckill(seckillGoodsId);
        return Result.success(preOrderId);
    }

    /**
     * 删除预订单接口
     * @param preOrderId 预订单主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @PostMapping("/deletePreOrder")
    @ApiOperation(value = "删除预订单，释放库存", notes = "取消预下单，删除预订单并释放抢占的秒杀库存")
    public Result<Boolean> deletePreOrder(@RequestParam Long preOrderId) {
        boolean remove = seckillPreOrderService.deletePreOrder(preOrderId);
        return Result.success(remove);
    }

}
