package org.miaosha.miaosha.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.dto.seckill.stock.SeckillStockAdminQueryDTO;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.vo.seckill.stock.SeckillStockAdminVO;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 秒杀库存模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@RestController
@Api(tags = "秒杀库存接口")
@RequestMapping("/seckillStock")
public class SeckillStockController {
    @Resource
    private ISeckillStockService seckillStockService;

    /**
     * 管理员分页查询库存接口
     * @param queryDTO 分页参数+库存筛选条件
     * @return Result<IPage<SeckillStockAdminVO>> 分页结果对象
     */
    @ApiOperation(value = "管理员分页查询库存（支持商品ID、库存区间筛选）", notes = "后台管理员分页查看秒杀库存数据，支持秒杀商品ID、剩余库存数量区间条件筛选")
    @GetMapping("/admin/page")
    public Result<IPage<SeckillStockAdminVO>> adminPage(SeckillStockAdminQueryDTO queryDTO) {
        IPage<SeckillStockAdminVO> pageResult = seckillStockService.adminStockPage(queryDTO);
        return Result.success(pageResult);
    }

}
