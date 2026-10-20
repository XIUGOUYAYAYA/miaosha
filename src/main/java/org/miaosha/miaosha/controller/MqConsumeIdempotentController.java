package org.miaosha.miaosha.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * MQ消费幂等表 前端控制器
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-06
 */
@RestController
@Api(tags = "MQ消费幂等记录模块")
@RequestMapping("/mqConsumeIdempotent")
public class MqConsumeIdempotentController {

}
