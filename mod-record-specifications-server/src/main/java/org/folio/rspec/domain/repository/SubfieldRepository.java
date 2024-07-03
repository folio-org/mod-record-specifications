package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.Subfield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubfieldRepository extends JpaRepository<Subfield, UUID> {

  @Query("select s from Subfield s where s.field.id = ?1 order by s.code")
  List<Subfield> findByFieldId(UUID fieldId);

}
