package com.bcp.services.trxantifraud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Jackson ObjectMapper.
 */
@Configuration
public class ObjectMapperConfig {

    @Bean(name = "objectMapper2")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

