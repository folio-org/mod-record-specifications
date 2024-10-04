package org.folio.rspec.service.processor.request.strategy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.builders.SubfieldBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SubfieldUpdateRequestProcessingStrategyTest {

  private @Mock SpecificationRepository specificationRepository;
  private @Mock SpecificationFieldService fieldService;
  private @Mock Page<Specification> specificationPage;

  private @InjectMocks SubfieldUpdateRequestProcessingStrategy strategy;

  @Test
  void process_positive_saveSubfield() {
    var subfield = SubfieldBuilder.system().buildDto();
    var requestEvent = new SubfieldUpdateRequestEvent();
    requestEvent.setFamily(Family.MARC);
    requestEvent.setProfile(FamilyProfile.BIBLIOGRAPHIC);
    requestEvent.setTargetFieldTag("100");
    requestEvent.setSubfield(subfield);
    var specification = new Specification();
    specification.setId(UUID.randomUUID());
    specification.setFamily(Family.MARC);
    specification.setProfile(FamilyProfile.BIBLIOGRAPHIC);

    when(specificationPage.isEmpty()).thenReturn(false);
    when(specificationPage.toList()).thenReturn(List.of(specification));
    when(specificationRepository.findByFamilyAndProfile(requestEvent.getFamily(), requestEvent.getProfile(),
      OffsetRequest.of(0, 1))).thenReturn(specificationPage);

    strategy.process(requestEvent);

    verify(fieldService)
      .saveSubfield(specification.getId(), requestEvent.getTargetFieldTag(), requestEvent.getSubfield());
  }
}
