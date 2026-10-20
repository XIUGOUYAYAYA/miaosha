package org.miaosha.miaosha;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        // ==========【这里改你的数据库信息】==========
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/miaosha?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&remarks=true&useInformationSchema=true";
        String dbUser = "root";
        String dbPwd = "20021221";

        FastAutoGenerator.create(jdbcUrl, dbUser, dbPwd)
                .globalConfig(builder -> {
                    builder.author("丫丫") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir("D:/JavaPrograms/miaosha/miaosha/src/main/java"); // 实体类输出到项目源码目录
                })
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                // 自定义类型转换
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder ->
                        builder.parent("org.miaosha.miaosha") // 改成你的项目父包名
                                .moduleName("") // 不需要子模块就留空字符串
                                // 修正拼写错误 miaohsa → miaosha
                                .pathInfo(Collections.singletonMap(OutputFile.xml, "D:/JavaPrograms/miaosha/miaosha/src/main/resources/mapper"))
                )
                .strategyConfig(builder -> builder
                        .addInclude("seckill_order") // 只生成user表，多张表逗号分隔
                        // 实体配置
                        .entityBuilder()
                        .enableLombok()
                        .enableFileOverride() // 实体允许覆盖
//                        // mapper配置
//                        .mapperBuilder()
//                        .enableFileOverride()
//                        // service接口+实现类配置
//                        .serviceBuilder()
//                        .enableFileOverride()
//                        // controller配置，REST风格
//                        .controllerBuilder()
//                        .enableRestStyle()
//                        .enableFileOverride()
                )
                // 模板配置：只生成entity，不生成mapper/service/controller
                .templateConfig(builder -> builder
                        .mapper("")
                        .service("")
                        .serviceImpl("")
                        .controller("")
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
