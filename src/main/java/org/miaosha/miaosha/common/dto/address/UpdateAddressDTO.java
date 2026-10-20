package org.miaosha.miaosha.common.dto.address;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@ApiModel(value = "UpdateAddressDTO", description = "修改收货地址入参")
public class UpdateAddressDTO {

    @NotNull(message = "地址ID不能为空")
    @ApiModelProperty(value = "地址ID", required = true, example = "1")
    private Long id;

    @NotBlank(message = "省份不能为空")
    @ApiModelProperty(value = "省份", example = "浙江省")
    private String province;

    @NotBlank(message = "城市不能为空")
    @ApiModelProperty(value = "城市", example = "杭州市")
    private String city;

    @NotBlank(message = "区县不能为空")
    @ApiModelProperty(value = "区县", example = "滨江区")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过200个字符")
    @ApiModelProperty(value = "详细地址", example = "长河街道网商路123号阿里园区A栋")
    private String detailAddress;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    @ApiModelProperty(value = "收货人姓名", example = "张三")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "收货人手机号", example = "13800138001")
    private String receiverPhone;

    @NotNull(message = "是否默认地址标识不能为空")
    @ApiModelProperty(value = "是否默认地址：0=否 1=是", example = "0")
    private Integer isDefault;
}
