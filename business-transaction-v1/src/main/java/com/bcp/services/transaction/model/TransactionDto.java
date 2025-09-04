package com.bcp.services.transaction.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private UUID id;
    private UUID accountExternalIdDebit;

    private UUID accountExternalIdCredit;

    private String transferTypeId;

    private BigDecimal value;
}
