package org.miaosha.miaosha.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.dto.address.UpdateAddressDTO;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.vo.address.AddressVO;
import org.miaosha.miaosha.entity.UserAddress;
import org.miaosha.miaosha.service.IUserAddressService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户收货地址模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@RestController
@Api(tags = "收货地址管理")
@RequestMapping("/userAddress")
public class UserAddressController {

    @Resource
    private IUserAddressService userAddressService;

    /**
     * 获取地址列表接口
     * @return Result<List<UserAddress>> 当前用户的收货地址列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取我的地址列表", notes = "获取当前登录用户所有收货地址")
    public Result<List<UserAddress>> listAddress(){
        List<UserAddress> list = userAddressService.listAddress();
        return Result.success(list);
    }

    /**
     * 新增收货地址接口
     * @param address 收货地址实体入参JSON
     * @return Result<Void>
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增收货地址", notes = "为当前登录用户新增一条收货地址")
    public Result<Void> addAddress(@RequestBody UserAddress address){
        userAddressService.addAddress(address);
        return Result.success();
    }

    /**
     * 修改收货地址接口
     * @param updateAddressDTO 修改地址入参JSON
     * @return Result<AddressVO> 修改后的地址VO
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改收货地址", notes = "更新当前用户的收货地址信息")
    public Result<AddressVO> updateAddress(@RequestBody UpdateAddressDTO updateAddressDTO) {
        AddressVO addressVO = userAddressService.updateAddress(updateAddressDTO);
        return Result.success(addressVO);
    }

    /**
     * 删除收货地址接口
     * @param id 收货地址主键id，路径变量传递
     * @return Result<Void>
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除地址（逻辑删除）", notes = "根据地址ID逻辑删除收货地址")
    public Result<Void> deleteAddress(@PathVariable Long id){
        userAddressService.deleteAddress(id);
        return Result.success();
    }

    /**
     * 设置默认收货地址接口
     * @param id 收货地址主键id，路径变量传递
     * @return Result<Void>
     */
    @PostMapping("/setDefault/{id}")
    @ApiOperation(value = "设置默认地址", notes = "将指定地址设置为当前用户的默认收货地址，其他地址取消默认")
    public Result<Void> setDefault(@PathVariable Long id){
        userAddressService.setDefaultAddress(id);
        return Result.success();
    }

}
