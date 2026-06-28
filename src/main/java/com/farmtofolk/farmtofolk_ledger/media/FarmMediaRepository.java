package com.farmtofolk.farmtofolk_ledger.media;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmMediaRepository extends JpaRepository<FarmMedia, UUID> {

  List<FarmMedia> findByFarmIdOrderByCreatedAtAsc(UUID farmId);
}
