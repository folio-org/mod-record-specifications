package org.folio.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.TWO_MINUTES;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.SpecificationITBase;
import org.folio.support.TestConstants;
import org.folio.support.builders.FieldBuilder;
import org.folio.support.builders.SubfieldBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class UpdateRequestEventConsumerIT extends SpecificationITBase {

  private @Autowired FieldRepository fieldRepository;
  private @Autowired SubfieldRepository subfieldRepository;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void shouldConsumeEvent_positive_systemSubfieldCreated() {
    var field = executeInContext(() -> fieldRepository.save(FieldBuilder.standard()
      .specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID)
      .buildEntity())
    );

    var requestEvent = new SubfieldUpdateRequestEvent();
    requestEvent.setFamily(Family.MARC);
    requestEvent.setProfile(FamilyProfile.BIBLIOGRAPHIC);
    requestEvent.setDefinitionType(DefinitionType.SUBFIELD);
    requestEvent.setTargetFieldTag(field.getTag());
    var subfield = SubfieldBuilder.system().buildDto();
    requestEvent.setSubfield(subfield);

    sendEvent(requestEvent);

    await().atMost(TWO_MINUTES).untilAsserted(() -> assertSpecificationUpdatedEvents(1));

    var savedSubfield = executeInContext(() -> subfieldRepository.findById(subfield.getId()));
    assertThat(savedSubfield).isPresent();
    assertThat(savedSubfield.get())
      .extracting(s -> s.getField().getId(), Subfield::getCode, Subfield::getLabel, Subfield::getScope)
      .contains(field.getId(), subfield.getCode(), subfield.getLabel(), subfield.getScope());
  }

  private void sendEvent(SubfieldUpdateRequestEvent requestEvent) {
    var producerRecord = new ProducerRecord<String, Object>(TestConstants.specificationUpdateTopic(), requestEvent);
    defaultHeaders().forEach(
      (headerName, headerValues) -> producerRecord.headers().add(headerName, headerValues.getFirst().getBytes()));
    kafkaTemplate.send(producerRecord);
  }
}
