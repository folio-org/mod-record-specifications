package org.folio.rspec.service.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ExtendedTenantServiceTest {

  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private KafkaAdminService kafkaAdminService;
  @Mock
  private SpecificationService specificationService;
  @Mock
  private JdbcTemplate jdbcTemplate;
  @Mock
  private FolioSpringLiquibase folioSpringLiquibase;

  @Spy
  @InjectMocks
  private ExtendedTenantService service;

  @Test
  @SneakyThrows
  void createOrUpdateTenant_positive_shouldSyncSpecifications() {
    var folioMetadata = mock(FolioModuleMetadata.class);
    var spec1 = new SpecificationDto().id(UUID.randomUUID()).title("spec_bib").url("https://spec_bib_url.com");
    var spec2 = new SpecificationDto().id(UUID.randomUUID()).title("spec_auth").url("https://spec_auth_url.com");
    when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Boolean>>any(), any()))
      .thenReturn(false);
    when(folioExecutionContext.getFolioModuleMetadata()).thenReturn(folioMetadata);
    when(folioExecutionContext.getTenantId()).thenReturn("test_tenant");
    when(folioMetadata.getDBSchemaName(anyString())).thenReturn("schema");
    when(specificationService.findSpecifications(null, null, IncludeParam.NONE, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(spec1, spec2)));

    service.createOrUpdateTenant(new TenantAttributes());

    verify(folioSpringLiquibase).performLiquibaseUpdate();
    verify(specificationService).findSpecifications(null, null, IncludeParam.NONE, 100, 0);
    verify(specificationService).sync(spec1.getId());
    verify(specificationService).sync(spec2.getId());
  }

  @Test
  @SneakyThrows
  void createOrUpdateTenant_positive_shouldNotSyncSpecifications() {
    var folioMetadata = mock(FolioModuleMetadata.class);
    when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Boolean>>any(), any())).thenReturn(true);
    when(folioExecutionContext.getFolioModuleMetadata()).thenReturn(folioMetadata);
    when(folioExecutionContext.getTenantId()).thenReturn("test_tenant");
    when(folioMetadata.getDBSchemaName(anyString())).thenReturn("schema");

    service.createOrUpdateTenant(new TenantAttributes());

    verify(folioSpringLiquibase).performLiquibaseUpdate();
    verifyNoInteractions(specificationService);
  }
}
