package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.dto.address.UpdateAddressDTO;
import org.miaosha.miaosha.common.vo.address.AddressVO;
import org.miaosha.miaosha.entity.UserAddress;
import org.miaosha.miaosha.common.result.Result;
import java.util.List;

/**
 * <p>
 * 用户收货地址表 服务类
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-22
 */
public interface IUserAddressService extends IService<UserAddress> {

    /**
     * 获取当前登录用户全部地址列表
     * @return 地址集合
     */
    List<UserAddress> listAddress();

    /**
     * 新增收货地址
     * @param address 地址实体
     */
    void addAddress(UserAddress address);

    /**
     * 修改收货地址
     * @param updateAddressDTO 修改地址入参DTO
     * @return AddressVO 修改后的地址信息
     */
    AddressVO updateAddress(UpdateAddressDTO updateAddressDTO);

    /**
     * 删除地址（逻辑删除）
     * @param addressId 地址id
     */
    void deleteAddress(Long addressId);

    /**
     * 设置为默认地址，同一个用户只能有一个默认地址
     * @param addressId 地址id
     */
    void setDefaultAddress(Long addressId);

}
