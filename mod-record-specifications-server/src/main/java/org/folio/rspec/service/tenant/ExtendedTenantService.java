package org.folio.rspec.service.tenant;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.Parameter;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
@Log4j2
public class ExtendedTenantService extends TenantService {

  private static final String SYNC_SPECIFICATIONS_PARAM = "syncSpecifications";

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

      if (shouldSyncSpecifications(tenantAttributes.getParameters())) {
        var specifications = specificationService.findSpecifications(null, null, IncludeParam.NONE, 100, 0)
          .getSpecifications();

        for (var spec : specifications) {
          log.info("About to start syncing record specification: [id: {}, family: {}, profile: {}, url: {}] ",
            spec.getId(), spec.getFamily(), spec.getProfile(), spec.getUrl());
          specificationService.sync(spec.getId());
        }
      }
    }
  }

  private boolean shouldSyncSpecifications(List<Parameter> parameters) {
    // by default, if there is no param, we consider the param value as true
    if (parameters == null || parameters.isEmpty()) {
      return true;
    }

    var syncParamValue = parameters.stream()
      .filter(parameter -> parameter.getKey().equals(SYNC_SPECIFICATIONS_PARAM))
      .findFirst()
      .map(Parameter::getValue);

    return syncParamValue.isEmpty() || !syncParamValue.get().equals("false");
  }
}
