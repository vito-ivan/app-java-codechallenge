package com.bcp.services.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity.
 * This class is used to represent a Transaction.
 */
@Table("trx.transaction")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    private String id;
    @Column("accountExternalIdDebit")
    private String accountExternalIdDebit;
    @Column("accountExternalIdCredit")
    private String accountExternalIdCredit;
    @Column("transactionTypeCode")
    private String transactionTypeCode;
    @Column("amount")
    private BigDecimal amount;
    @Column("status")
    private String status;
    @Column("createdAt")
    private LocalDateTime createdAt;
    @Column("updatedAt")
    private LocalDateTime updatedAt;

}
