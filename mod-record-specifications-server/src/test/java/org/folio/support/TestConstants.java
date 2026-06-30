package org.folio.support;

import static org.folio.support.KafkaUtils.fullTopicName;

import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.rspec.integration.kafka.KafkaTopicName;

@UtilityClass
public class TestConstants {

  public static final String TENANT_ID = "test_tenant";
  public static final String USER_ID = "38d3a441-c100-5e8d-bd12-71bde492b723";
  public static final UUID BIBLIOGRAPHIC_SPECIFICATION_ID = UUID.fromString("6eefa4c6-bbf7-4845-ad82-de7fc4abd0e3");
  public static final UUID AUTHORITY_SPECIFICATION_ID = UUID.fromString("a37d01ba-75c7-4a0c-997e-309823f1df3f");

  public static String specificationUpdatedTopic() {
    return specificationUpdatedTopic(TENANT_ID);
  }

  public static String specificationUpdatedTopic(String tenantId) {
    return fullTopicName(KafkaTopicName.SPECIFICATION_UPDATED.getTopicName(), tenantId);
  }

  public static String specificationUpdateTopic() {
    return specificationUpdateTopic(TENANT_ID);
  }

  public static String specificationUpdateTopic(String tenantId) {
    return fullTopicName(KafkaTopicName.SPECIFICATION_UPDATE.getTopicName(), tenantId);
  }
}
