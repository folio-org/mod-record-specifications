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
    List<String> lines1 = List.of("$1 - Name1 (NR)");
    ArrayNode expected1 = MAPPER.createArrayNode().add(newNode("1", "Name1", false, false));

    List<String> lines2 = List.of("$2 - Name2 (NR) [OBSOLETE]");
    ArrayNode expected2 = MAPPER.createArrayNode().add(newNode("2", "Name2", false, true));

    List<String> lines3 = List.of("$a - Name3 (R)");
    ArrayNode expected3 = MAPPER.createArrayNode().add(newNode("a", "Name3", true, false));

    return Stream.of(
      Arguments.of(lines1, expected1),
      Arguments.of(lines2, expected2),
      Arguments.of(lines3, expected3)
    );
  }

  private static ObjectNode newNode(String code, String label, boolean repeatable, boolean deprecated) {
    ObjectNode node = MAPPER.createObjectNode();
    node.put("code", code);
    node.put("label", label);
    node.put("repeatable", repeatable);
    node.put("required", false);
    node.put("deprecated", deprecated);
    return node;
  }
}
