package org.folio.rspec.domain.repository;

import java.util.Collection;
import java.util.List;
import org.folio.rspec.domain.entity.AppliedSpecUpdate;
import org.folio.rspec.domain.entity.AppliedSpecUpdateId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppliedSpecUpdateRepository extends JpaRepository<AppliedSpecUpdate, AppliedSpecUpdateId> {

  List<AppliedSpecUpdate> findByIdCodeIn(Collection<String> codes);
}
