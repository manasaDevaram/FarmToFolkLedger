package com.farmtofolk.farmtofolk_ledger.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
}
