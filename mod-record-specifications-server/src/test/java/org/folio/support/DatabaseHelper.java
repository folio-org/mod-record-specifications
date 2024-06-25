package org.folio.support;

import java.util.UUID;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseHelper {

  public static final String UPDATE_SYNC_URL_SQL =
    "update %s_mod_record_specifications.specification_metadata set sync_url = ? where specification_id = '?';";

  private final FolioModuleMetadata metadata;
  private final JdbcTemplate jdbcTemplate;

  public DatabaseHelper(FolioModuleMetadata metadata, JdbcTemplate jdbcTemplate) {
    this.metadata = metadata;
    this.jdbcTemplate = jdbcTemplate;
  }

  public void updateSyncUrlBySpecification(UUID specificationId, String syncUrl, String tenant) {
    var sql = "UPDATE " + getDbPath(tenant, "specification_metadata")
              + " SET sync_url = ? where specification_id = ?";
    jdbcTemplate.update(sql, syncUrl, specificationId);
  }

  private String getDbPath(String tenantId, String basePath) {
    return metadata.getDBSchemaName(tenantId) + "." + basePath;
  }
}
