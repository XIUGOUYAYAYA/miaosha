package org.miaosha.miaosha.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.dto.address.UpdateAddressDTO;
import org.miaosha.miaosha.common.enums.UserAddressDefaultStatusEnum;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.UserAddressCodeEnum;
import org.miaosha.miaosha.common.vo.address.AddressVO;
import org.miaosha.miaosha.entity.UserAddress;
import org.miaosha.miaosha.mapper.UserAddressMapper;
import org.miaosha.miaosha.service.IUserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 用户收货地址服务实现类
 * 负责用户收货地址的查询、新增、修改、逻辑删除、设置默认地址
 */
@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService {

    /**
     * 获取当前登录用户全部地址列表（过滤逻辑删除）
     * 业务规则：查询当前用户未删除地址，默认地址排在最前面
     * @return 地址集合
     */
    @Override
    public List<UserAddress> listAddress() {
        log.info("【查询用户地址列表】userId:{}", UserContext.getUserId());
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        // 默认地址放最前面
        wrapper.orderByDesc(UserAddress::getIsDefault);
        List<UserAddress> addressList = this.list(wrapper);
        log.info("【查询用户地址列表】查询完成，共{}条地址", addressList.size());
        return addressList;
    }

    /**
     * 新增收货地址
     * 业务规则：最多保存8条地址；新增为默认地址时，先清空该用户所有地址的默认标记
     * @param address 地址实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(UserAddress address) {
        log.info("【新增收货地址】userId:{}", UserContext.getUserId());
        Long userId = UserContext.getUserId();
        address.setUserId(userId);

        // 统计当前用户的地址数量，最多8条
        LambdaQueryWrapper<UserAddress> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(UserAddress::getUserId, userId);
        long count = this.count(countWrapper);
        if(count >= 8){
            log.warn("【新增地址失败】地址数量已达上限，userId:{}", userId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_MAX_LIMIT);
        }

        // 如果本次新增的是默认地址，先把用户所有地址取消默认
        if(UserAddressDefaultStatusEnum.IS_DEFAULT.getCode().equals(address.getIsDefault())){
            log.info("【新增地址】本次为默认地址，清空用户原有默认标记 userId:{}", userId);
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, UserAddressDefaultStatusEnum.NOT_DEFAULT.getCode());
            this.update(updateWrapper);
        }
        // 插入地址
        boolean saveResult = this.save(address);
        log.info("【新增收货地址】保存结果:{}，addressId:{}", saveResult, address.getId());
    }

    /**
     * 修改收货地址
     * 业务规则：校验地址归属；修改为默认地址时，清空原有默认标记；仅更新DTO非null字段
     * @param updateAddressDTO 地址实体
     * @return 更新后的地址VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(UpdateAddressDTO updateAddressDTO) {
        log.info("【修改收货地址】addressId:{}, userId:{}", updateAddressDTO.getId(), UserContext.getUserId());
        Long userId = UserContext.getUserId();

        // 查询原地址（拿数据库原始数据）
        UserAddress oldAddr = this.getById(updateAddressDTO.getId());
        if (oldAddr == null) {
            log.warn("【修改地址失败】地址不存在 addressId:{}", updateAddressDTO.getId());
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_EXIST);
        }
        // 校验归属，不能修改别人地址
        if (!oldAddr.getUserId().equals(userId)) {
            log.warn("【修改地址失败】非法操作，不是该用户的地址 addressId:{}", updateAddressDTO.getId());
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_BELONG_USER);
        }

        // 如果本次修改为默认地址，先把该用户全部地址取消默认
        if (UserAddressDefaultStatusEnum.IS_DEFAULT.getCode().equals(updateAddressDTO.getIsDefault())) {
            log.info("【修改地址】修改为默认地址，清空用户原有默认标记 userId:{}", userId);
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, UserAddressDefaultStatusEnum.NOT_DEFAULT.getCode());
            this.update(updateWrapper);
        }

        // Hutool：只拷贝DTO中非null字段，null字段保留oldAddr数据库原值
        BeanUtil.copyProperties(updateAddressDTO, oldAddr, true);
        // 强制锁定userId，防止被篡改
        oldAddr.setUserId(userId);

        // 更新数据库
        this.updateById(oldAddr);

        // 查询更新后的数据，转为VO返回
        UserAddress newAddress = this.getById(updateAddressDTO.getId());
        AddressVO vo = convertToVO(newAddress);
        log.info("【修改收货地址】更新成功 addressId:{}", newAddress.getId());
        return vo;
    }

    /**
     * UserAddress实体转AddressVO，只返回指定字段
     * @param userAddress 数据库实体
     * @return 地址VO
     */
    private AddressVO convertToVO(UserAddress userAddress) {
        AddressVO vo = new AddressVO();
        vo.setProvince(userAddress.getProvince());
        vo.setCity(userAddress.getCity());
        vo.setDistrict(userAddress.getDistrict());
        vo.setDetailAddress(userAddress.getDetailAddress());
        vo.setReceiverName(userAddress.getReceiverName());
        vo.setReceiverPhone(userAddress.getReceiverPhone());
        vo.setIsDefault(userAddress.getIsDefault());
        return vo;
    }

    /**
     * 删除地址（逻辑删除）
     * 业务规则：使用MyBatis-Plus内置逻辑删除；校验地址归属，只能删除本人地址
     * @param addressId 地址id
     */
    @Override
    public void deleteAddress(Long addressId) {
        Long userId = UserContext.getUserId();
        log.info("【删除收货地址】addressId:{}, userId:{}", addressId, userId);

        UserAddress oldAddr = this.getById(addressId);
        if(oldAddr == null){
            log.warn("【删除地址失败】地址不存在 addressId:{}", addressId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_EXIST);
        }
        if(!oldAddr.getUserId().equals(userId)){
            log.warn("【删除地址失败】非法操作，非本人地址 addressId:{}", addressId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_BELONG_USER);
        }

        boolean updateResult = this.removeById(addressId);
        log.info("【删除收货地址】逻辑删除结果:{}, addressId:{}", updateResult, addressId);
    }


    /**
     * 设置为默认地址，同一个用户只能有一个默认地址
     * 业务规则：先清空用户全部地址默认标记，再将目标地址设为默认
     * @param addressId 地址id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId) {
        log.info("【设置默认地址】addressId:{}, userId:{}", addressId, UserContext.getUserId());
        Long userId = UserContext.getUserId();
        UserAddress oldAddr = this.getById(addressId);
        if(oldAddr == null){
            log.warn("【设置默认地址失败】地址不存在 addressId:{}", addressId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_EXIST);
        }
        if(!oldAddr.getUserId().equals(userId)){
            log.warn("【设置默认地址失败】非法操作，非本人地址 addressId:{}", addressId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_BELONG_USER);
        }

        // 1.当前用户全部地址取消默认
        LambdaUpdateWrapper<UserAddress> cancelWrapper = new LambdaUpdateWrapper<>();
        cancelWrapper.eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, UserAddressDefaultStatusEnum.NOT_DEFAULT.getCode());
        this.update(cancelWrapper);

        // 2.目标地址设为默认
        LambdaUpdateWrapper<UserAddress> setWrapper = new LambdaUpdateWrapper<>();
        setWrapper.eq(UserAddress::getId, addressId)
                .set(UserAddress::getIsDefault, UserAddressDefaultStatusEnum.IS_DEFAULT.getCode());
        boolean updateResult = this.update(setWrapper);
        log.info("【设置默认地址】操作完成，结果:{}, addressId:{}", updateResult, addressId);
    }
}
