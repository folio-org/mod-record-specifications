package org.folio.rspec.controller;

import static org.folio.support.ApiEndpoints.fieldPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageFieldsController.class)
@Import(ApiExceptionHandler.class)
@ComponentScan(basePackages = "org.folio.rspec.controller.handler")
class SpecificationStorageFieldsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SpecificationFieldService specificationFieldService;

  @Test
  void deleteField_returnNoContent() throws Exception {
    var id = UUID.randomUUID();
    doNothing().when(specificationFieldService).deleteField(id);

    mockMvc.perform(delete(fieldPath(id))
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @Test
  void updateField_returnAccepted() throws Exception {
    UUID id = UUID.randomUUID();
    SpecificationFieldChangeDto specificationFieldChangeDto = new SpecificationFieldChangeDto();
    SpecificationFieldDto specificationFieldDto = new SpecificationFieldDto();

    when(specificationFieldService.updateField(eq(id), any(SpecificationFieldChangeDto.class))).thenReturn(
      specificationFieldDto);

    mockMvc.perform(put(fieldPath(id))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "tag": 888,
            "label": "Custom Field - Contributor Data",
            "url": "http://www.example.org/field888.html",
            "repeatable": true,
            "required": true,
            "deprecated": true
          }
          """))
      .andExpect(status().isAccepted());
  }
}
