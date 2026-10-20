package org.miaosha.miaosha.common.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson全局时间格式化配置
 * 统一LocalDateTime序列化与反序列化格式：yyyy-MM-dd HH:mm:ss
 * 支持前端传入 "2026-09-23 13:10:00" 这种带空格的时间字符串
 */
@Configuration
public class JacksonConfig {

    // 定义统一时间模板
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            // 序列化：后端返回给前端LocalDateTime → 转为 "yyyy-MM-dd HH:mm:ss"字符串
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            // 反序列化：前端传 "yyyy-MM-dd HH:mm:ss" → 解析成LocalDateTime对象
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        };
    }
}
