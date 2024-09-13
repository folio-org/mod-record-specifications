package org.folio.rspec.service.tenant;

import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
@Log4j2
public class ExtendedTenantService extends TenantService {

  private final FolioExecutionContext folioExecutionContext;
  private final KafkaAdminService kafkaAdminService;
  private final SpecificationService specificationService;

  public ExtendedTenantService(JdbcTemplate jdbcTemplate,
                               FolioSpringLiquibase folioSpringLiquibase,
                               FolioExecutionContext folioExecutionContext,
                               KafkaAdminService kafkaAdminService,
                               SpecificationService specificationService) {
    super(jdbcTemplate, folioExecutionContext, folioSpringLiquibase);
    this.folioExecutionContext = folioExecutionContext;
    this.kafkaAdminService = kafkaAdminService;
    this.specificationService = specificationService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    super.afterTenantUpdate(tenantAttributes);
    kafkaAdminService.createTopics(folioExecutionContext.getTenantId());
  }

  @Override
  protected void afterTenantDeletion(TenantAttributes tenantAttributes) {
    var tenantId = context.getTenantId();
    kafkaAdminService.deleteTopics(tenantId);
  }

  @Override
  public synchronized void createOrUpdateTenant(TenantAttributes tenantAttributes) {
    if (Boolean.FALSE.equals(tenantExists())) {
      super.createOrUpdateTenant(tenantAttributes);
      var specificationIds = specificationService.findSpecifications(null, null, IncludeParam.NONE, 100, 0)
        .getSpecifications().stream()
        .map(SpecificationDto::getId)
        .toList();

      log.info("About to start record specifications syncing");
      for (var id : specificationIds) {
        specificationService.sync(id);
      }
    }
  }
}
