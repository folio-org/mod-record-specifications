package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndicatorCodeRepository extends JpaRepository<IndicatorCode, UUID> {

  @Query("select c from IndicatorCode c where c.indicator.id = ?1 order by code")
  List<IndicatorCode> findByIndicatorId(UUID indicatorId);

}
