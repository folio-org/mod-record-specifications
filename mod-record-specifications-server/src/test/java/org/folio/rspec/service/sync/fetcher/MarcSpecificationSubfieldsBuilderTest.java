package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class MarcSpecificationSubfieldsBuilderTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final MarcSpecificationSubfieldsBuilder marcSpecificationSubfieldsBuilder =
    new MarcSpecificationSubfieldsBuilder(MAPPER);

  @ParameterizedTest
  @MethodSource("provideParametersForBuildTest")
  void build_givenLines_returnsArrayNode(List<String> lines, ArrayNode expected) {
    var actual = marcSpecificationSubfieldsBuilder.build(lines);
    assertEquals(expected, actual);
  }

  @SuppressWarnings("checkstyle:methodLength")
  private static Stream<Arguments> provideParametersForBuildTest() {
    var lines1 = List.of("$1 - Name1 (NR)");
    var expected1 = MAPPER.createArrayNode().add(newNode("1", "Name1", false, false));

    var lines2 = List.of("$2 - Name2 (NR) [OBSOLETE]");
    var expected2 = MAPPER.createArrayNode().add(newNode("2", "Name2", false, true));

    var lines3 = List.of("$a - Name3 (R)");
    var expected3 = MAPPER.createArrayNode().add(newNode("a", "Name3", true, false));

    var lines4 = List.of("$b - Name4 (NR)[OBSOLETE]");
    var expected4 = MAPPER.createArrayNode().add(newNode("b", "Name4", false, true));

    var lines5 = List.of("$b - Name5 (NR) (MU VM SE) [OBSOLETE]");
    var expected5 = MAPPER.createArrayNode().add(newNode("b", "Name5", false, true));

    var lines6 = List.of("$b - Name6 (R) (MU VM SE) [OBSOLETE]");
    var expected6 = MAPPER.createArrayNode().add(newNode("b", "Name6", true, true));

    var lines7 = List.of("$b - Name7 (Pre-AACR 2) (NR) [LOCAL]");
    var expected7 = MAPPER.createArrayNode().add(newNode("b", "Name7 (Pre-AACR 2)", false, false));

    var lines8 = List.of("$b - Name8");
    var expected8 = MAPPER.createArrayNode().add(newNode("b", "Name8", true, false));

    var lines9 = List.of("$a - Content of non-MARC field (NR)");
    var expected9 = MAPPER.createArrayNode().add(newNode("a", "Content of non-MARC field", false, false));

    var lines10 = List.of("$l - ISSN-L (NR) [OBSOLETE]");
    var expected10 = MAPPER.createArrayNode().add(newNode("l", "ISSN-L", false, true));

    var lines11 = List.of("$b - Date 1 (B.C.E. date) (NR)");
    var expected11 = MAPPER.createArrayNode().add(newNode("b", "Date 1 (B.C.E. date)", false, false));

    var lines12 = List.of("$b - DDC number--abridged NST version (SE) [OBSOLETE]");
    var expected12 = MAPPER.createArrayNode().add(newNode("b", "DDC number--abridged NST version", true, true));

    var lines13 = List.of("$b - Number  [OBSOLETE]");
    var expected13 = MAPPER.createArrayNode().add(newNode("b", "Number", true, true));

    return Stream.of(
      Arguments.of(lines1, expected1),
      Arguments.of(lines2, expected2),
      Arguments.of(lines3, expected3),
      Arguments.of(lines4, expected4),
      Arguments.of(lines5, expected5),
      Arguments.of(lines6, expected6),
      Arguments.of(lines7, expected7),
      Arguments.of(lines8, expected8),
      Arguments.of(lines9, expected9),
      Arguments.of(lines10, expected10),
      Arguments.of(lines11, expected11),
      Arguments.of(lines12, expected12),
      Arguments.of(lines13, expected13)
    );
  }

  private static ObjectNode newNode(String code, String label, boolean repeatable, boolean deprecated) {
    var node = MAPPER.createObjectNode();
    node.put("code", code);
    node.put("label", label);
    node.put("repeatable", repeatable);
    node.put("required", false);
    node.put("deprecated", deprecated);
    return node;
  }
}
