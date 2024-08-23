package com.bcp.services.transaction.repository;

import com.bcp.services.transaction.model.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Transaction Postgresql Repository.
 * This interface is used to interact with the Postgresql database.
 * It extends the ReactiveCrudRepository interface.
 *
 * @see ReactiveCrudRepository
 * @see TransactionEntity
 * @see TransactionRepository
 */
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, String> {

}
