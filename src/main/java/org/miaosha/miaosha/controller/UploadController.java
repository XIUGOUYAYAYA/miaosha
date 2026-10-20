package org.miaosha.miaosha.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.util.LocalFileUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * 文件上传模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务工具类、统一返回Result封装结果
 */
@RestController
@RequestMapping("/upload")
@Api(tags = "【开发调试】本地图片上传接口，禁止上线")
public class UploadController {

    @Resource
    private LocalFileUtil localFileUtil;

    /**
     * 上传商品图片接口
     * @param file 上传的图片文件，表单参数传递
     * @return Result<String> 返回图片访问地址
     */
    @PostMapping("/goodsImg")
    @ApiOperation(value = "上传商品图片（本地存储）", notes = "开发调试用本地图片上传接口，将图片保存到本地磁盘，返回图片访问路径，该接口禁止上线使用")
    public Result<String> uploadGoodsImg(@RequestParam("file") @NotNull MultipartFile file) {
        try {
            String imgUrl = localFileUtil.upload(file);
            return Result.success(imgUrl);
        } catch (RuntimeException e) {
            // 业务异常，返回自定义错误信息
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败");
        }
    }
}
