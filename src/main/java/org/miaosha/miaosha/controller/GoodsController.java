package org.miaosha.miaosha.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.dto.goods.GoodsAddDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsQueryDTO;
import org.miaosha.miaosha.common.dto.goods.GoodsUpdateDTO;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.vo.goods.GoodsPageVO;
import org.miaosha.miaosha.service.IGoodsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * 商品模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@RestController
@RequestMapping("/goods")
@Api(tags = "商品模块")
public class GoodsController {

    @Resource
    private IGoodsService goodsService;

    /**
     * 新增商品接口
     * @param goodsAddDTO 新增商品入参JSON，@Validated开启DTO参数校验
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "新增商品", notes = "新增商品基础信息")
    @PostMapping("/add")
    public Result<Boolean> addGoods(@RequestBody @Validated GoodsAddDTO goodsAddDTO) {
        boolean success = goodsService.addGoods(goodsAddDTO);
        return Result.success(success);
    }

    /**
     * 修改商品接口
     * @param goodsUpdateDTO 修改商品入参JSON
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "修改商品", notes = "更新商品基础信息")
    @PostMapping("/update")
    public Result<Boolean> updateGoods(@RequestBody @Validated GoodsUpdateDTO goodsUpdateDTO) {
        boolean success = goodsService.updateGoods(goodsUpdateDTO);
        return Result.success(success);
    }

    /**
     * 逻辑删除商品接口
     * @param goodsId 商品主键id，url查询参数传递
     * @return Result<Boolean> 操作结果
     */
    @ApiOperation(value = "逻辑删除商品", notes = "根据商品ID逻辑删除商品")
    @PostMapping("/delete")
    public Result<Boolean> deleteGoods(@RequestParam Long goodsId) {
        boolean success = goodsService.deleteGoods(goodsId);
        return Result.success(success);
    }


    /**
     * 商品分页条件查询接口
     * @param queryDTO 分页参数+商品名称模糊查询条件
     * @return Result<IPage<Goods>> 分页结果对象
     */
    @ApiOperation(value = "商品分页条件查询", notes = "根据条件分页查询商品列表")
    @PostMapping("/page")
    public Result<IPage<GoodsPageVO>> pageGoods(@RequestBody GoodsQueryDTO queryDTO) {
        IPage<GoodsPageVO> pageResult = goodsService.pageGoods(queryDTO);
        return Result.success(pageResult);
    }
}
