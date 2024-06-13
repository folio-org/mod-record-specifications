package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.repository.SpecificationMetadataRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecificationMetadataService {

  private final SpecificationMetadataRepository metadataRepository;

  public SpecificationMetadata getSpecificationMetadata(UUID specificationId) {
    return metadataRepository.findBySpecificationId(specificationId);
  }

  public void saveSpecificationMetadata(SpecificationMetadata metadata) {
    metadataRepository.save(metadata);
  }
}
