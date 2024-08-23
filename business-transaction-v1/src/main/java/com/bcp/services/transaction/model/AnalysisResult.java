package com.bcp.services.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AnalysisResult.
 * This class is used to represent an AnalysisResult.
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String transactionId;
    private Boolean isFraudulent;
    private String reason;
}
