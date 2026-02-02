package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcSpecificationFieldBuilderTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private MarcSpecificationFieldBuilder marcSpecificationFieldBuilder;

  @Mock
  private MarcSpecificationFieldLabelModifier labelModifier;

  @BeforeEach
  void setUp() {
    marcSpecificationFieldBuilder = new MarcSpecificationFieldBuilder(labelModifier, MAPPER);
    lenient().when(labelModifier.modify(anyString())).thenAnswer(i -> i.getArgument(0).toString().trim());
  }

  @MethodSource("testDataProvider")
  @ParameterizedTest
  void build_validLine_createsCorrectObject(String line, ObjectNode expected) {
    ObjectNode actual = marcSpecificationFieldBuilder.build(line);

    assertEquals(expected, actual);
  }

  @ValueSource(strings = {
    "Invalid Field",
    "111 - ",
    "111 - (NR)"
  })
  @ParameterizedTest
  void build_invalidLine_createsEmptyObject(String line) {
    ObjectNode node = marcSpecificationFieldBuilder.build(line);

    assertTrue(node.isEmpty());
  }

  private static Stream<Arguments> testDataProvider() {
    return Stream.of(
      arguments("111 - My Non-Repeatable Field (NR)",
        createFieldNode("111", "My Non-Repeatable Field", false, false)),
      arguments("112 - My Deprecated Field (NR) [OBSOLETE]",
        createFieldNode("112", "My Deprecated Field [OBSOLETE]", false, true)),
      arguments("113 - My Deprecated Field (NR)[OBSOLETE]",
        createFieldNode("113", "My Deprecated Field [OBSOLETE]", false, true)),
      arguments("123 - My Repeatable Field 1 (Some Value)",
        createFieldNode("123", "My Repeatable Field 1", true, false)),
      arguments("124 - My Repeatable Field 2 (R)",
        createFieldNode("124", "My Repeatable Field 2", true, false))
    );
  }

  private static ObjectNode createFieldNode(String tag, String label,
                                            boolean repeatable, boolean deprecated) {
    var node = MAPPER.createObjectNode();
    node.put("tag", tag);
    node.put("label", label);
    node.put("repeatable", repeatable);
    node.put("required", false);
    node.put("deprecated", deprecated);
    return node;
  }
}
