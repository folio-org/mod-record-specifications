package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class MarcSpecificationFieldLabelModifierTest {

  private final MarcSpecificationFieldLabelModifier modifier = new MarcSpecificationFieldLabelModifier();

  @ParameterizedTest
  @CsvSource({
    "'Field LABEL', 'Field Label'",
    "'ISSN FIELD', 'ISSN Field'",
    "'GPO RECORD', 'GPO Record'",
    "'TITLE OF THE BOOK', 'Title of the Book'",
    "'THIS of in and or to for the an TEST', 'This of in and or to for the an Test'",
    "'FIELD1--FIELD2', 'Field1 - Field2'",
    "'FIELD1/FIELD2', 'Field1/Field2'",
    "'LC CODE', 'LC Code'",
    "'', ''",
    "' ', ''",
    "' multiple    spaces ', 'Multiple Spaces'",
    "'mixed CASE', 'Mixed Case'",
    "'lowercase of upper', 'Lowercase of Upper'",
    "'uppercase LC', 'Uppercase LC'",
    "'hyphenated--field', 'Hyphenated - Field'",
    "'ends with space ', 'Ends With Space'"
  })
  void modifyTest(String input, String expected) {
    assertEquals(expected, modifier.modify(input));
  }
}
