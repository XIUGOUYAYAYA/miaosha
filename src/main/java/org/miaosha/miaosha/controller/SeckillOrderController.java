package org.miaosha.miaosha.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderUserQueryDTO;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderAdminVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderDetailVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderUserVO;
import org.miaosha.miaosha.service.ISeckillOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 秒杀订单模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@Api(tags = "秒杀订单模块")
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

    @Resource
    private ISeckillOrderService seckillOrderService;


    /**
     * 确认下单接口
     * @param preOrderId 预订单主键id，url查询参数传递
     * @param addressId 收货地址主键id，url查询参数传递
     * @return Result<Long> 生成的正式订单ID
     */
    @PostMapping("/confirmSeckillOrder")
    @ApiOperation(value = "确认秒杀下单，生成正式订单", notes = "选择收货地址后，传入预订单与地址ID，生成正式秒杀订单")
    public Result<Long> confirmSeckillOrder(@RequestParam Long preOrderId,
                                            @RequestParam Long addressId) {
        Long orderId = seckillOrderService.confirmSeckillOrder(preOrderId, addressId);
        return Result.success(orderId);
    }


    /**
     * 订单支付接口
     * @param orderId 秒杀订单主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "订单支付", notes = "对未支付的秒杀订单执行支付操作")
    @PostMapping("/pay")
    public Result<Boolean> pay(@RequestParam Long orderId) {
        boolean result = seckillOrderService.payOrder(orderId);
        return Result.success(result);
    }

    /**
     * 手动取消秒杀订单接口
     * @param orderNo 秒杀订单编号，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "手动取消秒杀订单", notes = "用户主动取消订单，触发库存归还逻辑")
    @PostMapping("/cancel")
    public Result<Boolean> cancelOrder(@RequestParam String orderNo) {
        boolean result = seckillOrderService.cancelOrder(orderNo);
        return Result.success(result);
    }

    /**
     * 查询订单详情接口
     * @param orderId 秒杀订单主键id，路径变量传递
     * @return Result<SeckillOrderDetailVO> 订单详情VO对象
     */
    @ApiOperation(value = "查询订单详情", notes = "根据订单id查询订单完整详情信息")
    @GetMapping("/detail/{orderId}")
    public Result<SeckillOrderDetailVO> getOrderDetail(@PathVariable Long orderId){
        SeckillOrderDetailVO detailVO = seckillOrderService.getOrderDetail(orderId);
        return Result.success(detailVO);
    }


    /**
     * 用户端分页查询本人秒杀订单接口
     * @param queryDTO 分页参数+订单号查询条件
     * @return Result<IPage<SeckillOrderUserVO>> 分页结果对象
     */
    @ApiOperation(value = "用户端：分页查询本人所有秒杀订单", notes = "仅查询当前登录用户的订单，支持订单号模糊检索")
    @GetMapping("/user/page")
    public Result<IPage<SeckillOrderUserVO>> userPageOrder(
            @Validated @ModelAttribute SeckillOrderUserQueryDTO queryDTO
    ) {
        IPage<SeckillOrderUserVO> pageResult = seckillOrderService.userPageOrder(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 管理员端分页查询秒杀订单接口
     * @param queryDTO 分页参数+订单筛选条件
     * @return Result<IPage<SeckillOrderAdminVO>> 分页结果对象
     */
    @GetMapping("/admin/page")
    @ApiOperation(value = "管理员分页查询秒杀订单（联表带出秒杀活动价seckillPrice）", notes = "后台管理员查询全平台订单，支持订单号、支付状态筛选，用于对账")
    public Result<IPage<SeckillOrderAdminVO>> getAdminOrderPage(
            @Validated @ModelAttribute SeckillOrderAdminQueryDTO queryDTO
    ){
        IPage<SeckillOrderAdminVO> pageResult = seckillOrderService.getAdminOrderPage(queryDTO);
        return Result.success(pageResult);
    }

}
