package com.hc.manage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @project: hc-manage
 * @package: com.hc.manage.config
 * @describe：
 * @author: hc
 * @date: 2020/7/12 11:08
 */

@ConfigurationProperties(prefix = "spring.redis")
@Setter
@Getter
@Configuration
@Component
public class CustomConfigProperties {

    private String name;
    private String host;
    private int port;
    private int timeout;
    private String password;
}
