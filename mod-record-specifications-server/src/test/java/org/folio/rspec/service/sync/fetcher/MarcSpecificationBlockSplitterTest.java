package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MarcSpecificationBlockSplitterTest {

  private final MarcSpecificationBlockSplitter blockSplitter = new MarcSpecificationBlockSplitter();

  @Test
  void split_givesCorrectResult() {
    String textBlock = """
      123 - Field1 (R)
      --Comment
      234 - Field2 (R) [OBSOLETE]
       Indicators
          First - Undefined
             # - Undefined
          Second - Undefined
             # - Undefined
       Subfield Codes
          $a - Carrier type term (R)
          $b - Carrier type code (R)
      324 - Field3 (NR)
      """;
    List<List<String>> result = blockSplitter.split(textBlock);

    assertEquals(3, result.size());
    assertEquals(1, result.get(0).size());
    assertEquals(9, result.get(1).size());
    assertEquals(1, result.get(2).size());
    assertEquals("123 - Field1 (R)", result.get(0).get(0));
    assertEquals("234 - Field2 (R) [OBSOLETE]", result.get(1).get(0));
  }

  @Test
  void getIndicatorsLineNumber_givesCorrectResult() {
    List<String> lines = List.of("First Line", "Indicators Start Here", "Third Line");
    int result = blockSplitter.getIndicatorsLineNumber(lines);

    assertEquals(2, result);
  }

  @Test
  void getSubfieldLineNumber_givesCorrectResult() {
    List<String> lines = List.of("First Line", "Subfield Code Start Here", "Third Line");
    int result = blockSplitter.getSubfieldLineNumber(lines);

    assertEquals(2, result);
  }

  @Test
  void getLineNumberMatches_whenNotFoundReturnsNegative() {
    List<String> lines = List.of("First Line", "Second Line", "Third Line");
    int indicatorsLine = blockSplitter.getIndicatorsLineNumber(lines);
    int subfieldsLineNumber = blockSplitter.getSubfieldLineNumber(lines);

    assertEquals(-1, indicatorsLine);
    assertEquals(-1, subfieldsLineNumber);
  }
}
