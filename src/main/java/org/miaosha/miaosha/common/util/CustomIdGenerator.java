package org.miaosha.miaosha.common.util;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

@Component
public class CustomIdGenerator implements IdentifierGenerator {

    private final Snowflake snowflake = IdUtil.createSnowflake(1,1);

    @Override
    public Number nextId(Object entity) {
        return snowflake.nextId();
    }
}

