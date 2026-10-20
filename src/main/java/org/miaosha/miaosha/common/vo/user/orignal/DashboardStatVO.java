package org.miaosha.miaosha.common.vo.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("后台首页统计VO")
public class DashboardStatVO {

    @ApiModelProperty("基础商品总数(未删除)")
    private Long baseGoodsTotal;

    @ApiModelProperty("秒杀活动总数(未删除)")
    private Long seckillTotal;

    @ApiModelProperty("平台用户总数(未删除)")
    private Long userTotal;

    @ApiModelProperty("秒杀订单总数(未删除)")
    private Long orderTotal;
}

