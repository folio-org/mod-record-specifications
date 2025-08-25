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

  public static Stream<Arguments> provideParametersForBuildTest() {
    var lines1 = List.of("$1 - Name1 (NR)");
    var expected1 = MAPPER.createArrayNode().add(newNode("1", "Name1", false, false));

    var lines2 = List.of("$2 - Name2 (NR) [OBSOLETE]");
    var expected2 = MAPPER.createArrayNode().add(newNode("2", "Name2", false, true));

    var lines3 = List.of("$a - Name3 (R)");
    var expected3 = MAPPER.createArrayNode().add(newNode("a", "Name3", true, false));

    var lines4 = List.of("$b - Name4 (NR)[OBSOLETE]");
    var expected4 = MAPPER.createArrayNode().add(newNode("b", "Name4", false, true));

    return Stream.of(
      Arguments.of(lines1, expected1),
      Arguments.of(lines2, expected2),
      Arguments.of(lines3, expected3),
      Arguments.of(lines4, expected4)
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
