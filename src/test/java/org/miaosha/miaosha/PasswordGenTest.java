package org.miaosha.miaosha;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.annotation.Resource;

@SpringBootTest
public class PasswordGenTest {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Test
    void generatePassword() {
        String rawPwd = "123456";
        String encryptPwd = passwordEncoder.encode(rawPwd);
        System.out.println("明文【123456】对应的BCrypt加密密码：");
        System.out.println(encryptPwd);

        // 顺便校验匹配，验证正确性
        boolean match = passwordEncoder.matches(rawPwd, encryptPwd);
        System.out.println("校验是否匹配：" + match);
    }
}

