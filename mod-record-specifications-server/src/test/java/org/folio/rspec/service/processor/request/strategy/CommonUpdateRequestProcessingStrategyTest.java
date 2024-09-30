package org.folio.rspec.service.processor.request.strategy;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BiConsumer;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.UpdateRequestProcessingException;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CommonUpdateRequestProcessingStrategyTest {

  private @Mock SpecificationRepository specificationRepository;
  private @Mock BiConsumer<UpdateRequestEvent, Specification> consumer;
  private @Mock Page<Specification> specificationPage;

  private CommonUpdateRequestProcessingStrategy processingStrategy;

  @BeforeEach
  void setUp() {
    processingStrategy = new CommonUpdateRequestProcessingStrategy(specificationRepository) {
      @Override
      public DefinitionType getType() {
        return null;
      }

      @Override
      protected void process(UpdateRequestEvent event, Specification specification) {
        consumer.accept(event, specification);
      }
    };
  }

  @Test
  void process_positive_findSpecificationAndCallAbstract() {
    var requestEvent = new SubfieldUpdateRequestEvent();
    requestEvent.setFamily(Family.MARC);
    requestEvent.setProfile(FamilyProfile.BIBLIOGRAPHIC);
    var specification = new Specification();
    specification.setFamily(Family.MARC);
    specification.setProfile(FamilyProfile.BIBLIOGRAPHIC);

    when(specificationPage.isEmpty()).thenReturn(false);
    when(specificationPage.toList()).thenReturn(List.of(specification));

    when(specificationRepository.findByFamilyAndProfile(requestEvent.getFamily(), requestEvent.getProfile(),
      OffsetRequest.of(0, 1))).thenReturn(specificationPage);

    processingStrategy.process(requestEvent);

    verify(consumer).accept(requestEvent, specification);
  }

  @Test
  void process_negative_throwException_specificationNotFound() {
    var requestEvent = new SubfieldUpdateRequestEvent();
    requestEvent.setFamily(Family.MARC);
    requestEvent.setProfile(FamilyProfile.BIBLIOGRAPHIC);

    when(specificationPage.isEmpty()).thenReturn(true);

    when(specificationRepository.findByFamilyAndProfile(requestEvent.getFamily(), requestEvent.getProfile(),
      OffsetRequest.of(0, 1)))
      .thenReturn(specificationPage);

    assertThrows(UpdateRequestProcessingException.class, () -> processingStrategy.process(requestEvent));

    verify(consumer, never()).accept(any(), any());
  }
}
