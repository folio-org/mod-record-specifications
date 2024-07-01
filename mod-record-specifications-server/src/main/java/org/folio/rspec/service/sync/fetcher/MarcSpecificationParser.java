package org.folio.rspec.service.sync.fetcher;

import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.INDICATORS_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.SUBFIELDS_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MarcSpecificationParser {

  private static final String SPEC_ELEMENT_CSS_QUERY = "pre";

  private final ObjectMapper objectMapper;
  private final MarcSpecificationBlockSplitter fieldBlockSplitter;
  private final MarcSpecificationFieldBuilder fieldBuilder;
  private final MarcSpecificationIndicatorBuilder indicatorsBuilder;
  private final MarcSpecificationSubfieldsBuilder subfieldsBuilder;

  public ArrayNode parse(Document document) {
    var specElement = document.select(SPEC_ELEMENT_CSS_QUERY).first();
    if (specElement == null) {
      log.error("Spec element not found in document with uri {}", document.baseUri());
      throw new SpecificationFetchingFailedException();
    }
    var fieldBlocks = fieldBlockSplitter.split(specElement.text());

    var array = objectMapper.createArrayNode();

    fieldBlocks.stream()
      .map(this::parseBlock)
      .filter(jsonObject -> !jsonObject.isEmpty())
      .forEach(array::add);

    return array;
  }

  private ObjectNode parseBlock(List<String> block) {
    var fieldObject = fieldBuilder.build(block.get(0));

    var subfieldLineNumber = fieldBlockSplitter.getSubfieldLineNumber(block);
    if (subfieldLineNumber == INDEX_NOT_FOUND) {
      return fieldObject;
    }

    var subfieldsBlock = block.subList(subfieldLineNumber, block.size());
    fieldObject.set(SUBFIELDS_PROP, subfieldsBuilder.build(subfieldsBlock));

    var indicatorsLineNumber = fieldBlockSplitter.getIndicatorsLineNumber(block);
    var indicatorsBlock = block.subList(indicatorsLineNumber, subfieldLineNumber - 1);
    fieldObject.set(INDICATORS_PROP, indicatorsBuilder.build(indicatorsBlock));

    return fieldObject;
  }
}
