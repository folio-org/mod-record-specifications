package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class MarcSpecificationIndicatorBuilderTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final MarcSpecificationIndicatorBuilder builder = new MarcSpecificationIndicatorBuilder(MAPPER);

  @ParameterizedTest
  @MethodSource("provideParametersForBuildTest")
  void build_givenLines_returnsArrayNode(List<String> lines, ArrayNode expected) {
    ArrayNode result = builder.build(lines);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideParametersForBuildTest() {
    var lines1 = List.of(
      "First - Simple Indicator1",
      "# - Undefined",
      "Second - Simple Indicator2",
      "# - Undefined"
    );
    var expected1 =
      newArrayNode(
        newObjectNode(1, "Simple Indicator1", newArrayNode(newObjectNode("#", "Undefined"))),
        newObjectNode(2, "Simple Indicator2", newArrayNode(newObjectNode("#", "Undefined")))
      );

    var lines2 = List.of(
      "First - Indicator With Codes",
      "a - Code a",
      "b - Code b",
      "Second - Simple Indicator2",
      "# - Undefined"
    );
    var expected2 =
      newArrayNode(
        newObjectNode(1, "Indicator With Codes",
          newArrayNode(newObjectNode("a", "Code a"), newObjectNode("b", "Code b"))),
        newObjectNode(2, "Simple Indicator2", newArrayNode(newObjectNode("#", "Undefined")))
      );

    var lines3 = List.of(
      "First - Simple Indicator1",
      "# - Undefined",
      "Second - Indicator With Codes",
      "a - Code a",
      "b - Code b"
    );
    var expected3 =
      newArrayNode(
        newObjectNode(1, "Simple Indicator1", newArrayNode(newObjectNode("#", "Undefined"))),
        newObjectNode(2, "Indicator With Codes",
          newArrayNode(newObjectNode("a", "Code a"), newObjectNode("b", "Code b")))
      );

    var lines4 = List.of(
      "First - Indicator With Codes1",
      "a - Code a",
      "b - Code b",
      "Second - Indicator With Codes2",
      "c - Code c",
      "d - Code d"
    );
    var expected4 =
      newArrayNode(
        newObjectNode(1, "Indicator With Codes1",
          newArrayNode(newObjectNode("a", "Code a"), newObjectNode("b", "Code b"))),
        newObjectNode(2, "Indicator With Codes2",
          newArrayNode(newObjectNode("c", "Code c"), newObjectNode("d", "Code d")))
      );

    var lines5 = List.of(
      "First - Indicator[OBSOLETE]",
      "# - Undefined",
      "First - Indicator With Codes1",
      "a - Code a",
      "b - Code b",
      "First - Indicator [OBSOLETE]",
      "# - Undefined",
      "Second - Indicator [OBSOLETE]",
      "# - Undefined",
      "Second - Indicator With Codes2",
      "c - Code c [OBSOLETE]",
      "d - Code d",
      "Second - Indicator [OBSOLETE]",
      "# - Undefined"
    );
    var expected5 =
      newArrayNode(
        newObjectNode(1, "Indicator With Codes1",
          newArrayNode(newObjectNode("a", "Code a"), newObjectNode("b", "Code b"))),
        newObjectNode(2, "Indicator With Codes2",
          newArrayNode(newObjectNode("c", "Code c", true), newObjectNode("d", "Code d")))
      );

    var lines6 = List.of(
      "First - Simple Indicator1",
      "# - Undefined",
      "Second - Indicator With Codes",
      "0-9 - Code x"
    );
    var expected6 =
      newArrayNode(
        newObjectNode(1, "Simple Indicator1", newArrayNode(newObjectNode("#", "Undefined"))),
        newObjectNode(2, "Indicator With Codes", newArrayNode(IntStream.range(0, 10).boxed()
          .map(num -> newObjectNode(String.valueOf(num), "Code x"))
          .toArray(ObjectNode[]::new)))
      );

    return Stream.of(
      Arguments.of(lines1, expected1),
      Arguments.of(lines2, expected2),
      Arguments.of(lines3, expected3),
      Arguments.of(lines4, expected4),
      Arguments.of(lines5, expected5),
      Arguments.of(lines6, expected6)
    );
  }

  private static ObjectNode newObjectNode() {
    return MAPPER.createObjectNode();
  }

  private static ObjectNode newObjectNode(int order, String label, ArrayNode codes) {
    var node = newObjectNode();
    node.put("label", label);
    node.put("order", order);
    node.set("codes", codes);
    return node;
  }

  private static ObjectNode newObjectNode(String code, String label) {
    var node = newObjectNode();
    node.put("code", code);
    node.put("label", label);
    return newObjectNode(code, label, false);
  }

  private static ObjectNode newObjectNode(String code, String label, boolean deprecated) {
    var node = newObjectNode();
    node.put("code", code);
    node.put("label", label);
    node.put("deprecated", deprecated);
    return node;
  }

  private static ArrayNode newArrayNode(ObjectNode... nodes) {
    var arrayNode = new ObjectMapper().createArrayNode();
    for (ObjectNode node : nodes) {
      arrayNode.add(node);
    }
    return arrayNode;
  }
}
