package org.folio.rspec.service.sync.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.folio.spring.testing.type.UnitTest;
import org.jsoup.helper.DataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

@UnitTest
@Import(JacksonAutoConfiguration.class)
@SpringBootTest(classes = {
  MarcSpecificationParser.class, MarcSpecificationSubfieldsBuilder.class, MarcSpecificationIndicatorBuilder.class,
  MarcSpecificationFieldLabelModifier.class, MarcSpecificationFieldBuilder.class, MarcSpecificationBlockSplitter.class
})
class FullMarcSpecificationParserTest {

  @Autowired
  private MarcSpecificationParser parser;

  @SneakyThrows
  @Test
  void parseBib() {
    var result = doParseForFile("__files/marc/bibliographic.html");

    assertThat(result).hasSize(293);
    assertAllFieldsExist(result);
  }

  @SneakyThrows
  @Test
  void parseAuthority() {
    var result = doParseForFile("__files/marc/authority.html");

    assertThat(result).hasSize(147);
    assertAllFieldsExist(result);
  }

  private void assertAllFieldsExist(ArrayNode result) {
    for (JsonNode jsonNode : result) {
      assertFieldFields(jsonNode);

      if (jsonNode.has("subfields")) {
        var subfields = jsonNode.get("subfields");
        for (JsonNode subfield : subfields) {
          assertSubfieldFields(subfield);
        }
        var indicators = jsonNode.get("indicators");
        for (JsonNode indicator : indicators) {
          assertIndicatorFields(indicator);
          var codes = indicator.get("codes");
          for (JsonNode code : codes) {
            assertIndicatorCodeFields(code);
          }
        }
      }
    }
  }

  private void assertIndicatorCodeFields(JsonNode code) {
    assertThat(code.fieldNames()).toIterable()
      .describedAs("Failed for indicator code: %s", code)
      .containsExactlyInAnyOrder("code", "label");
  }

  private void assertIndicatorFields(JsonNode indicator) {
    assertThat(indicator.fieldNames()).toIterable()
      .describedAs("Failed for indicator: %s", indicator)
      .containsExactlyInAnyOrder("label", "order", "codes");
  }

  private void assertSubfieldFields(JsonNode subfield) {
    assertThat(subfield.fieldNames()).toIterable()
      .describedAs("Failed for subfield: %s", subfield)
      .containsExactlyInAnyOrder("code", "label", "repeatable", "required", "deprecated");
  }

  private void assertFieldFields(JsonNode jsonNode) {
    assertThat(jsonNode.fieldNames()).toIterable()
      .describedAs("Failed for: %s", jsonNode)
      .contains("tag", "label", "repeatable", "required", "deprecated");
  }

  private ArrayNode doParseForFile(String path) throws IOException {
    var classPathResource = new ClassPathResource(path);
    var document = DataUtil.load(classPathResource.getFile(), StandardCharsets.UTF_8.name(), "/");
    return parser.parse(document);
  }

}
