package com.bcp.services.transaction.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Application Properties.<br/>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private Map<String, String> trxTypes;
    private TrxStatuses trxStatuses;

    /**
     * Class that contains Counterparty Type.
     *
     * @author Vito
     * @version 1.0
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrxStatuses {
        private String pending;
        private String approved;
        private String rejected;
    }
}