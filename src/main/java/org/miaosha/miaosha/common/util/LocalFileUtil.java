package org.miaosha.miaosha.common.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Component
public class LocalFileUtil {

    @Value("${upload.local-path}")
    private String localPath;

    @Value("${upload.access-prefix}")
    private String accessPrefix;

    /**
     * 本地上传，返回可访问的url
     * 包含校验：文件大小、图片后缀
     */
    public String upload(MultipartFile file) throws Exception {
        // ========= 全部校验逻辑移到这里 =========
        // 校验图片大小
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("图片不能超过10MB");
        }
        String filename = file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".jpg") && !filename.endsWith(".png") && !filename.endsWith(".jpeg")) {
            throw new RuntimeException("仅支持jpg、png、jpeg格式图片");
        }

        // 创建文件夹，如果不存在
        File dir = new File(localPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 获取后缀，UUID防止重名覆盖
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;

        // 保存到磁盘
        File saveFile = new File(localPath + fileName);
        file.transferTo(saveFile);

        // 返回访问URL
        return accessPrefix + "/" + fileName;
    }
}


