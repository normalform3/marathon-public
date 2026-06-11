package org.example.marathonwebapp.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            GlobalConfig globalConfig = properties.getGlobalConfig();
            if (globalConfig == null) {
                globalConfig = new GlobalConfig();
                properties.setGlobalConfig(globalConfig);
            }

            GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
            if (dbConfig == null) {
                dbConfig = new GlobalConfig.DbConfig();
                globalConfig.setDbConfig(dbConfig);
            }

            dbConfig.setColumnFormat("`%s`");
        };
    }
}
