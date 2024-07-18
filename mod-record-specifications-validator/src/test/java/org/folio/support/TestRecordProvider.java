package org.folio.support;

import org.marc4j.marc.Record;

public class TestRecordProvider {

  public static Record getMarc4jRecord() {
    return InputOutputTestUtils.readRecord("testdata/marc-bib-record.json");
  }
}
