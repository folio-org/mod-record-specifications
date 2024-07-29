package org.folio.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Subfield.SUBFIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.subfieldPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.system;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.SpecificationITBase;
import org.folio.support.builders.SubfieldBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DatabaseCleanup(tables = {SUBFIELD_TABLE_NAME, FIELD_TABLE_NAME}, tenants = TENANT_ID)
class SpecificationStorageSubfieldsApiIT extends SpecificationITBase {

  @Autowired
  private FieldRepository fieldRepository;
  @Autowired
  private SubfieldRepository subfieldRepository;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void deleteSubfield_shouldReturn204AndDeleteLocalSubfield() {
    var subfieldId = executeInContext(() -> {
      var field = fieldRepository.save(local().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity());
      var entity = SubfieldBuilder.local().buildEntity();
      entity.setField(field);
      var subfield = subfieldRepository.save(entity);
      return subfield.getId();
    });

    doDelete(subfieldPath(subfieldId));

    var subfieldOptional = executeInContext(() -> subfieldRepository.findById(subfieldId));
    assertThat(subfieldOptional).isEmpty();
    assertSpecificationUpdatedEvents(1);
  }

  @Test
  void deleteSubfield_shouldReturn404_whenSubfieldNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryDelete(subfieldPath(notExistId))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("subfield with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void deleteSubfield_shouldReturn400_whenSubfieldIsNotAllowedToDelete() throws Exception {
    var subfieldId = executeInContext(() -> {
      var field = fieldRepository.save(system().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity());
      var entity = SubfieldBuilder.standard().buildEntity();
      entity.setField(field);
      var subfield = subfieldRepository.save(entity);
      return subfield.getId();
    });

    tryDelete(subfieldPath(subfieldId))
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("A standard scope subfield cannot be deleted.")));
  }

  @Test
  void updateSubfield_shouldReturn202AndUpdateLocalSubfield() {
    var subfieldId = executeInContext(() -> {
      var field = fieldRepository.save(system().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity());
      var entity = SubfieldBuilder.local().buildEntity();
      entity.setField(field);
      var subfield = subfieldRepository.save(entity);
      return subfield.getId();
    });

    var updatedSubfield = SubfieldBuilder.local().code("b").required(true).buildChangeDto();

    doPut(subfieldPath(subfieldId), updatedSubfield);

    var subfieldOptional = executeInContext(() -> subfieldRepository.findById(subfieldId));
    assertThat(subfieldOptional).isPresent()
      .get()
      .extracting(Subfield::getCode, Subfield::isRequired)
      .containsExactly(updatedSubfield.getCode(), updatedSubfield.getRequired());

    assertSpecificationUpdatedEvents(1);
  }

  @Test
  void updateIndicatorCode_shouldReturn400WhenIndicatorCodeIsNotAllowedToUpdate() throws Exception {
    var subfieldId = executeInContext(() -> {
      var field = fieldRepository.save(system().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity());
      var entity = SubfieldBuilder.system().buildEntity();
      entity.setField(field);
      var subfield = subfieldRepository.save(entity);
      return subfield.getId();
    });

    var updatedSubfield = SubfieldBuilder.local().code("b").required(true).buildChangeDto();

    tryPut(subfieldPath(subfieldId), updatedSubfield)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'code' modification is not allowed for system scope.")));
  }

  @Test
  void updateIndicatorCode_shouldReturn400WhenCodeForIndicatorAlreadyExist() throws Exception {
    var subfieldId = executeInContext(() -> {
      var field = fieldRepository.save(system().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity());
      var subA = SubfieldBuilder.system().code("a").buildEntity();
      subA.setField(field);
      subfieldRepository.save(subA);
      var subB = SubfieldBuilder.local().code("b").buildEntity();
      subB.setField(field);
      return subfieldRepository.save(subB).getId();
    });

    var updatedSubfield = SubfieldBuilder.local().code("a").buildChangeDto();

    tryPut(subfieldPath(subfieldId), updatedSubfield)
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'code' must be unique.")));
  }

  @Test
  void updateIndicatorCode_shouldReturn404WhenIndicatorCodeNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPut(subfieldPath(notExistId), SubfieldBuilder.local().buildChangeDto())
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("subfield with ID [%s] was not found.".formatted(notExistId))));
  }

}
