package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.List;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.folio.spring.testing.type.UnitTest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcSpecificationParserTest {

  public static final ObjectMapper MAPPER = new ObjectMapper();
  @Mock
  private MarcSpecificationBlockSplitter fieldBlockSplitter;
  @Mock
  private MarcSpecificationFieldBuilder fieldBuilder;
  @Mock
  private MarcSpecificationIndicatorBuilder indicatorsBuilder;
  @Mock
  private MarcSpecificationSubfieldsBuilder subfieldsBuilder;

  private MarcSpecificationParser marcSpecificationParser;

  @BeforeEach
  void setUp() {
    marcSpecificationParser =
      new MarcSpecificationParser(MAPPER, fieldBlockSplitter, fieldBuilder, indicatorsBuilder,
        subfieldsBuilder);
  }

  @Test
  void parse_givenNullSpecElement_throwsException() {
    var mockDocument = mock(Document.class);
    var mockElements = mock(Elements.class);

    when(mockDocument.select(anyString())).thenReturn(mockElements);
    when(mockElements.first()).thenReturn(null);

    assertThrows(SpecificationFetchingFailedException.class, () -> marcSpecificationParser.parse(mockDocument));
  }

  @Test
  void parse_givenValidSpecElement_returnsArrayNode() {
    var mockDocument = mock(Document.class);
    var mockElements = mock(Elements.class);
    var mockElement = mock(Element.class);

    when(mockDocument.select(anyString())).thenReturn(mockElements);
    when(mockElements.first()).thenReturn(mockElement);
    when(mockElement.text()).thenReturn("some text");

    var block = List.of("Line1", "Indicator line", "Line 3", "Subfields line", "Line5");
    var blocks = Arrays.asList(block, block);
    when(fieldBlockSplitter.split(anyString())).thenReturn(blocks);
    when(fieldBlockSplitter.getIndicatorsLineNumber(anyList())).thenReturn(1);
    when(fieldBlockSplitter.getSubfieldLineNumber(anyList())).thenReturn(3);
    when(fieldBuilder.build("Line1")).thenReturn(getObjectNode());
    when(indicatorsBuilder.build(List.of("Indicator line", "Line 3"))).thenReturn(getArrayNode());
    when(subfieldsBuilder.build(List.of("Subfields line", "Line5"))).thenReturn(getArrayNode());

    ArrayNode result = marcSpecificationParser.parse(mockDocument);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.get(0).has("tag"));
    assertTrue(result.get(0).has("subfields"));
    assertTrue(result.get(0).has("indicators"));
  }

  private ObjectNode getObjectNode() {
    var node = MAPPER.createObjectNode();
    node.put("tag", "123");
    return node;
  }

  private ArrayNode getArrayNode() {
    return MAPPER.createArrayNode();
  }
}
