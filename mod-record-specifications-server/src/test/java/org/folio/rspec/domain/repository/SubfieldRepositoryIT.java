package org.folio.rspec.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@EnablePostgres
@IntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubfieldRepositoryIT {

  private final UUID fieldId = UUID.randomUUID();
  @Autowired
  private SubfieldRepository subfieldRepository;
  @Autowired
  private FieldRepository fieldRepository;

  @BeforeEach
  void setUp() {
    var specification = new Specification();
    specification.setId(TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID);
    var metadata = metadata();
    var field = field(specification, metadata);
    var subA = subfield("a", field, metadata);
    var subB = subfield("b", field, metadata);
    var sub1 = subfield("1", field, metadata);
    subfieldRepository.save(subA);
    subfieldRepository.save(subB);
    subfieldRepository.save(sub1);
  }

  @AfterEach
  void tearDown() {
    fieldRepository.deleteAll();
    subfieldRepository.deleteAll();
  }

  @Test
  void findByFieldId_returnSubfieldsSortedByCode() {
    var byFieldId = subfieldRepository.findByFieldId(fieldId);
    assertThat(byFieldId)
      .hasSize(3)
      .extracting(Subfield::getCode)
      .containsExactly("1", "a", "b");
  }

  private Metadata metadata() {
    var metadata = new Metadata();
    metadata.setCreatedByUserId(UUID.randomUUID());
    metadata.setUpdatedByUserId(UUID.randomUUID());
    metadata.setCreatedDate(Timestamp.from(Instant.now()));
    metadata.setUpdatedDate(Timestamp.from(Instant.now()));
    return metadata;
  }

  private Field field(Specification specification, Metadata metadata) {
    var field = new Field();
    field.setId(fieldId);
    field.setTag("123");
    field.setLabel("Tag 123");
    field.setScope(Scope.STANDARD);
    field.setSpecification(specification);
    field.setMetadata(metadata);
    return field;
  }

  private Subfield subfield(String code, Field field, Metadata metadata) {
    var sub1 = new Subfield();
    sub1.setCode(code);
    sub1.setLabel("subfield " + code);
    sub1.setScope(Scope.LOCAL);
    sub1.setField(field);
    sub1.setMetadata(metadata);
    return sub1;
  }
}
