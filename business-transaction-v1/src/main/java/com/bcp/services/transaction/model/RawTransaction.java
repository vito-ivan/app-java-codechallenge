package com.bcp.services.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * RawTransaction.
 * This class is used to represent a RawTransaction.
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RawTransaction {
    private String id;
    private BigDecimal amount;
    private String currency;
}