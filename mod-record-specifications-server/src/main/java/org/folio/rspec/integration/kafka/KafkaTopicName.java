package org.folio.rspec.integration.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopicName {

  SPECIFICATION_UPDATE("specification-storage.specification.update"),
  SPECIFICATION_UPDATED("specification-storage.specification.updated");

  private final String topicName;

  KafkaTopicName(String topicName) {
    this.topicName = topicName;
  }
}
