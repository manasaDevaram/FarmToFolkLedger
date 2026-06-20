package com.farmtofolk.farmtofolk_ledger.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FarmMediaRepository extends JpaRepository<FarmMedia, UUID> {

    List<FarmMedia> findByFarmIdOrderByCreatedAtAsc(UUID farmId);
}
