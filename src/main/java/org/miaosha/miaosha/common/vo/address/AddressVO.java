package org.miaosha.miaosha.common.vo.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AddressVO", description = "收货地址返回VO")
public class AddressVO {

    @ApiModelProperty("省份")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("区县")
    private String district;

    @ApiModelProperty("详细地址")
    private String detailAddress;

    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货人手机号")
    private String receiverPhone;

    @ApiModelProperty("是否默认地址：0否 1是")
    private Integer isDefault;
}

