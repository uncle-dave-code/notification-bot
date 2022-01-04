package com.uncledavecode.notificator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfiguration {

    private String username;
    private String token;
}
