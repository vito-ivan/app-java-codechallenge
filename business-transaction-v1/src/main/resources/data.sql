CREATE SCHEMA IF NOT EXISTS trx;


DROP TABLE IF EXISTS trx.transaction;


CREATE TABLE trx.transaction(id varchar(36) PRIMARY KEY,
                             accountExternalIdDebit varchar(40),
                             accountExternalIdCredit varchar(40),
                             transactionTypeCode varchar(20),
                             amount numeric,
                             status varchar(10),
                             createdAt timestamp,
                             updatedAt timestamp);

GRANT ALL PRIVILEGES ON SCHEMA trx TO postgresuser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA trx TO postgresuser;