package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndicatorRepository extends JpaRepository<Indicator, UUID> {

  @Query("select i from Indicator i where i.field.id = ?1 order by order")
  List<Indicator> findByFieldId(UUID fieldId);

}
