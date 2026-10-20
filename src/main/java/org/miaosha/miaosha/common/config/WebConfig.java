package org.miaosha.miaosha.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 读取yml配置的本地路径
    @Value("${upload.local-path}")
    private String localPath;

    // 只保留静态资源配置，删掉addInterceptors方法
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
        // knife4j静态资源映射
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        /*
         * 访问：http://localhost:8080/upload/images/xxx.jpg
         * 映射到磁盘 D:/miaosha-upload/goods/xxx.jpg
         */
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + localPath);
    }
}
