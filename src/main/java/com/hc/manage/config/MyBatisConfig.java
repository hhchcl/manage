package com.hc.manage.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * @project: hc-manage
 * @package: com.hc.manage.config
 * @describe：开启mybatis的驼峰命名法
 * @author: hc
 * @date: 2020/5/17 9:49
 */

@Configuration
public class MyBatisConfig {

    public ConfigurationCustomizer configurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }
}
