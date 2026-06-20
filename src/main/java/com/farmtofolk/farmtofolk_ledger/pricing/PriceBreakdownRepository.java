package com.farmtofolk.farmtofolk_ledger.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PriceBreakdownRepository extends JpaRepository<PriceBreakdown, UUID> {
}
