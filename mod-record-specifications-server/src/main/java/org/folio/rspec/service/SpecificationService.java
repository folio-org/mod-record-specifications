package org.folio.rspec.service;

import static java.lang.Math.toIntExact;

import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.service.mapper.SpecificationEntityMapper;
import org.folio.spring.data.OffsetRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecificationService {

  private final SpecificationRepository specificationRepository;
  private final SpecificationEntityMapper specificationEntityMapper;

  public SpecificationDtoCollection findSpecifications(Family family, FamilyProfile profile, String include,
                                                    Integer limit, Integer offset) {
    var specificationCollection = new SpecificationDtoCollection();

    var page = specificationRepository.findByFamilyAndProfile(family, profile, OffsetRequest.of(offset, limit));

    page.map(specificationEntityMapper::toDto)
      .forEach(specificationCollection::addSpecificationsItem);

    return specificationCollection.totalRecords(toIntExact(page.getTotalElements()));
  }
}
