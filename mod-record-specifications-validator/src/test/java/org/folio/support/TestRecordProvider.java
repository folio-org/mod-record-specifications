package org.folio.support;

import org.marc4j.marc.Record;

public class TestRecordProvider {

  public static Record getMarc4jRecord(String filePath) {
    return InputOutputTestUtils.readRecord(filePath);
  }
}
