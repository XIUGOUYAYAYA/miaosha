package org.miaosha.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@MapperScan("org.miaosha.miaosha.mapper") //扫描mapper接口包
public class MiaoshaApplication {

    public static void main(String[] args) {

        // ========== 强制设置JVM为东八区【上海时区】，必须放在第一行 ==========
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        // 打印校验，启动看控制台
        System.out.println("JVM当前时区：" + TimeZone.getDefault().getID());

        SpringApplication.run(MiaoshaApplication.class, args);
    }

}
