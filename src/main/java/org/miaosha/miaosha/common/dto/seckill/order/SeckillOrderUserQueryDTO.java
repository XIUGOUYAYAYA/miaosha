package org.miaosha.miaosha.common.dto.seckill.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@ApiModel("用户端-秒杀订单分页查询入参")
public class SeckillOrderUserQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Long pageSize = 10L;

    @ApiModelProperty(value = "订单号(模糊查询，非必传)", example = "SK20260925001")
    private String orderNo;
}
